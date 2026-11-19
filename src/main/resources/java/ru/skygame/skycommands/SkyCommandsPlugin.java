package ru.skygame.skycommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import ru.skygame.skycommands.command.CommandRegistry;
import ru.skygame.skycommands.command.LightningCommand;

/**
 * SkyCommands — ядро игровых команд SkyGame.
 * Только команды: без механик, фиксов и сложной логики.
 * Всё общее вынесено в SkyCommand и CommandRegistry.
 */
public final class SkyCommandsPlugin extends JavaPlugin {

    private CommandRegistry registry;

    @Override
    public void onEnable() {
        // Создаём конфиг по умолчанию, если его нет
        saveDefaultConfig();

        registry = new CommandRegistry(this);

        // Регистрируем команды — сюда добавляй новые одной строкой
        registry.register(new LightningCommand(this));
        // Пример для следующей команды:
        // registry.register(new HealCommand(this));

        registry.start();

        // Привязываем команду /skycommands к этому плагину
        Command cmd = getCommand("skycommands");
        if (cmd != null) {
            cmd.setExecutor(this);
        }

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
        if (!command.getName().equalsIgnoreCase("skycommands")) {
            return false;
        }

        // Обработка /skycommands reload
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

        // Зашитое в код сообщение (не из конфига)
        sender.sendMessage("[SkyCommands] Ядро собственных команд для SkyGame наравне с MyCommands. "
                + "Написано Alice AI под руководством PlayeR_SkiLL для легендарного SkyGame!");
        return true;
    }

    /**
     * Возвращает префикс из конфига с раскраской.
     */
    public String prefix() {
        return colorize(getConfig().getString("messages.prefix", ""));
    }

    /**
     * Конвертирует &-коды в цвета Bukkit.
     */
    public String colorize(String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
