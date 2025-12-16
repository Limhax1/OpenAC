package me.limhax.openAC.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import me.limhax.openAC.OpenAC;
import me.limhax.openAC.config.ConfigManager;
import me.limhax.openAC.data.PlayerData;
import org.bukkit.entity.Player;

public class OpenACCommandManager extends BaseCommand {

    @CommandAlias("alerts")
    @CommandPermission("openac.alerts")
    @Default
    public void onAlerts(Player player) {
        PlayerData playerData = OpenAC.getInstance().getListener().getPlayerDataMap().get(player.getEntityId());
        ConfigManager config = OpenAC.getInstance().getConfigManager();

        if (playerData == null) {
            playerData = new PlayerData(player);
            OpenAC.getInstance().getListener().getPlayerDataMap().put(player.getEntityId(), playerData);
        }

        playerData.setAlertsEnabled(!playerData.isAlertsEnabled());
        String message = playerData.isAlertsEnabled() ? config.getAlertsEnabled() : config.getAlertsDisabled();
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    @CommandAlias("verbose")
    @CommandPermission("openac.verbose")
    @Default
    public void onVerbose(Player player) {
        PlayerData playerData = OpenAC.getInstance().getListener().getPlayerDataMap().get(player.getEntityId());
        ConfigManager config = OpenAC.getInstance().getConfigManager();

        if (playerData == null) {
            playerData = new PlayerData(player);
            OpenAC.getInstance().getListener().getPlayerDataMap().put(player.getEntityId(), playerData);
        }

        playerData.setVerboseEnabled(!playerData.isVerboseEnabled());
        String message = playerData.isVerboseEnabled() ? config.getVerboseEnabled() : config.getVerboseDisabled();
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    @CommandAlias("rl|oacr")
    @CommandPermission("openac.reload")
    @Default
    public void onReload(Player player) {
        ConfigManager config = OpenAC.getInstance().getConfigManager();

        try {
            config.loadConfig();
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(config.getReloadSuccess()));
        } catch (Exception e) {
            String error = config.getReloadError().replace("%error%", e.getMessage());
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(error));
        }
    }
}
