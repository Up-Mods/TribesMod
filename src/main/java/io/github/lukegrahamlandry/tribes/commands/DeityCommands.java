package io.github.lukegrahamlandry.tribes.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.lukegrahamlandry.tribes.api.tribe.DeityInfo;
import io.github.lukegrahamlandry.tribes.api.tribe.Member;
import io.github.lukegrahamlandry.tribes.commands.util.DeityArgumentType;
import io.github.lukegrahamlandry.tribes.config.TribesConfig;
import io.github.lukegrahamlandry.tribes.init.BannerInit;
import io.github.lukegrahamlandry.tribes.tribe_data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.items.ItemHandlerHelper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class DeityCommands {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("deity")
                .then(Commands.literal("book").executes(DeityCommands::createBook))
                .then(Commands.literal("list").executes(DeityCommands::handleList))
                .then(Commands.literal("banner").executes(DeityCommands::createBanner))
                .then(Commands.literal("choose")
                        .then(Commands.argument("deity", DeityArgumentType.tribe())
                                .executes(DeityCommands::handleChoose))
                        .executes(ctx -> {
                            ctx.getSource().sendFailure(TribeError.ARG_DEITY.getText());
                            return 0;
                        }))
                .then(Commands.literal("describe")
                        .then(Commands.argument("deity", DeityArgumentType.tribe())
                                .executes(DeityCommands::handleDescribe))
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(TribeError.ARG_DEITY.getText(), false);
                            return 0;
                        }))
                ;
    }

    private static int handleChoose(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        DeitiesManager.DeityData deity = DeityArgumentType.getDeity(source, "deity");
        ServerPlayer player = source.getSource().getPlayerOrException();

        if (!TribesManager.playerHasTribe(player.getUUID())) {
            source.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }
        Tribe tribe = TribesManager.getTribeOf(player.getUUID());

        if (tribe.getRankOf(player.getUUID()) != Member.Rank.LEADER) {
            source.getSource().sendFailure(TribeError.RANK_TOO_LOW.getText());
            return 0;
        }
        Instant lastChange = tribe.getDeityInfo().map(DeityInfo::getLastChanged).orElse(Instant.MIN);
        if (lastChange.isAfter(Instant.now().minus(TribesConfig.daysBetweenDeityChanges(), ChronoUnit.DAYS))) {
            long hoursToWait = Instant.now().until(lastChange.plus(TribesConfig.daysBetweenDeityChanges(), ChronoUnit.DAYS), ChronoUnit.HOURS);
            source.getSource().sendFailure(TribeError.getWaitText(hoursToWait));
        } else {
            ConfirmCommand.add(player, () -> {
                tribe.setDeityInfo(new DeityInfo(deity.key, Instant.now()));
                source.getSource().sendSuccess(TribeSuccessType.CHOOSE_DEITY.getText(deity.displayName), true);
            });
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int handleList(CommandContext<CommandSourceStack> source) {
        DeitiesManager.deities.forEach((key, data) -> {
            var domains = String.join(", ", data.domains);
            if (data.domains.size() > 1) {
                var first = data.domains.subList(0, data.domains.size() - 1);
                domains = String.join(", ", first) + " and " + data.domains.get(data.domains.size() - 1);
            }
            source.getSource().sendSuccess(TribeSuccessType.DESCRIBE_DEITY.getBlueText(data.displayName, data.label, domains), false);
        });
        return Command.SINGLE_SUCCESS;
    }

    private static int handleDescribe(CommandContext<CommandSourceStack> source) {
        DeitiesManager.DeityData data = DeityArgumentType.getDeity(source, "deity");
        if (data != null) {
            //FIXME make translatable
            String domains;
            if (data.domains.size() < 2) {
                domains = data.domains.get(0);
            } else {
                domains = String.join(", ", data.domains.subList(0, data.domains.size() - 2)) + " and " + data.domains.get(data.domains.size() - 1);
            }
            source.getSource().sendSuccess(TribeSuccessType.DESCRIBE_DEITY.getBlueText(data.displayName, data.label, domains), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int createBanner(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (!TribesManager.playerHasTribe(player.getUUID())) {
            context.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        String deityName = TribesManager.getTribeOf(player.getUUID()).getDeityInfo().map(DeityInfo::getName).orElse(null);
        if (deityName == null) {
            context.getSource().sendFailure(TribeError.NO_DEITY.getText());
            return 0;
        }

        ItemStack banner = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(banner.getItem() instanceof BannerItem)) {
            context.getSource().sendFailure(TribeError.HOLD_BANNER.getText());
            return 0;
        }

        DeitiesManager.DeityData data = DeitiesManager.deities.get(deityName);

        // dont actually need the BannerPattern here, just hashname
        BannerPattern bannerpattern = BannerInit.get(data.bannerKey);
        DyeColor dyecolor = DyeColor.WHITE;
        CompoundTag compoundnbt = banner.getOrCreateTagElement("BlockEntityTag");
        ListTag listnbt = compoundnbt.getList("Patterns", Tag.TAG_COMPOUND);

        CompoundTag compoundnbt1 = new CompoundTag();
        compoundnbt1.putString("Pattern", bannerpattern.getHashname());
        compoundnbt1.putInt("Color", dyecolor.getId());
        listnbt.add(compoundnbt1);
        compoundnbt.put("Patterns", listnbt);

        player.setItemInHand(InteractionHand.MAIN_HAND, banner);

        context.getSource().sendSuccess(TribeSuccessType.MAKE_HOLY_BANNER.getText(), false);

        return Command.SINGLE_SUCCESS;
    }


    private static int createBook(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (!TribesManager.playerHasTribe(player.getUUID())) {
            context.getSource().sendFailure(TribeError.YOU_NOT_IN_TRIBE.getText());
            return 0;
        }

        String deityName = TribesManager.getTribeOf(player.getUUID()).getDeityInfo().map(DeityInfo::getName).orElse(null);
        if (deityName == null) {
            context.getSource().sendFailure(TribeError.NO_DEITY.getText());
            return 0;
        }

        Item currentlyHeld = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();

        if (currentlyHeld != Items.BOOK && currentlyHeld != Items.WRITABLE_BOOK && currentlyHeld != Items.BOOKSHELF) {
            context.getSource().sendFailure(TribeError.HOLD_BOOK.getText());
            return 0;
        }

        DeitiesManager.DeityData data = DeitiesManager.deities.get(deityName);
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

        CompoundTag tag = new CompoundTag();

        tag.putString("author", data.bookAuthor);
        tag.putString("title", data.bookTitle);
        tag.putBoolean("resolved", true);

        ListTag pages = new ListTag();
        for (String content : data.bookPages) {
            Tag page = StringTag.valueOf("{\"text\": \"" + content + "\"}");
            pages.add(page);
        }
        tag.put("pages", pages);

        book.setTag(tag);

        player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);

        int copies = currentlyHeld == Items.BOOKSHELF ? 3 : 1;
        for (int i = 0; i < copies; i++) {
            ItemHandlerHelper.giveItemToPlayer(player, book.copy());
        }

        context.getSource().sendSuccess(TribeSuccessType.MAKE_HOLY_BOOK.getText(), false);

        return copies;
    }

}
