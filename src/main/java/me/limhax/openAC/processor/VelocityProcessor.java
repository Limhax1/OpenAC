package me.limhax.openAC.processor;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import lombok.Getter;
import lombok.Setter;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.MathHelper;
import me.limhax.openAC.util.Packet;

public class VelocityProcessor {
  private final PlayerData data;
  @Getter
  private int sinceVelocity = -1;
  @Getter
  @Setter
  private Vector3d velocity = new Vector3d(0, 0, 0);
  @Getter
  private double velocityXZ;

  public VelocityProcessor(PlayerData data) {
    this.data = data;
  }

  public void onSend(PacketSendEvent event) {
    if (!Packet.isVelocity(event)) return;

    WrapperPlayServerEntityVelocity wrapper = new WrapperPlayServerEntityVelocity(event);

    int wrapperId = wrapper.getEntityId();
    int playerId = data.getPlayer().getEntityId();
    if (wrapperId != playerId) return;

    Vector3d velocity = wrapper.getVelocity();
    data.getConnectionProcessor().runOnPong(() -> {
      this.velocity = velocity;
      this.sinceVelocity = 0;
      this.velocityXZ = MathHelper.hypot(velocity.x, velocity.z);
    });
  }

  public void onReceive(PacketReceiveEvent event) {
    if (Packet.isPos(event)) {
        if (!data.getMovementProcessor().isDuplicatePosition()) {
          sinceVelocity++;
        }
    }
  }
}
