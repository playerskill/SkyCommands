package ru.player_skill.skycommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import ru.player_skill.skycommands.command.CommandRegistry;
import ru.player_skill.skycommands.command.LightningCommand;

/**
 * SkyCommands — ядро игровых команд SkyGame.
 * В отличие от SkyControl не содержит механик и фиксов:
 * только перехватываемые игровые команды с тумблерами в конфиге.
 */
public final class SkyCommandsPlugin extends JavaPlugin {

    private CommandRegistry registry;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        registry = new CommandRegistry(this);
        registry.register(new LightningCommand(this));
        registry.start();

        getCommand("skycommands").setExecutor(this);

        getLogger().info("SkyCommands запущен: зарегистрировано команд — " + registry.size() + ".");
    }

    @Override
    public void onDisable() {
        if (registry != null) {
            registry.shutdown();
            registry = null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("skycommands")) {
            if (args.length > 0 && "reload".equalsIgnoreCase(args[0])) {
                if (!sender.hasPermission("skycommands.admin")) {
                    sender.sendMessage(format(getConfig().getString(
                            "messages.no-permission",
                            "&cУ вас нет прав для выполнения этой команды.")));
                    return true;
                }
                reloadConfig();
                registry.reload();
                sender.sendMessage(format("&aКонфиг SkyCommands перезагружен."));
                return true;
            }
            // Зашитое в код сообщение — из конфига не читается.
            sender.sendMessage(format("%prefix%&fЯдро собственных команд для &aСервера SkyGame&f! "
                    + "Написано &7Alice AI &fпод руководством &7PlayeR_SkiLL&f."));
            return true;
        }
        return false;
    }

    /**
     * Готовит сообщение к отправке: подставляет %prefix% из конфига
     * и переводит цветовые коды &x в настоящие цвета.
     */
    public String format(String raw) {
        String text = raw.replace("%prefix%", getConfig().getString("messages.prefix", ""));
        return colorize(text);
    }

    public String prefix() {
        return colorize(getConfig().getString("messages.prefix", ""));
    }

    public String colorize(String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
