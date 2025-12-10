package me.limhax.openAC.check.impl.movement.speed;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.MathHelper;
import me.limhax.openAC.util.Packet;

@CheckInfo(name = "Speed", type = "A", description = "Prediction speed check.", experimental = true)
public class SpeedA extends Check {
  public SpeedA(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (!Packet.isPos(event)) return;
    if (data.getMovementProcessor().getClientAirTicks() <= 3) return;
    if (data.getCollisionProcessor().getSinceHorizontal() < 4) return;

    if (data.getCollisionProcessor().hasCollision("cobweb") ||
        data.getCollisionProcessor().hasCollision("water") ||
        data.getCollisionProcessor().hasCollision("lava") ||
        data.getCollisionProcessor().hasCollision("ladder") ||
        data.getCollisionProcessor().hasCollision("vine") ||
        data.getPlayer().isFlying()) {
      return;
    }

    // get the current deltas of the player
    final double deltaX = data.getMovementProcessor().getDeltaX();
    final double deltaZ = data.getMovementProcessor().getDeltaZ();
    final double deltaXZ = data.getMovementProcessor().getDeltaXZ();

    final double diff = getDiff(deltaX, deltaZ);
    final int velTicks = data.getVelocityProcessor().getSinceVelocity();
    if (diff > 0.003) {
      if (increaseBuffer(1, 4)) {
        fail("diff=" + diff + " delta=" + deltaXZ + " vel=" + velTicks);
      }
    } else {
      decreaseBufferBy(0.025);
    }
   }

  private double getDiff(double deltaX, double deltaZ) {
    final double lastDeltaX = data.getVelocityProcessor().getSinceVelocity() == 1 ? data.getVelocityProcessor().getVelocity().getX() : data.getMovementProcessor().getLastDeltaX();
    final double lastDeltaZ = data.getVelocityProcessor().getSinceVelocity() == 1 ? data.getVelocityProcessor().getVelocity().getZ() : data.getMovementProcessor().getLastDeltaZ();
    final int velTicks = data.getVelocityProcessor().getSinceVelocity();
    float friction = 0.91F;

    if (velTicks == 1) {
      friction = 1F;
    }

    double best = Double.MAX_VALUE;

    {
      double predictedX = lastDeltaX * friction;
      double predictedZ = lastDeltaZ * friction;

      double diffX = deltaX - predictedX;
      double diffZ = deltaZ - predictedZ;

      best = Math.min(best, MathHelper.hypot(diffX, diffZ));
    }

    {
      double predictedX = lastDeltaX * friction * 0.6F;
      double predictedZ = lastDeltaZ * friction * 0.6F;

      double diffX = deltaX - predictedX;
      double diffZ = deltaZ - predictedZ;

      best = Math.min(best, MathHelper.hypot(diffX, diffZ));
    }

    return best - 0.026F;
  }
}
