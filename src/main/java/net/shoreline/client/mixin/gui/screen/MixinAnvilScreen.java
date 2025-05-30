package net.shoreline.client.mixin.gui.screen;

import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MixinAnvilScreen.class)
public abstract class MixinAnvilScreen {
    @Shadow
    @Final
    @Mutable
    private TextFieldWidget nameField;

    @Accessor("nameField")
    public abstract TextFieldWidget getNameField();

    @Accessor("nameField")
    public abstract void setNameField(TextFieldWidget nameField);
}
