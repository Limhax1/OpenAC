package me.limhax.openAC.processor;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import lombok.Getter;
import me.limhax.openAC.data.PlayerData;
import me.limhax.openAC.util.Packet;

public class CombatProcessor {
  private final PlayerData data;
  @Getter
  int targetId = -1;
  @Getter
  int sinceAttack;

  public CombatProcessor(PlayerData data) {
    this.data = data;
  }

  public void onReceive(PacketReceiveEvent event) {
    if (Packet.isInteract(event)) {
      WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
      if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;
      targetId = wrapper.getEntityId();
      sinceAttack = 0;
    } else if (Packet.isFlying(event)) {
      if (data.getMovementProcessor().isDuplicatePosition()) return;
      sinceAttack++;
    }
  }
}
