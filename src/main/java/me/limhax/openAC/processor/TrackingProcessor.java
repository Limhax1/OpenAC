package me.limhax.openAC.processor;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import lombok.Getter;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.AABB;
import me.limhax.openAC.util.EntityUtils;
import me.limhax.openAC.util.Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrackingProcessor {

  private final PlayerData data;

  @Getter
  private final Map<Integer, TrackedEntity> tracked = new ConcurrentHashMap<>();

  public TrackingProcessor(PlayerData data) {
    this.data = data;
  }

  public void onSend(PacketSendEvent event) {
    if (!(event.getPacketType() instanceof PacketType.Play.Server type)) return;

    switch (type) {
      case SPAWN_PLAYER -> {
        WrapperPlayServerSpawnPlayer packet = new WrapperPlayServerSpawnPlayer(event);
        addEntity(packet.getPosition(), packet.getEntityId());
      }
      case SPAWN_ENTITY -> {
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(event);
        addEntity(packet.getPosition(), packet.getEntityId());
      }
      case ENTITY_RELATIVE_MOVE -> {
        WrapperPlayServerEntityRelativeMove packet = new WrapperPlayServerEntityRelativeMove(event);
        moveEntity(packet.getEntityId(), packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ());
      }
      case ENTITY_RELATIVE_MOVE_AND_ROTATION -> {
        WrapperPlayServerEntityRelativeMoveAndRotation packet = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
        moveEntity(packet.getEntityId(), packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ());
      }
      case ENTITY_TELEPORT -> {
        WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(event);
        teleportEntity(packet.getEntityId(), packet.getPosition());
      }
      case ENTITY_POSITION_SYNC -> {
        WrapperPlayServerEntityPositionSync packet = new WrapperPlayServerEntityPositionSync(event);
        teleportEntity(packet.getId(), packet.getValues().getPosition());
      }
      case DESTROY_ENTITIES -> {
        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(event);
        for (int id : packet.getEntityIds()) {
          tracked.remove(id);
        }
      }
    }
  }

  public void onReceive(PacketReceiveEvent event) {
    if (Packet.isFlying(event) && !data.getMovementProcessor().isDuplicatePosition()) {
      // TODO: fix false positive where we don't interpolate
      //  because "isDuplicatePosition" is true, leading to false positives.
      tracked.values().forEach(TrackedEntity::interpolate);
    }
  }

  private void addEntity(Vector3d position, int entityId) {
    if (!tracked.containsKey(entityId) /*&& EntityUtils.getPlayerByEntityId(entityId) != null*/) {
      tracked.put(entityId, new TrackedEntity(entityId, position));
    }
  }

  private void moveEntity(int entityId, double deltaX, double deltaY, double deltaZ) {
    TrackedEntity entity = tracked.get(entityId);
    if (entity == null) return;

    data.getConnectionProcessor().runOnPong(() -> {
      Vector3d newPos = new Vector3d(
          entity.targetLocation.x + deltaX,
          entity.targetLocation.y + deltaY,
          entity.targetLocation.z + deltaZ
      );
      entity.setPosition(newPos);
    });
  }

  private void teleportEntity(int entityId, Vector3d position) {
    TrackedEntity entity = tracked.get(entityId);
    if (entity == null) return;

    data.getConnectionProcessor().runOnPong(() -> {
      entity.setPosition(position);
    });
  }

  @Getter
  public static class TrackedEntity {
    private static final double ENTITY_WIDTH = 0.6;
    private static final double ENTITY_HEIGHT = 1.8;
    private static final int INTERPOLATION_STEPS = 3;
    private final int id;
    private Vector3d location;
    private Vector3d targetLocation;
    private Vector3d startLocation;
    private int steps = 0;
    private int maxSteps = 0;
    private AABB box;
    private AABB interpBox;

    TrackedEntity(int id, Vector3d loc) {
      this.id = id;
      this.location = copy(loc);
      this.targetLocation = copy(loc);
      this.startLocation = copy(loc);
      this.box = getBox(location);
      this.interpBox = box.copy();
    }

    private Vector3d copy(Vector3d vec) {
      return new Vector3d(vec.x, vec.y, vec.z);
    }

    private AABB getBox(Vector3d loc) {
      AABB aabb = new AABB(loc.x, loc.y, loc.z);
      aabb.add(0, ENTITY_HEIGHT / 2, 0);
      aabb.expand(ENTITY_WIDTH / 2, ENTITY_HEIGHT / 2, ENTITY_WIDTH / 2);
      return aabb;
    }

    public void setPosition(Vector3d newPos) {
      this.startLocation = copy(this.location);
      this.targetLocation = copy(newPos);
      this.steps = INTERPOLATION_STEPS;
      this.maxSteps = INTERPOLATION_STEPS;
    }

    public void interpolate() {
      if (steps <= 0) {
        this.box = getBox(location);
        this.interpBox = box;
        return;
      }

      AABB prev = this.box;

      int stepsCompleted = maxSteps - steps;
      double progress = (double) (stepsCompleted + 1) / maxSteps;

      this.location = new Vector3d(
          startLocation.x + (targetLocation.x - startLocation.x) * progress,
          startLocation.y + (targetLocation.y - startLocation.y) * progress,
          startLocation.z + (targetLocation.z - startLocation.z) * progress
      );

      steps--;

      AABB next = getBox(location);
      this.interpBox = prev.union(next);
      this.box = next;
    }
  }
}