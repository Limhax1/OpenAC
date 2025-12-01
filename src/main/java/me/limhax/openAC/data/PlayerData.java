package me.limhax.openAC.data;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import lombok.Getter;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.impl.Reach;
import me.limhax.openAC.check.impl.ReachB;
import me.limhax.openAC.processor.ConnectionProcessor;
import me.limhax.openAC.processor.EntityTracker;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class PlayerData {
  private final Player player;
  private final List<Check> checks;
  private EntityTracker entityTracker;
  private ConnectionProcessor connectionProcessor;

  public PlayerData(Player player) {
    this.player = player;
    this.checks = new CopyOnWriteArrayList<>();
    this.entityTracker = new EntityTracker(this);
    this.connectionProcessor = new ConnectionProcessor(this);

    checks.add(new Reach(this));
    checks.add(new ReachB(this));
  }

  public void onReceive(PacketReceiveEvent event) {
    for (Check check : checks) {
      check.onReceive(event);
    }
  }

  public void onSend(PacketSendEvent event) {

    entityTracker.onSend(event);

    for (Check check : checks) {
      check.onSend(event);
    }
  }


  public void cleanup() {
    checks.clear();
    entityTracker.cleanup();
    entityTracker = null;
    if (connectionProcessor != null) {
      connectionProcessor.cleanup();
      connectionProcessor = null;
    }
  }
}
