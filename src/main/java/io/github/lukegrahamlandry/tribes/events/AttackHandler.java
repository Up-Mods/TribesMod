package io.github.lukegrahamlandry.tribes.events;


import com.mojang.authlib.GameProfile;
import io.github.lukegrahamlandry.tribes.TribesMain;
import io.github.lukegrahamlandry.tribes.api.tribe.Relation;
import io.github.lukegrahamlandry.tribes.config.TribesConfig;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TribesMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AttackHandler {
    @SubscribeEvent
    public static void blockFriendlyFire(LivingDamageEvent event){
        if (TribesConfig.getFriendlyFireEnabled()) return;

        Entity source = event.getSource().getEntity();
        Entity target = event.getEntityLiving();

        if (source instanceof Player && !source.getCommandSenderWorld().isClientSide()){
            Tribe sourceTribe = TribesManager.getTribeOf(source.getUUID());
            Tribe targetTribe = TribesManager.getTribeOf(target.getUUID());

            if (sourceTribe != null && targetTribe != null){
                if (sourceTribe.equals(targetTribe)) event.setAmount(0);
            }
        }
    }

    @SubscribeEvent
    public static void punishDeath(LivingDeathEvent event){
        Entity dead = event.getEntity();
        if (!(dead instanceof Player deadPlayer) || dead.getCommandSenderWorld().isClientSide()) return;

        Entity killer = event.getSource().getEntity();
        if (killer instanceof Player killerPlayer){
            tryDropHead(deadPlayer, killerPlayer);
        }

        Tribe tribe = TribesManager.getTribeOf(event.getEntityLiving().getUUID());
        if (tribe == null) return;

        if (event.getSource().getEntity() instanceof Player) tribe.deathWasPVP = true;

        tribe.claimDisableTime = TribesConfig.getDeathClaimDisableTime(tribe.deathIndex, tribe.deathWasPVP);
        tribe.deathIndex++;
    }

    private static void tryDropHead(Player dead, Player killer) {
        Tribe deadTribe = TribesManager.getTribeOf(dead.getUUID());
        Tribe killerTribe = TribesManager.getTribeOf(killer.getUUID());
        if (killerTribe == null || deadTribe == null) return;
        if (killerTribe.getId().equals(deadTribe.getId())) return;

        if (killerTribe.getRelations().containsKey(deadTribe.getId()) && killerTribe.getRelations().get(deadTribe.getId()).type() == Relation.Type.ALLY) return;

        // actually drop the head
        GameProfile gameprofile = dead.getGameProfile();
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

        stack.getOrCreateTag().put(SkullBlockEntity.TAG_SKULL_OWNER, NbtUtils.writeGameProfile(new CompoundTag(), gameprofile));
        ItemEntity itementity = new ItemEntity(dead.getLevel(), dead.getX(), dead.getY(), dead.getZ(), stack);
        dead.getCommandSenderWorld().addFreshEntity(itementity);
    }
}
