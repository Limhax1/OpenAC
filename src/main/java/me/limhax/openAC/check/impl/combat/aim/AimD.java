package me.limhax.openAC.check.impl.combat.aim;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import me.limhax.openAC.check.Check;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.MathHelper;
import me.limhax.openAC.util.Packet;

@CheckInfo(name = "Aim", type = "D", description = "Invalid constant.")
public class AimD extends Check {
  public AimD(PlayerData data) {
    super(data);
  }

  @Override
  public void onReceive(PacketReceiveEvent event) {
    if (!Packet.isRot(event)) return;
    if (Math.abs(data.getRotationProcessor().getPitch()) > 80) return;
    if (data.getCombatProcessor().getSinceAttack() > 3) return;
    if (data.getMovementProcessor().getSinceTeleport() < 3) return;

    final float deltaPitch = data.getRotationProcessor().getDeltaPitch();
    final float lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch();

    final float deltaYaw = data.getRotationProcessor().getDeltaYaw();

    final long expDp = (long) (MathHelper.EXPANDER * deltaPitch);
    final long expLDp = (long) (MathHelper.EXPANDER * lastDeltaPitch);

    final long gcd = MathHelper.getGcd(expDp, expLDp);

    final boolean notFuckedAngles = deltaYaw > 0.185 && deltaPitch > 0.05 && deltaPitch < 20.0f && deltaYaw < 20.0f;
    final boolean smallSens = data.getRotationProcessor().getSensPitch() < 50 && data.getRotationProcessor().getSensPitch() > 0;

    if (gcd < 131072L && notFuckedAngles && !smallSens) {
      if (increaseBuffer(1, 9.5)) {
        fail("gcd=" + gcd + " deltaPitch=" + deltaPitch + " deltaYaw=" + deltaYaw);
      }
    } else {
      decreaseBufferBy(0.95);
    }  
  }
}
