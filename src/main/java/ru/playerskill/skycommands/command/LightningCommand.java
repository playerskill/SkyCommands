package ru.playerskill.skycommands.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * LightningCommand — отдельная команда для плагина SkyCommands.
 * Делает только одно: вызывает безопасную молнию по команде игрока.
 */
public final class LightningCommand implements Listener {

    public static final String PERMISSION = "skycommands.lightning";

    private final JavaPlugin plugin;
    private volatile boolean enabled;
    private volatile long cooldownSeconds;
    private volatile String msgNoPermission;
    private volatile String msgCooldown;
    private volatile String msgBolt;

    private final Map<UUID, Long> lastUsed = new HashMap<>();

    public LightningCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        applyConfig();
        plugin.getLogger().info(enabled
                ? "[Lightning] Команда /lightning готова (алиас /mylightning)."
                : "[Lightning] Отключена в конфиге.");
    }

    public void reload() {
        applyConfig();
    }

    public void shutdown() {
        lastUsed.clear();
    }

    private void applyConfig() {
        enabled = plugin.getConfig().getBoolean("lightning.enabled", true);
        cooldownSeconds = Math.max(0L, plugin.getConfig().getLong("lightning.cooldown-seconds", 5L));
        msgNoPermission = plugin.getConfig().getString(
                "lightning.messages.no-permission",
                "&cКоманда доступна только донатерам.");
        msgCooldown = plugin.getConfig().getString(
                "lightning.messages.cooldown",
                "&cМолния будет доступна через %seconds% сек.");
        msgBolt = plugin.getConfig().getString(
                "lightning.messages.bolt",
                "&6⚡ &e%player% &6вызвал молнию!");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!enabled || event.isCancelled()) {
            return;
        }

        String raw = event.getMessage().toLowerCase();
        boolean isLightning = raw.equals("/lightning") || raw.startsWith("/lightning ");
        boolean isMyLightning = raw.equals("/mylightning") || raw.startsWith("/mylightning ");

        if (!(isLightning || isMyLightning)) {
            return;
        }

        event.setCancelled(true); // перехватили, Essentials не увидит
        Player player = event.getPlayer();

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(colorize(msgNoPermission));
            return;
        }

        long now = System.currentTimeMillis();
        if (cooldownSeconds > 0L) {
            Long previous = lastUsed.get(player.getUniqueId());
            if (previous != null) {
                long elapsed = (now - previous) / 1000L;
                if (elapsed < cooldownSeconds) {
                    long remain = cooldownSeconds - elapsed;
                    player.sendMessage(colorize(msgCooldown.replace("%seconds%", String.valueOf(remain))));
                    return;
                }
            }
            lastUsed.put(player.getUniqueId(), now);
        }

        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world != null) {
            world.strikeLightningEffect(loc); // только эффект, без урона
        }

        String text = colorize(msgBolt.replace("%player%", player.getDisplayName()));
        for (CommandSender recipient : Bukkit.getOnlinePlayers()) {
            recipient.sendMessage(text);
        }
    }

    private String colorize(String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
