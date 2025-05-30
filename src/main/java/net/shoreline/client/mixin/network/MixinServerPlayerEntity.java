package net.shoreline.client.mixin.network;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.shoreline.client.OvaqPlus;
import net.shoreline.client.impl.event.world.LoadWorldEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity
{
    @Inject(method = "worldChanged", at = @At(value = "HEAD"))
    private void hookMoveToWorld(ServerWorld origin, CallbackInfo ci)
    {
        LoadWorldEvent loadWorldEvent = new LoadWorldEvent();
        OvaqPlus.EVENT_HANDLER.dispatch(loadWorldEvent);
    }
}
