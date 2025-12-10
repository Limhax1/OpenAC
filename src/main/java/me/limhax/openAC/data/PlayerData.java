package me.limhax.openAC.data;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import lombok.Getter;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.impl.combat.aim.AimA;
import me.limhax.openAC.check.impl.combat.aim.AimB;
import me.limhax.openAC.check.impl.combat.aim.AimC;
import me.limhax.openAC.check.impl.combat.reach.Hitbox;
import me.limhax.openAC.check.impl.combat.reach.Reach;
import me.limhax.openAC.check.impl.combat.velocity.VelocityA;
import me.limhax.openAC.check.impl.combat.velocity.VelocityB;
import me.limhax.openAC.check.impl.movement.flight.FlightA;
import me.limhax.openAC.check.impl.movement.flight.FlightB;
import me.limhax.openAC.check.impl.movement.speed.SpeedA;
import me.limhax.openAC.check.impl.movement.speed.SpeedB;
import me.limhax.openAC.check.impl.movement.speed.SpeedC;
import me.limhax.openAC.processor.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class PlayerData {
  private final Player player;
  private final List<Check> checks;
  private TrackingProcessor entityTracker;
  private MovementProcessor movementProcessor;
  private RotationProcessor rotationProcessor;
  private WorldProcessor worldProcessor;
  private CollisionProcessor collisionProcessor;
  private ConnectionProcessor connectionProcessor;
  private VelocityProcessor velocityProcessor;
  private CombatProcessor combatProcessor;

  public PlayerData(Player player) {
    this.player = player;
    this.checks = new CopyOnWriteArrayList<>();
    this.entityTracker = new TrackingProcessor(this);
    this.connectionProcessor = new ConnectionProcessor(this);
    this.movementProcessor = new MovementProcessor(this);
    this.rotationProcessor = new RotationProcessor(this);
    this.collisionProcessor = new CollisionProcessor(this);
    this.worldProcessor = new WorldProcessor(this);
    this.velocityProcessor = new VelocityProcessor(this);
    this.combatProcessor = new CombatProcessor(this);

    checks.add(new Reach(this));
    checks.add(new Hitbox(this));

    checks.add(new SpeedA(this));
    checks.add(new SpeedB(this));
    checks.add(new SpeedC(this));

    checks.add(new VelocityA(this));
    checks.add(new VelocityB(this));

    checks.add(new FlightA(this));
    checks.add(new FlightB(this));

    checks.add(new AimA(this));
    checks.add(new AimB(this));
    checks.add(new AimC(this));
  }

  public void onReceive(PacketReceiveEvent event) {

    movementProcessor.onReceive(event);
    rotationProcessor.onReceive(event);
    collisionProcessor.onReceive(event);
    velocityProcessor.onReceive(event);
    combatProcessor.onReceive(event);
    entityTracker.onReceive(event);

    for (Check check : checks) {
      check.onReceive(event);
    }
  }

  public void onSend(PacketSendEvent event) {

    entityTracker.onSend(event);
    worldProcessor.onSend(event);
    velocityProcessor.onSend(event);
    movementProcessor.onSend(event);

    for (Check check : checks) {
      check.onSend(event);
    }
  }

  public void cleanup() {
    checks.clear();
    entityTracker = null;
    if (connectionProcessor != null) {
      connectionProcessor.cleanup();
      connectionProcessor = null;
    }
    if (worldProcessor != null) {
      worldProcessor.clearChunks();
    }
    worldProcessor = null;
    collisionProcessor = null;
    movementProcessor = null;
    rotationProcessor = null;
  }
}
