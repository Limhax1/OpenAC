package me.limhax.openAC.check.impl.combat.velocity;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.MathHelper;
import me.limhax.openAC.util.Packet;

@CheckInfo(name = "Velocity", type = "B", description = "Horizontal velocity.", experimental = true)
public class VelocityB extends Check {

  public VelocityB(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (!Packet.isPos(event)) return;
    if (data.getCollisionProcessor().getSinceHorizontal() < 3) return;
    if (data.getCollisionProcessor().getSinceVerticalTop() < 3) return;

    if (data.getCollisionProcessor().hasCollision("cobweb") ||
        data.getCollisionProcessor().hasCollision("water") ||
        data.getCollisionProcessor().hasCollision("lava") ||
        data.getCollisionProcessor().hasCollision("ladder") ||
        data.getMovementProcessor().getSinceTeleport() < 4 ||
        data.getCollisionProcessor().hasCollision("vine")) {
      return;
    }

    if (data.getMovementProcessor().getClientAirTicks() < 4) return;

    final int velocityTicks = data.getVelocityProcessor().getSinceVelocity();

    final double deltaX = data.getMovementProcessor().getDeltaX();
    final double deltaZ = data.getMovementProcessor().getDeltaZ();

    final double velX = data.getVelocityProcessor().getVelocity().getX();
    final double velZ = data.getVelocityProcessor().getVelocity().getZ();

    if (velocityTicks != 1) return;

    final double diffX = deltaX - velX;
    final double diffZ = deltaZ - velZ;

    final double diffX2 = deltaX - velX * 0.6F;
    final double diffZ2 = deltaZ - velZ * 0.6F;

    final boolean sprinting = data.getMovementProcessor().isSprint() || data.getMovementProcessor().isLastSprint();
    final double diff = MathHelper.hypot(diffX, diffZ) - (sprinting ? 0.026F : 0.2F);
    final double diff2 = MathHelper.hypot(diffX2, diffZ2) - (sprinting ? 0.026F : 0.2F);
    final double diff3 = Math.min(diff, diff2);
    if (diff3 > 0.001) {
      if (increaseBuffer(1, 2)) {
        fail("diff=" + diff);
      }
    } else {
      decreaseBufferBy(0.05);
    }
  }
}