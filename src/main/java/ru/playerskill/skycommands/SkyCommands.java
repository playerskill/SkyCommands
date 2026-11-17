package ru.playerskill.skycommands;

import org.bukkit.plugin.java.JavaPlugin;
import ru.playerskill.skycommands.command.LightningCommand;

public final class SkyCommands extends JavaPlugin {

    private LightningCommand lightningCommand;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // создаст config.yml при первом запуске, если нет

        // Инициализируем команды
        lightningCommand = new LightningCommand(this);
        lightningCommand.start();

        getLogger().info("SkyCommands включён: игровые команды готовы.");
    }

    @Override
    public void onDisable() {
        if (lightningCommand != null) {
            lightningCommand.shutdown();
        }
        getLogger().info("SkyCommands выключен.");
    }
}
