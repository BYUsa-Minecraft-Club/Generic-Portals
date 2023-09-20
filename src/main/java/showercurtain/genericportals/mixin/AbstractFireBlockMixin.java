package showercurtain.genericportals.mixin;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import showercurtain.genericportals.GenericPortals;

@Mixin(AbstractFireBlock.class)
public abstract class AbstractFireBlockMixin {
    /**
     * @author showercurtain
     * @reason Allow nether portals to light in custom dimensions. Also, it's a small enough function that I think an overwrite is justified
     */
    @Overwrite
    private static boolean isOverworldOrNether(World world) {
        return GenericPortals.relativeNetherSymmetric(world.getRegistryKey().getValue()) != null || world.getRegistryKey() == World.NETHER || world.getRegistryKey() == World.OVERWORLD;
    }
}
