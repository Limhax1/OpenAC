package me.limhax.openAC.check;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.util.ColorUtil;
import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import lombok.Getter;
import me.limhax.openAC.OpenAC;
import me.limhax.openAC.check.annotation.CheckInfo;
import me.limhax.openAC.config.ConfigManager;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.Debug;
import me.limhax.openAC.util.MathHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@Getter
public abstract class Check {
  public final PlayerData data;
  private final Player player;
  private int vl;
  private int maxVl = 10;
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
    ConfigManager config = OpenAC.getInstance().getConfigManager();
    String format = config.getVerboseFormat();

    format = format
        .replace("%prefix%", config.getVerbosePrefix())
        .replace("%player%", this.getPlayer().getName())
        .replace("%check%", this.getName())
        .replace("%dev%", this.isExperimental() ? "*" : "")
        .replace("%type%", this.getType())
        .replace("%vl%", String.valueOf(this.getVl()))
        .replace("%percent%", String.valueOf(MathHelper.decimalRound((buffer / max) * 100, 3)))
        .replace("%max-vl%", String.valueOf(this.getMaxVl()));

    Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(format)
        .hoverEvent(HoverEvent.showText(
            Component.text(MathHelper.decimalRound((buffer / max) * 100, 3) + "%", NamedTextColor.GRAY)
        ));

    for (Player player1 : Bukkit.getOnlinePlayers()) {
      PlayerData playerData = OpenAC.getInstance().getListener().getPlayerDataMap().get(player1.getEntityId());
      if (playerData != null && playerData.isVerboseEnabled()) {
        player1.sendMessage(message);
      }
    }

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
    ++vl;
    ConfigManager config = OpenAC.getInstance().getConfigManager();
    String format = config.getAlertFormat();

    format = format
        .replace("%prefix%", config.getAlertPrefix())
        .replace("%player%", this.getPlayer().getName())
        .replace("%check%", this.getName())
        .replace("%dev%", this.isExperimental() ? "*" : "")
        .replace("%type%", this.getType())
        .replace("%vl%", String.valueOf(this.getVl()))
        .replace("%max-vl%", String.valueOf(this.getMaxVl()));

    Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(format)
        .hoverEvent(HoverEvent.showText(
            Component.text(info, NamedTextColor.GRAY)
        ));

    for (Player player1 : Bukkit.getOnlinePlayers()) {
      PlayerData playerData = OpenAC.getInstance().getListener().getPlayerDataMap().get(player1.getEntityId());
      if (playerData != null && playerData.isAlertsEnabled()) {
        player1.sendMessage(message);
      }
    }
    buffer = 0;
  }

  public abstract void onReceive(PacketReceiveEvent event);
  public void onSend(PacketSendEvent event) {}
}
