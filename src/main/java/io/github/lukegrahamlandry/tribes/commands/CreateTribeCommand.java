package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeError;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class CreateTribeCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            Player player = ctx.getSource().getPlayerOrException();
                            String name = StringArgumentType.getString(ctx, "name");

                            var result = TribesManager.createNewTribe(name, player);
                            if (!result.success()) {
                                ctx.getSource().sendFailure(result.error().getText());
                                return 0;
                            }
                            ctx.getSource().sendSuccess(TribeSuccessType.MADE_TRIBE.getText(result.value().getName()), true);

                            return Command.SINGLE_SUCCESS;
                        })
                ).executes(ctx -> {
                            ctx.getSource().sendSuccess(TribeError.ARG_MISSING.getText(), false);
                            return 0;
                        }
                );

    }
}
