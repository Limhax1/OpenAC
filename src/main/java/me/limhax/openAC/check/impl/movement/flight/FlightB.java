package me.limhax.openAC.check.impl.movement.flight;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.Packet;

@CheckInfo(name = "Flight", type = "B", description = "Invalid ground state.")
public class FlightB extends Check {
  public FlightB(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (!Packet.isFlying(event)) return;
    WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
    if (data.getMovementProcessor().isDuplicatePosition()) return;
    if (data.getMovementProcessor().isLastZeroPointZeroThree()) return;
    if (data.getMovementProcessor().getSinceTeleport() < 3) return;

    final boolean clientGround = flying.isOnGround();
    final boolean serverGround = flying.getLocation().getY() % ((double) 1 / 64) <= 1E-7;

    if (clientGround && !serverGround) {
      fail("client=" + clientGround + " server=" + serverGround);
    }
  }
}
