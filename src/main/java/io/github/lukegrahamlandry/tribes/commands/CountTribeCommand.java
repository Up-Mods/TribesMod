package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.commands.util.TribeArgumentType;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CountTribeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("count")
                .then(Commands.argument("tribe", TribeArgumentType.tribe())
                        .executes(CountTribeCommand::handleCount)
                );

    }

    public static int handleCount(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        Tribe tribe = TribeArgumentType.getTribe(source, "tribe");

        source.getSource().sendSuccess(TribeSuccessType.COUNT_TRIBE.getBlueText(tribe, tribe.getMemberCount(), tribe.getTribeTier()), true);

        return Command.SINGLE_SUCCESS;
    }
}
