package net.shoreline.client.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.RotationModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.EvictingQueue;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.player.RotationUtil;
import net.shoreline.client.util.world.ExplosionUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;

public class AutoMinebase extends RotationModule {


    Config<Boolean> multitaskConfig = new BooleanConfig("Multitask", "Allows mining while using items", false);
    Config<Boolean> autoConfig = new BooleanConfig("Auto", "Automatically mines nearby players feet", false);
    Config<Boolean> autoRemineConfig = new BooleanConfig("AutoRemine", "Automatically remines mined blocks", true, () -> this.autoConfig.getValue());
    Config<Boolean> strictDirectionConfig = new BooleanConfig("StrictDirection", "Only mines on visible faces", false, () -> this.autoConfig.getValue());
    Config<Float> enemyRangeConfig = new NumberConfig<Float>("EnemyRange", "Range to search for targets", Float.valueOf(1.0f), Float.valueOf(5.0f), Float.valueOf(10.0f), () -> this.autoConfig.getValue());
    Config<Boolean> doubleBreakConfig = new BooleanConfig("DoubleBreak", "Allows you to mine two blocks at once", false);
    Config<Float> rangeConfig = new NumberConfig<Float>("Range", "The range to mine blocks", Float.valueOf(0.1f), Float.valueOf(4.0f), Float.valueOf(5.0f));
    Config<Float> speedConfig = new NumberConfig<Float>("Speed", "The speed to mine blocks", Float.valueOf(0.1f), Float.valueOf(1.0f), Float.valueOf(1.0f));
    Config<Boolean> rotateConfig = new BooleanConfig("Rotate", "Rotates when mining the block", true);
    Config<Boolean> switchResetConfig = new BooleanConfig("SwitchReset", "Resets mining after switching items", false);
    Config<Boolean> grimConfig = new BooleanConfig("Grim", "Uses grim block breaking speeds", false);
    Config<Boolean> instantConfig = new BooleanConfig("Instant", "Instant remines mined blocks", true);
    Config<Color> miningColorConfig = new ColorConfig("Color", "The color for mining outlines", new Color(200, 60, 60,100));
 //   private Deque<MiningData> miningQueue = new EvictingQueue<MiningData>(2);
    private long lastBreak;
    private boolean manualOverride;

