package me.limhax.openAC.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import lombok.Getter;
import me.limhax.openAC.OpenAC;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.EntityUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Listener extends PacketListenerAbstract implements org.bukkit.event.Listener {

  @Getter
  public Map<Integer, PlayerData> playerDataMap = new HashMap<>();

  @Override
  public void onPacketReceive(PacketReceiveEvent event) {
    Player player = event.getPlayer();
    if (player == null) return;

    if (playerDataMap.containsKey(player.getEntityId())) {
      PlayerData data = playerDataMap.get(player.getEntityId());
      data.onReceive(event);
    } else {
      PlayerData data = new PlayerData(player);
      playerDataMap.put(player.getEntityId(), data);
    }
  }

  @Override
  public void onPacketSend(PacketSendEvent event) {
    Player player = event.getPlayer();
    if (player == null) return;

    if (playerDataMap.containsKey(player.getEntityId())) {
      PlayerData data = playerDataMap.get(player.getEntityId());
      data.onSend(event);
    } else {
      PlayerData data = new PlayerData(player);
      if (!EntityUtils.getPlayers().contains(player)) {
        EntityUtils.addPlayer(event.getPlayer());
      }
      playerDataMap.put(player.getEntityId(), data);
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    if (player == null) return;
    if (EntityUtils.getPlayers().contains(player)) {
      EntityUtils.removePlayer(event.getPlayer());
    }
    if (playerDataMap.containsKey(player.getEntityId())) {
      PlayerData data = playerDataMap.get(player.getEntityId());
      OpenAC.getInstance().getExecutor().schedule(data::cleanup, 250, TimeUnit.MILLISECONDS);
    }
  }
}
