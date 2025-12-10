package me.limhax.openAC.check.impl.combat.velocity;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.Debug;
import me.limhax.openAC.util.Packet;

@CheckInfo(name = "Velocity", type = "A", description = "Vertical velocity.", experimental = true)
public class VelocityA extends Check {
  public VelocityA(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (!Packet.isPos(event)) return;
    if (data.getVelocityProcessor().getSinceVelocity() != 1) return;
    if (data.getCollisionProcessor().getSinceHorizontal() < 3) return;
    if (data.getCollisionProcessor().getSinceVerticalTop() < 3) return;

    if (data.getCollisionProcessor().hasCollision("cobweb") ||
        data.getCollisionProcessor().hasCollision("water") ||
        data.getCollisionProcessor().hasCollision("lava") ||
        data.getCollisionProcessor().hasCollision("ladder") ||
        data.getMovementProcessor().getSinceTeleport() < 3 ||
        data.getCollisionProcessor().hasCollision("vine")) {
      return;
    }

    // get the velocityY that the player should take
    final double velY = data.getVelocityProcessor().getVelocity().getY();
    // too small, ignore
    if (velY < 0.03) return;

    // get the deltaY of the player
    final double deltaY = data.getMovementProcessor().getDeltaY();

    // get the difference
    final double diff = Math.abs(deltaY - velY);
    // check for jumpreset
    final double diffTwo = Math.abs(deltaY - 0.42F);
    // not sure why this can happen but sure
    final double diffThree = Math.abs((velY - 0.08) * 0.98D);

    final double min = Math.min(diff, diffTwo);
    final double minOne = Math.min(min, diffThree);

    final double pct = deltaY / velY;

    // we can be strict, as this shouldn't go above 1E-10
    if (minOne > 1E-5) {
      if (increaseBuffer(1, 2)) {
        fail("diff=" + diff + " velY=" + velY + " deltaY=" + deltaY + " " + pct * 100 + "%");
      }
    } else {
      decreaseBufferBy(0.05);
    }
  }
}
