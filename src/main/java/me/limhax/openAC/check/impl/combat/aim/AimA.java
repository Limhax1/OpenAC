package me.limhax.openAC.check.impl.combat.aim;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.Packet;

@CheckInfo(name = "Aim", type = "A", description = "Flat aim.", experimental = false)
public class AimA extends Check {
  public AimA(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (!Packet.isRot(event)) return;
    if (data.getCombatProcessor().getSinceAttack() > 3) return;

    if (Math.abs(data.getRotationProcessor().getPitch()) > 86.7) return;

    final float deltaYaw = data.getRotationProcessor().getDeltaYaw();
    final float deltaPitch = data.getRotationProcessor().getDeltaPitch();

    final boolean invalidYaw = deltaPitch > 2 && deltaYaw < 0.009;
    final boolean invalidPitch = deltaPitch < 0.009 && deltaYaw > 2;

    if (invalidPitch || invalidYaw) {
      if (increaseBuffer(1, 12.5)) {
        fail("deltaYaw=" + deltaYaw + " deltaPitch=" + deltaPitch);
      }
    } else {
      decreaseBufferBy(0.2);
    }
  }
}
