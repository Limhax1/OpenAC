package me.limhax.openAC.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import org.bukkit.entity.Player;

public class Packet {

  private Packet() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static boolean isPos(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION
        || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION;
  }

  public static boolean isRot(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION
        || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION;
  }

  public static boolean isFlying(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
        || event.getPacketType() == PacketType.Play.Client.PLAYER_FLYING
        || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION
        || event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION;
  }

  public static boolean isFlying(PacketTypeCommon packet) {
    return packet == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
        || packet == PacketType.Play.Client.PLAYER_FLYING
        || packet == PacketType.Play.Client.PLAYER_POSITION
        || packet == PacketType.Play.Client.PLAYER_ROTATION;
  }

  public static boolean isResponse(PacketTypeCommon packetType) {
    return packetType == PacketType.Play.Client.PONG
        || packetType == PacketType.Play.Client.WINDOW_CONFIRMATION;
  }

  public static boolean isTick(PacketTypeCommon packetType) {
    return packetType == PacketType.Play.Client.CLIENT_TICK_END;
  }

  public static boolean isInteract(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY;
  }

  public static boolean isAction(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION;
  }

  public static boolean isAnimation(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.ANIMATION;
  }

  public static boolean isDig(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING;
  }

  public static boolean isBlockPlace(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT;
  }

  public static boolean isUse(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.USE_ITEM;
  }

  public static boolean isAbility(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.PLAYER_ABILITIES;
  }

  public static boolean isCreative(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION;
  }

  public static boolean isChat(PacketReceiveEvent event) {
    return event.getPacketType().toString().toLowerCase().contains("chat");
  }

  public static boolean isInput(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.PLAYER_INPUT;
  }

  public static boolean isVehicle(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.VEHICLE_MOVE
        || event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE
        || event.getPacketType() == PacketType.Play.Client.STEER_BOAT;
  }

  public static boolean isWindow(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW;
  }

  public static boolean isCloseWindow(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW;
  }

  public static boolean isSlot(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE;
  }

  public static boolean isPluginMessage(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE;
  }

  public static boolean isTab(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE;
  }

  public static boolean isTeleportConfirm(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.TELEPORT_CONFIRM;
  }

  public static boolean isKeepAlive(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE;
  }

  public static boolean isPong(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.PONG;
  }

  public static boolean isTick(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.CLIENT_TICK_END;
  }

  public static boolean isSettings(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.CLIENT_SETTINGS;
  }

  public static boolean isTransaction(PacketReceiveEvent event) {
    return event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION;
  }

  public static boolean isTeleport(PacketSendEvent event) {
    return event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK;
  }

  public static boolean isVelocity(PacketSendEvent event) {
    return event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY;
  }

  public static boolean isEffect(PacketSendEvent event) {
    return event.getPacketType() == PacketType.Play.Server.ENTITY_EFFECT;
  }

  public static boolean isRemoveEffect(PacketSendEvent event) {
    return event.getPacketType() == PacketType.Play.Server.REMOVE_ENTITY_EFFECT;
  }

  public static boolean isMeta(PacketSendEvent event) {
    return event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA;
  }

  public static boolean isEquipment(PacketSendEvent event) {
    return event.getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT;
  }

  public static boolean isAttribute(PacketSendEvent event) {
    return event.getPacketType() == PacketType.Play.Server.UPDATE_ATTRIBUTES;
  }

  public static boolean isInfo(PacketSendEvent event) {
    return event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_UPDATE;
  }

  public static boolean isPing(PacketSendEvent event) {
    return event.getPacketType() == PacketType.Play.Server.PING;
  }

  public static ServerVersion getServerVersion() {
    return PacketEvents.getAPI().getServerManager().getVersion();
  }

  public static String getFormattedServerVersion() {
    return formatVersion(getServerVersion());
  }

  public static ClientVersion getClientVersion(Player player) {
    return PacketEvents.getAPI().getPlayerManager().getClientVersion(player);
  }

  public static String getFormattedClientVersion(Player player) {
    return formatVersion(getClientVersion(player));
  }

  private static String formatVersion(Enum<?> version) {
    return version.name().replace("V_", "").replace("_", ".");
  }
}