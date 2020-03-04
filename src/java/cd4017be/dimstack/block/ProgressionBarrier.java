package cd4017be.dimstack.block;

import static cd4017be.lib.util.TooltipUtil.format;
import static cd4017be.lib.util.TooltipUtil.translate;

import java.util.Arrays;
import java.util.List;
import cd4017be.api.recipes.ItemOperand;
import cd4017be.lib.block.BaseBlock;
import cd4017be.lib.script.obj.Array;
import cd4017be.lib.script.obj.Error;
import cd4017be.lib.script.obj.IOperand;
import cd4017be.lib.script.obj.Nil;
import cd4017be.lib.script.obj.Text;
import cd4017be.lib.script.obj.Number;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;


/**
 * @author CD4017BE
 *
 */
public class ProgressionBarrier extends BaseBlock implements IOperand {

	public static final PropertyInteger variant = PropertyInteger.create("var", 0, 15);
	public ItemStack[][] requiredTools = new ItemStack[16][];
	public float[] hardness = new float[16], resistance = new float[16];
	public short witherProof;

	public ProgressionBarrier(String id, Material m) {
		super(id, m);
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag advanced) {
		String name = getUnlocalizedName();
		list.add(translate(name + ".tools"));
		int i = stack.getMetadata();
		ItemStack[] tools = requiredTools[i];
		if (tools == null) {
			IBlockState state = getStateFromMeta(i);
			String tool = getHarvestTool(state);
			if (tool != null)
				list.add(format("dimstack.toollvl." + tool, getHarvestLevel(state)));
		} else for (ItemStack tool : tools)
			list.add("- " + tool.getDisplayName());
		list.add(format(name + ".expl", resistance[i] > 1e6F));
		list.add(format(name + ".mob", (witherProof >> i & 1) != 0));
		super.addInformation(stack, player, list, advanced);
	}

	@Override
	public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
		for (int i = 0; i < 16; i++)
			items.add(new ItemStack(this, 1, i));
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(variant);
	}

	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
		if (!canBreak(state, player.getHeldItemMainhand()) && !canBreak(state, player.getHeldItemOffhand())) return 0;
		return ForgeHooks.blockStrength(state, player, world, pos);
	}

	private boolean canBreak(IBlockState state, ItemStack stack) {
		if (stack.isEmpty()) return false;
		ItemStack[] ref = requiredTools[state.getValue(variant)];
		if (ref == null) {
			String tool = getHarvestTool(state);
			return tool == null || stack.getItem().getHarvestLevel(stack, tool, null, state) >= getHarvestLevel(state);
		} else for (ItemStack r : ref)
			if (r != null && stack.isItemEqualIgnoreDurability(r))
				return true;
		return false;
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return getHarvestTool(world.getBlockState(pos)) == null || super.canHarvestBlock(world, pos, player);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (hardness[state.getValue(variant)] >= 0) return false;
		ItemStack stack = playerIn.getHeldItem(hand);
		if (!canBreak(state, stack)) return false;
		removedByPlayer(state, worldIn, pos, playerIn, true);
		harvestBlock(worldIn, playerIn, pos, state, null, stack);
		return true;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(variant, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(variant);
	}

	@Override
	public float getBlockHardness(IBlockState state, World worldIn, BlockPos pos) {
		return hardness[state.getValue(variant)];
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, variant);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		return resistance[world.getBlockState(pos).getValue(variant)] / 5.0F;
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		return (witherProof >> state.getValue(variant) & 1) == 0;
	}

	//config integration

	@Override
	public boolean asBool() throws Error {return true;}

	@Override
	public Object value() {return this;}

	@Override
	public IOperand get(IOperand idx) {
		int i = idx.asIndex();
		if (i < 0 || i >= 16) return Nil.NIL;
		ItemStack[] tools = requiredTools[i];
		IBlockState state = getStateFromMeta(i);
		String tool = getHarvestTool(state);
		int lvl = getHarvestLevel(state);
		return new Array(
				tools == null ? Nil.NIL : new Array(tools, ItemOperand::new),
				new Text(tool == null ? "" : tool + ":" + lvl),
				new Number(hardness[i]),
				new Number(resistance[i]),
				new Number(witherProof >> i & 1)
			);
	}

	@Override
	public void put(IOperand idx, IOperand val) {
		int i = idx.asIndex();
		if (i < 0 || i >= 16) return;
		if (val instanceof Array) {
			Object[] arr = ((Array)val).value();
			Object el;
			switch(arr.length) {
			default:
				if ((el = arr[4]) instanceof Double)
					if ((Double)el > 0.5) witherProof |= 1 << i;
					else witherProof &= ~(1 << i);
			case 4:
				if ((el = arr[3]) instanceof Double)
					resistance[i] = ((Double)el).floatValue();
			case 3:
				if ((el = arr[2]) instanceof Double)
					hardness[i] = ((Double)el).floatValue();
			case 2:
				if ((el = arr[1]) instanceof String) {
					String s = (String)el;
					int p = s.indexOf(':'), lvl = 0;
					if (p >= 0) {
						try {lvl = Integer.parseInt(s.substring(p + 1));}
						catch(NumberFormatException e) {}
						s = s.substring(0, p);
					}
					setHarvestLevel(s.isEmpty() ? null : s, lvl, getStateFromMeta(i));
				}
			case 1:
				if ((el = arr[0]) instanceof ItemStack)
					requiredTools[i] = new ItemStack[] {(ItemStack)el};
				else if (el instanceof Object[])
					requiredTools[i] = Arrays.copyOf((Object[])el, ((Object[])el).length, ItemStack[].class);
				else
					requiredTools[i] = null;
			case 0:
			}
		}
	}

}
