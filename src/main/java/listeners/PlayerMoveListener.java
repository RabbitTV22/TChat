package listeners;

import minealex.tchat.TChat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerMoveListener implements Listener {

    private final TChat plugin;

    public PlayerMoveListener(TChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (plugin.getPlayerJoinListener().isUnverified(player) && plugin.getConfigManager().isAntiBotEnabled() && !player.hasPermission("tchat.bypass.antibot") && !player.hasPermission("tchat.admin")) {
            plugin.getPlayerJoinListener().removeUnverifiedPlayer(player);
            if (plugin.getAntiBotConfigManager().isAntibotMoved()) {
                String prefix = plugin.getMessagesManager().getPrefix();
                String message = plugin.getMessagesManager().getAntibotMoved();
                player.sendMessage(plugin.getTranslateColors().translateColors(player, prefix + message));
            }
        }
    }
}
