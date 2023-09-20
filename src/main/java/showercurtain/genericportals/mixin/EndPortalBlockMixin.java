package showercurtain.genericportals.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import showercurtain.genericportals.GenericPortals;

@Mixin(EndPortalBlock.class)
public abstract class EndPortalBlockMixin {
    @ModifyVariable(method = "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V", at = @At(value="STORE", ordinal = 0))
    private RegistryKey<World> smolChange(RegistryKey<World> old, BlockState state, World world, BlockPos pos, Entity entity) {
        Identifier to = GenericPortals.relativeEndSymmetric(world.getRegistryKey().getValue());
        if (to == null) return old;
        return RegistryKey.of(RegistryKeys.WORLD, to);
    }
}