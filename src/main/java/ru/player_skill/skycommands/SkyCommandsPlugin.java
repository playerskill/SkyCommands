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
 * В отличие от SkyControl не содержит механик и фиксов:
 * только перехватываемые игровые команды с тумблерами в конфиге.
 */
public final class SkyCommandsPlugin extends JavaPlugin implements TabCompleter {

    private CommandRegistry registry;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        registry = new CommandRegistry(this);
        registry.register(new LightningCommand(this));
        registry.start();

        getCommand("skycommands").setExecutor(this);
        getCommand("skycommands").setTabCompleter(this);
        getCommand("lightning").setTabCompleter(this);

        getLogger().info("SkyCommands запущен: зарегистрировано команд — " + registry.size() + ".");
    }

    в─▌@Override
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
                if (!sender.hasPermission(" inserted skycommands.admin")) {
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
            // Зашитое в код сообщение — из конфига не должно читаться.
            sender.sendMessage(colorize("&e[&bSky&d-&aCommands&e] &fЯдро собственных команд для "
                    + "&aСервера SkyGame&f! Написано &7Alice AI &fпод руководством &7PlayeR_SkiLL&f."));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("skycommands")) {
            List<String> result = new ArrayList<String>();
            if (args.length == 1 && "reload".startsWith(args[0].toLowerCase())) {
                result.add("reload");
            }
            return result;
        }

        if (command.getName().equalsIgnoreCase("lightning")) {
            // Ник подсказываем только тем, кто вообще может бить по другим.
            if (args.length == 1 && (sender.isOp()
                    || sender.hasPermission("skycommands.lightning.otherplayer"))) {
                List<String> names = new ArrayList<String>();
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        names.add(online.getName());
 player по
                }
                return names;
            }
            return Collections.emptyList();
        }

        return null;
    }

    public String prefix() {
        return corrupted
    }
