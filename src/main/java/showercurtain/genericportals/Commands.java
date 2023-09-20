package showercurtain.genericportals;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.block.Block;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import showercurtain.genericportals.links.Link;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Commands {
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("genericportals").requires(PermCheck.hasPerm("genericportals.endLink").or(PermCheck.hasPerm("genericportals.netherLink")))
                .then(literal("linkNether").requires(PermCheck.hasPerm("genericportals.netherLink"))
                        .then(argument("from", DimensionArgumentType.dimension())
                                .then(argument("to", DimensionArgumentType.dimension()).executes(Commands::createNetherLink))))
                .then(literal("linkEnd").requires(PermCheck.hasPerm("genericportals.endLink"))
                        .then(argument("from", DimensionArgumentType.dimension())
                                .then(argument("to", DimensionArgumentType.dimension()).executes(Commands::createEndLink))))
                .then(literal("list").executes(Commands::listLinks))
                .then(literal("unlinkNether").requires(PermCheck.hasPerm("genericportals.netherLink"))
                        .then(argument("from", DimensionArgumentType.dimension())
                                .then(argument("to", DimensionArgumentType.dimension()).executes(Commands::deleteNetherLink))))
                .then(literal("unlinkEnd").requires(PermCheck.hasPerm("genericportals.endLink"))
                        .then(argument("from", DimensionArgumentType.dimension())
                                .then(argument("to", DimensionArgumentType.dimension()).executes(Commands::deleteEndLink)))));
    }

    public static int createNetherLink(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Identifier from = IdentifierArgumentType.getIdentifier(ctx, "from");
        Identifier to = IdentifierArgumentType.getIdentifier(ctx, "to");
        if (from.equals(to)) {
            throw new SimpleCommandExceptionType(Text.literal("Cannot link a dimension to itself")).create();
        }
        if (GenericPortals.relativeNetherSymmetric(from) != null || GenericPortals.relativeNetherSymmetric(to) != null) {
            throw new SimpleCommandExceptionType(Text.literal("At least one dimension alreaty has a relative nether")).create();
        }
        GenericPortals.links.nether.add(new Link(from, to));
        ctx.getSource().sendFeedback(() -> Text.literal("Created new nether connection between " + from + " and " + to), true);
        return 1;
    }

    public static int createEndLink(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Identifier from = IdentifierArgumentType.getIdentifier(ctx, "from");
        Identifier to = IdentifierArgumentType.getIdentifier(ctx, "to");
        if (from.equals(to)) {
            throw new SimpleCommandExceptionType(Text.literal("Cannot link a dimension to itself")).create();
        }
        if (GenericPortals.relativeEndSymmetric(from) != null || GenericPortals.relativeEndSymmetric(to) != null) {
            throw new SimpleCommandExceptionType(Text.literal("At least one dimension already has a relative end")).create();
        }
        GenericPortals.links.end.add(new Link(from, to));
        ctx.getSource().sendFeedback(() -> Text.literal("Created new end connection between " + from + " and " + to), true);
        return 1;
    }

    public static int deleteEndLink(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Identifier from = IdentifierArgumentType.getIdentifier(ctx, "from");
        Identifier to = IdentifierArgumentType.getIdentifier(ctx, "to");
        Link l = new Link(from, to);
        if (!GenericPortals.links.end.contains(l)) throw new SimpleCommandExceptionType(Text.literal("Portal link does not exist")).create();

        GenericPortals.links.end.remove(l);
        ctx.getSource().sendFeedback(() -> Text.literal("Deleted end connection between " + from + " and "+to), true);

        return 1;
    }

    public static int deleteNetherLink(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Identifier from = IdentifierArgumentType.getIdentifier(ctx, "from");
        Identifier to = IdentifierArgumentType.getIdentifier(ctx, "to");
        Link l = new Link(from, to);
        if (!GenericPortals.links.nether.contains(l)) throw new SimpleCommandExceptionType(Text.literal("Portal link does not exist")).create();

        GenericPortals.links.nether.remove(l);
        ctx.getSource().sendFeedback(() -> Text.literal("Deleted nether connection between " + from + " and "+to), true);

        return 1;
    }

    public static int listLinks(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ctx.getSource().sendFeedback(()->{
            MutableText out = Text.literal("Nether:\n");
            for (Link i : GenericPortals.links.nether) {
                out.append(i.from() + " -> " + i.to()+"\n");
            }
            out.append("End:\n");
            for (Link i : GenericPortals.links.end) {
                out.append(i.from() + " -> " + i.to()+"\n");
            }
            return out;
        }, false);

        return 1;
    }
}