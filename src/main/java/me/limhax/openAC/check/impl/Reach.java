package me.limhax.openAC.check.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.AABB;
import me.limhax.openAC.util.Debug;
import me.limhax.openAC.util.Packet;
import me.limhax.openAC.processor.EntityTracker.EntityTrackerEntry.Location;

import java.util.List;

@CheckInfo(name = "Reach", type = "A", description = "test")
public class Reach extends Check {

  private boolean sentFlying = false;
  private double x, y, z;

  public Reach(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (Packet.isFlying(event)) {
      WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
      if (!flying.hasPositionChanged()) return;

      sentFlying = true;
      this.x = flying.getLocation().getX();
      this.y = flying.getLocation().getY();
      this.z = flying.getLocation().getZ();
    }

    if (Packet.isInteract(event)) {
      if (!sentFlying) return;
      WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
      if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;
      final int id = wrapper.getEntityId();
      if (!data.getEntityTracker().getTrackedEntities().containsKey(id)) return;

      List<Location> locations = data.getEntityTracker()
          .getTrackedEntities()
          .get(id)
          .getLocationHistory();

      int size = locations.size();
      if (size < 5) return;
      List<Location> lastThree = locations.subList(Math.max(size - 3, 0), size);

      double distance = getDistance(lastThree);

      if (distance > 3) {
        Debug.debug("d=" + distance);
      }
      sentFlying = false;
    }
  }

  private double getDistance(List<Location> lastThree) {
    double distance = Double.MAX_VALUE;

    for (Location loc : lastThree) {
      double minDistance = Double.MAX_VALUE;
      AABB box = new AABB(loc.getX(), loc.getY(), loc.getZ());
      box.add(0, 1.8 / 2, 0);
      box.expand(0.3, 1.8 / 2, 0.3);

      double[] possibleEyeHeights = {0.4, 1.27, 1.62};
      for (double eyeHeight : possibleEyeHeights) {
        double eyeY = this.y + eyeHeight;

        double closestX = Math.max(box.getMinX(), Math.min(this.x, box.getMaxX()));
        double closestY = Math.max(box.getMinY(), Math.min(eyeY, box.getMaxY()));
        double closestZ = Math.max(box.getMinZ(), Math.min(this.z, box.getMaxZ()));

        double dx = closestX - this.x;
        double dy = closestY - eyeY;
        double dz = closestZ - this.z;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        minDistance = Math.min(minDistance, dist);
      }
      distance = Math.min(minDistance, distance);
    }
    return distance;
  }
}
