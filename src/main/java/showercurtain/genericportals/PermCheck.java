package showercurtain.genericportals;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

public class PermCheck {
    private static boolean usingPerms;
    static {
        try {
            Class.forName("me.lucko.fabric.api.permissions.Permissions", false, PermCheck.class.getClassLoader());
            usingPerms = true;
        } catch (ClassNotFoundException ignored) {
            usingPerms = false;
        }
    }

    public static void initPerms() {
        if (usingPerms) {
            Permissions.require("genericportals.endLink");
            Permissions.require("genericportals.netherLink");
        }
    }

    public static Predicate<ServerCommandSource> hasPerm(String perm) {
        if (usingPerms) {
            return src -> src.hasPermissionLevel(4);
        } else {
            return src -> {
                if (src.hasPermissionLevel(4)) return true;
                if (!src.isExecutedByPlayer()) return false;
                return Permissions.check(src, perm);
            };
        }
    }
}
