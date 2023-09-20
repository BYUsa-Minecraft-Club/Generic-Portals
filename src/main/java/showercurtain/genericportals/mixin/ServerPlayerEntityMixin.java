package showercurtain.genericportals.mixin;

import net.fabricmc.fabric.mixin.dimension.EntityMixin;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import showercurtain.genericportals.GenericPortals;


@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
	@Shadow
	public boolean notInAnyWorld;

	@Shadow
	private boolean seenCredits;

	@Shadow
	public ServerPlayNetworkHandler networkHandler;

	@Shadow
	public abstract ServerWorld getServerWorld();

	@Inject(method="moveToWorld", at=@At("HEAD"), cancellable = true)
	public void sendPacket(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
		ServerWorld world = getServerWorld();
		Identifier from = world.getRegistryKey().getValue();
		Identifier to = destination.getRegistryKey().getValue();
		Identifier relativeEnd = GenericPortals.endRelativeOverworld(from);
		if (relativeEnd!=null && relativeEnd.equals(to)) {
			((Entity)(Object)this).detach();
			this.getServerWorld().removePlayer((ServerPlayerEntity)(Object) this, Entity.RemovalReason.CHANGED_DIMENSION);
			if (!this.notInAnyWorld) {
				this.notInAnyWorld = true;
				this.networkHandler
						.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_WON, this.seenCredits ? GameStateChangeS2CPacket.DEMO_OPEN_SCREEN : 1.0F));
				this.seenCredits = true;
			}

			cir.setReturnValue((ServerPlayerEntity)(Object) this);
		}
	}

	@Inject(method="getTeleportTarget", at=@At("RETURN"), cancellable = true)
	public void setTeleportTarget(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir) {
		TeleportTarget out = cir.getReturnValue();
		if (out == null) return;
		Identifier relativeEnd = GenericPortals.relativeEnd(((Entity)(Object)this).getWorld().getRegistryKey().getValue());
		if (relativeEnd != null && relativeEnd.equals(destination.getRegistryKey().getValue())) {
			Vec3d vec3d = out.position.add(0.0, -1.0, 0.0);
			cir.setReturnValue(new TeleportTarget(vec3d, Vec3d.ZERO, 90.0F, 0.0F));
		}
	}
}