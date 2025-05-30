package net.shoreline.client.mixin.entity.player;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shoreline.client.OvaqPlus;
import net.shoreline.client.api.event.DeathEvent;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.impl.event.entity.player.LedgeClipEvent;
import net.shoreline.client.impl.event.entity.player.PlayerJumpEvent;
import net.shoreline.client.impl.event.entity.player.PushFluidsEvent;
import net.shoreline.client.impl.event.entity.player.TravelEvent;
import net.shoreline.client.util.Globals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements Globals {
    /**
     * @param entityType
     * @param world
     */
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract void travel(Vec3d movementInput);

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onDeath(CallbackInfo info) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        DeathEvent deathEvent = new DeathEvent(player);
        OvaqPlus.EVENT_HANDLER.dispatch(deathEvent);
        if (deathEvent.isCanceled()) {
            info.cancel();
        }
    }
    /**
     * @param movementInput
     * @param ci
     */
    @Inject(method = "travel", at = @At(value = "HEAD"), cancellable = true)
    private void hookTravelHead(Vec3d movementInput, CallbackInfo ci) {
        TravelEvent travelEvent = new TravelEvent(movementInput);
        travelEvent.setStage(EventStage.PRE);
        OvaqPlus.EVENT_HANDLER.dispatch(travelEvent);
        if (travelEvent.isCanceled()) {
            move(MovementType.SELF, getVelocity());
            ci.cancel();
        }
    }


    /**
     * @param movementInput
     * @param ci
     */
    @Inject(method = "travel", at = @At(value = "RETURN"), cancellable = true)
    private void hookTravelTail(Vec3d movementInput, CallbackInfo ci) {
        TravelEvent travelEvent = new TravelEvent(movementInput);
        travelEvent.setStage(EventStage.POST);
        OvaqPlus.EVENT_HANDLER.dispatch(travelEvent);
    }

    /**
     * @param cir
     */
    @Inject(method = "isPushedByFluids", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookIsPushedByFluids(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this != mc.player) {
            return;
        }
        PushFluidsEvent pushFluidsEvent = new PushFluidsEvent();
        OvaqPlus.EVENT_HANDLER.dispatch(pushFluidsEvent);
        if (pushFluidsEvent.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    /**
     * @param ci
     */
    @Inject(method = "jump", at = @At(value = "HEAD"), cancellable = true)
    private void hookJumpPre(CallbackInfo ci) {
        if ((Object) this != mc.player) {
            return;
        }
        PlayerJumpEvent playerJumpEvent = new PlayerJumpEvent();
        playerJumpEvent.setStage(EventStage.PRE);
        OvaqPlus.EVENT_HANDLER.dispatch(playerJumpEvent);
        if (playerJumpEvent.isCanceled()) {
            ci.cancel();
        }
    }

    /**
     * @param ci
     */
    @Inject(method = "jump", at = @At(value = "RETURN"), cancellable = true)
    private void hookJumpPost(CallbackInfo ci) {
        if ((Object) this != mc.player) {
            return;
        }
        PlayerJumpEvent playerJumpEvent = new PlayerJumpEvent();
        playerJumpEvent.setStage(EventStage.POST);
        OvaqPlus.EVENT_HANDLER.dispatch(playerJumpEvent);
    }

    @Inject(method = "clipAtLedge", at = @At(value = "HEAD"), cancellable = true)
    private void hookClipAtLedge(CallbackInfoReturnable<Boolean> cir)
    {
        LedgeClipEvent ledgeClipEvent = new LedgeClipEvent();
        OvaqPlus.EVENT_HANDLER.dispatch(ledgeClipEvent);
        if (ledgeClipEvent.isCanceled())
        {
            cir.setReturnValue(ledgeClipEvent.isClipped());
        }
    }
}
