package net.shoreline.client.impl.module.render;

import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.render.item.RenderFirstPersonEvent;

/**
 * @author linus
 * @since 1.0
 */
public class ViewModelModule extends ToggleModule
{

    // Config<Boolean> eatingConfig = (new BooleanConfig("Eating", "Modifies eating transformations", true);
    Config<Float> positionXConfig = (new NumberConfig<>("X", "Translation in x-direction", -3.0f, 0.0f, 3.0f));
    Config<Float> positionYConfig = (new NumberConfig<>("Y", "Translation in y-direction", -3.0f, 0.0f, 3.0f));
    Config<Float> positionZConfig = (new NumberConfig<>("Z", "Translation in z-direction", -3.0f, 0.0f, 3.0f));
    Config<Float> scaleXConfig = (new NumberConfig<>("ScaleX", "Scaling in x-direction", 0.1f, 1.0f, 2.0f));
    Config<Float> scaleYConfig = (new NumberConfig<>("ScaleY", "Scaling in y-direction", 0.1f, 1.0f, 2.0f));
    Config<Float> scaleZConfig = (new NumberConfig<>("ScaleZ", "Scaling in z-direction", 0.1f, 1.0f, 2.0f));
    Config<Float> rotateXConfig = (new NumberConfig<>("RotateX", "Rotation in x-direction", -180.0f, 0.0f, 180.0f));
    Config<Float> rotateYConfig = (new NumberConfig<>("RotateY", "Rotation in y-direction", -180.0f, 0.0f, 180.0f));
    Config<Float> rotateZConfig = (new NumberConfig<>("RotateZ", "Rotation in z-direction", -180.0f, 0.0f, 180.0f));

    public ViewModelModule()
    {
        super("ViewModel", "Changes the first-person viewmodel", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderFirstPerson(RenderFirstPersonEvent event)
    {
        event.matrices.scale(scaleXConfig.getValue(), scaleYConfig.getValue(), scaleZConfig.getValue());
        event.matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotateXConfig.getValue()));
        if (event.hand == Hand.MAIN_HAND)
        {
            event.matrices.translate(positionXConfig.getValue(), positionYConfig.getValue(), positionZConfig.getValue());
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotateYConfig.getValue()));
            event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotateZConfig.getValue()));
        }
        else
        {
            event.matrices.translate(-positionXConfig.getValue(), positionYConfig.getValue(), positionZConfig.getValue());
            event.matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-rotateYConfig.getValue()));
            event.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-rotateZConfig.getValue()));
        }
    }
}
