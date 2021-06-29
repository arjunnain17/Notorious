package me.gavin.notorious.hack.hacks.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.gavin.notorious.event.events.PlayerLivingUpdateEvent;
import me.gavin.notorious.hack.Hack;
import me.gavin.notorious.hack.RegisterHack;
import me.gavin.notorious.hack.RegisterSetting;
import me.gavin.notorious.setting.ColorSetting;
import me.gavin.notorious.setting.ModeSetting;
import me.gavin.notorious.setting.NumSetting;
import me.gavin.notorious.util.RenderUtil;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

@RegisterHack(name = "VoidESP", description = "ez", category = Hack.Category.Render)
public class VoidESP extends Hack {

    @RegisterSetting
    public final NumSetting range = new NumSetting("Range", 15, 1, 20, 1);
    @RegisterSetting
    public final ModeSetting mode = new ModeSetting("Mode", "Both", "Both", "Outline", "Box");
    @RegisterSetting
    public final ColorSetting outlineColor = new ColorSetting("OutlineColor", 255, 255, 255, 125);
    @RegisterSetting
    public final ColorSetting boxColor = new ColorSetting("BoxColor", 255, 255, 255, 125);

    public final ArrayList<BlockPos> voidBlocks = new ArrayList<>();

    @Override
    public String getMetaData() {
        return " [" + ChatFormatting.GRAY + range.getValue() + ChatFormatting.RESET + "]";
    }

    @SubscribeEvent
    public void onUpdate(PlayerLivingUpdateEvent event) {
        if (mc.player == null) return;

        voidBlocks.clear();

        final Vec3i player_pos = new Vec3i(mc.player.posX, mc.player.posY, mc.player.posZ);

        for (int x = (int) (player_pos.getX() - range.getValue()); x < player_pos.getX() + range.getValue(); x++) {
            for (int z = (int) (player_pos.getZ() - range.getValue()); z < player_pos.getZ() + range.getValue(); z++) {
                for (int y = (int) (player_pos.getY() + range.getValue()); y > player_pos.getY() - range.getValue(); y--) {
                    final BlockPos blockPos = new BlockPos(x, y, z);

                    if (is_void_hole(blockPos))
                        voidBlocks.add(blockPos);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        new ArrayList<>(voidBlocks).forEach(blockPos -> {
            AxisAlignedBB bb = mc.world.getBlockState(blockPos).getSelectedBoundingBox(mc.world, blockPos);
            RenderUtil.renderFilledBB(bb, boxColor.getAsColor());
            RenderUtil.renderOutlineBB(bb, outlineColor.getAsColor());
        });
    }

    public boolean is_void_hole(BlockPos blockPos) {
        if (blockPos.getY() != 0)
            return false;
        if (mc.world.getBlockState(blockPos).getBlock() != Blocks.AIR)
            return false;
        return true;
    }
}
