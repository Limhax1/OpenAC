package me.limhax.openAC.check.impl.movement.speed;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.Debug;
import me.limhax.openAC.util.MathHelper;
import me.limhax.openAC.util.Packet;

@CheckInfo(name = "Speed", type = "C", description = "Ground speed prediction check.", experimental = true)
public class SpeedC extends Check {
  public SpeedC(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (!Packet.isPos(event)) return;

    if (data.getMovementProcessor().getClientAirTicks() > 0) return;

    if (data.getCollisionProcessor().hasCollision("cobweb") ||
        data.getCollisionProcessor().hasCollision("water") ||
        data.getCollisionProcessor().hasCollision("lava") ||
        data.getCollisionProcessor().hasCollision("ladder") ||
        data.getCollisionProcessor().hasCollision("vine") ||
        data.getCollisionProcessor().hasCollision("ice") ||
        data.getCollisionProcessor().hasCollision("slime") ||
        data.getMovementProcessor().getSinceTeleport() < 3 ||
        data.getPlayer().isFlying()) {
      return;
    }

    final double deltaXZ = data.getMovementProcessor().getDeltaXZ();
    final double maxSpeed = getMaxGroundSpeed();
    final double diff = deltaXZ - maxSpeed;

    final int velTicks = data.getVelocityProcessor().getSinceVelocity();
    if (diff > 0.001) {
      if (increaseBuffer(1, 2)) {
        fail("diff=" + diff + " delta=" + deltaXZ + " max=" + maxSpeed + " vel=" + velTicks);
      }
    } else {
      decreaseBufferBy(0.025);
    }
  }

  private double getMaxGroundSpeed() {
    float acceleration = getAcceleration();

    double lastDeltaX = data.getMovementProcessor().getLastDeltaX();
    double lastDeltaZ = data.getMovementProcessor().getLastDeltaZ();

    if (data.getVelocityProcessor().getSinceVelocity() == 1) {
      lastDeltaX = data.getVelocityProcessor().getVelocity().getX();
      lastDeltaZ = data.getVelocityProcessor().getVelocity().getZ();
    }

    float lSlipperiness = data.getMovementProcessor().isLastLastOnGround() ? 0.6F : 0.91F;

    double motionX = lastDeltaX * (data.getVelocityProcessor().getSinceVelocity() == 1 ? 1 : lSlipperiness);
    double motionZ = lastDeltaZ * (data.getVelocityProcessor().getSinceVelocity() == 1 ? 1 : lSlipperiness);

    double maxPossible = 0.0;

    double[][] inputs = {
        {0, 0.98},
        {0, -0.98},
        {-0.98, 0},
        {0.98, 0},
        {-0.98, 0.98},
        {0.98, 0.98},
        {-0.98, -0.98},
        {0.98, -0.98},
        {0, 0}
    };

    for (double[] input : inputs) {
      double strafeInput = input[0];
      double forwardInput = input[1];

      double inputMagnitude = Math.sqrt(strafeInput * strafeInput + forwardInput * forwardInput);
      if (inputMagnitude > 1.0) {
        strafeInput /= inputMagnitude;
        forwardInput /= inputMagnitude;
      }

      double yaw = Math.toRadians(data.getRotationProcessor().getYaw());
      double sin = Math.sin(yaw);
      double cos = Math.cos(yaw);

      double moveX = strafeInput * cos - forwardInput * sin;
      double moveZ = forwardInput * cos + strafeInput * sin;

      double testMotionX = motionX + moveX * acceleration;
      double testMotionZ = motionZ + moveZ * acceleration;

      double resultSpeed = Math.sqrt(testMotionX * testMotionX + testMotionZ * testMotionZ);
      maxPossible = Math.max(maxPossible, resultSpeed);
    }

    return maxPossible;
  }

  private float getAcceleration() {
    float baseSpeed = 0.1F;
    float slipperiness = data.getMovementProcessor().isLastOnGround() ? 0.6F : 0.91F;

    boolean sprinting = data.getMovementProcessor().isSprint();
    if (sprinting) {
      baseSpeed *= 1.3F;
    }

    int speedLevel = 0;
    if (speedLevel > 0) {
      baseSpeed *= 1.0F + (0.2F * speedLevel);
    }

    int slownessLevel = 0;
    if (slownessLevel > 0) {
      baseSpeed *= 1.0F - (0.15F * slownessLevel);
    }

    return baseSpeed * 0.21600002F / (slipperiness * slipperiness * slipperiness);
  }
}