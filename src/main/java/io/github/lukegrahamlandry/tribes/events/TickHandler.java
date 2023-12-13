package io.github.lukegrahamlandry.tribes.events;


import io.github.lukegrahamlandry.tribes.TribesMain;
import io.github.lukegrahamlandry.tribes.config.TribesConfig;
import io.github.lukegrahamlandry.tribes.init.NetworkHandler;
import io.github.lukegrahamlandry.tribes.item.TribeCompass;
import io.github.lukegrahamlandry.tribes.network.CompassChunkPacket;
import io.github.lukegrahamlandry.tribes.network.LandOwnerPacket;
import io.github.lukegrahamlandry.tribes.network.PacketOpenJoinGUI;
import io.github.lukegrahamlandry.tribes.tribe_data.LandClaimHelper;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = TribesMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickHandler {

    private static boolean check_scheduled = true;

    private static int timer = 0;
    static int ONE_MINUTE = 60 * 20;

    @SubscribeEvent
    public static void updateLandOwnerAndCompassAndEffects(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() || event.phase != TickEvent.Phase.START) return;

        if (event.player instanceof ServerPlayer player && player.tickCount % 20 == 0) {

            // land owner display
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                    new LandOwnerPacket(player.getUUID(), LandClaimHelper.getOwnerDisplayFor(player), LandClaimHelper.canAccessLandAt(player, player.blockPosition())));

            // tribe compass direction
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (stack.getItem() instanceof TribeCompass) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new CompassChunkPacket(player.getUUID(), TribeCompass.caclulateTargetPosition(player, stack)));
            }

            // apply tribe effects
            Tribe tribe = TribesManager.getTribeOf(player.getUUID());
            if (tribe != null) {
                tribe.getEffects().getEffects().forEach(effectInfo -> player.addEffect(new MobEffectInstance(effectInfo.effect(), 5 * 20, effectInfo.level() - 1)));
            } else if (TribesConfig.isTribeRequired()) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketOpenJoinGUI(player));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (TribesManager.getTribeOf(event.getPlayer().getUUID()) == null && TribesConfig.isTribeRequired() && event.getPlayer() instanceof ServerPlayer player) {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketOpenJoinGUI(player));
        }
    }

    @SubscribeEvent
    public static void tickDeathPunishments(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        timer++;

        if (timer >= ONE_MINUTE) {
            for (Tribe tribe : TribesManager.getTribes()) {
                if (tribe.claimDisableTime > 0) {
                    tribe.claimDisableTime--;
                    if (tribe.claimDisableTime == 0) {
                        tribe.deathWasPVP = false;
                        tribe.deathIndex = 0;
                    }
                }
            }

            check_scheduled = true;
            timer = 0;
        }

        if (check_scheduled && event.haveTime()) {
            // remove inactive people from their tribes
            RemoveInactives.check(ServerLifecycleHooks.getCurrentServer());
            check_scheduled = false;
        }
    }
}
