package me.limhax.openAC.processor;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import lombok.Getter;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.AABB;
import me.limhax.openAC.util.Packet;

import java.util.HashSet;
import java.util.Set;

public class CollisionProcessor {
  private final PlayerData data;

  @Getter private boolean horizontal = false;
  @Getter private boolean verticalTop = false;
  @Getter private boolean verticalBottom = false;
  @Getter private int sinceHorizontal = 0;
  @Getter private int sinceVerticalTop = 0;
  @Getter private int sinceVerticalBottom = 0;

  private Set<BlockPosition> horizontalCollisions = new HashSet<>();
  private Set<BlockPosition> verticalTopCollisions = new HashSet<>();
  private Set<BlockPosition> verticalBottomCollisions = new HashSet<>();
  private Set<BlockPosition> allCollisions = new HashSet<>();

  public CollisionProcessor(PlayerData data) {
    this.data = data;
  }

  public void onReceive(PacketReceiveEvent event) {
    if (!Packet.isPos(event) && !Packet.isRot(event)) return;
    if (data.getMovementProcessor().getSinceTeleport() < 3) return;
    if (data.getMovementProcessor().isDuplicatePosition()) return;

    double x = data.getMovementProcessor().getX();
    double y = data.getMovementProcessor().getY();
    double z = data.getMovementProcessor().getZ();

    horizontalCollisions.clear();
    verticalTopCollisions.clear();
    verticalBottomCollisions.clear();
    allCollisions.clear();

    AABB current = createBox(x, y, z);

    checkCollisions(current);

    horizontal = !horizontalCollisions.isEmpty();
    verticalTop = !verticalTopCollisions.isEmpty();
    verticalBottom = !verticalBottomCollisions.isEmpty();

    sinceHorizontal = horizontal ? 0 : sinceHorizontal + 1;
    sinceVerticalTop = verticalTop ? 0 : sinceVerticalTop + 1;
    sinceVerticalBottom = verticalBottom ? 0 : sinceVerticalBottom + 1;
  }

  private AABB createBox(double x, double y, double z) {
    return new AABB(x - 0.3, y, z - 0.3, x + 0.3, y + 1.8, z + 0.3)
        .expand(Math.min(data.getMovementProcessor().getDeltaXZ(), 0.3));
  }

  private void checkCollisions(AABB playerBox) {
    int minX = (int) Math.floor(playerBox.minX);
    int maxX = (int) Math.floor(playerBox.maxX);
    int minY = (int) Math.floor(playerBox.minY);
    int maxY = (int) Math.floor(playerBox.maxY);
    int minZ = (int) Math.floor(playerBox.minZ);
    int maxZ = (int) Math.floor(playerBox.maxZ);

    for (int y = minY; y <= maxY; y++) {
      if (y < -64 || y >= 320) continue;

      for (int z = minZ; z <= maxZ; z++) {
        for (int x = minX; x <= maxX; x++) {
          WrappedBlockState state = data.getWorldProcessor().getBlockState(x, y, z);
          if (state == null || !isCollidable(state)) continue;

          AABB blockBox = new AABB(x, y, z, x + 1, y + 1, z + 1);

          if (playerBox.intersects(blockBox)) {
            BlockPosition pos = new BlockPosition(x, y, z, state.getType());
            allCollisions.add(pos);
            categorizeCollision(pos, blockBox, playerBox);
          }
        }
      }
    }
  }

  private void categorizeCollision(BlockPosition pos, AABB blockBox, AABB playerBox) {
    double deltaX = data.getMovementProcessor().getDeltaX();
    double deltaY = data.getMovementProcessor().getDeltaY();
    double deltaZ = data.getMovementProcessor().getDeltaZ();

    boolean collidesVertically = checkVerticalCollision(playerBox, blockBox, deltaY);
    boolean collidesHorizontally = checkHorizontalCollision(playerBox, blockBox, deltaX, deltaZ);

    if (collidesVertically) {
      if (deltaY > 0 || playerBox.minY < blockBox.minY) {
        verticalTopCollisions.add(pos);
      } else {
        verticalBottomCollisions.add(pos);
      }
    }

    if (collidesHorizontally) {
      horizontalCollisions.add(pos);
    }
  }

