package net.shoreline.client.mixin.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.OvaqPlus;
import net.shoreline.client.impl.event.entity.*;
import net.shoreline.client.util.Globals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends MixinEntity implements Globals {
    //
    @Shadow
    protected ItemStack activeItemStack;

    /**
     * @param effect
     * @return
     */
    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow
    public abstract float getYaw(float tickDelta);

    @Shadow
    protected abstract float getJumpVelocity();

    @Shadow
    public abstract boolean isDead();

    @Shadow
    public int deathTime;

    @Shadow private int jumpingCooldown;

    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    private void hookGetHandSwingDuration(CallbackInfoReturnable<Integer> cir)
    {
        SwingSpeedEvent swingSpeedEvent = new SwingSpeedEvent();
        OvaqPlus.EVENT_HANDLER.dispatch(swingSpeedEvent);
        if (swingSpeedEvent.isCanceled())
        {
            if (swingSpeedEvent.getSelfOnly() && ((Object) this != mc.player))
            {
                return;
            }
            cir.cancel();
            cir.setReturnValue(swingSpeedEvent.getSwingSpeed());
        }
    }

    @Inject(method = "jump", at = @At(value = "HEAD"), cancellable = true)
    private void hookJump$getYaw(CallbackInfo ci) {
        if ((LivingEntity) (Object) this != mc.player) {
            return;
        }
        final JumpRotationEvent event = new JumpRotationEvent();
        OvaqPlus.EVENT_HANDLER.dispatch(event);
        if (event.isCanceled()) {
            ci.cancel();
            Vec3d vec3d = this.getVelocity();
            setVelocity(new Vec3d(vec3d.x, getJumpVelocity(), vec3d.z));
            if (isSprinting()) {
                float f = event.getYaw() * ((float)Math.PI / 180);
                setVelocity(getVelocity().add(-MathHelper.sin(f) * 0.2f, 0.0, MathHelper.cos(f) * 0.2f));
            }
            velocityDirty = true;
        }
    }

    @Inject(method = "updateTrackedPositionAndAngles", at = @At(value = "HEAD"))
    private void hookUpdateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, CallbackInfo ci)
    {
        UpdateServerPositionEvent updateServerPositionEvent = new UpdateServerPositionEvent((LivingEntity) (Object) this, x, y, z, yaw, pitch);
        OvaqPlus.EVENT_HANDLER.dispatch(updateServerPositionEvent);
    }

    /**
     * @param instance
     * @param effect
     * @return
     */
    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/" +
            "minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/" +
            "entity/effect/StatusEffect;)Z"))
    private boolean hookHasStatusEffect(LivingEntity instance, StatusEffect effect) {
        if (instance.equals(mc.player)) {
            LevitationEvent levitationEvent = new LevitationEvent();
            OvaqPlus.EVENT_HANDLER.dispatch(levitationEvent);
            return !levitationEvent.isCanceled() && hasStatusEffect(effect);
        }
        return hasStatusEffect(effect);
    }

    /**
     * @param ci
     */
    @Inject(method = "consumeItem", at = @At(value = "INVOKE", target = "Lnet/" +
            "minecraft/item/ItemStack;finishUsing(Lnet/minecraft/world/World;" +
            "Lnet/minecraft/entity/LivingEntity;)" +
            "Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER))
    private void hookConsumeItem(CallbackInfo ci) {
        if ((Object) this != mc.player) {
            return;
        }
        ConsumeItemEvent consumeItemEvent = new ConsumeItemEvent(activeItemStack);
        OvaqPlus.EVENT_HANDLER.dispatch(consumeItemEvent);
    }

    @Inject(method = "tickMovement", at = @At(value = "HEAD"), cancellable = true)
    private void hookTickMovement(CallbackInfo ci) {
        JumpDelayEvent jumpDelayEvent = new JumpDelayEvent();
        OvaqPlus.EVENT_HANDLER.dispatch(jumpDelayEvent);
        if (jumpDelayEvent.isCanceled()) {
            jumpingCooldown = 0;
        }
    }
    @Inject(method = "getStepHeight", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetStepHeight(CallbackInfoReturnable<Float> cir)
    {
        StepEvent stepEvent = new StepEvent((LivingEntity) (Object) this, cir.getReturnValueF());
        OvaqPlus.EVENT_HANDLER.dispatch(stepEvent);
        if (stepEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(stepEvent.getStepHeight());
        }
    }
    @Inject(method = "travel", at = @At(value = "HEAD"), cancellable = true)
    private void hookTravelPre(Vec3d movementInput, CallbackInfo ci)
    {
        EntityTravelEvent entityTravelEvent = new EntityTravelEvent((LivingEntity) (Object) this, true);
        OvaqPlus.EVENT_HANDLER.dispatch(entityTravelEvent);
        if (entityTravelEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "travel", at = @At(value = "RETURN"))
    private void hookTravelPost(Vec3d movementInput, CallbackInfo ci)
    {
        EntityTravelEvent entityTravelEvent = new EntityTravelEvent((LivingEntity) (Object) this, false);
        OvaqPlus.EVENT_HANDLER.dispatch(entityTravelEvent);
    }

}
