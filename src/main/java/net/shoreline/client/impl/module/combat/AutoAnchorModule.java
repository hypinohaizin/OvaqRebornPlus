package net.shoreline.client.impl.module.combat;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.module.BlockPlacerModule;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.render.RenderBuffers;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.InventoryUtil;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.world.EntityUtil;
import net.shoreline.client.util.world.ExplosionUtil;
import net.shoreline.client.api.event.listener.EventListener;

import java.awt.*;
import java.util.List;
import java.util.*;

public class AutoAnchorModule extends BlockPlacerModule
{
    private static AutoAnchorModule INSTANCE;

    Config<Boolean> multitaskConfig = new BooleanConfig("Multitask", "Allows attacking while using items", false);
    Config<Float> targetRangeConfig = (new NumberConfig<>("EnemyRange", "Range to search for potential enemies", 1.0f, 10.0f, 13.0f));
    Config<Boolean> swingConfig = (new BooleanConfig("Swing", "Swing hand when exploding anchors", true));
    Config<Boolean> rotateConfig = (new BooleanConfig("Rotate", "Rotate before exploding", false));
    Config<Boolean> playersConfig = (new BooleanConfig("Players", "Target players", true));
    Config<Boolean> monstersConfig = (new BooleanConfig("Monsters", "Target monsters", false));
    Config<Boolean> neutralsConfig = (new BooleanConfig("Neutrals", "Target neutrals", false));
    Config<Boolean> animalsConfig = (new BooleanConfig("Animals", "Target animals", false));
    Config<Float> rangeConfig = (new NumberConfig<>("Range", "Range to explode anchors", 0.1f, 4.0f, 6.0f));
    Config<Float> explodeSpeedConfig = (new NumberConfig<>("ExplodeSpeed", "Speed to explode anchors", 0.1f, 18.0f, 20.0f));
    Config<Boolean> placeConfig = (new BooleanConfig("Place", "Places anchors to damage enemies", true));
    Config<Float> placeSpeedConfig = (new NumberConfig<>("PlaceSpeed", "Speed to place anchors", 0.1f, 18.0f, 20.0f, () -> placeConfig.getValue()));
    Config<Boolean> strictDirectionConfig = (new BooleanConfig("StrictDirection", "Interacts with only visible directions when placing crystals", false, () -> placeConfig.getValue()));
    Config<Boolean> grimConfig = (new BooleanConfig("Grim", "Places using grim instant rotations", false, () -> placeConfig.getValue()));
    Config<Boolean> assumeArmorConfig = (new BooleanConfig("AssumeBestArmor", "Assumes Prot 0 armor is max armor", false));
    Config<Float> minDamageConfig = (new NumberConfig<>("MinDamage", "Minimum damage required to consider exploding anchors", 1.0f, 4.0f, 10.0f));
    Config<Boolean> safetyConfig = (new BooleanConfig("Safety", "Accounts for total player safety when exploding anchors", true));
    Config<Float> maxLocalDamageConfig = (new NumberConfig<>("MaxLocalDamage", "The maximum player damage", 4.0f, 12.0f, 20.0f));
    Config<Boolean> blockDestructionConfig = (new BooleanConfig("BlockDestruction", "Accounts for explosion block destruction when calculating damages", false));
    Config<Boolean> selfExtrapolateConfig = (new BooleanConfig("SelfExtrapolate", "Accounts for motion when calculating self damage", false));
    Config<Integer> extrapolateTicksConfig = (new NumberConfig<>("ExtrapolationTicks", "Accounts for motion when calculating enemy positions, not fully accurate.", 0, 0, 10));
    Config<Boolean> renderConfig = (new BooleanConfig("Render", "Renders where anchors will be placed", true));
    Config<Integer> fadeTimeConfig = (new NumberConfig<>("Fade-Time", "Timer for the fade", 0, 250, 1000, () -> false));
    //
    private AnchorCalc anchorCalc;
    private final Timer explodeTimer = new CacheTimer();
    private final Timer placeTimer = new CacheTimer();
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();

    public AutoAnchorModule()
    {
        super("AutoAnchor", "Automatically places and explodes respawn anchors", ModuleCategory.COMBAT, 740);
        INSTANCE = this;
    }

    public static AutoAnchorModule getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void onDisable()
    {
        anchorCalc = null;
        fadeList.clear();
    }

