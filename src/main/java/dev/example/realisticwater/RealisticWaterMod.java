
package dev.example.realisticwater;

import net.fabricmc.api.ModInitializer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;

public final class RealisticWaterMod implements ModInitializer {

    public static final String MOD_ID = "realistic_water";

    public static final Item REALISTIC_WATER_BUCKET = registerItem(
        "realistic_water_bucket",
        new RealisticWaterBucketItem(
            new Item.Properties()
                .setId(
                    ResourceKey.create(
                        Registries.ITEM,
                        Identifier.fromNamespaceAndPath(
                            MOD_ID,
                            "realistic_water_bucket"
                        )
                    )
                )
                .stacksTo(1)
        )
    );

    private static Item registerItem(String name, Item item) {
        return Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(MOD_ID, name),
            item
        );
    }

    @Override
    public void onInitialize() {
        // The custom bucket uses Minecraft's built-in water.
        // No extra water-spreading simulation is started.
    }

    public static final class RealisticWaterBucketItem extends Item {

        public RealisticWaterBucketItem(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            Level level = context.getLevel();

            BlockPos target = context.getClickedPos()
                .relative(context.getClickedFace());

            Player player = context.getPlayer();

            // Only place water where the target block is replaceable.
            if (!level.getBlockState(target).canBeReplaced()) {
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide()) {

                // Place one ordinary Minecraft water source.
                boolean placed = level.setBlock(
                    target,
                    Blocks.WATER.defaultBlockState(),
                    3
                );

                if (!placed) {
                    return InteractionResult.FAIL;
                }

                level.gameEvent(
                    player,
                    GameEvent.FLUID_PLACE,
                    target
                );

                level.playSound(
                    null,
                    target,
                    SoundEvents.BUCKET_EMPTY,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
                );

                // Return an empty bucket in Survival.
                if (player == null || !player.getAbilities().instabuild) {
                    ItemStack heldStack = context.getItemInHand();

                    heldStack.shrink(1);

                    if (player != null) {
                        player.getInventory().add(
                            new ItemStack(Items.BUCKET)
                        );
                    }
                }
            }

            return InteractionResult.SUCCESS;
        }
    }
}
