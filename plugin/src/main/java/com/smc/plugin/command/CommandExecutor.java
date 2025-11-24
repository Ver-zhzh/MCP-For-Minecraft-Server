package com.smc.plugin.command;

import com.smc.plugin.SMCPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * Handles command execution on the Minecraft server.
 * 
 * This class provides functionality to:
 * - Execute commands synchronously on the main server thread
 * - Capture command output and results
 * - Handle command timeouts
 * - Validate command input
 * 
 * Commands are executed with console permissions (highest level).
 */
public class CommandExecutor {
    
    private final SMCPlugin plugin;
    private final int timeoutSeconds;
    private final List<String> commandBlacklist;
    
    /**
     * Creates a new command executor.
     * 
     * @param plugin The plugin instance
     */
    public CommandExecutor(SMCPlugin plugin) {
        this.plugin = plugin;
        this.timeoutSeconds = plugin.getConfigManager().getCommandTimeoutSeconds();
        this.commandBlacklist = plugin.getConfigManager().getCommandBlacklist();
    }
    
    /**
     * Executes a single command and returns the result.
     * 
     * @param command The command to execute (without leading slash)
     * @return The command result containing output and success status
     */
    public CommandResult executeCommand(String command) {
        // Validate the command
        ValidationResult validation = validateCommand(command);
        if (!validation.isValid()) {
            return CommandResult.failure(command, validation.getErrorMessage());
        }
        
        try {
            // Execute the command asynchronously with timeout
            CompletableFuture<CommandResult> future = executeCommandAsync(command);
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
            
        } catch (TimeoutException e) {
            String error = "Command execution timed out after " + timeoutSeconds + " seconds";
            plugin.getLogger().warning("Command timeout: " + command);
            return CommandResult.failure(command, error);
            
        } catch (Exception e) {
            String error = "Command execution failed: " + e.getMessage();
            plugin.getLogger().log(Level.SEVERE, "Error executing command: " + command, e);
            return CommandResult.failure(command, error);
        }
    }
    
    /**
     * Executes multiple commands in sequence and returns all results.
     * 
     * @param commands The list of commands to execute
     * @return List of command results in the same order as input
     */
    public List<CommandResult> executeCommands(List<String> commands) {
        List<CommandResult> results = new ArrayList<>();
        
        for (String command : commands) {
            CommandResult result = executeCommand(command);
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * Executes a command asynchronously on the main server thread.
     * 
     * @param command The command to execute
     * @return A CompletableFuture that will contain the command result
     */
    private CompletableFuture<CommandResult> executeCommandAsync(String command) {
        CompletableFuture<CommandResult> future = new CompletableFuture<>();
        
        // Schedule command execution on the main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                // Get the console command sender
                ConsoleCommandSender console = Bukkit.getConsoleSender();
                
                // Create a custom command sender to capture output
                CommandOutputCapture outputCapture = new CommandOutputCapture(console);
                
                // Remove leading slash if present
                String cleanCommand = command.trim();
                if (cleanCommand.startsWith("/")) {
                    cleanCommand = cleanCommand.substring(1);
                }
                
                // Execute the command
                boolean success = Bukkit.dispatchCommand(outputCapture, cleanCommand);
                
                // Get the captured output
                String output = outputCapture.getOutput();
                
                // Create the result
                CommandResult result;
                if (success) {
                    result = CommandResult.success(command, output);
                } else {
                    result = CommandResult.failure(command, "Command execution returned false");
                }
                
                future.complete(result);
                
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error in command execution: " + command, e);
                future.complete(CommandResult.failure(command, "Exception: " + e.getMessage()));
            }
        });
        
        return future;
    }
    
    /**
     * Validates a command before execution.
     * 
     * @param command The command to validate
     * @return Validation result indicating if the command is valid
     */
    public ValidationResult validateCommand(String command) {
        // Check if command is null or empty
        if (command == null || command.trim().isEmpty()) {
            return ValidationResult.invalid("Command cannot be empty");
        }
        
        // Check command length (max 1000 characters as per security requirements)
        if (command.length() > 1000) {
            return ValidationResult.invalid("Command is too long (max 1000 characters)");
        }
        
        // Check if command is blacklisted
        if (plugin.getConfigManager().isCommandBlacklisted(command)) {
            return ValidationResult.invalid("Command is blacklisted and cannot be executed");
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Represents the result of a command execution.
     */
    public static class CommandResult {
        private final String command;
        private final String output;
        private final boolean success;
        private final String error;
        
        private CommandResult(String command, String output, boolean success, String error) {
            this.command = command;
            this.output = output;
            this.success = success;
            this.error = error;
        }
        
        /**
         * Creates a successful command result.
         * 
         * @param command The executed command
         * @param output The command output
         * @return A successful command result
         */
        public static CommandResult success(String command, String output) {
            return new CommandResult(command, output, true, null);
        }
        
        /**
         * Creates a failed command result.
         * 
         * @param command The executed command
         * @param error The error message
         * @return A failed command result
         */
        public static CommandResult failure(String command, String error) {
            return new CommandResult(command, "", false, error);
        }
        
        public String getCommand() {
            return command;
        }
        
        public String getOutput() {
            return output;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getError() {
            return error;
        }
        
        @Override
        public String toString() {
            if (success) {
                return "CommandResult{command='" + command + "', success=true, output='" + output + "'}";
            } else {
                return "CommandResult{command='" + command + "', success=false, error='" + error + "'}";
            }
        }
    }
    
    /**
     * Represents the result of command validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        /**
         * Creates a valid validation result.
         * 
         * @return A valid validation result
         */
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        /**
         * Creates an invalid validation result.
         * 
         * @param errorMessage The validation error message
         * @return An invalid validation result
         */
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
