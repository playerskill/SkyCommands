package ru.player_skill.skycommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ru.player_skill.skycommands.command.CommandRegistry;
import ru.player_skill.skycommands.command.LightningCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SkyCommands — ядро игровых команд SkyGame.
 */
public final class SkyCommandsPlugin extends JavaPlugin implements TabCompleter {

    private CommandRegistry registry;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        registry = new CommandRegistry(this);
        registry.register(new LightningCommand(this));
        registry.start();

        // Команда /skycommands
        Command skyCmd = getCommand("skycommands");
        if (skyCmd != null) {
            skyCmd.setExecutor(this);
            skyCmd.setTabCompleter(this);
        }

        // Команда /lightning — таб будет работать только если она объявлена в plugin.yml
        Command lightningCmd = getCommand("lightning");
        if (lightningCmd != null) {
            lightningCmd.setTabCompleter(this);
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
        // Важно: args — это массив String[], поэтому используем args.length
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
            // Сообщение о плагине
            sender.sendMessage(colorize("&e[&bSky&d-&aCommands&e] &fЯдро собственных команд для "
                    + "&aСервера SkyGame&f! Написано &7Alice AI &fпод руководством &7PlayeR_SkiLL&f."));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmdName = command.getName().toLowerCase();

        // Таб для /skycommands [reload]
        if (cmdName.equals("skycommands")) {
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                if ("reload".startsWith(input)) {
                    return Collections.singletonList("reload");
                }
            }
            return null;
        }

        // Таб для /lightning [ник игрока]
        if (cmdName.equals("lightning")) {
            // Подсказываем ник только если есть право бить по другим или игрок — оператор
            if (args.length == 1) {
                if (sender.isOp() || sender.hasPermission("skycommands.lightning.otherplayer")) {
                    List<String> matches = new ArrayList<>();
                    String input = args[0].toLowerCase();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(input)) {
                            matches.add(p.getName());
                        }
                    }
                    return matches;
                }
            }
            return Collections.emptyList();
        }

        return null;
    }

    public String prefix() {
        return colorize(getConfig().getString("messages.prefix", ""));
    }

    public String colorize(String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
