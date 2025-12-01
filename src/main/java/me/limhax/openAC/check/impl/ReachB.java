package me.limhax.openAC.check.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.processor.EntityTracker.EntityTrackerEntry;
import me.limhax.openAC.processor.EntityTracker.EntityTrackerEntry.Location;
import me.limhax.openAC.util.AABB;
import me.limhax.openAC.util.Debug;
import me.limhax.openAC.util.MinecraftMath;
import me.limhax.openAC.util.Packet;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "Reach", type = "B", description = "Hit from too far (raytraced).")
public class ReachB extends Check {

  private static final double[] POSSIBLE_EYE_HEIGHTS = {1.62, 1.27, 0.4};
  private static final double MAX_REACH = 3.0;
  private static final double RAY_LENGTH = 6.0;

  private double x, y, z;
  private float yaw, pitch;
  private float lastYaw, lastPitch;
  private boolean hasPosition = false;

  private final List<QueuedAttack> queue = new ArrayList<>();

  private record QueuedAttack(int entityId, double x, double y, double z, float yaw, float pitch, float lastYaw,
                              float lastPitch) {
  }

  public ReachB(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (Packet.isFlying(event)) {
      WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);

      if (flying.hasPositionChanged()) {
        this.x = flying.getLocation().getX();
        this.y = flying.getLocation().getY();
        this.z = flying.getLocation().getZ();
        this.hasPosition = true;
      }

      if (flying.hasRotationChanged()) {
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        this.yaw = flying.getLocation().getYaw();
        this.pitch = flying.getLocation().getPitch();
      }
    } else if (shouldSchedule(event.getPacketType())) {
      processQueue();
    } else if (Packet.isInteract(event)) {
      if (!hasPosition) return;

      WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
      if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

      int entityId = wrapper.getEntityId();
      EntityTrackerEntry entry = data.getEntityTracker().getTrackedEntities().get(entityId);
      if (entry == null) return;

      if (queue.size() > 10) {
        event.setCancelled(true);
        return;
      }

      queue.add(new QueuedAttack(entityId, x, y, z, yaw, pitch, lastYaw, lastPitch));
    }
  }

  private boolean shouldSchedule(PacketTypeCommon packetType) {
    return Packet.isFlying(packetType) || Packet.isResponse(packetType) || Packet.isTick(packetType);
  }

  private void processQueue() {
    for (QueuedAttack attack : queue) {
      EntityTrackerEntry entry = data.getEntityTracker().getTrackedEntities().get(attack.entityId);
      if (entry == null) continue;

      List<Location> locations = entry.getLocationHistory();
      if (locations.isEmpty()) continue;

      AABB box = buildCombinedHitbox(locations);

      double bestDistance = Double.MAX_VALUE;

      for (double eyeHeight : POSSIBLE_EYE_HEIGHTS) {
        Vector3d eye = new Vector3d(attack.x, attack.y + eyeHeight, attack.z);

        if (box.isInside(eye.x, eye.y, eye.z)) {
          bestDistance = 0.0;
          break;
        }

        Vector3d dir1 = MinecraftMath.getLookVector(attack.yaw, attack.pitch).mul(RAY_LENGTH);
        Vector3d end1 = new Vector3d(eye).add(dir1);
        Vector3d intercept1 = MinecraftMath.calculateIntercept(box, eye, end1);
        if (intercept1 != null) {
          double dist = eye.distance(intercept1);
          if (dist < bestDistance) bestDistance = dist;
        }

        Vector3d dir2 = MinecraftMath.getLookVector(attack.lastYaw, attack.pitch).mul(RAY_LENGTH);
        Vector3d end2 = new Vector3d(eye).add(dir2);
        Vector3d intercept2 = MinecraftMath.calculateIntercept(box, eye, end2);
        if (intercept2 != null) {
          double dist = eye.distance(intercept2);
          if (dist < bestDistance) bestDistance = dist;
        }

        Vector3d dir3 = MinecraftMath.getLookVector(attack.lastYaw, attack.lastPitch).mul(RAY_LENGTH);
        Vector3d end3 = new Vector3d(eye).add(dir3);
        Vector3d intercept3 = MinecraftMath.calculateIntercept(box, eye, end3);
        if (intercept3 != null) {
          double dist = eye.distance(intercept3);
          if (dist < bestDistance) bestDistance = dist;
        }
      }

      if (bestDistance > MAX_REACH && bestDistance != Double.MAX_VALUE) {
        if (this.increaseBuffer(1, 4)) {
          fail("distance=" +  bestDistance);
        }
      } else {
        decreaseBufferBy(0.03);
      }
    }

    queue.clear();
  }

  private AABB buildCombinedHitbox(List<Location> locations) {
    AABB combined = null;

    for (Location loc : locations) {
      AABB box = new AABB(loc.getX(), loc.getY(), loc.getZ());
      box.add(0, 0.9, 0);
      box.expand(0.3, 0.9, 0.3);

      if (combined == null) {
        combined = box;
      } else {
        combined = combined.union(box);
      }
    }

    return combined != null ? combined : new AABB();
  }
}
