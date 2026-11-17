package ru.playerskill.skycommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import ru.playerskill.skycommands.command.LightningCommand;

public final class SkyCommands extends JavaPlugin implements CommandExecutor {

    private LightningCommand lightningCommand;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Инициализируем модуль с молнией
        lightningCommand = new LightningCommand(this);
        lightningCommand.start();

        // Регистрируем команду /skycommands прямо здесь
        getCommand("skycommands").setExecutor(this);

        getLogger().info("SkyCommands включён: игровые команды готовы.");
    }

    @Override
    public void onDisable() {
        if (lightningCommand != null) {
            lightningCommand.shutdown();
        }
        getLogger().info("SkyCommands выключен.");
    }

    // Обработчик команды /skycommands
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String message = ChatColor.translateAlternateColorCodes('&',
                "[SkyCommands] Ядро собственных команд для SkyGame наравне с MyCommands. Написано Alice AI под руководством PlayeR_SkiLL."
        );
        sender.sendMessage(message);
        return true;
    }
}
