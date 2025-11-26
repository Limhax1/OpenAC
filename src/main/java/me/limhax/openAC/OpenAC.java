package me.limhax.openAC;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import me.limhax.openAC.listener.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public final class OpenAC extends JavaPlugin {

  @Getter
  private static OpenAC instance;
  @Getter
  private ScheduledThreadPoolExecutor executor;

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
    PacketEvents.getAPI().getEventManager().registerListener(new Listener());
  }

  @Override
  public void onDisable() {
    PacketEvents.getAPI().terminate();
  }
}
