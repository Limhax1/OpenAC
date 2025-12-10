package me.limhax.openAC.processor;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.MathUtil;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import lombok.Getter;
import lombok.Setter;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.MathHelper;
import me.limhax.openAC.util.Packet;
import me.limhax.openAC.util.Vec3;
import org.bukkit.Material;

public class MovementProcessor {
  private final PlayerData data;

  @Getter
  private double x, y, z;
  @Getter
  private double lastX, lastY, lastZ;
  @Getter
  private double deltaX, deltaY, deltaZ, deltaXZ;
  @Getter
  private double lastDeltaX, lastDeltaY, lastDeltaZ, lastDeltaXZ;
  @Getter
  private double lastLastDeltaX, lastLastDeltaY, lastLastDeltaZ;
  @Setter
  @Getter
  private boolean onGround, lastOnGround, lastLastOnGround;

  @Getter
  private boolean zeroPointZeroThree;
  @Getter
  private boolean lastZeroPointZeroThree;
  @Getter
  private boolean duplicatePosition;
  @Getter
  private int sinceTeleport;
  @Getter
  private int sinceSprintTicks, sprintTicks;
  @Getter
  private int sinceSneakTicks, sneakTicks;
  @Getter
  private int clientAirTicks, clientGroundTicks;
  @Getter
  private boolean sprint, sneak, gliding, lastSneak, lastSprint;
  @Getter
  private Vector3d claimed = new Vector3d(0, 0, 0);
  @Getter
  private Vec3 teleport = new Vec3(0, 0, 0);
  @Getter
  private Vec3 beforeTP = new Vec3(0, 0, 0);

  public MovementProcessor(PlayerData data) {
    this.data = data;
  }

  public void onReceive(PacketReceiveEvent event) {
    if (Packet.isFlying(event)) {
      WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);

      Location location = flying.getLocation();

      ClientVersion version = PacketEvents.getAPI().getPlayerManager().getUser(data.getPlayer()).getClientVersion();
      boolean isNewVersion = version.isNewerThanOrEquals(ClientVersion.V_1_17);

      if (sinceTeleport != 1 && flying.hasPositionChanged() && flying.hasRotationChanged() && isNewVersion && claimed.distanceSquared(location.getPosition()) < 0.003 * 0.003) {
        duplicatePosition = true;
        return;
      } else {
        duplicatePosition = false;
      }

      this.sinceTeleport++;

      if (!flying.hasPositionChanged()) {
        this.zeroPointZeroThree = true;
        return;
      }

      this.lastZeroPointZeroThree = zeroPointZeroThree;

      this.claimed = new Vector3d(location.getX(), location.getY(), location.getZ());

      this.zeroPointZeroThree = false;
      this.duplicatePosition = false;

      this.lastX = x;
      this.lastY = y;
      this.lastZ = z;

      this.lastDeltaX = deltaX;
      this.lastDeltaY = deltaY;
      this.lastDeltaZ = deltaZ;
      this.lastDeltaXZ = deltaXZ;

      this.lastLastDeltaX = lastDeltaX;
      this.lastLastDeltaY = lastDeltaY;
      this.lastLastDeltaZ = lastDeltaZ;

      this.lastLastOnGround = this.lastOnGround;
      this.lastOnGround = this.onGround;
      this.onGround = flying.isOnGround();

      this.lastSneak = sneak;
      this.lastSprint = sprint;

      this.x = location.getX();
      this.y = location.getY();
      this.z = location.getZ();


      this.deltaX = this.x - this.lastX;
      this.deltaY = this.y - this.lastY;
      this.deltaZ = this.z - this.lastZ;
      this.deltaXZ = MathHelper.hypot(deltaX, deltaZ);

      if (gliding && (data.getPlayer().getInventory().getChestplate() == null
          || data.getPlayer().getInventory().getChestplate().getType() != Material.ELYTRA)) {
        gliding = false;
      }

      if (sprint) {
        sprintTicks++;
        sinceSprintTicks = 0;
      } else {
        sprintTicks = 0;
        sinceSprintTicks++;
      }

      if (sneak) {
        sneakTicks++;
        sinceSneakTicks = 0;
      } else {
        sneakTicks = 0;
        sinceSneakTicks++;
      }

      if (!onGround) {
        clientAirTicks++;
        clientGroundTicks = 0;
      } else {
        clientAirTicks = 0;
        clientGroundTicks++;
      }
    }

    if (Packet.isAction(event)) {
      WrapperPlayClientEntityAction actionWrapper = new WrapperPlayClientEntityAction(event);
      WrapperPlayClientEntityAction.Action action = actionWrapper.getAction();
      switch (action) {
        case START_SPRINTING:
          sprint = true;
          break;
        case STOP_SPRINTING:
          sprint = false;
          break;
        case START_SNEAKING:
          sneak = true;
          break;
        case STOP_SNEAKING:
          sneak = false;
          break;
        case START_FLYING_WITH_ELYTRA:
          gliding = true;
          break;
      }
    }
  }

  public void handle(PacketSendEvent event) {
    if (Packet.isTeleport(event)) {
      WrapperPlayServerPlayerPositionAndLook teleport = new WrapperPlayServerPlayerPositionAndLook(event);
      final double x = teleport.getX();
      final double y = teleport.getY();
      final double z = teleport.getZ();

      data.getConnectionProcessor().runOnPong(() -> {
        this.teleport = new Vec3(x, y, z);
        this.beforeTP = new Vec3(this.lastDeltaX, this.lastDeltaY, this.lastDeltaZ);
        this.sinceTeleport = 0;
        this.gliding = false;
      });
    }
  }
}
