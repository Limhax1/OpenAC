package me.limhax.openAC.check.impl.movement.flight;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.Debug;
import me.limhax.openAC.util.Packet;

@CheckInfo(name = "Flight", type = "A", description = "Prediction fly check.", experimental = true)
public class FlightA extends Check {
  public FlightA(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (!Packet.isPos(event)) return;
    if (data.getCollisionProcessor().hasCollision("cobweb") ||
        data.getCollisionProcessor().hasCollision("water") ||
        data.getCollisionProcessor().hasCollision("lava") ||
        data.getCollisionProcessor().hasCollision("ladder") ||
        data.getCollisionProcessor().hasCollision("scaffolding") ||
        data.getCollisionProcessor().getSinceVerticalTop() < 3 ||
        data.getMovementProcessor().getSinceTeleport() < 3 ||
        data.getCollisionProcessor().hasCollision("vine") ||
        data.getPlayer().isFlying()) {
      return;
    }

    final int airTicks = data.getMovementProcessor().getClientAirTicks();
    if (airTicks == 0) return;
    final double deltaY = data.getMovementProcessor().getDeltaY();
    final double lastDeltaY = data.getMovementProcessor().getLastDeltaY();
    final int velTicks = data.getVelocityProcessor().getSinceVelocity();
    double predicted = (lastDeltaY - 0.08) * 0.98D;

    if (velTicks < 3) return;

    if (airTicks == 1 && deltaY > 0) {
      predicted = 0.42F;
    } else if (airTicks == 1 && deltaY < 0) {
      predicted = (0 - 0.08) * 0.98D;
    } else if (airTicks == 1 && !data.getCollisionProcessor().getVerticalBottomCollisions().isEmpty()) {
      predicted = 0;
    }

    if (airTicks == 1 && (
        data.getCollisionProcessor().getVerticalBottomCollisions().has("slime") ||
        data.getCollisionProcessor().getVerticalBottomCollisions().has("honey") ||
        data.getCollisionProcessor().getVerticalBottomCollisions().has("bed"))) {
      predicted = deltaY;
    }

    if (Math.abs(predicted) < 0.003) {
      predicted = 0;
    }

    if (!data.getCollisionProcessor().getCollisions().isEmpty()) {
      if (deltaY == 0.5) {
        predicted = 0.5;
      }
    }

    final double diff = Math.abs(predicted - deltaY);
    if (diff > 1E-7) {
      if (increaseBuffer(1, 3)) {
        fail("diff=" + diff + " pred=" + predicted + " delta=" + deltaY + " at=" + airTicks + " vel=" + velTicks + " last=" + lastDeltaY);
      } else {
        decreaseBufferBy(0.035);
      }
    }
  }
}
