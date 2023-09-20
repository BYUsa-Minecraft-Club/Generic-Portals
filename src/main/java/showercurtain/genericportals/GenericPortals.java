package showercurtain.genericportals;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import showercurtain.genericportals.links.Link;
import showercurtain.genericportals.links.PortalLinks;

public class GenericPortals implements ModInitializer, ServerLifecycleEvents.ServerStarted {
    public static final Logger LOGGER = LoggerFactory.getLogger("generic-portals");
	public static PortalLinks links;

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(this);
		CommandRegistrationCallback.EVENT.register(Commands::registerCommands);
	}

	@Override
	public void onServerStarted(MinecraftServer server) {
		PermCheck.initPerms();
		links = PortalLinks.getOrCreate(server);
	}

	@Nullable
	public static Identifier relativeEnd(Identifier from) {
		for (Link l : links.end) {
			if (l.from().equals(from)) return l.to();
		}
		return null;
	}

	@Nullable
	public static Identifier relativeNether(Identifier from) {
		for (Link l : links.nether) {
			if (l.from().equals(from)) return l.to();
		}
		return null;
	}

	@Nullable
	public static Identifier endRelativeOverworld(Identifier from) {
		for (Link l : links.end) {
			if (l.to().equals(from)) return l.from();
		}
		return null;
	}

	@Nullable
	public static Identifier netherRelativeOverworld(Identifier from) {
		for (Link l : links.nether) {
			if (l.to().equals(from)) return l.from();
		}
		return null;
	}

	@Nullable
	public static Identifier relativeNetherSymmetric(Identifier from) {
		for (Link l : links.nether) {
			if (l.to().equals(from)) return l.from();
			if (l.from().equals(from)) return l.to();
		}
		return null;
	}

	@Nullable
	public static Identifier relativeEndSymmetric(Identifier from) {
		for (Link l : links.end) {
			if (l.to().equals(from)) return l.from();
			if (l.from().equals(from)) return l.to();
		}
		return null;
	}
}