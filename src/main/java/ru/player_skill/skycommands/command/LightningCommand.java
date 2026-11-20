package ru.player_skill.skycommands.command;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import ru.player_skill.skycommands.SkyCommandsPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * /lightning (алиасы /mylightning, /zap, /bolt, /volt) — молния как
 * в Essentials, но без урона.
 *
 * Все сообщения поддерживают плейсхолдер %prefix% — префикс из messages.prefix
 * подставляется там, где он написан, а не приклеивается автоматически.
 *
 * Без аргументов: удар в точку, куда смотрит игрок. Луч идёт от глаз до
 * strike-distance блоков: попали в блок — молния в него, смотрим в небо —
 * молния вспыхивает в воздухе в конце луча (как у Essentials: сам удар не
 * виден, но небо сверкает).
 *
 * С аргументом (/lightning Ник): удар по указанному игроку. Требуется право
 * skycommands.lightning.otherplayer (операторы проходят всегда).
 *
 * strikeLightningEffect даёт только визуал и звук: без урона, поджога
 * и разрушений — приваты не страдают.
 *
 * Ключи конфига (commands.lightning):
 *   enabled               — тумблер команды (общий, читается в SkyCommand)
 *   permission            — право доступа (общее, читается в SkyCommand)
 *   permission-required   — своё сообщение вместо общего no-permission
 *                           (общее, читается в SkyCommand)
 *   cooldown-seconds      — кулдаун между ударами (0 — без кулдауна)
 *   strike-distance       — дальность прицеливания взглядом (в блоках)
 *   show-message          — показывать ли сообщение после удара
 *   message               — текст при ударе в точку взгляда
 *   message-other         — текст при ударе по игроку, %player% — ник
 *   cooldown-message      — текст при кулдауне, %seconds% — остаток
 *   player-not-found      — текст, если игрок с таким ником не найден
 */
public final class LightningCommand extends SkyCommand {

    /** Право на удар молнией по другому игроку. */
    private static final String PERMISSION_OTHER = "skycommands.lightning.otherplayer";

    /** Кулдаун на игрока: UUID -> момент последнего удара (мс). */
    private final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();

    private volatile long cooldownSeconds = 5L;
    private volatile int strikeDistance = 120;
    private volatile boolean showMessage = true;
    private volatile String message = "&eВы ударили молнией!";
    private volatile String messageOther = "&eВы ударили молнией по игроку &f%player%&e!";
    private volatile String cooldownMessage = "&cМолния будет доступна через %seconds% сек.";
    private volatile String playerNotFound = "&cИгрок не найден.";

    public LightningCommand(SkyCommandsPlugin plugin) {
        super(plugin, "lightning", "mylightning", "zap", "bolt", "volt");
    }

    @Override
    protected String defaultPermission() {
        return "skycommands.lightning";
    }

    @Override
    protected void reloadCustom() {
        String base = "commands.lightning";
        cooldownSeconds = Math.max(0L, plugin.getConfig().getLong(base + ".cooldown-seconds", 5L));
        strikeDistance = Math.max(1, plugin.getConfig().getInt(base + ".strike-distance", 120));
        showMessage = plugin.getConfig().getBoolean(base + ".show-message", true);
        message = plugin.getConfig().getString(base + ".message", "&eВы ударили молнией!");
        messageOther = plugin.getConfig().getString(base + ".message-other",
                "&eВы ударили молнией по игроку &f%player%&e!");
        cooldownMessage = plugin.getConfig().getString(base + ".cooldown-message",
                "&cМолния будет доступна через %seconds% сек.");
        playerNotFound = plugin.getConfig().getString(base + ".player-not-found", "&cИгрок не найден.");
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
                    player.sendMessage(plugin.format(
                            cooldownMessage.replace("%seconds%", String.valueOf(remain))));
                    return;
                }
            }
            cooldowns.put(player.getUniqueId(), now);
        }

        Location target;

        if (args.length > 0) {
            // Удар по другому игроку — нужна отдельная правка (оператор проходит всегда).
            if (!player.isOp() && !player.hasPermission(PERMISSION_OTHER)) {
                player.sendMessage(plugin.format(plugin.getConfig().getString(
                        "messages.no-permission",
                        "&cУ вас нет прав для выполнения этой команды.")));
                return;
            }

            Player victim = plugin.getServer().getPlayerExact(args[0]);
            if (victim == null) {
                // На всякий случай — совпадение по началу ника, как в командах Minecraft.
                victim = matchPlayer(args[0]);
            }
            if (victim == null) {
                player.sendMessage(plugin.format(playerNotFound));
                return;
            }

            strike(victim.getLocation());

            if (showMessage) {
                player.sendMessage(plugin.format(messageOther.replace("%player%", victim.getName())));
            }
            plugin.getLogger().info("Игрок " + player.getName() + " ударил молнией по игроку "
                    + victim.getName() + ".");
            return;
        }

        // Удар в точку взгляда — как в Essentials.
        target = locateBySight(player);
        strike(target);

        if (showMessage) {
            player.sendMessage(plugin.format(message));
        }
        plugin.getLogger().info("Игрок " + player.getName() + " ударил молнией.");
    }

    /**
     * Ищет точку вдоль луча взгляда. Первый непрозрачный блок — удар в него;
     * если до предела луча всё пусто (смотрим в небо) — удар в последнюю
     * точку луча высоко в воздухе.
     */
    private Location locateBySight(Player player) {
        Location eye = player.getEyeLocation();
        BlockIterator ray = new BlockIterator(eye, 0, strikeDistance);

        Location target = null;
        while (ray.hasNext()) {
            Block block = ray.next();
            Location center = block.getLocation().add(0.5D, 0.0D, 0.5D);

            if (!block.getType().isTransparent()) {
                // Попали в блок — молния в его центр, как в Essentials.
                return center;
            }
            // Пока идём по воздуху — запоминаем последнюю точку луча.
            target = center;
        }
        // Смотрим в небо: удар в конец луча (молния сверкает в воздухе).
        return target != null ? target : eye;
    }

    /** Сам удар: только эффект, без урона, поджога и разрушений. */
    private void strike(Location location) {
        World world = location.getWorld();
        if (world != null) {
            world.strikeLightningEffect(location);
        }
    }

    /** Приблизительный поиск игрока по началу ника (без учёта регистра). */
    private Player matchPlayer(String name) {
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.getName().equalsIgnoreCase(name)) {
                return online;
            }
        }
        return null;
    }

    @Override
    protected void shutdownCustom() {
        cooldowns.clear();
    }
}
