package com.smc.plugin.command;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;

/**
 * A custom CommandSender implementation that captures command output.
 * 
 * This class wraps the console command sender and intercepts all messages
 * sent during command execution, allowing us to capture the output.
 */
public class CommandOutputCapture implements ConsoleCommandSender {
    
    private final ConsoleCommandSender delegate;
    private final StringBuilder outputBuffer;
    
    /**
     * Creates a new command output capture wrapper.
     * 
     * @param delegate The console command sender to wrap
     */
    public CommandOutputCapture(ConsoleCommandSender delegate) {
        this.delegate = delegate;
        this.outputBuffer = new StringBuilder();
    }
    
    /**
     * Gets the captured output.
     * 
     * @return The captured command output
     */
    public String getOutput() {
        return outputBuffer.toString().trim();
    }
    
    @Override
    public void sendMessage(String message) {
        outputBuffer.append(message).append("\n");
    }
    
    @Override
    public void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }
    
    @Override
    public void sendMessage(java.util.UUID sender, String message) {
        outputBuffer.append(message).append("\n");
    }
    
    @Override
    public void sendMessage(java.util.UUID sender, String[] messages) {
        for (String message : messages) {
            sendMessage(sender, message);
        }
    }
    
    // Delegate all other methods to the wrapped console sender
    
    @Override
    public Server getServer() {
        return delegate.getServer();
    }
    
    @Override
    public String getName() {
        return delegate.getName();
    }
    
    @Override
    public boolean isPermissionSet(String name) {
        return delegate.isPermissionSet(name);
    }
    
    @Override
    public boolean isPermissionSet(Permission perm) {
        return delegate.isPermissionSet(perm);
    }
    
    @Override
    public boolean hasPermission(String name) {
        return delegate.hasPermission(name);
    }
    
    @Override
    public boolean hasPermission(Permission perm) {
        return delegate.hasPermission(perm);
    }
    
    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return delegate.addAttachment(plugin, name, value);
    }
    
    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return delegate.addAttachment(plugin);
    }
    
    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return delegate.addAttachment(plugin, name, value, ticks);
    }
    
    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return delegate.addAttachment(plugin, ticks);
    }
    
    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        delegate.removeAttachment(attachment);
    }
    
    @Override
    public void recalculatePermissions() {
        delegate.recalculatePermissions();
    }
    
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return delegate.getEffectivePermissions();
    }
    
    @Override
    public boolean isOp() {
        return delegate.isOp();
    }
    
    @Override
    public void setOp(boolean value) {
        delegate.setOp(value);
    }
    
    @Override
    public boolean isConversing() {
        return delegate.isConversing();
    }
    
    @Override
    public void acceptConversationInput(String input) {
        delegate.acceptConversationInput(input);
    }
    
    @Override
    public boolean beginConversation(Conversation conversation) {
        return delegate.beginConversation(conversation);
    }
    
    @Override
    public void abandonConversation(Conversation conversation) {
        delegate.abandonConversation(conversation);
    }
    
    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
        delegate.abandonConversation(conversation, details);
    }
    
    @Override
    public void sendRawMessage(String message) {
        outputBuffer.append(message).append("\n");
    }
    
    @Override
    public void sendRawMessage(java.util.UUID sender, String message) {
        outputBuffer.append(message).append("\n");
    }
    
    @Override
    public CommandSender.Spigot spigot() {
        return delegate.spigot();
    }
}
