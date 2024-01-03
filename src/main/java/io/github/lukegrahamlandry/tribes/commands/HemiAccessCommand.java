package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.api.claims.HemisphereDirection;
import io.github.lukegrahamlandry.tribes.config.TribesConfig;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeHelper;
import io.github.lukegrahamlandry.tribes.tribe_data.TribeSuccessType;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

public class HemiAccessCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("hemisphere")
                .then(Commands.argument("side", StringArgumentType.word())
                        .executes(HemiAccessCommand::handleSelect)
                ).executes(ctx -> {
                            if (TribesConfig.getHemisphereDirection() == HemisphereDirection.NORTH_SOUTH) {
                                ctx.getSource().sendSuccess(new TextComponent("Pick which hemisphere (north or south) you want your tribe to be able to place and destroy blocks in. Your choice can never be changed, so choose carefully!").withStyle(style -> style.withColor(TextColor.fromRgb(0x00FFFF))), false);
                            } else {
                                ctx.getSource().sendSuccess(new TextComponent("Pick which hemisphere (east or west) you want your tribe to be able to place and destroy blocks in. Your choice can never be changed, so choose carefully!").withStyle(style -> style.withColor(TextColor.fromRgb(0x00FFFF))), false);
                            }
                            return 0;
                        }
                );
    }

    public static int handleSelect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        String side = StringArgumentType.getString(ctx, "side");

        Tribe tribe = TribesManager.getTribeOf(player.getUUID());
        var result = tribe.validateSelectHemi(player, side);

        if (!result.success()) {
            ctx.getSource().sendFailure(result.error().getText());
            return 0;
        }

        player.sendMessage(TribeSuccessType.MUST_CONFIRM_HEMISPHERE.getBlueText(side), Util.NIL_UUID);
        ConfirmCommand.add(player, () -> {
            if (tribe.selectHemisphere(player, result.value()).success()) {
                TribeHelper.broadcastMessage(tribe, TribeSuccessType.CHOOSE_HEMISPHERE, player, ctx.getSource().getServer(), side);
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
