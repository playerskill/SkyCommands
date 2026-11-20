package ru.player_skill.skycommands.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import ru.player_skill.skycommands.SkyCommandsPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SkyCommand {

    protected final SkyCommandsPlugin plugin;
    private final String name;
    private final List<String> aliases;

    private volatile boolean enabled = true;
    private volatile String permission = "";
    // Было errorMessage, теперь permissionRequired
    private volatile String permissionRequired = null;

    protected SkyCommand(SkyCommandsPlugin plugin, String name, String... aliases) {
        this.plugin = plugin;
        this.name = name.toLowerCase();
        this.aliases = new ArrayList<>();
        for (String alias : aliases) {
            this.aliases.add(alias.toLowerCase());
        }
    }

    public boolean matches(String commandName) {
        if (name.equals(commandName.toLowerCase())) {
            return true;
        }
        for (String alias : aliases) {
            if (alias.equals(commandName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public final void reload() {
        String base = "commands." + name;
        enabled = plugin.getConfig().getBoolean(base + ".enabled", true);
        permission = plugin.getConfig().getString(base + ".permission", defaultPermission());

        // Читаем permission-required вместо error-message
        String req = plugin.getConfig().getString(base + ".permission-required", null);
        permissionRequired = (req != null && !req.trim().isEmpty()) ? req : null;

        reloadCustom();
    }

    public final void shutdown() {
        shutdownCustom();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasAccess(Player player) {
        // Оператор всегда имеет доступ к командам плагина.
        if (player.isOp()) {
            return true;
        }
        return permission == null || permission.isEmpty() || player.hasPermission(permission);
    }

    // Теперь метод называется sendPermissionError, но логика та же
    public void sendPermissionError(Player player) {
        String text;
        if (permissionRequired != null) {
            text = permissionRequired;
        } else {
            text = plugin.getConfig().getString("messages.no-permission",
                    "&cУ вас нет прав для выполнения этой команды.");
        }
        // Префикс больше не приклеивается автоматически —
        // он подставляется плейсхолдером %prefix% внутри самого текста.
        player.sendMessage(plugin.format(text));
    }

    protected String colorize(String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    protected abstract String defaultPermission();
    protected abstract void reloadCustom();
    protected abstract void execute(Player player, String[] args);
    protected void shutdownCustom() {}
}
