package showercurtain.genericportals.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.NetherPortal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import showercurtain.genericportals.GenericPortals;

import java.util.Optional;


@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	public abstract World getWorld();

	@Shadow
	public abstract double getX();
	@Shadow
	public abstract double getY();
	@Shadow
	public abstract double getZ();

	@Shadow
	public abstract Vec3d getVelocity();

	@Shadow
	public abstract float getYaw();
	@Shadow
	public abstract float getPitch();

	@Shadow
	protected abstract Optional<BlockLocating.Rectangle> getPortalRect(ServerWorld destWorld, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder);

	@Shadow
	protected BlockPos lastNetherPortalPosition;

	@Shadow
	protected abstract Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect);

	@Inject(method="getTeleportTarget", at=@At("HEAD"), cancellable = true)
	public void setTeleportTarget(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir) {
		TeleportTarget out = getCustomTeleportTarget(destination);
		if (out == null) return;
		cir.setReturnValue(out);
	}

	@Unique
	public TeleportTarget getCustomTeleportTarget(ServerWorld destination) {
		Identifier here = this.getWorld().getRegistryKey().getValue();
		Identifier there = destination.getRegistryKey().getValue();
		Identifier relativeNether = GenericPortals.relativeNether(here);
		Identifier relativeEnd = GenericPortals.relativeEnd(here);
		Identifier endRelativeOverworld = GenericPortals.endRelativeOverworld(here);
		Identifier netherRelativeOverworld = GenericPortals.netherRelativeOverworld(here);
		boolean fromEndToOverworld = endRelativeOverworld != null && endRelativeOverworld.equals(there);
		boolean fromOverworldToEnd = relativeEnd != null && relativeEnd.equals(there);

		if (!fromEndToOverworld && !fromOverworldToEnd) {
			boolean fromOverworldToNether = relativeNether != null && relativeNether.equals(there);

			if (!(fromOverworldToNether || (netherRelativeOverworld != null && netherRelativeOverworld.equals(there)))) {
				return null;
			} else {
				WorldBorder worldBorder = destination.getWorldBorder();
				double d = DimensionType.getCoordinateScaleFactor(this.getWorld().getDimension(), destination.getDimension());
				BlockPos blockPos2 = worldBorder.clamp(this.getX() * d, this.getY(), this.getZ() * d);
				return this.getPortalRect(destination, blockPos2, fromOverworldToNether, worldBorder)
						.map(
								rect -> {
									BlockState blockState = this.getWorld().getBlockState(this.lastNetherPortalPosition);
									Direction.Axis axis;
									Vec3d vec3d;
									if (blockState.contains(Properties.HORIZONTAL_AXIS)) {
										axis = blockState.get(Properties.HORIZONTAL_AXIS);
										BlockLocating.Rectangle rectangle = BlockLocating.getLargestRectangle(
												this.lastNetherPortalPosition, axis, 21, Direction.Axis.Y, 21, pos -> this.getWorld().getBlockState(pos) == blockState
										);
										vec3d = this.positionInPortal(axis, rectangle);
									} else {
										axis = Direction.Axis.X;
										vec3d = new Vec3d(0.5, 0.0, 0.0);
									}

									return NetherPortal.getNetherTeleportTarget(destination, rect, axis, vec3d, ((Entity)(Object)this), this.getVelocity(), this.getYaw(), this.getPitch());
								}
						)
						.orElse(null);
			}
		} else {
			BlockPos blockPos;
			if (fromOverworldToEnd) {
				blockPos = ServerWorld.END_SPAWN_POS;
				ServerWorld.createEndSpawnPlatform(destination);
			} else {
				BlockPos spawn = destination.getSpawnPos();
				destination.setChunkForced(spawn.getX()/16, spawn.getZ()/16, true);
				blockPos = destination.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawn);
				destination.setChunkForced(spawn.getX()/16, spawn.getZ()/16, false);
			}

			return new TeleportTarget(
					new Vec3d((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5), this.getVelocity(), this.getYaw(), this.getPitch()
			);
		}
	}

	@ModifyVariable(method="tickPortal()V", at=@At(value="STORE"))
	private RegistryKey<World> redirectNetherPortal(RegistryKey<World> old) {
		return RegistryKey.of(RegistryKeys.WORLD, GenericPortals.relativeNetherSymmetric(getWorld().getRegistryKey().getValue()));
	}
}