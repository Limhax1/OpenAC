package me.limhax.openAC.check.impl.combat.reach;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.processor.TrackingProcessor;
import me.limhax.openAC.util.AABB;
import me.limhax.openAC.util.Packet;

@CheckInfo(name = "Reach", type = "A", description = "Hit from too far.", experimental = false)
public class ReachA extends Check {

  private static final double[] POSSIBLE_EYE_HEIGHTS = {1.62, 1.27, 0.4};

  public ReachA(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (Packet.isInteract(event)) {
      WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
      if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;
      if (data.getMovementProcessor().getSinceTeleport() < 5) return;
      int entityId = wrapper.getEntityId();
      TrackingProcessor.TrackedEntity entry = data.getEntityTracker().getTracked().get(entityId);
      if (entry == null) return;

      if (isImpossible(entry)) {
        event.setCancelled(true);
      }
    }
  }

  private boolean isImpossible(TrackingProcessor.TrackedEntity entry) {
    AABB box = entry.getInterpBox();
    if (box == null) return false;

    double cx = data.getMovementProcessor().getX();
    double cy = data.getMovementProcessor().getY();
    double cz = data.getMovementProcessor().getZ();
    double min = getMinimumDistance(cx, cy, cz, entry);

    if (min > 3D) {
      if (increaseBuffer(1, 1)) {
        fail("distance=" + min);
      } else {
        decreaseBufferBy(0.02);
      }
    }
    return min > 3D;
  }

  private double getMinimumDistance(double px, double py, double pz, TrackingProcessor.TrackedEntity e) {
    AABB box = e.getInterpBox().copy();

    double minDistance = Double.MAX_VALUE;

    for (double eyeHeight : POSSIBLE_EYE_HEIGHTS) {
      double eyeY = py + eyeHeight;

      double closestX = Math.max(box.getMinX(), Math.min(px, box.getMaxX()));
      double closestY = Math.max(box.getMinY(), Math.min(eyeY, box.getMaxY()));
      double closestZ = Math.max(box.getMinZ(), Math.min(pz, box.getMaxZ()));

      double dx = closestX - px;
      double dy = closestY - eyeY;
      double dz = closestZ - pz;
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

      minDistance = Math.min(minDistance, dist);
    }

    return minDistance;
  }
}