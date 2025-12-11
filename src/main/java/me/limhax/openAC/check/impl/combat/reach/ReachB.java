package me.limhax.openAC.check.impl.combat.reach;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.processor.TrackingProcessor;
import me.limhax.openAC.util.AABB;
import me.limhax.openAC.util.Debug;
import me.limhax.openAC.util.MinecraftMath;
import me.limhax.openAC.util.Packet;
import org.bukkit.ChatColor;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

@CheckInfo(name = "Reach", type = "B", description = "Hit from too far.", experimental = true)
public class ReachB extends Check {

  private static final double[] POSSIBLE_EYE_HEIGHTS = {1.62, 1.27, 0.4};
  private static final double MAX_REACH = 3.0;
  private static final double RAYTRACE_DISTANCE = 6.0;
  
  private final Map<Integer, AttackData> queue = new HashMap<>();

  public ReachB(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (Packet.isInteract(event)) {
      WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
      if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;
      if (data.getMovementProcessor().getSinceTeleport() < 5) return;

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

      double minDistance = Double.MAX_VALUE;

      double currentDistance = calculateRaytraceDistance(
          attackData.position, targetBox, yaw, pitch
      );
      minDistance = Math.min(minDistance, currentDistance);

      double lastDistance = calculateRaytraceDistance(
          attackData.position, targetBox, lastYaw, lastPitch
      );
      minDistance = Math.min(minDistance, lastDistance);

      if (minDistance > MAX_REACH && minDistance != Double.MAX_VALUE) {
        //Debug.debug(ChatColor.GREEN + "distance=" + minDistance);
        if (increaseBuffer(1, 1)) {
          fail("distance=" + minDistance);
        }
      } else {
        //Debug.debug(ChatColor.GRAY + "distance=" + minDistance);
        decreaseBufferBy(0.02);
      }
    }

    queue.clear();
  }

  private double calculateRaytraceDistance(Vector3d position, AABB targetBox, float yaw, float pitch) {
    double minDistance = Double.MAX_VALUE;

    for (double eyeHeight : POSSIBLE_EYE_HEIGHTS) {
      Vector3d eye = new Vector3d(
          position.x,
          position.y + eyeHeight,
          position.z
      );

      Vector3d lookVector = MinecraftMath.getLookVector(yaw, pitch);
      Vector3d lookEnd = new Vector3d(eye).add(lookVector.mul(RAYTRACE_DISTANCE, new Vector3d()));
      
      Vector3d intercept = MinecraftMath.calculateIntercept(targetBox, eye, lookEnd);

      if (intercept != null) {
        double distance = eye.distance(intercept);
        minDistance = Math.min(minDistance, distance);
      }
    }

    return minDistance;
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