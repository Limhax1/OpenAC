package me.limhax.openAC;

import com.github.retrooper.packetevents.PacketEvents;
import dev.thomazz.pledge.Pledge;
import dev.thomazz.pledge.pinger.ClientPinger;
import dev.thomazz.pledge.pinger.ClientPingerListener;
import dev.thomazz.pledge.pinger.ClientPingerOptions;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.listener.Listener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public final class OpenAC extends JavaPlugin implements ClientPingerListener {

  @Getter
  private static OpenAC instance;
  @Getter
  private ScheduledThreadPoolExecutor executor;
  @Getter
  private Listener listener;
  @Getter
  private Pledge pledge;
  @Getter
  private ClientPinger pinger;

  @Override
  public void onLoad() {
    instance = this;
    executor = new ScheduledThreadPoolExecutor(2);
    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
    PacketEvents.getAPI().load();
  }

  @Override
  public void onEnable() {
    PacketEvents.getAPI().init();

    this.pledge = Pledge.getOrCreate(this);
    this.pinger = this.pledge.createPinger(ClientPingerOptions.range(-1, -30000));
    this.pinger.attach(this);

    this.listener = new Listener();
    PacketEvents.getAPI().getEventManager().registerListener(listener);
    getServer().getPluginManager().registerEvents(listener, this);
  }

  @Override
  public void onDisable() {
    pledge.destroy();
    PacketEvents.getAPI().terminate();
  }

  @Override
  public void onPingSendStart(Player player, int id) {
    // Unused for now.
  }

  @Override
  public void onPingSendEnd(Player player, int id) {
    PlayerData data = listener.getPlayerDataMap().get(player.getEntityId());
    if (data != null && data.getConnectionProcessor() != null) {
      data.getConnectionProcessor().onPingSendEnd(id);
    }
  }

  @Override
  public void onPongReceiveStart(Player player, int id) {
    // Unused for now.
  }

  @Override
  public void onPongReceiveEnd(Player player, int id) {
    PlayerData data = listener.getPlayerDataMap().get(player.getEntityId());
    if (data != null && data.getConnectionProcessor() != null) {
      data.getConnectionProcessor().onPongReceiveEnd(id);
    }
  }
}
