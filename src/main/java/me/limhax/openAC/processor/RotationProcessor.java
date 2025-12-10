package me.limhax.openAC.processor;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.MathHelper;
import me.limhax.openAC.util.Packet;

import java.util.ArrayDeque;

public class RotationProcessor {
  private final PlayerData data;
  private final ArrayDeque<Integer> sensSamplesYaw;
  private final ArrayDeque<Integer> sensSamplesYawLow;
  private final ArrayDeque<Integer> sensSamplesPitch;
  private final ArrayDeque<Integer> sensSamplesPitchLow;
  @Getter
  private float yaw, pitch;
  @Getter
  private float lastYaw, lastPitch;
  @Getter
  private float lastLastYaw, lastLastPitch;
  @Getter
  private float deltaYaw, deltaPitch;
  @Getter
  private float nonAbsDeltaYaw, nonAbsDeltaPitch;
  @Getter
  private float lastDeltaYaw, lastDeltaPitch;
  @Getter
  private float lastLastDeltaYaw, lastLastDeltaPitch;
  @Getter
  private float lastNonAbsDeltaYaw, lastNonAbsDeltaPitch;
  @Getter
  private double finalSensitivity, finalSensitivityYaw;
  @Getter
  private double mcpSensitivity, mcpSensitivityYaw;
  @Getter
  private double mcpSensitivityPitchLow, mcpSensitivityYawLow;
  @Getter
  private int sensPitch, sensYaw;

  public RotationProcessor(PlayerData data) {
    this.data = data;
    this.sensSamplesYaw = new ArrayDeque<Integer>();
    this.sensSamplesYawLow = new ArrayDeque<Integer>();
    this.sensSamplesPitch = new ArrayDeque<Integer>();
    this.sensSamplesPitchLow = new ArrayDeque<Integer>();
  }

  public void onReceive(PacketReceiveEvent event) {
    if (!Packet.isRot(event)) return;

    WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
    if (!flying.hasRotationChanged()) return;

    this.lastLastYaw = this.lastYaw;
    this.lastLastPitch = this.lastPitch;
    this.lastLastDeltaYaw = this.lastDeltaYaw;
    this.lastLastDeltaPitch = this.lastDeltaPitch;

    this.lastYaw = this.yaw;
    this.lastPitch = this.pitch;
    this.lastDeltaYaw = this.deltaYaw;
    this.lastDeltaPitch = this.deltaPitch;

    this.lastNonAbsDeltaPitch = nonAbsDeltaPitch;
    this.lastNonAbsDeltaYaw = nonAbsDeltaYaw;

    this.yaw = flying.getLocation().getYaw();
    this.pitch = flying.getLocation().getPitch();
    this.deltaYaw = Math.abs(this.yaw - this.lastYaw);
    this.deltaPitch = Math.abs(this.pitch - this.lastPitch);
    this.nonAbsDeltaPitch = this.pitch - this.lastPitch;
    this.nonAbsDeltaYaw = this.yaw - this.lastYaw;

    if (deltaYaw > 0 && deltaYaw < 5f && lastDeltaYaw > 0 && lastDeltaYaw < 5f && data.getMovementProcessor().getSinceTeleport() > 5) {
      sensYaw();
    }

    if (deltaPitch > 0 && deltaPitch < 10f && lastDeltaPitch > 0 && lastDeltaPitch < 10f && data.getMovementProcessor().getSinceTeleport() > 5) {
      sensPitch();
    }
  }

  private void sensYaw() {
    final double gcd = MathHelper.getGcd(this.deltaYaw, this.lastDeltaYaw);
    final double sensitivityModifier = Math.cbrt(0.833333333333333333 * gcd);
    final double sensitivityStepTwo = 1.66666666666666666 * sensitivityModifier - 0.333333333333333333;
    final double finalSensitivity = sensitivityStepTwo * 200.0;
    this.finalSensitivityYaw = finalSensitivity;
    this.sensSamplesYaw.add((int) finalSensitivity);
    this.sensSamplesYawLow.add((int) finalSensitivity);
    if (this.sensSamplesYaw.size() == 40) {
      this.sensYaw = MathHelper.getMode(this.sensSamplesYaw);
      //if (this.hasValidSensitivity(this.sensYaw)) {
      //  Double value = Constants.GCD_VALUES_OLD.get(this.sensYaw);
      //  if (value != null) {
      //    this.mcpSensitivityYaw = value;
      //  }
      //}
      this.sensSamplesYaw.clear();
    }

    if (this.sensSamplesYawLow.size() == 15) {
      int s = MathHelper.getMode(this.sensSamplesYawLow);
      //if (this.hasValidSensitivity(s)) {
      //  Double value = Constants.GCD_VALUES_OLD.get(s);
      //  if (value != null) {
      //    this.mcpSensitivityYawLow = value;
      //  }
      //}
      this.sensSamplesYawLow.clear();
    }
  }

  private void sensPitch() {
    final double gcd = MathHelper.getGcd(this.deltaPitch, this.lastDeltaPitch);
    final double sensitivityModifier = Math.cbrt(0.833333333333333333 * gcd);
    final double sensitivityStepTwo = 1.66666666666666666 * sensitivityModifier - 0.333333333333333333;
    final double finalSensitivity = sensitivityStepTwo * 200.0;
    this.finalSensitivity = finalSensitivity;
    this.sensSamplesPitch.add((int) finalSensitivity);
    this.sensSamplesPitchLow.add((int) finalSensitivity);
    if (this.sensSamplesPitch.size() == 40) {
      this.sensPitch = MathHelper.getMode(this.sensSamplesPitch);
      //if (this.hasValidSensitivity(this.sensPitch)) {
      //  Double value = Constants.GCD_VALUES_OLD.get(this.sensPitch);
      //  if (value != null) {
      //    this.mcpSensitivity = value;
      //  }
      //}
      this.sensSamplesPitch.clear();
    }

    if (this.sensSamplesPitchLow.size() == 15) {
      int s = MathHelper.getMode(this.sensSamplesPitchLow);
      //if (this.hasValidSensitivity(s)) {
      //  Double value = Constants.GCD_VALUES_OLD.get(s);
      //  if (value != null) {
      //    this.mcpSensitivityPitchLow = value;
      //  }
      //}
      this.sensSamplesPitchLow.clear();
    }
  }

  public boolean hasValidSensitivity(int value) {
    return value >= 0 && value <= 200;
  }
}
