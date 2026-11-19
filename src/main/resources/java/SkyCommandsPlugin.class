package ru.skygame.skycommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import ru.skygame.skycommands.command.CommandRegistry;
import ru.skygame.skycommands.command.LightningCommand;

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
                    sender.sendMessage(prefix() + colorize(getConfig().getString(
                            "messages.no-permission",
                            "&cУ вас нет прав для выполнения этой команды.")));
                    return true;
                }
                reloadConfig();
                registry.reload();
                sender.sendMessage(prefix() + colorize("&aКонфиг SkyCommands перезагружен."));
                return true;
            }
            // Зашитое в код сообщение — из конфига не читается.
            sender.sendMessage("[SkyCommands] Ядро собственных команд для SkyGame наравне с MyCommands. "
                    + "Написано Alice AI под руководством PlayeR_SkiLL для легендарного SkyGame!");
            return true;
        }
        return false;
    }

    public String prefix() {
        return colorize(getConfig().getString("messages.prefix", ""));
    }

    public String colorize(String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
