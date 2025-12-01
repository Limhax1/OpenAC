package me.limhax.openAC.check;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import lombok.Getter;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.Debug;
import org.bukkit.entity.Player;

@Getter
public abstract class Check {
  public final PlayerData data;
  private final Player player;
  private int vl;
  private double buffer;
  private double bufferDecay;
  private double maxBuffer;
  private final String type;
  private final String name;
  private final String description;
  private final boolean experimental;

  public Check(PlayerData data) {
    this.data = data;
    this.player = data.getPlayer();

    Class<?> clazz = this.getClass();
    if (!clazz.isAnnotationPresent(CheckInfo.class)) {
      throw new RuntimeException("Check class " + clazz.getName() + " is not annotated with @CheckInfo");
    }

    CheckInfo info = clazz.getAnnotation(CheckInfo.class);

    this.name = info.name();
    this.type = info.type();
    this.description = info.description();
    this.experimental = info.experimental();
  }

  protected boolean increaseBuffer(double amount) {
    buffer += amount;
    return buffer > maxBuffer;
  }

  protected boolean increaseBuffer(double amount, double max) {
    //Debug.debug("verbosed " + this.getName() + " " + this.getType() + " " + (buffer / max) * 100 + "%");
    buffer += amount;
    return buffer > max;
  }

  protected void decreaseBuffer() {
    buffer = Math.max(0, buffer - bufferDecay);
  }

  protected void decreaseBufferBy(double amount) {
    buffer = Math.max(0, buffer - amount);
  }

  protected void fail(String info) {
    Debug.debug(this.player.getName() + " failed " + this.name + " " + this.type + " (" + info + ")");
    buffer = 0;
  }

  public abstract void onReceive(PacketReceiveEvent event);
  public void onSend(PacketSendEvent event) {}
}
