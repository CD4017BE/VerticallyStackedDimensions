package cd4017be.dimstack.item;

import cd4017be.dimstack.Objects;
import cd4017be.dimstack.core.PortalConfiguration;
import cd4017be.lib.item.BaseItemBlock;
import cd4017be.lib.util.DimPos;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 
 * @author cd4017be
 */
public class ItemPortalAugment extends BaseItemBlock {

	public ItemPortalAugment(Block id) {
		super(id);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState state = worldIn.getBlockState(pos);
		Block block = state.getBlock();
		if (block != Objects.PORTAL) return EnumActionResult.FAIL;
		if (worldIn.isRemote) return EnumActionResult.SUCCESS;
		
		ItemStack stack = player.getHeldItem(hand);
		if (!stack.isEmpty() && player.canPlayerEdit(pos, facing, stack)) {
			int i = this.getMetadata(stack.getMetadata());
			IBlockState state1 = this.block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, i, player, hand);
			
			if (placeBlockAt(stack, player, worldIn, pos, facing, hitX, hitY, hitZ, state1)) {
				state1 = worldIn.getBlockState(pos);
				SoundType soundtype = state1.getBlock().getSoundType(state1, worldIn, pos, player);
				worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				stack.shrink(1);
			}
			return EnumActionResult.SUCCESS;
		} else return EnumActionResult.FAIL;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		DimPos other = PortalConfiguration.getAdjacentPos(new DimPos(pos, world));
		if (other != null && super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
			other.setBlock(newState);
			return true;
		} else return false;
	}

	@Override
	public boolean canItemEditBlocks() {
		return true;
	}

}
