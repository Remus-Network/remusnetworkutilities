package net.remusnetworkutilities.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WetSpongeBlock;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.remusnetworkutilities.RemusNetworkUtilitiesSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WetSpongeBlock.class)
public abstract class SpongeDryingMixin { // Sponge Drying
    @Inject(method = "onBlockAdded", at = @At("HEAD"), cancellable = true)
    private void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        if ((world.getDimension().ultrawarm() || isOnMagmaBlock(world, pos) && !isSubmergedInWater(world, pos))) {
            world.setBlockState(pos, Blocks.SPONGE.getDefaultState(), Block.NOTIFY_ALL);
            world.syncWorldEvent(WorldEvents.WET_SPONGE_DRIES_OUT, pos, 0);
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0f, (1.0f + world.getRandom().nextFloat() * 0.2f) * 0.7f);
            ci.cancel();
        }
    }
    @Unique
    private boolean isOnMagmaBlock(World world, BlockPos pos) {
        if (!RemusNetworkUtilitiesSettings.SpongeDrying) return false;
        BlockPos belowPos = pos.down();
        BlockState blockState = world.getBlockState(belowPos);
        return blockState.getBlock() == Blocks.MAGMA_BLOCK;
    }
    @Unique
    private boolean isSubmergedInWater(World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (world.getFluidState(pos.offset(direction, 2)).isIn(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }
}
