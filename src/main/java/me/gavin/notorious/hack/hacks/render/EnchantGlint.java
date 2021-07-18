package me.gavin.notorious.hack.hacks.render;

import me.gavin.notorious.hack.Hack;
import me.gavin.notorious.hack.RegisterHack;
import me.gavin.notorious.hack.RegisterSetting;
import me.gavin.notorious.setting.BooleanSetting;
import me.gavin.notorious.setting.ColorSetting;
import me.gavin.notorious.setting.NumSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;

@RegisterHack(name = "EnchantGlint", description = "ez", category = Hack.Category.Render)
public class EnchantGlint extends Hack {

    @RegisterSetting
    public final ColorSetting colorShit = new ColorSetting("Color", 255, 255, 255, 255);
    @RegisterSetting
    public final BooleanSetting rainbow = new BooleanSetting("Rainbow", false);

    public static Color getColor(long offset, float fade) {
        float hue = (float) (System.nanoTime() + offset) / 1.0E10F % 1.0F;
        long color = Long.parseLong(Integer.toHexString(Integer.valueOf(Color.HSBtoRGB(hue, 1.0F, 1.0F)).intValue()), 16);
        Color c = new Color((int) color);
        return new Color(c.getRed() / 255.0F * fade, c.getGreen() / 255.0F * fade, c.getBlue() / 255.0F * fade, c.getAlpha() / 255.0F);
    }

    @SubscribeEvent
    public void onUpdate(TickEvent event) {
        if (rainbow.getValue()) {
            cycleRainbow();
        }
    }

    public void cycleRainbow() {
        float[] tick_color = {
                (System.currentTimeMillis() % (360 * 32)) / (360f * 32)
        };
        int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8f, 0.8f);

        colorShit.getRed().setValue((color_rgb_o >> 16) & 0xFF);
        colorShit.getGreen().setValue((color_rgb_o >> 8) & 0xFF);
        colorShit.getBlue().setValue(color_rgb_o & 0xFF);
    }
}
