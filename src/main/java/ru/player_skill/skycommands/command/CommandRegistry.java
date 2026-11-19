package ru.player_skill.skycommands.command;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import ru.player_skill.skycommands.SkyCommandsPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Реестр команд: перехватывает ввод на PlayerCommandPreprocessEvent
 * в приоритете LOWEST — раньше диспетчера плагинов и Essentials.
 * Если команда зарегистрирована и включена — отменяем событие и исполняем.
 * Если выключена — не трогаем: команда уходит в другие плагины как раньше.
 *
 * Реестр один на весь плагин: новые команды добавляются
 * одной строкой registry.register(...) в onEnable, без правок этого файла.
 */
public final class CommandRegistry implements Listener {

    private final SkyCommandsPlugin plugin;
    private final List<SkyCommand> commands = new ArrayList<SkyCommand>();

    public CommandRegistry(SkyCommandsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(SkyCommand command) {
        commands.add(command);
    }

    public int size() {
        return commands.size();
    }

    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        reload();
    }

    public void reload() {
        for (SkyCommand command : commands) {
            command.reload();
        }
    }

    public void shutdown() {
        for (SkyCommand command : commands) {
            command.shutdown();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message.length() < 2 || message.charAt(0) != '/') {
            return;
        }

        String body = message.substring(1);
        String name;
        String[] args;
        int spaceIndex = body.indexOf(' ');
        if (spaceIndex == -1) {
            name = body;
            args = new String[0];
        } else {
            name = body.substring(0, spaceIndex);
            args = body.substring(spaceIndex + 1).split(" ");
        }

        for (SkyCommand command : commands) {
            if (!command.matches(name)) {
                continue;
            }

            if (!command.isEnabled()) {
                // Команда выключена — не трогаем, уходит в другие плагины
                return;
            }

            event.setCancelled(true);
            Player player = event.getPlayer();

            if (!command.hasAccess(player)) {
                // permission-required команды или общий no-permission
                command.sendPermissionError(player);
                return;
            }

            command.execute(player, args);
            return;
        }
    }
}
