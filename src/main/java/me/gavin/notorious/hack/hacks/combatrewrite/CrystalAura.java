package me.gavin.notorious.hack.hacks.combatrewrite;

import me.gavin.notorious.hack.Hack;
import me.gavin.notorious.hack.RegisterHack;
import me.gavin.notorious.hack.RegisterSetting;
import me.gavin.notorious.setting.BooleanSetting;
import me.gavin.notorious.setting.ColorSetting;
import me.gavin.notorious.setting.ModeSetting;
import me.gavin.notorious.setting.NumSetting;
import me.gavin.notorious.util.RenderUtil;
import me.gavin.notorious.util.TimerUtils;
import me.gavin.notorious.util.zihasz.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

@RegisterHack(name = "AutoCrystalRewrite", description = "nigus", category = Hack.Category.CombatRewrite)
public class CrystalAura extends Hack {

	//Range
	@RegisterSetting private final NumSetting targetRange = new NumSetting("TargetRange", 7, 0, 10, 1);
	@RegisterSetting private final NumSetting placeRange = new NumSetting("PlaceRange", 5, 0, 7, 1);
	//Delay
	@RegisterSetting private final NumSetting placeDelay = new NumSetting("PlaceDelay", 0, 0, 1000, 1);
	@RegisterSetting private final NumSetting breakDelay = new NumSetting("BreakDelay", 50, 0, 1000, 1);
	//Damage
	@RegisterSetting private final NumSetting minTDamage = new NumSetting("MinTargetDamage", 4, 0, 36, 1);
	@RegisterSetting private final NumSetting maxSDamage = new NumSetting("MaxSelfDamage", 10, 0, 36, 1);
	//Render
	@RegisterSetting private final ModeSetting renderMode = new ModeSetting("RenderMode", "Both", "Both", "Outline", "Fill");
	@RegisterSetting private final ColorSetting outlineColor = new ColorSetting("OutlineColor", 255, 255, 255, 255);
	@RegisterSetting private final ColorSetting fillColor = new ColorSetting("OutlineColor", 255, 255, 255, 255);
	//Misc
	@RegisterSetting private final BooleanSetting oneThirteen = new BooleanSetting("1.13+", false);
	@RegisterSetting private final BooleanSetting entityCheck = new BooleanSetting("EntityCheck", false);

	private final TimerUtils rTimer = new TimerUtils();
	private final TimerUtils pTimer = new TimerUtils();
	private final TimerUtils bTimer = new TimerUtils();

	private EntityPlayer target;
	private BlockPos renderPos;

	@Override
	protected void onEnable() {

	}

	@Override
	public void onTick() {
		doAutoCrystal();
	}

	@SubscribeEvent
	public void onRender3D(RenderWorldLastEvent event) {
		boolean fill;
		boolean outline;
		if(renderMode.getMode().equals("Both")) {
			outline = true;
			fill = true;
		}else if(renderMode.getMode().equals("Outline")) {
			outline = true;
			fill = false;
		}else {
			fill = true;
			outline = false;
		}
		if (renderPos != null) {
			AxisAlignedBB bb = new AxisAlignedBB(renderPos);
			if(fill)
				RenderUtil.renderFilledBB(bb, new Color(fillColor.getAsColor().getRed(), fillColor.getAsColor().getGreen(), fillColor.getAsColor().getBlue(), 125));
			if(outline)
				RenderUtil.renderOutlineBB(bb, new Color(outlineColor.getAsColor().getRed(), outlineColor.getAsColor().getGreen(), outlineColor.getAsColor().getBlue(), 255));
			if (rTimer.hasTimeElapsed(500)) {
				renderPos = null;
				rTimer.reset();
			}
		}
	}

	@Override
	protected void onDisable() {
		target = null;
	}

	private void doAutoCrystal() {
		target = getTarget();

		doPlace();
		doBreak();
	}

	private void doPlace() {
		if (target == null) return;
		if (!pTimer.hasTimeElapsed((long) placeDelay.getValue())) return;

		// Debugging, just in case the null check fucks up
		FMLLog.log.info(target);

		BlockPos optimal = null;
		for (BlockPos block : WorldUtil.getSphere(mc.player.getPosition(), placeRange.getValue(), false)) {

			if (mc.world.isAirBlock(block)) continue;
			if (!isPlaceable(block)) continue;
			if (!canPlaceCry(block, oneThirteen.getValue(), entityCheck.getValue())) continue;

			if (optimal == null)
				optimal = block;

			if (target.getDistanceSq(block) < target.getDistanceSq(optimal)) {
				optimal = block;
				continue;
			}
		}

		if (optimal == null) return;

		RayTraceResult result = mc.world.rayTraceBlocks(new Vec3d(optimal.getX(), optimal.getY(), optimal.getZ()), mc.player.getPositionVector());

		EnumFacing facing = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;
		Vec3d hitVec = result == null || result.hitVec == null ? new Vec3d(0, 0, 0) : result.hitVec;

		renderPos = optimal;
		mc.playerController.processRightClickBlock(mc.player, mc.world, optimal, facing, hitVec, getHand());

		pTimer.reset();
	}

	private void doBreak() {
		if (!bTimer.hasTimeElapsed((long) breakDelay.getValue())) return;

		for (Entity entity : mc.world.loadedEntityList) {
			if (!(entity instanceof EntityEnderCrystal)) continue;
			if (entity.isDead) continue;

			mc.playerController.attackEntity(mc.player, entity);
		}

		bTimer.reset();
	}

	private EntityPlayer getTarget() {
		EntityPlayer optimal = null;

		for (EntityPlayer player : mc.world.playerEntities) {
			if (player == null) continue;
			if (player == mc.player) continue;
			if (player.isDead || player.getHealth() <= 0 || !player.isEntityAlive()) continue;

			if (notorious.friend.isFriend(player.getName())) continue;

			if (mc.player.getDistance(player) > targetRange.getValue())
				continue;

			if (optimal == null) {
				optimal = player;
				continue;
			}

			if (player.getHealth() > optimal.getHealth()) {
				optimal = player;
				continue;
			}

			if (mc.player.getDistance(player) < mc.player.getDistance(optimal)) {
				optimal = player;
				continue;
			}
		}

		return optimal;
	}

	private EnumHand getHand() {
		return mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
	}

	private boolean isPlaceable(BlockPos pos) {
		Block block = mc.world.getBlockState(pos).getBlock();
		return block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.BEDROCK);
	}

	private boolean canPlaceCry(BlockPos pos, boolean oneThirteen, boolean entityCheck) {
		BlockPos up1 = pos.add(0, 1, 0);
		BlockPos up2 = pos.add(0, 2, 0);

		// checking 1 block above
		if (!mc.world.isAirBlock(up1))
			return false;

		if (entityCheck && !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(up2)).isEmpty())
			return false;

		for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(up1))) {
			if (!(entity instanceof EntityEnderCrystal))
				return false;
		}

		// checking 2 block above
		if (!oneThirteen) {
			if (!mc.world.isAirBlock(up2))
				return false;

			if (entityCheck && !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(up2)).isEmpty())
				return false;

			for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(up2))) {
				if (!(entity instanceof EntityEnderCrystal))
					return false;
			}
		}

		return true;
	}

	private boolean isHoldingCrystal() {
		return mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL) ||
				mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL);
	}

}
