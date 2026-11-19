package ru.player_skill.skycommands.command;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import ru.player_skill.skycommands.SkyCommandsPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * /lightning (алиас /mylightning) — удар молнией как в Essentials,
 * но принципиально безопасный: strikeLightningEffect даёт только
 * визуал и звук, без урона, поджога и разрушений — приваты не страдают.
 *
 * Ключи конфига (commands.lightning):
 *   enabled           — тумблер команды (общий, читается в SkyCommand)
 *   permission        — право доступа (общий, читается в SkyCommand)
 *   permission-required — своё сообщение вместо общего no-permission
 *                         (общий, читается в SkyCommand)
 *   cooldown-seconds  — кулдаун между ударами (0 — без кулдауна)
 *   show-message      — показывать ли сообщение "Вы ударили молнией"
 *   message           — текст этого сообщения
 *   cooldown-message  — текст при кулдауне, %seconds% — остаток
 */
public final class LightningCommand extends SkyCommand {

    /** Кулдаун на игрока: UUID -> момент последнего удара (мс). */
    private final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();

    private volatile long cooldownSeconds = 5L;
    private volatile boolean showMessage = true;
    private volatile String message = "&eВы ударили молнией!";
    private volatile String cooldownMessage = "&cМолния будет доступна через %seconds% сек.";

    public LightningCommand(SkyCommandsPlugin plugin) {
        super(plugin, "lightning", "mylightning");
    }

    @Override
    protected String defaultPermission() {
        return "skycommands.lightning";
    }

    @Override
    protected void reloadCustom() {
        String base = "commands.lightning";
        cooldownSeconds = Math.max(0L, plugin.getConfig().getLong(base + ".cooldown-seconds", 5L));
        showMessage = plugin.getConfig().getBoolean(base + ".show-message", true);
        message = plugin.getConfig().getString(base + ".message", "&eВы ударили молнией!");
        cooldownMessage = plugin.getConfig().getString(base + ".cooldown-message",
                "&cМолния будет доступна через %seconds% сек.");
    }

    @Override
    protected void execute(Player player, String[] args) {
        // Кулдаун: проверка до удара, запись — только если удар разрешён.
        if (cooldownSeconds > 0L) {
            long now = System.currentTimeMillis();
            Long previous = cooldowns.get(player.getUniqueId());
            if (previous != null) {
                long elapsed = (now - previous) / 1000L;
                if (elapsed < cooldownSeconds) {
                    long remain = cooldownSeconds - elapsed;
                    player.sendMessage(plugin.prefix()
                            + colorize(cooldownMessage.replace("%seconds%", String.valueOf(remain))));
                    return;
                }
            }
            cooldowns.put(player.getUniqueId(), now);
        }

        // Безопасная молния: только эффект (визуал и звук),
        // без урона, поджога и разрушений блоков.
        Location location = player.getLocation();
        World world = location.getWorld();
        if (world != null) {
            world.strikeLightningEffect(location);
        }

        // Сообщение игроку — если не отключено в конфиге (show-message: false).
        if (showMessage && message != null && !message.trim().isEmpty()) {
            player.sendMessage(plugin.prefix() + colorize(message));
        }

        plugin.getLogger().info("Игрок " + player.getName() + " ударил молнией.");
    }

    @Override
    protected void shutdownCustom() {
        cooldowns.clear();
    }
}
