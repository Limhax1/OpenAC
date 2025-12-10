package me.limhax.openAC.processor;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUnloadChunk;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.Packet;

public class WorldProcessor {
  private final PlayerData data;

  private final Long2ObjectMap<Column> serverWorld = new Long2ObjectOpenHashMap<>();
  private final Long2ObjectMap<Column> clientWorld = new Long2ObjectOpenHashMap<>();

  public WorldProcessor(PlayerData data) {
    this.data = data;
  }

  public void onSend(PacketSendEvent event) {
    if (event.getPacketType() == PacketType.Play.Server.CHUNK_DATA) {
      WrapperPlayServerChunkData chunkData = new WrapperPlayServerChunkData(event);
      Column column = chunkData.getColumn();
      long key = getChunkKey(column.getX(), column.getZ());

      serverWorld.put(key, column);
      clientWorld.put(key, column);

    } else if (event.getPacketType() == PacketType.Play.Server.UNLOAD_CHUNK) {
      WrapperPlayServerUnloadChunk unload = new WrapperPlayServerUnloadChunk(event);
      long key = getChunkKey(unload.getChunkX(), unload.getChunkZ());

      serverWorld.remove(key);
      clientWorld.remove(key);

    } else if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
      WrapperPlayServerBlockChange blockChange = new WrapperPlayServerBlockChange(event);
      int x = blockChange.getBlockPosition().getX();
      int y = blockChange.getBlockPosition().getY();
      int z = blockChange.getBlockPosition().getZ();

      setBlockStateInWorld(serverWorld, x, y, z, blockChange.getBlockState());
      setBlockStateInWorld(clientWorld, x, y, z, blockChange.getBlockState());

    } else if (event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
      WrapperPlayServerMultiBlockChange multiBlock = new WrapperPlayServerMultiBlockChange(event);

      for (WrapperPlayServerMultiBlockChange.EncodedBlock block : multiBlock.getBlocks()) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        WrappedBlockState state = block.getBlockState(Packet.getClientVersion(data.getPlayer()));

        setBlockStateInWorld(serverWorld, x, y, z, state);
        setBlockStateInWorld(clientWorld, x, y, z, state);
      }
    }
  }

  public WrappedBlockState getBlockState(int x, int y, int z) {
    Long2ObjectMap<Column> world = clientWorld;
    return getBlockStateFromWorld(world, x, y, z);
  }

  private WrappedBlockState getBlockStateFromWorld(Long2ObjectMap<Column> world, int x, int y, int z) {
    int chunkX = x >> 4;
    int chunkZ = z >> 4;
    long key = getChunkKey(chunkX, chunkZ);
    Column column = world.get(key);
    if (column == null) return null;

    int localX = x & 15;
    int localZ = z & 15;

    int worldMinY = -64;
    int sectionY = (y - worldMinY) >> 4;

    BaseChunk[] sections = column.getChunks();
    if (sectionY < 0 || sectionY >= sections.length) return null;

    BaseChunk section = sections[sectionY];
    if (section == null) return null;

    int localY = (y - worldMinY) & 15;
    return section.get(localX, localY, localZ);
  }

  private void setBlockStateInWorld(Long2ObjectMap<Column> world, int x, int y, int z, WrappedBlockState state) {
    int chunkX = x >> 4;
    int chunkZ = z >> 4;
    long key = getChunkKey(chunkX, chunkZ);
    Column column = world.get(key);
    if (column == null) return;

    int worldMinY = -64;
    int localX = x & 15;
    int localZ = z & 15;
    int sectionY = (y - worldMinY) >> 4;

    BaseChunk[] sections = column.getChunks();
    if (sectionY < 0 || sectionY >= sections.length) return;

    BaseChunk section = sections[sectionY];
    if (section == null) return;

    int localY = (y - worldMinY) & 15;
    section.set(localX, localY, localZ, state);
  }

  private long getChunkKey(int chunkX, int chunkZ) {
    return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
  }

  public void clearChunks() {
    serverWorld.clear();
    clientWorld.clear();
  }
}