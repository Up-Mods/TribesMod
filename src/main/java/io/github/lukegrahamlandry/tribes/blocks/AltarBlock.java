package io.github.lukegrahamlandry.tribes.blocks;

import io.github.lukegrahamlandry.tribes.api.tribe.DeityInfo;
import io.github.lukegrahamlandry.tribes.tile.AltarBlockEntity;
import io.github.lukegrahamlandry.tribes.tribe_data.DeitiesManager;
import io.github.lukegrahamlandry.tribes.tribe_data.Tribe;
import io.github.lukegrahamlandry.tribes.tribe_data.TribesManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Optional;

public class AltarBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;

    public AltarBlock(Block.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer instanceof ServerPlayer player) {
            Optional.ofNullable(TribesManager.getTribeOf(player.getUUID())).flatMap(Tribe::getDeityInfo).map(DeityInfo::getName).ifPresent(deityName -> {
                DeitiesManager.DeityData deity = DeitiesManager.deities.get(deityName);
                if (worldIn.getBlockEntity(pos) instanceof AltarBlockEntity be) {
                    be.setBannerKey(deity.bannerKey);
                }
            });
        }
    }

    @Deprecated
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facingState.is(this) && facing.getAxis().isHorizontal()) {
            ChestType chesttype = facingState.getValue(TYPE);
            if (stateIn.getValue(TYPE) == ChestType.SINGLE && chesttype != ChestType.SINGLE && stateIn.getValue(FACING) == facingState.getValue(FACING) && getDirectionToAttached(facingState) == facing.getOpposite()) {
                return stateIn.setValue(TYPE, chesttype.getOpposite());
            }
        } else if (getDirectionToAttached(stateIn) == facing) {
            return stateIn.setValue(TYPE, ChestType.SINGLE);
        }

        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    // todo: export different ones for each facing direction. need the bbmodel file
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /*
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (state.get(TYPE) == ChestType.SINGLE) {
            return SHAPE_SINGLE;
        } else {
            switch(getDirectionToAttached(state)) {
                case NORTH:
                default:
                    return SHAPE_NORTH;
                case SOUTH:
                    return SHAPE_SOUTH;
                case WEST:
                    return SHAPE_WEST;
                case EAST:
                    return SHAPE_EAST;
            }
        }
    }
     */

    /**
     * Returns a facing pointing from the given state to its attached double chest
     */
    public static Direction getDirectionToAttached(BlockState state) {
        Direction direction = state.getValue(FACING);
        return state.getValue(TYPE) == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        ChestType chesttype = ChestType.SINGLE;
        Direction direction = context.getHorizontalDirection().getOpposite();
        boolean flag = context.isSecondaryUseActive();
        Direction direction1 = context.getClickedFace();
        if (direction1.getAxis().isHorizontal() && flag) {
            Direction direction2 = this.getDirectionToAttach(context, direction1.getOpposite());
            if (direction2 != null && direction2.getAxis() != direction1.getAxis()) {
                direction = direction2;
                chesttype = direction2.getCounterClockWise() == direction1.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
            }
        }

        if (chesttype == ChestType.SINGLE && !flag) {
            if (direction == this.getDirectionToAttach(context, direction.getClockWise())) {
                chesttype = ChestType.LEFT;
            } else if (direction == this.getDirectionToAttach(context, direction.getCounterClockWise())) {
                chesttype = ChestType.RIGHT;
            }
        }

        return this.defaultBlockState().setValue(FACING, direction).setValue(TYPE, chesttype);
    }

    /**
     * Returns facing pointing to a chest to form a double chest with, null otherwise
     */
    @Nullable
    private Direction getDirectionToAttach(BlockPlaceContext context, Direction direction) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos().relative(direction));
        return blockstate.is(this) && blockstate.getValue(TYPE) == ChestType.SINGLE ? blockstate.getValue(FACING) : null;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_48725_) {
        p_48725_.add(FACING, TYPE);
    }
}
