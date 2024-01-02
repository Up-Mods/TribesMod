package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.TribesMain;
import io.github.lukegrahamlandry.tribes.commands.util.TribeArgumentType;
import io.github.lukegrahamlandry.tribes.tribe_data.SaveHandler;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

import java.io.IOException;

public class AdminCommands {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("admin")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_ADMINS))
                .then(Commands.literal("save").executes(AdminCommands::saveData))
                .then(Commands.literal("load").executes(AdminCommands::loadData))
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(AdminCommands::handleDelete)
                        )
                )
                .then(Commands.literal("rename")
                        .then(Commands.argument("tribe", TribeArgumentType.tribe())
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(AdminCommands::handleRename))
                                .executes(ctx -> {
                                    ctx.getSource().sendSuccess(new TextComponent("Choose a new name for " + StringArgumentType.getString(ctx, "name")), false);
                                    return 0;
                                })
                        )
                );
    }

    private static int handleRename(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Tribe tribe = TribeArgumentType.getTribe(context, "tribe");
        String name = StringArgumentType.getString(context, "name");

        if (!TribesManager.isNameAvailable(name)) {
            context.getSource().sendFailure(TribeError.NAME_TAKEN.getText());
            return 0;
        }

        TribesManager.renameTribe(tribe, name);
        context.getSource().sendSuccess(new TextComponent("The tribe <" + name + "> is now called <" + name + ">"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int handleDelete(CommandContext<CommandSourceStack> source) {
        String name = StringArgumentType.getString(source, "name");
        var tribe = TribesManager.findTribe(name);

        if (tribe == null) {
            source.getSource().sendFailure(TribeError.INVALID_TRIBE.getText());
            return 0;
        }

        TribesManager.forceDeleteTribe(tribe);
        source.getSource().sendSuccess(new TextComponent("Tribe deleted: " + name), true);

        return Command.SINGLE_SUCCESS;
    }

    public static int saveData(CommandContext<CommandSourceStack> source) {
        try {
            SaveHandler.save(source.getSource().getServer());
        } catch (IOException e) {
            TribesMain.LOGGER.error("unable to save tribe data", e);
            throw new CommandRuntimeException(new TextComponent("unable to save tribe data, check log for details!"));
        }
        source.getSource().sendSuccess(new TextComponent("tribe data has been saved!"), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int loadData(CommandContext<CommandSourceStack> source) {
        try {
            SaveHandler.load(source.getSource().getServer());
        } catch (IOException e) {
            TribesMain.LOGGER.error("unable to reload tribe data", e);
            throw new CommandRuntimeException(new TextComponent("unable to load tribe data, check log for details!"));
        }
        source.getSource().sendSuccess(new TextComponent("tribe data has been reloaded!"), true);
        return Command.SINGLE_SUCCESS;
    }

}
