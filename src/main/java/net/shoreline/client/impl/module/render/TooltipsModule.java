package net.shoreline.client.impl.module.render;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.collection.DefaultedList;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.gui.RenderTooltipEvent;
import net.shoreline.client.init.Modules;

/**
 * @author linus
 * @since 1.0
 */
public class TooltipsModule extends ToggleModule
{

    // Config<Boolean> enderChestsConfig = register(new BooleanConfig("EnderChests", "Renders all the contents of ender chests in tooltips", false);
    Config<Boolean> shulkersConfig = (new BooleanConfig("Shulkers", "Renders all the contents of shulkers in tooltips", true));
    Config<Boolean> mapsConfig = (new BooleanConfig("Maps", "Renders a preview of maps in tooltips", false));

    public TooltipsModule()
    {
        super("Tooltips", "Renders detailed tooltips showing items", ModuleCategory.RENDER);
    }

    @EventListener
    public void onRenderTooltip(RenderTooltipEvent event) {
        final ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;

        if (shulkersConfig.getValue()) {
            NbtCompound blockEntityTag = BlockItem.getBlockEntityNbt(stack);
            if (blockEntityTag != null && blockEntityTag.contains("Items", NbtElement.LIST_TYPE)) {
                event.cancel();
                event.context.getMatrices().push();
                event.context.getMatrices().translate(0.0f, 0.0f, 600.0f);

                DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
                Inventories.readNbt(blockEntityTag, inventory);

                RenderManager.rect(event.context.getMatrices(), event.getX() + 8.0,
                        event.getY() - 21.0, 150.0, 13.0, Modules.COLORS.getRGB(170));
                RenderManager.renderText(event.getContext(), stack.getName().getString(),
                        event.getX() + 11.0f, event.getY() - 18.0f, -1);
                RenderManager.rect(event.context.getMatrices(), event.getX() + 8.0,
                        event.getY() - 7.0, 150.0, 55.0, 0x77000000);

                for (int i = 0; i < inventory.size(); i++) {
                    int x = event.getX() + (i % 9) * 16 + 9;
                    int y = event.getY() + (i / 9) * 16 - 5;
                    ItemStack item = inventory.get(i);
                    event.context.drawItem(item, x, y);
                    event.context.drawItemInSlot(mc.textRenderer, item, x, y);
                }

                event.context.getMatrices().pop();
            }
        }
        if (mapsConfig.getValue() && stack.getItem() instanceof FilledMapItem) {
            event.cancel();
            event.context.getMatrices().push();
            event.context.getMatrices().translate(0.0f, 0.0f, 600.0f);

            RenderManager.rect(event.context.getMatrices(), event.getX() + 8.0,
                    event.getY() - 21.0, 128.0, 13.0, Modules.COLORS.getRGB(170));
            RenderManager.renderText(event.getContext(), stack.getName().getString(),
                    event.getX() + 11.0f, event.getY() - 18.0f, -1);

            NbtCompound tag = stack.getNbt();
            if (tag != null && tag.contains("map")) {
                int mapId = tag.getInt("map");
                MapState mapState = FilledMapItem.getMapState(mapId, mc.world);
                if (mapState != null) {
                    event.getContext().getMatrices().translate(event.getX() + 8.0f, event.getY() - 8.0f, 0.0f);
                    mc.gameRenderer.getMapRenderer().draw(
                            event.getContext().getMatrices(),
                            event.getContext().getVertexConsumers(),
                            mapId, mapState, true, 1);
                }
            }

            event.context.getMatrices().pop();
        }
    }
}