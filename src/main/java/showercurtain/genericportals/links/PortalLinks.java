package showercurtain.genericportals.links;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class PortalLinks extends PersistentState {
    public final HashSet<Link> nether = new HashSet<>();
    public final HashSet<Link> end = new HashSet<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound n = new NbtCompound();
        for (Link l : nether) {
            n.putString(l.from().toString(), l.to().toString());
        }
        NbtCompound e = new NbtCompound();
        for (Link l : end) {
            e.putString(l.from().toString(), l.to().toString());
        }
        nbt.put("nether", n);
        nbt.put("end", e);
        return nbt;
    }

    public static PortalLinks fromNbt(NbtCompound nbt) {
        PortalLinks out = new PortalLinks();

        NbtCompound n = nbt.getCompound("nether");
        for (String k : n.getKeys()) {
            out.nether.add(new Link(new Identifier(k), new Identifier(n.getString(k))));
        }

        NbtCompound e = nbt.getCompound("end");
        for (String k : e.getKeys()) {
            out.end.add(new Link(new Identifier(k), new Identifier(e.getString(k))));
        }

        return out;
    }

    public static PortalLinks getOrCreate(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
        return manager.getOrCreate(
                PortalLinks::fromNbt,
                PortalLinks::new,
                "registeredPortals"
        );
    }

    @Override
    public boolean isDirty() { return true; }
}
