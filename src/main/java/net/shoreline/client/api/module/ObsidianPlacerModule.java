package net.shoreline.client.api.module;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.impl.module.combat.SurroundModule;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author linus
 * @see SurroundModule
 * @since 1.0
 */
public class ObsidianPlacerModule extends BlockPlacerModule
{
    private static final List<Block> RESISTANT_BLOCKS = new LinkedList<>() {{
       add(Blocks.OBSIDIAN);
       add(Blocks.CRYING_OBSIDIAN);
       add(Blocks.ENDER_CHEST);
    }};
    protected static final BlockState DEFAULT_OBSIDIAN_STATE = Blocks.OBSIDIAN.getDefaultState();

    protected Config<Boolean> strictDirectionConfig = new BooleanConfig("StrictDirection", "Places on visible sides only", false);
    protected Config<Boolean> grimConfig = new BooleanConfig("Grim", "Places using grim instant rotations", false);

    public ObsidianPlacerModule(String name, String desc, ModuleCategory category) {
        super(name, desc, category);
        register(strictDirectionConfig, grimConfig);
    }

    public ObsidianPlacerModule(String name, String desc, ModuleCategory category, int rotationPriority) {
        super(name, desc, category, rotationPriority);
        register(strictDirectionConfig, grimConfig);
    }

    protected int getResistantBlockItem() {
        final Set<BlockSlot> blockSlots = new HashSet<>();

        for (final Block type : RESISTANT_BLOCKS) {
            final int slot = getBlockItemSlot(type);
            if (slot != -1) {
                blockSlots.add(new BlockSlot(type, slot));
            }
        }

        for (Block target : List.of(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.ENDER_CHEST)) {
            BlockSlot result = blockSlots.stream().filter(b -> b.block() == target).findFirst().orElse(null);
            if (result != null) {
                return result.slot();
            }
        }

        if (!blockSlots.isEmpty()) {
            return blockSlots.iterator().next().slot();
        }

        return -1;
    }
    public record BlockSlot(Block block, int slot)
    {
        @Override
        public boolean equals(Object obj)
        {
            return obj instanceof BlockSlot b && b.block() == block;
        }
    }
}