    @EventListener
    public void onPlayerTick(TickEvent event)
    {
        if (event.getStage() != EventStage.PRE)
        {
            return;
        }

        if (!InventoryUtil.hasItemInHotbar(Items.RESPAWN_ANCHOR) || !InventoryUtil.hasItemInHotbar(Items.GLOWSTONE))
        {
            anchorCalc = null;
            return;
        }

        if (!multitaskConfig.getValue() && checkMultitask())
        {
            anchorCalc = null;
            return;
        }

        anchorCalc = calculateAnchorExplosion();
        if (anchorCalc == null)
        {
            return;
        }

        final BlockPos anchorPos = anchorCalc.pos();
        if (anchorCalc.isAnchor())
        {
            if (rotateConfig.getValue())
            {
                float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), anchorPos.toCenterPos());
                setRotation(rotations[0], rotations[1]);
            }
            if (explodeTimer.passed(1000.0f - explodeSpeedConfig.getValue() * 50.0f))
            {
                setAnchor(anchorPos);
                explodeTimer.reset();
            }
        }
        else
        {
            if (mc.world.getBlockState(anchorPos).getBlock() == Blocks.RESPAWN_ANCHOR)
            {
                return;
            }
            int slot = getBlockItemSlot(Blocks.RESPAWN_ANCHOR);
            if (slot == -1)
            {
                return;
            }
            if (placeTimer.passed(1000.0f - placeSpeedConfig.getValue() * 50.0f))
            {
                Managers.INVENTORY.setSlot(slot);
                Managers.INTERACT.placeBlock(anchorPos, slot, grimConfig.getValue(), strictDirectionConfig.getValue(), false, (state, angles) ->
                {
                    if (rotateConfig.getValue())
                    {
                        if (state)
                        {
                            Managers.ROTATION.setRotationSilent2(angles[0], angles[1]);
                        }
                        else
                        {
                            if (grimConfig.getValue())
                            {
                                Managers.ROTATION.setRotationSilentSync2();
                            }
                        }
                    }
                });
                Managers.INVENTORY.syncToClient();
                placeTimer.reset();
            }
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event)
    {
        if (renderConfig.getValue())
        {
            RenderBuffers.preRender();
            for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
            {
                if (anchorCalc != null && set.getKey() == anchorCalc.pos())
                {
                    continue;
                }
                set.getValue().setState(false);
                int boxAlpha = (int) (40 * set.getValue().getFactor());
                int lineAlpha = (int) (100 * set.getValue().getFactor());
                Color boxColor = Modules.COLORS.getColor(boxAlpha);
                Color lineColor = Modules.COLORS.getColor(lineAlpha);
                RenderManager.renderBox(event.getMatrices(), set.getKey(), boxColor.getRGB());
                RenderManager.renderBoundingBox(event.getMatrices(), set.getKey(), 1.5f, lineColor.getRGB());
            }
            RenderBuffers.postRender();

            fadeList.entrySet().removeIf(e ->
                    e.getValue().getFactor() == 0.0);

            if (anchorCalc != null)
            {
                Animation animation = new Animation(true, fadeTimeConfig.getValue());
                fadeList.put(anchorCalc.pos(), animation);
            }
        }
    }

    private void setAnchor(BlockPos pos)
    {
        BlockState state = mc.world.getBlockState(pos);
        if (!(state.getBlock() instanceof RespawnAnchorBlock))
        {
            return;
        }
        int slot1 = findNonBlockSlot();
        if (slot1 == -1)
        {
            return;
        }
        int charges = state.get(RespawnAnchorBlock.CHARGES);
        if (charges <= 0)
        {
            int slot = getBlockItemSlot(Blocks.GLOWSTONE);
            if (slot == -1)
            {
                return;
            }
            Managers.INVENTORY.setSlot(slot);
            BlockHitResult result = new BlockHitResult(pos.toCenterPos(), Managers.INTERACT.getInteractDirection(pos, strictDirectionConfig.getValue()), pos, true);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            Managers.INVENTORY.setSlot(slot1);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
            if (swingConfig.getValue())
            {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            else
            {
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            Managers.INVENTORY.syncToClient();
        }
        else
        {
            Managers.INVENTORY.setSlot(slot1);
            BlockHitResult result1 = new BlockHitResult(pos.toCenterPos(), Managers.INTERACT.getInteractDirection(pos, strictDirectionConfig.getValue()), pos, true);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result1);
            if (swingConfig.getValue())
            {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            else
            {
                Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            Managers.INVENTORY.syncToClient();
        }
    }

    private AnchorCalc calculateAnchorExplosion()
    {
        // explosion
        BlockPos data = null;
        double bestAnchorDamage = 0.0f;
        boolean isAnchor = false;

        for (BlockPos pos : getSphere(mc.player.getPos()))
        {
            BlockState state = mc.world.getBlockState(pos);
            double dist1 = mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos());
            if (dist1 > ((NumberConfig) rangeConfig).getValueSq())
            {
                continue;
            }

            boolean explosion = state.getBlock() instanceof RespawnAnchorBlock;
            if (state.isReplaceable() || explosion)
            {
                double selfDamage = ExplosionUtil.getDamageTo(mc.player,
                        pos.toCenterPos(), blockDestructionConfig.getValue(), 10.0f, Set.of(pos), selfExtrapolateConfig.getValue() ? extrapolateTicksConfig.getValue() : 0, false); // Anchor explosions power = 10
                boolean unsafeToPlayer = playerDamageCheck(selfDamage);
                if (unsafeToPlayer)
                {
                    continue;
                }

                if (Modules.AIR_INTERACT == null || (!Modules.AIR_INTERACT.isEnabled()
                        && Managers.INTERACT.getInteractDirectionInternal(pos, false) == null))
                {
                    continue;
                }


                for (Entity entity : mc.world.getEntities())
                {
                    if (entity.getBoundingBox().intersects(new Box(pos)))
                    {
                        continue;
                    }

                    if (entity == null || !entity.isAlive() || entity == mc.player
                            || !isValidTarget(entity)
                            || Managers.SOCIAL.isFriend(entity.getName()))
                    {
                        continue;
                    }

                    double blockDist = pos.getSquaredDistance(entity.getPos());
                    if (blockDist > 144.0f)
                    {
                        continue;
                    }
                    double dist = mc.player.squaredDistanceTo(entity);
                    if (dist > targetRangeConfig.getValue() * targetRangeConfig.getValue())
                    {
                        continue;
                    }
                    double damage = ExplosionUtil.getDamageTo(entity,
                            pos.toCenterPos(), blockDestructionConfig.getValue(), 10.0f, Set.of(pos), extrapolateTicksConfig.getValue(), assumeArmorConfig.getValue());
                    if (damage > bestAnchorDamage)
                    {
                        data = pos;
                        bestAnchorDamage = damage;
                        isAnchor = explosion;
                    }
                }
            }
        }

        if (data != null && bestAnchorDamage >= minDamageConfig.getValue())
        {
            return new AnchorCalc(data, isAnchor);
        }

        return null;
    }

    private boolean playerDamageCheck(double playerDamage)
    {
        if (!mc.player.isCreative())
        {
            float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            if (safetyConfig.getValue() && playerDamage >= health + 0.5f)
            {
                return true;
            }
            return playerDamage > maxLocalDamageConfig.getValue();
        }
        return false;
    }

    private List<BlockPos> getSphere(Vec3d origin)
    {
        List<BlockPos> sphere = new ArrayList<>();
        double rad = Math.ceil(rangeConfig.getValue());
        for (double x = -rad; x <= rad; ++x)
        {
            for (double y = -rad; y <= rad; ++y)
            {
                for (double z = -rad; z <= rad; ++z)
                {
                    Vec3i pos = new Vec3i((int) (origin.getX() + x),
                            (int) (origin.getY() + y), (int) (origin.getZ() + z));
                    final BlockPos p = new BlockPos(pos);
                    sphere.add(p);
                }
            }
        }
        return sphere;
    }

    private boolean isValidTarget(Entity e)
    {
        return e instanceof PlayerEntity && playersConfig.getValue()
                || EntityUtil.isMonster(e) && monstersConfig.getValue()
                || EntityUtil.isNeutral(e) && neutralsConfig.getValue()
                || EntityUtil.isPassive(e) && animalsConfig.getValue();
    }

    private int findNonBlockSlot()
    {
        int slot = -1;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem)
            {
                continue;
            }
            slot = i;
            break;
        }
        return slot;
    }

    private record AnchorCalc(BlockPos pos, boolean isAnchor) {}

    public boolean checkMultitask()
    {
        return checkMultitask(false);
    }

    public boolean checkMultitask(boolean checkOffhand)
    {
        if (checkOffhand && mc.player.getActiveHand() != Hand.MAIN_HAND)
        {
            return false;
        }
        return mc.player.isUsingItem();
    }
}