  private boolean checkVerticalCollision(AABB playerBox, AABB blockBox, double deltaY) {
    boolean overlapX = playerBox.maxX > blockBox.minX && playerBox.minX < blockBox.maxX;
    boolean overlapZ = playerBox.maxZ > blockBox.minZ && playerBox.minZ < blockBox.maxZ;

    if (!overlapX || !overlapZ) return false;

    return playerBox.maxY > blockBox.minY && playerBox.minY < blockBox.maxY;
  }

  private boolean checkHorizontalCollision(AABB playerBox, AABB blockBox, double deltaX, double deltaZ) {
    boolean overlapY = playerBox.maxY > blockBox.minY && playerBox.minY < blockBox.maxY;

    if (!overlapY) return false;

    boolean overlapX = playerBox.maxX > blockBox.minX && playerBox.minX < blockBox.maxX;
    boolean overlapZ = playerBox.maxZ > blockBox.minZ && playerBox.minZ < blockBox.maxZ;

    return overlapX || overlapZ;
  }

  private boolean isCollidable(WrappedBlockState state) {
    String name = state.getType().getName().toString().toLowerCase();

    if (name.contains("air")) return false;

    return !name.contains("sapling") && !name.contains("flower") && !name.contains("button") &&
        !name.contains("sign") && !name.contains("lever") && !name.contains("pressure_plate") &&
        !name.contains("torch") && !name.contains("redstone_wire") && !name.contains("rail") &&
        !name.contains("tripwire") && !(name.contains("grass") && !name.contains("block")) &&
        !name.contains("fern") && !name.contains("dead_bush") && !name.contains("seagrass") &&
        !name.contains("kelp") && !name.contains("vine") && !name.contains("sugar_cane") &&
        !name.contains("fire") && !name.contains("fungus") && !name.contains("roots");
  }

  public CollisionSet getHorizontalCollisions() {
    return new CollisionSet(horizontalCollisions);
  }

  public CollisionSet getVerticalTopCollisions() {
    return new CollisionSet(verticalTopCollisions);
  }

  public CollisionSet getVerticalBottomCollisions() {
    return new CollisionSet(verticalBottomCollisions);
  }

  public CollisionSet getCollisions() {
    return new CollisionSet(allCollisions);
  }

  public boolean hasCollision(String blockName) {
    return getCollisions().has(blockName);
  }

  public static class BlockPosition {
    @Getter private final int x, y, z;
    @Getter private final StateType type;

    public BlockPosition(int x, int y, int z, StateType type) {
      this.x = x; this.y = y; this.z = z; this.type = type;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof BlockPosition)) return false;
      BlockPosition p = (BlockPosition) o;
      return x == p.x && y == p.y && z == p.z;
    }

    @Override
    public int hashCode() {
      return 31 * (31 * x + y) + z;
    }

    @Override
    public String toString() {
      return String.format("(%d, %d, %d) %s", x, y, z, type != null ? type.getName() : "null");
    }
  }

  public static class CollisionSet {
    private final Set<BlockPosition> blocks;

    public CollisionSet(Set<BlockPosition> blocks) {
      this.blocks = blocks;
    }

    public boolean has(String blockName) {
      if (blockName == null || blocks.isEmpty()) return false;
      String search = blockName.toLowerCase();
      for (BlockPosition block : blocks) {
        if (block.type != null && block.type.getName().toString().toLowerCase().contains(search)) {
          return true;
        }
      }
      return false;
    }

    public boolean hasExact(String blockName) {
      if (blockName == null || blocks.isEmpty()) return false;
      String search = blockName.toLowerCase();
      for (BlockPosition block : blocks) {
        if (block.type != null) {
          String name = block.type.getName().toString().toLowerCase();
          if (name.equals(search) || name.equals("minecraft:" + search)) return true;
        }
      }
      return false;
    }

    public Set<BlockPosition> getMatching(String blockName) {
      Set<BlockPosition> result = new HashSet<>();
      if (blockName == null) return result;
      String search = blockName.toLowerCase();
      for (BlockPosition block : blocks) {
        if (block.type != null && block.type.getName().toString().toLowerCase().contains(search)) {
          result.add(block);
        }
      }
      return result;
    }

    public Set<BlockPosition> getAll() {
      return new HashSet<>(blocks);
    }

    public int size() {
      return blocks.size();
    }

    public boolean isEmpty() {
      return blocks.isEmpty();
    }
  }
}