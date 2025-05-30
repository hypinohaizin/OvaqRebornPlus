package net.shoreline.client.mixin.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.shoreline.client.OvaqPlus;
import net.shoreline.client.impl.event.camera.EntityCameraPositionEvent;
import net.shoreline.client.impl.event.entity.*;
import net.shoreline.client.impl.event.entity.decoration.TeamColorEvent;
import net.shoreline.client.impl.event.entity.player.PushEntityEvent;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.Globals;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @see Entity
 * @since 1.0
 */
@Mixin(Entity.class)
public abstract class MixinEntity implements Globals {

    @Shadow
    private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        return null;
    }

    @Shadow
    public abstract Box getBoundingBox();

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract void setVelocity(Vec3d velocity);

    @Shadow
    protected Vec3d movementMultiplier;

    @Shadow
    public float fallDistance;

    @Shadow
    public abstract boolean isSprinting();

    @Shadow
    public boolean velocityDirty;

    @Final
    @Shadow
    private EntityType<?> type;

    @Final
    @Shadow
    protected DataTracker dataTracker;

    @Final
    @Shadow
    protected static TrackedData<EntityPose> POSE;

    @Shadow @Final protected static TrackedData<Byte> FLAGS;

    @Inject(method = "getRotationVec", at = @At("RETURN"), cancellable = true)
    public void hookGetCameraPosVec(final float tickDelta, final CallbackInfoReturnable<Vec3d> info) {
        final EntityRotationVectorEvent event = new EntityRotationVectorEvent(
                tickDelta, (Entity) (Object) this, info.getReturnValue());
        OvaqPlus.EVENT_HANDLER.dispatch(event);
        info.setReturnValue(event.getPosition());
    }

    @Inject(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At(value = "HEAD"))
    public void hookMove(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        if ((Object) this != mc.player) {
            return;
        }
        // StepEvent stepEvent = new StepEvent(cir.getReturnValue().y - movement.y);
        // OvaqPlus.EVENT_HANDLER.dispatch(stepEvent);
    }

    /**
     * @param state
     * @param multiplier
     * @param ci
     */
    @Inject(method = "slowMovement", at = @At(value = "HEAD"), cancellable = true)
    private void hookSlowMovement(BlockState state, Vec3d multiplier, CallbackInfo ci)
    {
        if ((Object) this != mc.player)
        {
            return;
        }
        SlowMovementEvent slowMovementEvent = new SlowMovementEvent(state);
        OvaqPlus.EVENT_HANDLER.dispatch(slowMovementEvent);
        if (slowMovementEvent.isCanceled())
        {
            ci.cancel();
            this.fallDistance = 0.0f;
            this.movementMultiplier = multiplier.multiply(slowMovementEvent.getMultiplier());
        }
    }

    @Redirect(method = "getVelocityMultiplier", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;getBlock()" +
                    "Lnet/minecraft/block/Block;"))
    private Block hookGetVelocityMultiplier(BlockState instance) {
        if ((Object) this != mc.player) {
            return instance.getBlock();
        }
        VelocityMultiplierEvent velocityMultiplierEvent =
                new VelocityMultiplierEvent(instance);
        OvaqPlus.EVENT_HANDLER.dispatch(velocityMultiplierEvent);
        if (velocityMultiplierEvent.isCanceled()) {
            return Blocks.DIRT;
        }
        return instance.getBlock();
    }

    @Inject(method = "updateVelocity", at = @At(value = "HEAD"), cancellable = true)
    private void hookUpdateVelocity(float speed, Vec3d movementInput, CallbackInfo ci) {
        if ((Object) this == mc.player) {
            UpdateVelocityEvent updateVelocityEvent = new UpdateVelocityEvent(movementInput, speed, mc.player.getYaw(), movementInputToVelocity(movementInput, speed, mc.player.getYaw()));
            OvaqPlus.EVENT_HANDLER.dispatch(updateVelocityEvent);
            if (updateVelocityEvent.isCanceled()) {
                ci.cancel();
                mc.player.setVelocity(mc.player.getVelocity().add(updateVelocityEvent.getVelocity()));
            }
        }
    }

    @Inject(method = "pushAwayFrom", at = @At(value = "HEAD"), cancellable = true)
    private void hookPushAwayFrom(Entity entity, CallbackInfo ci) {
        PushEntityEvent pushEntityEvent = new PushEntityEvent((Entity) (Object) this, entity);
        OvaqPlus.EVENT_HANDLER.dispatch(pushEntityEvent);
        if (pushEntityEvent.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "getTeamColorValue", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookGetTeamColorValue(CallbackInfoReturnable<Integer> cir) {
        TeamColorEvent teamColorEvent =
                new TeamColorEvent((Entity) (Object) this);
        OvaqPlus.EVENT_HANDLER.dispatch(teamColorEvent);
        if (teamColorEvent.isCanceled()) {
            cir.setReturnValue(teamColorEvent.getColor());
            cir.cancel();
        }
    }

    @Inject(method = "changeLookDirection", at = @At(value = "HEAD"), cancellable = true)
    private void hookChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if ((Object) this == mc.player) {
            LookDirectionEvent lookDirectionEvent = new LookDirectionEvent(
                    (Entity) (Object) this, cursorDeltaX, cursorDeltaY);
            OvaqPlus.EVENT_HANDLER.dispatch(lookDirectionEvent);
            if (lookDirectionEvent.isCanceled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "emitGameEvent(Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/entity/Entity;)V", at = @At(value = "HEAD"))
    private void hookEmitGameEvent(GameEvent event, Entity entity, CallbackInfo ci) {
        EntityGameEvent entityGameEvent = new EntityGameEvent(event, entity);
        OvaqPlus.EVENT_HANDLER.dispatch(entityGameEvent);
    }

    @Inject(method = "getCameraPosVec", at = @At("RETURN"), cancellable = true)
    public void hookCameraPositionVec(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        EntityCameraPositionEvent cameraPositionEvent = new EntityCameraPositionEvent(cir.getReturnValue(), (Entity) (Object) this, tickDelta);
        OvaqPlus.EVENT_HANDLER.dispatch(cameraPositionEvent);
        cir.setReturnValue(cameraPositionEvent.getPosition());
    }

    @Inject(at = @At("HEAD"), method = "getPose()Lnet/minecraft/entity/EntityPose;", cancellable = true)
    public void getPose(CallbackInfoReturnable<EntityPose> info) {
        if (!((Object) this instanceof PlayerEntity)) return;
        if (equals(mc.player)) return;
        if (Modules.PROTOCOL.isEnabled() && Modules.PROTOCOL.getCrouchFix() && type == EntityType.PLAYER) {
            EntityPose pose = dataTracker.get(POSE);
            if (pose == EntityPose.SWIMMING)
                pose = EntityPose.STANDING;
            info.setReturnValue(pose);
            info.cancel();
        }
    }


    @Inject( at = @At("HEAD"), method = "getFlag", cancellable = true)
    public void getFlag(int flag, CallbackInfoReturnable<Boolean> cir) {
        FlagGetEvent flagGetEvent = new FlagGetEvent((Entity) (Object) this, flag, ((Byte) this.dataTracker.get(FLAGS) & 1 << flag) != 0);
        OvaqPlus.EVENT_HANDLER.dispatch(flagGetEvent);
        cir.setReturnValue(flagGetEvent.getReturnValue());
    }
}
