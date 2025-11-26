package me.limhax.openAC.processor;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import lombok.Getter;
import me.limhax.openAC.OpenAC;
import me.limhax.openAC.data.PlayerData;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
public class EntityTracker {

  private final Map<Integer, EntityTrackerEntry> trackedEntities = new HashMap<>();

  public void cleanup() {
    trackedEntities.clear();
  }

  @Getter
  public static class EntityTrackerEntry {
    private double x;
    private double y;
    private double z;
    private final Deque<Location> locationHistory = new ArrayDeque<>(20);

    @Getter
    public static class Location {
      private final double x;
      private final double y;
      private final double z;
      private final long timestamp;

      public Location(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = System.currentTimeMillis();
      }
    }

    public EntityTrackerEntry(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
      addLocationToHistory(x, y, z);
    }

    private void addLocationToHistory(double x, double y, double z) {
      if (locationHistory.size() >= 20) {
        locationHistory.pollFirst();
      }
      locationHistory.offerLast(new Location(x, y, z));
    }

    public void move(double deltaX, double deltaY, double deltaZ) {
      OpenAC.getInstance().getExecutor().schedule(() -> {
      this.x += deltaX;
      this.y += deltaY;
      this.z += deltaZ;
      addLocationToHistory(this.x, this.y, this.z);
      },  35, TimeUnit.MILLISECONDS);
    }

    public void teleport(double x, double y, double z) {
      OpenAC.getInstance().getExecutor().schedule(() -> {
      this.x = x;
      this.y = y;
      this.z = z;
      addLocationToHistory(x, y, z);
      },  50, TimeUnit.MILLISECONDS);
    }

    public List<Location> getLocationHistory() {
      return new ArrayList<>(locationHistory);
    }
  }

  private final PlayerData data;

  public EntityTracker(PlayerData data) {
    this.data = data;
  }

  public void onSend(PacketSendEvent event) {
    if (!(event.getPacketType() instanceof PacketType.Play.Server packet)) return;

    switch (packet) {
      case SPAWN_ENTITY:
        WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(event);
        final int entityId = spawnEntity.getEntityId();
        final double spawnX1 = spawnEntity.getPosition().getX();
        final double spawnY1 = spawnEntity.getPosition().getY();
        final double spawnZ1 = spawnEntity.getPosition().getZ();

        if (!trackedEntities.containsKey(entityId)) {
          trackedEntities.put(entityId, new EntityTrackerEntry(spawnX1, spawnY1, spawnZ1));
        }

        break;

      case SPAWN_PLAYER:
        WrapperPlayServerSpawnPlayer spawnPlayer = new WrapperPlayServerSpawnPlayer(event);
        final int spawnId = spawnPlayer.getEntityId();
        final double spawnX = spawnPlayer.getPosition().getX();
        final double spawnY = spawnPlayer.getPosition().getY();
        final double spawnZ = spawnPlayer.getPosition().getZ();

        if (!trackedEntities.containsKey(spawnId)) {
          trackedEntities.put(spawnId, new EntityTrackerEntry(spawnX, spawnY, spawnZ));
        }

        break;

      case ENTITY_RELATIVE_MOVE:
        WrapperPlayServerEntityRelativeMove relMove = new  WrapperPlayServerEntityRelativeMove(event);
        final int relMoveId = relMove.getEntityId();
        final double deltaX =  relMove.getDeltaX();
        final double deltaY = relMove.getDeltaY();
        final double deltaZ = relMove.getDeltaZ();

        if (trackedEntities.containsKey(relMoveId)) {
          trackedEntities.get(relMoveId).move(deltaX, deltaY, deltaZ);
        }

        break;

      case ENTITY_RELATIVE_MOVE_AND_ROTATION:
        WrapperPlayServerEntityRelativeMoveAndRotation relMoveAndRot = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
        final int relMoveAndRotId = relMoveAndRot.getEntityId();
        final double deltaX1 = relMoveAndRot.getDeltaX();
        final double deltaY1 = relMoveAndRot.getDeltaY();
        final double deltaZ1 = relMoveAndRot.getDeltaZ();

        if (trackedEntities.containsKey(relMoveAndRotId)) {
          trackedEntities.get(relMoveAndRotId).move(deltaX1, deltaY1, deltaZ1);
        }

        break;

      case ENTITY_TELEPORT:
        WrapperPlayServerEntityTeleport teleport = new  WrapperPlayServerEntityTeleport(event);
        final int teleportId = teleport.getEntityId();
        final double x = teleport.getPosition().getX();
        final double y = teleport.getPosition().getY();
        final double z = teleport.getPosition().getZ();

        if (trackedEntities.containsKey(teleportId)) {
          trackedEntities.get(teleportId).teleport(x, y, z);
        }

        break;

      case ENTITY_POSITION_SYNC:
        WrapperPlayServerEntityPositionSync sync = new WrapperPlayServerEntityPositionSync(event);
        final int syncId = sync.getId();
        final double x1 = sync.getValues().getPosition().getX();
        final double y1 = sync.getValues().getPosition().getY();
        final double z1 = sync.getValues().getPosition().getZ();

        if (trackedEntities.containsKey(syncId)) {
          trackedEntities.get(syncId).teleport(x1, y1, z1);
        }

        break;
    }
  }
}
