package me.gavin.notorious.hack.hacks.movement;

import me.gavin.notorious.event.events.PlayerLivingUpdateEvent;
import me.gavin.notorious.hack.Hack;
import me.gavin.notorious.hack.RegisterHack;
import me.gavin.notorious.hack.RegisterSetting;
import me.gavin.notorious.setting.ModeSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterHack(name = "AntiVoid", description = "ez", category = Hack.Category.Combat)
public class AntiVoid extends Hack {

    @RegisterSetting
    public final ModeSetting mode = new ModeSetting("Mode", "TP", "TP", "Strict");

    @SubscribeEvent
    public void onUpdate(PlayerLivingUpdateEvent event) {
        Double yLevel = mc.player.posY;
        if(mode.getMode().equals("TP")) {
            if(yLevel <= .5) {
                mc.player.jump();
                notorious.messageManager.sendMessage("Attempting to jump out of void hole.");
            }
        }else {
            if(yLevel <= .9) {
                mc.player.jump();
                notorious.messageManager.sendMessage("Attempting to jump out of void hole.");
            }
        }
    }
}