    public AutoMinebase() {
        super("AutoMine", "Automatically mines blocks", ModuleCategory.WORLD, 900);
    }
    /*
    @Override
    public String getModuleData() {
        if (!this.miningQueue.isEmpty()) {
            MiningData data = this.miningQueue.peek();
            return String.format("%.1f", Float.valueOf(Math.min(data.getBlockDamage(), 1.0f)));
        }
        return super.getModuleData();
    }

    @Override
    public void onEnable() {
        this.miningQueue = this.doubleBreakConfig.getValue() != false ? new EvictingQueue<MiningData>(2) : new EvictingQueue<MiningData>(1);
    }

    @Override
    protected void onDisable() {
        this.miningQueue.clear();
        this.manualOverride = false;
        Managers.INVENTORY.syncToClient();
    }

    @EventListener
    public void onPlayerTick(PlayerTickEvent event) {
        MiningData miningData = null;
        if (!this.miningQueue.isEmpty()) {
            miningData = this.miningQueue.getFirst();
        }
        if (this.autoConfig.getValue().booleanValue() && !this.manualOverride && (miningData == null || AutoMinebase.mc.world.isAir(miningData.getPos()))) {
            Object playerTarget = null;
            double minDistance = 3.4028234663852886E38;
            for (PlayerEntity entity : AutoMinebase.mc.world.getPlayers()) {
                double dist;
                if (entity == AutoMinebase.mc.player || Managers.SOCIAL.isFriend(entity.getName()) || (dist = (double) AutoMinebase.mc.player.distanceTo(entity)) > (double)this.enemyRangeConfig.getValue().floatValue() || !(dist < minDistance)) continue;
                minDistance = dist;
                playerTarget = entity;
            }
            if (playerTarget != null) {
                PriorityQueue<AutoMineCalc> cityPositions;
                PriorityQueue<AutoMineCalc> miningPositions = this.getMiningPosition((PlayerEntity)playerTarget);
                PriorityQueue<AutoMineCalc> miningPositionsNoAir = this.getNoAir(miningPositions);
                PriorityQueue<AutoMineCalc> priorityQueue = cityPositions = this.autoRemineConfig.getValue() != false ? miningPositions : miningPositionsNoAir;
                if (cityPositions.isEmpty()) {
                    return;
                }
                if (this.doubleBreakConfig.getValue().booleanValue()) {
                    AutoMineCalc cityPos = cityPositions.poll();
                    if (cityPos != null) {
                        miningPositionsNoAir.remove(cityPos);
                        BlockPos cityPos2 = null;
                        if (!miningPositionsNoAir.isEmpty()) {
                            cityPos2 = miningPositionsNoAir.poll().pos();
                        }
                        if (cityPos2 != null) {
                            if (!(AutoMinebase.mc.world.isAir(cityPos.pos()) || AutoMinebase.mc.world.isAir(cityPos2) || this.isBlockDelayGrim())) {
                                AutoMiningData data1 = new AutoMiningData(cityPos2, this.strictDirectionConfig.getValue() != false ? Managers.INTERACT.getPlaceDirectionGrim(cityPos2) : Direction.UP);
                                AutoMiningData data2 = new AutoMiningData(cityPos.pos(), this.strictDirectionConfig.getValue() != false ? Managers.INTERACT.getPlaceDirectionGrim(cityPos.pos()) : Direction.UP);
                                this.startMining(data1);
                                this.startMining(data2);
                                this.miningQueue.addFirst(data1);
                                this.miningQueue.addFirst(data2);
                            }
                        } else if (!AutoMinebase.mc.world.isAir(cityPos.pos()) && !this.isBlockDelayGrim()) {
                            AutoMiningData data = new AutoMiningData(cityPos.pos(), this.strictDirectionConfig.getValue() != false ? Managers.INTERACT.getPlaceDirectionGrim(cityPos.pos()) : Direction.UP);
                            this.startMining(data);
                            this.miningQueue.addFirst(data);
                        }
                    }
                } else {
                    AutoMineCalc cityBlockPos = cityPositions.poll();
                    if (cityBlockPos != null && !this.isBlockDelayGrim()) {
                        if (miningData instanceof AutoMiningData && miningData.isInstantRemine() && !AutoMinebase.mc.world.isAir(miningData.getPos()) && this.autoRemineConfig.getValue().booleanValue()) {
                            this.stopMining(miningData);
                        } else if (!AutoMinebase.mc.world.isAir(cityBlockPos.pos()) && !this.isBlockDelayGrim()) {
                            AutoMiningData data = new AutoMiningData(cityBlockPos.pos(), this.strictDirectionConfig.getValue() != false ? Managers.INTERACT.getPlaceDirectionGrim(cityBlockPos.pos()) : Direction.UP);
                            this.startMining(data);
                            this.miningQueue.addFirst(data);
                        }
                    }
                }
            }
        }
        if (this.miningQueue.isEmpty()) {
            return;
        }
        for (MiningData data : this.miningQueue) {
            if (this.isDataPacketMine(data) && data.getState().isAir()) {
                Managers.INVENTORY.syncToClient();
                this.miningQueue.remove(data);
                return;
            }
            float damageDelta = Modules.SPEEDMINE.calcBlockBreakingDelta(data.getState(), AutoMinebase.mc.world, data.getPos());
            data.damage(damageDelta);
            if (!(data.getBlockDamage() >= 1.0f) || !this.isDataPacketMine(data)) continue;
            if (AutoMinebase.mc.player.isUsingItem() && !this.multitaskConfig.getValue().booleanValue()) {
                return;
            }
            if (data.getSlot() == -1) continue;
            Managers.INVENTORY.setSlot(data.getSlot());
        }
        MiningData miningData2 = this.miningQueue.getFirst();
        if (miningData2 != null) {
            double distance = AutoMinebase.mc.player.getEyePos().squaredDistanceTo(miningData2.getPos().toCenterPos());
            if (distance > ((NumberConfig)this.rangeConfig).getValueSq()) {
                this.miningQueue.remove(miningData2);
                return;
            }
            if (miningData2.getState().isAir()) {
                if (this.manualOverride) {
                    this.manualOverride = false;
                    this.miningQueue.remove(miningData2);
                    return;
                }
                if (this.instantConfig.getValue().booleanValue()) {
                    if (miningData2 instanceof AutoMiningData && !this.autoRemineConfig.getValue().booleanValue()) {
                        this.miningQueue.remove(miningData2);
                        return;
                    }
                    miningData2.setInstantRemine();
                    miningData2.setDamage(1.0f);
                } else {
                    miningData2.resetDamage();
                }
                return;
            }
            if (miningData2.getBlockDamage() >= this.speedConfig.getValue().floatValue() || miningData2.isInstantRemine()) {
                if (AutoMinebase.mc.player.isUsingItem() && !this.multitaskConfig.getValue().booleanValue()) {
                    return;
                }
                this.stopMining(miningData2);
            }
        }
    }

    @EventListener
    public void onAttackBlock(AttackBlockEvent event) {
        if (event.getState().getBlock().getHardness() == -1.0f || event.getState().isAir() || AutoMinebase.mc.player.isCreative()) {
            return;
        }
        event.cancel();
        int queueSize = this.miningQueue.size();
        if (queueSize == 0) {
            this.attemptMine(event.getPos(), event.getDirection());
        } else if (queueSize == 1) {
            MiningData data = this.miningQueue.getFirst();
            if (data.getPos().equals(event.getPos())) {
                return;
            }
            if (data instanceof AutoMiningData) {
                this.manualOverride = true;
            }
            this.attemptMine(event.getPos(), event.getDirection());
        } else if (queueSize == 2) {
            MiningData data1 = this.miningQueue.getFirst();
            MiningData data2 = this.miningQueue.getLast();
            if (data1.getPos().equals(event.getPos()) || data2.getPos().equals(event.getPos())) {
                return;
            }
            if (data1 instanceof AutoMiningData || data2 instanceof AutoMiningData) {
                this.manualOverride = true;
            }
            this.attemptMine(event.getPos(), event.getDirection());
        }
        AutoMinebase.mc.player.swingHand(Hand.MAIN_HAND);
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event) {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket && this.switchResetConfig.getValue().booleanValue()) {
            for (MiningData data : this.miningQueue) {
                data.resetDamage();
            }
        }
    }

    @EventListener
    public void onRenderWorld(RenderWorldEvent event) {
        for (MiningData data : this.miningQueue) {
            this.renderMiningData(event.getMatrices(), data);
        }
    }

    private void renderMiningData(MatrixStack matrixStack, MiningData data) {
        if (data != null && !AutoMinebase.mc.player.isCreative() && data.getBlockDamage() > 0.01f) {
            float miningSpeed = this.isDataPacketMine(data) ? 1.0f : this.speedConfig.getValue().floatValue();
            BlockPos mining = data.getPos();
            VoxelShape outlineShape = VoxelShapes.fullCube();
            if (!data.isInstantRemine()) {
                outlineShape = data.getState().getOutlineShape(AutoMinebase.mc.world, mining);
                outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;
            }
            Box render1 = outlineShape.getBoundingBox();
            Box render = new Box((double)mining.getX() + render1.minX, (double)mining.getY() + render1.minY, (double)mining.getZ() + render1.minZ, (double)mining.getX() + render1.maxX, (double)mining.getY() + render1.maxY, (double)mining.getZ() + render1.maxZ);
            Vec3d center = render.getCenter();
            float scale = MathHelper.clamp(data.getBlockDamage() / miningSpeed, 0.0f, 1.0f);
            double dx = (render1.maxX - render1.minX) / 2.0;
            double dy = (render1.maxY - render1.minY) / 2.0;
            double dz = (render1.maxZ - render1.minZ) / 2.0;
            Box scaled = new Box(center, center).expand(dx * (double)scale, dy * (double)scale, dz * (double)scale);
            int color = miningColorConfig.getValue().getRGB();
            RenderManager.renderBox(matrixStack, scaled, color);
            RenderManager.renderBoundingBox(matrixStack, scaled, 2.5f, color);
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event) {
        if (event.getStage() == EventStage.POST && event.getConfig() == this.doubleBreakConfig) {
            this.miningQueue = this.doubleBreakConfig.getValue() != false ? new EvictingQueue<MiningData>(2) : new EvictingQueue<MiningData>(1);
        }
    }

    private PriorityQueue<AutoMineCalc> getNoAir(PriorityQueue<AutoMineCalc> calcs) {
        PriorityQueue<AutoMineCalc> noAir = new PriorityQueue<AutoMineCalc>();
        for (AutoMineCalc calc : calcs) {
            if (AutoMinebase.mc.world.isAir(calc.pos())) continue;
            noAir.add(calc);
        }
        return noAir;
    }

    private PriorityQueue<AutoMineCalc> getMiningPosition(PlayerEntity entity) {
        List<BlockPos> entityIntersections = Modules.SURROUND.getSurroundEntities(entity);
        PriorityQueue<AutoMineCalc> miningPositions = new PriorityQueue<AutoMineCalc>();
        for (BlockPos blockPos : entityIntersections) {
            double dist = AutoMinebase.mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > ((NumberConfig)this.rangeConfig).getValueSq() || AutoMinebase.mc.world.getBlockState(blockPos).isReplaceable()) continue;
            miningPositions.add(new AutoMineCalc(blockPos, Double.MAX_VALUE));
        }
        List<BlockPos> surroundBlocks = Modules.SURROUND.getEntitySurroundNoSupport(entity);
        for (BlockPos blockPos : surroundBlocks) {
            double dist = AutoMinebase.mc.player.getEyePos().squaredDistanceTo(blockPos.toCenterPos());
            if (dist > ((NumberConfig)this.rangeConfig).getValueSq()) continue;
            double damage = ExplosionUtil.getDamageTo(entity, blockPos.toCenterPos().subtract(0.0, -0.5, 0.0), true);
            miningPositions.add(new AutoMineCalc(blockPos, damage));
        }
        return miningPositions;
    }

    private void attemptMine(BlockPos pos, Direction direction) {
        if (this.isBlockDelayGrim()) {
            return;
        }
        MiningData miningData = new MiningData(pos, direction);
        this.startMining(miningData);
        this.miningQueue.addFirst(miningData);
    }

    private void startMining(MiningData data) {
        if (data.getState().isAir() || data.isStarted()) {
            return;
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        if (this.doubleBreakConfig.getValue().booleanValue()) {
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        }
        data.setStarted();
    }

    private void abortMining(MiningData data) {
        if (!data.isStarted() || data.getState().isAir() || data.isInstantRemine() || data.getBlockDamage() >= 1.0f) {
            return;
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        Managers.INVENTORY.syncToClient();
    }

    private void stopMining(MiningData data) {
        boolean canSwap;
        if (!data.isStarted() || data.getState().isAir()) {
            return;
        }
        boolean bl = canSwap = data.getSlot() != -1;
        if (canSwap) {
            Managers.INVENTORY.setSlot(data.getSlot());
        }
        if (this.rotateConfig.getValue().booleanValue()) {
            float[] rotations = RotationUtil.getRotationsTo(AutoMinebase.mc.player.getEyePos(), data.getPos().toCenterPos());
            this.setRotationSilent(rotations[0], rotations[1]);
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        this.lastBreak = System.currentTimeMillis();
        if (canSwap) {
            Managers.INVENTORY.syncToClient();
        }
        if (this.rotateConfig.getValue().booleanValue()) {
            Managers.ROTATION.setRotationSilentSync(true);
        }
    }

    private boolean isDataPacketMine(MiningData data) {
        return this.miningQueue.size() == 2 && data == this.miningQueue.getLast();
    }

    public boolean isBlockDelayGrim() {
        return System.currentTimeMillis() - this.lastBreak <= 280L && this.grimConfig.getValue() != false;
    }

    public static class MiningData {
        private final BlockPos pos;
        private final Direction direction;
        private float blockDamage;
        private boolean instantRemine;
        private boolean started;

        public MiningData(BlockPos pos, Direction direction) {
            this.pos = pos;
            this.direction = direction;
        }

        public boolean isInstantRemine() {
            return this.instantRemine;
        }

        public void setInstantRemine() {
            this.instantRemine = true;
        }

        public float damage(float dmg) {
            this.blockDamage += dmg;
            return this.blockDamage;
        }

        public void setDamage(float blockDamage) {
            this.blockDamage = blockDamage;
        }

        public void resetDamage() {
            this.instantRemine = false;
            this.blockDamage = 0.0f;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public Direction getDirection() {
            return this.direction;
        }

        public int getSlot() {
            return Modules.AUTO_TOOL.getBestToolNoFallback(this.getState());
        }

        public BlockState getState() {
            return Globals.mc.world.getBlockState(this.pos);
        }

        public boolean isStarted() {
            return this.started;
        }

        public void setStarted() {
            this.started = true;
        }

        public float getBlockDamage() {
            return this.blockDamage;
        }
    }

    private record AutoMineCalc(BlockPos pos, double entityDamage) implements Comparable<AutoMineCalc>
    {
        @Override
        public int compareTo(@NotNull AutoMineCalc o) {
            return Double.compare(-this.entityDamage(), -o.entityDamage());
        }
    }

    public static class AutoMiningData
            extends MiningData {
        public AutoMiningData(BlockPos pos, Direction direction) {
            super(pos, direction);
        }
    }*/
}
