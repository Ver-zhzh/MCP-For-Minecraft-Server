package com.smc.plugin.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smc.plugin.SMCPlugin;
import com.smc.plugin.command.CommandExecutor;
import com.smc.plugin.logging.LogCollector;

import fi.iki.elonen.NanoHTTPD;

public class HttpApiServer extends NanoHTTPD {
    
    private final SMCPlugin plugin;
    private final Gson gson;
    private boolean running;
    
    public HttpApiServer(SMCPlugin plugin, String host, int port) {
        super(host, port);
        this.plugin = plugin;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        this.running = false;
    }
    
    public boolean startServer() {
        if (running) {
            plugin.getLogger().warning("HTTP API server is already running");
            return false;
        }
        
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            running = true;
            plugin.getLogger().info("HTTP API server started successfully");
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start HTTP API server", e);
            return false;
        }
    }
    
    public void stopServer() {
        if (!running) {
            return;
        }
        
        try {
            plugin.getLogger().info("Stopping HTTP API server...");
            stop();
            running = false;
            plugin.getLogger().info("HTTP API server stopped");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error stopping HTTP API server", e);
        }
    }
    
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();
        
        plugin.getLogger().info(String.format("[HTTP] %s %s", method, uri));
        
        String apiKey = session.getHeaders().get("x-api-key");
        String expectedKey = plugin.getConfigManager().getApiKey();
        
        if (apiKey == null || !apiKey.equals(expectedKey)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unauthorized");
            error.put("message", "Invalid or missing API key");
            return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "application/json", gson.toJson(error));
        }
        
        try {
            if (uri.equals("/api/status") && method == Method.GET) {
                return handleStatus();
            } else if (uri.equals("/api/plugins") && method == Method.GET) {
                return handlePlugins();
            } else if (uri.equals("/api/command") && method == Method.POST) {
                return handleCommand(session);
            } else if (uri.equals("/api/logs") && method == Method.GET) {
                return handleLogs(session);
            } else if (uri.equals("/api/players") && method == Method.GET) {
                return handlePlayers();
            } else if (uri.equals("/api/logs/errors") && method == Method.GET) {
                return handleErrors(session);
            } else if (uri.equals("/api/logs/warnings") && method == Method.GET) {
                return handleWarnings(session);
            } else if (uri.equals("/api/commands") && method == Method.GET) {
                return handleCommands();
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not Found");
                error.put("message", "Endpoint not found");
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", gson.toJson(error));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error handling request", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", gson.toJson(error));
        }
    }
    
    private Response handleStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("connected", true);
            response.put("online", true);
            response.put("server_version", Bukkit.getVersion());
            response.put("minecraft_version", Bukkit.getBukkitVersion());
            response.put("plugin_version", plugin.getDescription().getVersion());
            
            return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(response));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error in /api/status", e);
            response.put("connected", false);
            response.put("online", false);
            response.put("error", e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", gson.toJson(response));
        }
    }
    
    private Response handlePlugins() {
        try {
            Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
            List<Map<String, Object>> pluginList = new ArrayList<>();
            
            for (Plugin p : plugins) {
                Map<String, Object> pluginInfo = new HashMap<>();
                pluginInfo.put("name", p.getName());
                pluginInfo.put("version", p.getDescription().getVersion());
                pluginInfo.put("enabled", p.isEnabled());
                pluginInfo.put("authors", p.getDescription().getAuthors());
                pluginList.add(pluginInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("plugins", pluginList);
            
            return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(response));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error in /api/plugins", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve plugins");
            error.put("message", e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", gson.toJson(error));
        }
    }
    
    private Response handleCommand(IHTTPSession session) {
        try {
            Map<String, String> files = new HashMap<>();
            session.parseBody(files);
            String body = files.get("postData");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = gson.fromJson(body, Map.class);
            Object commandsObj = bodyMap.get("commands");
            
            if (commandsObj == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing 'commands' field");
                error.put("message", "Request body must contain 'commands' field (string or array)");
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", gson.toJson(error));
            }
            
            CommandExecutor executor = plugin.getCommandExecutor();
            List<CommandExecutor.CommandResult> results;
            
            if (commandsObj instanceof String) {
                String command = (String) commandsObj;
                CommandExecutor.CommandResult result = executor.executeCommand(command);
                results = Collections.singletonList(result);
            } else if (commandsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> commands = (List<String>) commandsObj;
                results = executor.executeCommands(commands);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid 'commands' field type");
                error.put("message", "'commands' must be a string or array of strings");
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", gson.toJson(error));
            }
            
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (CommandExecutor.CommandResult result : results) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("command", result.getCommand());
                resultMap.put("success", result.isSuccess());
                resultMap.put("output", result.getOutput());
                if (!result.isSuccess()) {
                    resultMap.put("error", result.getError());
                }
                resultList.add(resultMap);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("results", resultList);
            
            return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(response));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error in /api/command", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Command execution failed");
            error.put("message", e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", gson.toJson(error));
        }
    }
    
    private Response handleLogs(IHTTPSession session) {
        try {
            Map<String, String> params = session.getParms();
            String limitStr = params.get("limit");
            Integer limit = limitStr != null ? Integer.valueOf(limitStr) : 100;
            String startTimeStr = params.get("start_time");
            String endTimeStr = params.get("end_time");
            
            Long startTime = null;
            Long endTime = null;
            
            if (startTimeStr != null) {
                startTime = Long.parseLong(startTimeStr);
            }
            if (endTimeStr != null) {
                endTime = Long.parseLong(endTimeStr);
            }
            
            LogCollector collector = plugin.getLogCollector();
            List<LogCollector.LogEntry> logs = collector.getLogs(limit, startTime, endTime);
            
            List<Map<String, Object>> logList = new ArrayList<>();
            for (LogCollector.LogEntry log : logs) {
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("timestamp", log.getTimestamp());
                logMap.put("level", log.getLevel());
                logMap.put("logger", log.getLogger());
                logMap.put("message", log.getMessage());
                logList.add(logMap);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("logs", logList);
            
            return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(response));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error in /api/logs", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve logs");
            error.put("message", e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", gson.toJson(error));
        }
    }
    
    private Response handlePlayers() {
        try {
            Collection<? extends Player> onlinePlayers = plugin.getVersionAdapter().getOnlinePlayers();
            
            List<Map<String, Object>> playerList = new ArrayList<>();
            for (Player player : onlinePlayers) {
                Map<String, Object> playerInfo = new HashMap<>();
                playerInfo.put("name", player.getName());
                playerInfo.put("uuid", player.getUniqueId().toString());
                
                int ping;
                try {
                    ping = (int) player.getClass().getMethod("getPing").invoke(player);
                } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                    ping = -1;
                }
                playerInfo.put("ping", ping);
                
                playerList.add(playerInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", playerList.size());
            response.put("players", playerList);
            
            return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(response));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error in /api/players", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve player list");
            error.put("message", e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", gson.toJson(error));
        }
    }
    
    private Response handleErrors(IHTTPSession session) {
        try {
            Map<String, String> params = session.getParms();
            String pluginName = params.get("plugin");
            String limitStr = params.get("limit");
            Integer limit = limitStr != null ? Integer.valueOf(limitStr) : 100;
            
            LogCollector collector = plugin.getLogCollector();
            List<LogCollector.LogEntry> errors = collector.getErrors(pluginName, limit);
            
            List<Map<String, Object>> errorList = new ArrayList<>();
            for (LogCollector.LogEntry log : errors) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("timestamp", log.getTimestamp());
                errorMap.put("plugin", log.getLogger());
                errorMap.put("message", log.getMessage());
                errorList.add(errorMap);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("errors", errorList);
            
            return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(response));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error in /api/logs/errors", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve error logs");
            error.put("message", e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", gson.toJson(error));
        }
    }
    
    private Response handleWarnings(IHTTPSession session) {
        try {
            Map<String, String> params = session.getParms();
            String pluginName = params.get("plugin");
            String limitStr = params.get("limit");
            Integer limit = limitStr != null ? Integer.valueOf(limitStr) : 100;
            
            LogCollector collector = plugin.getLogCollector();
            List<LogCollector.LogEntry> warnings = collector.getWarnings(pluginName, limit);
            
            List<Map<String, Object>> warningList = new ArrayList<>();
            for (LogCollector.LogEntry log : warnings) {
                Map<String, Object> warningMap = new HashMap<>();
                warningMap.put("timestamp", log.getTimestamp());
                warningMap.put("plugin", log.getLogger());
                warningMap.put("message", log.getMessage());
                warningList.add(warningMap);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("warnings", warningList);
            
            return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(response));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error in /api/logs/warnings", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve warning logs");
            error.put("message", e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", gson.toJson(error));
        }
    }
    
    private Response handleCommands() {
        try {
            List<Map<String, Object>> commandList = new ArrayList<>();
            
            try {
                Object commandMap = Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());
                @SuppressWarnings("unchecked")
                Map<String, org.bukkit.command.Command> knownCommands = 
                    (Map<String, org.bukkit.command.Command>) commandMap.getClass().getMethod("getKnownCommands").invoke(commandMap);
                
                for (Map.Entry<String, org.bukkit.command.Command> entry : knownCommands.entrySet()) {
                    org.bukkit.command.Command cmd = entry.getValue();
                    
                    Map<String, Object> commandInfo = new HashMap<>();
                    commandInfo.put("name", cmd.getName());
                    commandInfo.put("description", cmd.getDescription());
                    commandInfo.put("usage", cmd.getUsage());
                    commandInfo.put("aliases", new ArrayList<>(cmd.getAliases()));
                    commandInfo.put("permission", cmd.getPermission() != null ? cmd.getPermission() : "");
                    
                    String pluginName = "Unknown";
                    try {
                        Object owningPlugin = cmd.getClass().getMethod("getPlugin").invoke(cmd);
                        if (owningPlugin != null && owningPlugin instanceof Plugin) {
                            pluginName = ((Plugin) owningPlugin).getName();
                        } else {
                            pluginName = "Minecraft";
                        }
                    } catch (NoSuchMethodException e) {
                        String className = cmd.getClass().getName();
                        if (className.startsWith("org.bukkit.command")) {
                            pluginName = "Minecraft";
                        } else {
                            String[] parts = className.split("\\.");
                            if (parts.length > 2) {
                                pluginName = parts[2];
                            }
                        }
                    } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                        pluginName = "Unknown";
                    }
                    
                    commandInfo.put("plugin", pluginName);
                    commandList.add(commandInfo);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Could not retrieve command map using reflection", e);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("commands", commandList);
            response.put("count", commandList.size());
            
            return newFixedLengthResponse(Response.Status.OK, "application/json", gson.toJson(response));
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error in /api/commands", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve commands");
            error.put("message", e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", gson.toJson(error));
        }
    }
}
