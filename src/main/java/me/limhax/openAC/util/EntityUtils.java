package me.limhax.openAC.util;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EntityUtils {

  private static final ConcurrentHashMap<Integer, Player> entityToPlayer = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<UUID, Integer> uuidToEntity = new ConcurrentHashMap<>();

  @Getter
  private static final Set<Player> players = ConcurrentHashMap.newKeySet();

  private EntityUtils() {

  }

  public static Player getPlayerByEntityId(int entityId) {
    final Player player = entityToPlayer.get(entityId);
    if (player == null) return null;

    if (!player.isOnline()) {
      entityToPlayer.remove(entityId, player);
      uuidToEntity.remove(player.getUniqueId(), entityId);
      return null;
    }
    return player;
  }

  public static void addPlayer(Player player) {
    final int entityId = player.getEntityId();
    final UUID uuid = player.getUniqueId();
    entityToPlayer.put(entityId, player);
    uuidToEntity.put(uuid, entityId);
    players.add(player);
  }

  public static void removePlayer(Player player) {
    final int entityId = player.getEntityId();
    final UUID uuid = player.getUniqueId();
    entityToPlayer.remove(entityId);
    uuidToEntity.remove(uuid);
    players.remove(player);
  }

  public static void clearAll() {
    entityToPlayer.clear();
    uuidToEntity.clear();
    players.clear();
  }

  public static int getCacheSize() {
    return entityToPlayer.size();
  }
}