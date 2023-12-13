package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.config.TribesConfig;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeErrorType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

public class HemiAccessCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("hemisphere")
                .then(Commands.argument("side", StringArgumentType.word())
                        .executes(HemiAccessCommand::handleSelect)
                ).executes(ctx -> {
                            String types = TribesConfig.getUseNorthSouthHemisphereDirection() ? "North / South" : "East / West";
                            if (TribesConfig.getUseNorthSouthHemisphereDirection()) {
                                ctx.getSource().sendSuccess(new TextComponent("Pick which hemisphere (north or south) you want your tribe to be able to place and destroy blocks in. Your choice can never be changed, so choose carefully!").withStyle(style -> style.withColor(TextColor.fromRgb(0x00FFFF))), false);
                            } else {
                                ctx.getSource().sendSuccess(new TextComponent("Pick which hemisphere (east or west) you want your tribe to be able to place and destroy blocks in. Your choice can never be changed, so choose carefully!").withStyle(style -> style.withColor(TextColor.fromRgb(0x00FFFF))), false);
                            }
                            return 0;
                        }
                );

    }

    public static int handleSelect(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        Player player = source.getSource().getPlayerOrException();
        String side = StringArgumentType.getString(source, "side");

        Tribe tribe = TribesManager.getTribeOf(player.getUUID());
        TribeErrorType response = tribe.validateSelectHemi(player, side);

        if (response == TribeErrorType.SUCCESS) {
            ConfirmCommand.add(player, () -> {
                tribe.selectHemi(player, side);
            });
        } else {
            source.getSource().sendSuccess(response.getText(), true);
        }

        return Command.SINGLE_SUCCESS;
    }
}
