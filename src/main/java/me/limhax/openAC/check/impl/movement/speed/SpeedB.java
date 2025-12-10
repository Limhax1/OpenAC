package me.limhax.openAC.check.impl.movement.speed;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.Packet;

@CheckInfo(name = "Speed", type = "B", description = "Prediction speed check.", experimental = true)
public class SpeedB extends Check {
  public SpeedB(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (!Packet.isPos(event)) return;
    if (data.getMovementProcessor().isOnGround()) return;
    if (data.getPlayer().isFlying()) return;
    if (data.getMovementProcessor().getSinceTeleport() < 3) return;

    final double deltaXZ = data.getMovementProcessor().getDeltaXZ();
    final double lastDeltaXZ = data.getVelocityProcessor().getSinceVelocity() == 1 ? data.getVelocityProcessor().getVelocityXZ() : data.getMovementProcessor().getLastDeltaXZ();

    final int velTicks = data.getVelocityProcessor().getSinceVelocity();

    float friction = 0.91F;
    double tolerance = 0;

    if (velTicks == 1) {
      friction = 1F;
    }

    if (data.getMovementProcessor().getClientAirTicks() == 1) {
      tolerance = 0.31D;
    }

    final double predicted = lastDeltaXZ * friction + 0.026F;
    final double diff = (deltaXZ - predicted) - tolerance;

    if (diff > 1E-4) {
      if (increaseBuffer(1, 2)) {
        fail("diff=" + diff + " pred=" + predicted + " delta=" + deltaXZ + " vel=" + velTicks);
      }
    } else {
      decreaseBufferBy(0.015);
    }
  }
}
