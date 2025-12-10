package me.limhax.openAC.check.impl.combat.reach;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.processor.TrackingProcessor;
import me.limhax.openAC.util.AABB;
import me.limhax.openAC.util.MinecraftMath;
import me.limhax.openAC.util.Packet;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

@CheckInfo(name = "Hitbox", type = "A", description = "Invalid hit angle.", experimental = true)
public class Hitbox extends Check {

  private final static double[] possibleEyeHeights = {1.62F, 1.27F, 0.4F};
  private final Map<Integer, AttackData> queue = new HashMap<>();

  public Hitbox(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (Packet.isInteract(event)) {
      WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
      if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

      TrackingProcessor.TrackedEntity entry = data.getEntityTracker().getTracked().get(wrapper.getEntityId());
      if (entry == null) return;

      int entityId = wrapper.getEntityId();
      Vector3d attackPos = new Vector3d(
          data.getMovementProcessor().getX(),
          data.getMovementProcessor().getY(),
          data.getMovementProcessor().getZ()
      );

      queue.put(entityId, new AttackData(attackPos, entry.getInterpBox().copy()));
    }

    if (shouldSchedule(event.getPacketType())) {
      processQueue();
    }
  }

  private void processQueue() {
    for (Map.Entry<Integer, AttackData> entry : queue.entrySet()) {
      int entityId = entry.getKey();
      AttackData attackData = entry.getValue();

      TrackingProcessor.TrackedEntity e = data.getEntityTracker().getTracked().get(entityId);
      if (e == null) continue;

      AABB targetBox = attackData.targetBox;

      var rot = data.getRotationProcessor();
      float yaw = rot.getYaw();
      float pitch = rot.getPitch();
      float lastYaw = rot.getLastYaw();
      float lastPitch = rot.getLastPitch();

      double minOffset = Double.MAX_VALUE;

      for (double eyeHeight : possibleEyeHeights) {
        Vector3d eye = new Vector3d(
            attackData.position.x,
            attackData.position.y + eyeHeight,
            attackData.position.z
        );

        Vector3d lookVector = MinecraftMath.getLookVector(yaw, pitch);
        Vector3d lookEnd = new Vector3d(eye).add(lookVector.mul(6.0, new Vector3d()));
        Vector3d intercept = MinecraftMath.calculateIntercept(targetBox, eye, lookEnd);

        if (intercept != null) {
          minOffset = 0.0;
          break;
        }

        Vector3d closestPointOnBox = getClosestPoint(targetBox, eye);
        Vector3d toTarget = new Vector3d(closestPointOnBox).sub(eye).normalize();
        double dotProduct = lookVector.dot(toTarget);
        dotProduct = Math.max(-1.0, Math.min(1.0, dotProduct));
        double offset = Math.toDegrees(Math.acos(dotProduct));
        minOffset = Math.min(minOffset, offset);

        Vector3d lastLookVector = MinecraftMath.getLookVector(lastYaw, lastPitch);
        Vector3d lastLookEnd = new Vector3d(eye).add(lastLookVector.mul(6.0, new Vector3d()));
        Vector3d lastIntercept = MinecraftMath.calculateIntercept(targetBox, eye, lastLookEnd);

        if (lastIntercept != null) {
          minOffset = 0.0;
          break;
        }

        double lastDotProduct = lastLookVector.dot(toTarget);
        lastDotProduct = Math.max(-1.0, Math.min(1.0, lastDotProduct));
        double lastOffset = Math.toDegrees(Math.acos(lastDotProduct));
        minOffset = Math.min(minOffset, lastOffset);
      }

      if (minOffset > 0.0) {
        if (increaseBuffer(1, 2)) {
          fail("offset=" + minOffset);
        }
      } else {
        decreaseBufferBy(0.035);
      }
    }

    queue.clear();
  }

  private Vector3d getClosestPoint(AABB box, Vector3d point) {
    double closestX = Math.max(box.getMinX(), Math.min(point.x, box.getMaxX()));
    double closestY = Math.max(box.getMinY(), Math.min(point.y, box.getMaxY()));
    double closestZ = Math.max(box.getMinZ(), Math.min(point.z, box.getMaxZ()));

    return new Vector3d(closestX, closestY, closestZ);
  }

  private boolean shouldSchedule(PacketTypeCommon packetType) {
    return Packet.isFlying(packetType) || Packet.isResponse(packetType) || Packet.isTick(packetType);
  }

  private static class AttackData {
    final Vector3d position;
    final AABB targetBox;

    AttackData(Vector3d position, AABB targetBox) {
      this.position = position;
      this.targetBox = targetBox;
    }
  }
}