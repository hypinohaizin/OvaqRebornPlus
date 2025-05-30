package net.shoreline.client.mixin.accessor;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PostEffectPass.class)
public interface AccessorPostProcessShader {
    @Mutable
    @Accessor("input")
    void setInput(Framebuffer framebuffer);

    @Mutable
    @Accessor("output")
    void setOutput(Framebuffer framebuffer);
}