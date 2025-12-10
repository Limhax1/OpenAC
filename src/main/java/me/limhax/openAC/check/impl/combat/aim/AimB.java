package me.limhax.openAC.check.impl.combat.aim;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.Packet;

@CheckInfo(name = "Aim", type = "B", description = "Large acceleration.", experimental = false)
public class AimB extends Check {
  public AimB(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (!Packet.isRot(event)) return;
    if (data.getCombatProcessor().getSinceAttack() > 3) return;

    final float deltaYaw = data.getRotationProcessor().getDeltaYaw();
    final float lastDeltaYaw = data.getRotationProcessor().getLastDeltaYaw();
    final float deltaPitch = data.getRotationProcessor().getDeltaPitch();
    final float lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch();

    final float yawAccel = Math.abs(deltaYaw - lastDeltaYaw);
    final float pitchAccel = Math.abs(deltaPitch - lastDeltaPitch);

    final boolean invalidYaw = yawAccel > 18.5 && pitchAccel < 0.25;
    final boolean invalidPitch = pitchAccel > 8.5 && yawAccel < 0.125;

    if (invalidYaw || invalidPitch) {
      if (increaseBuffer(1, 4)) {
        fail("yA=" + yawAccel + " pA=" + pitchAccel + " deltaYaw=" + deltaYaw + " deltaPitch=" + deltaPitch);
      }
    } else {
      decreaseBufferBy(0.1);
    }
  }
}
