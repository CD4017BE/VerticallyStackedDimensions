package cd4017be.dimstack;

import cd4017be.dimstack.block.Portal;
import cd4017be.dimstack.item.ItemPortalAugment;
import cd4017be.dimstack.tileentity.DimensionalPipe;
import cd4017be.lib.block.AdvancedBlock;
import cd4017be.lib.item.BaseItemBlock;
import cd4017be.lib.templates.TabMaterials;
import cd4017be.lib.util.TooltipUtil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

/**
 * 
 * @author CD4017BE
 */
@EventBusSubscriber(modid = Main.ID)
@ObjectHolder(value = Main.ID)
public class Objects {

	public static TabMaterials tabDimStack = new TabMaterials(Main.ID);

	//Blocks
	public static final Portal PORTAL = null;
	public static final AdvancedBlock DIM_PIPE = null;

	//ItemBlocks
	public static final BaseItemBlock portal = null;
	public static final ItemPortalAugment dim_pipe = null;

	//Items

	static void init() {
		tabDimStack.item = new ItemStack(Blocks.OBSIDIAN);
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> ev) {
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		ev.getRegistry().registerAll(
			new Portal("portal"),
			new AdvancedBlock("dim_pipe", Material.IRON, SoundType.METAL, 0, DimensionalPipe.class).setBlockUnbreakable().setResistance(Float.POSITIVE_INFINITY)
		);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> ev) {
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		ev.getRegistry().registerAll(
			new BaseItemBlock(PORTAL).setCreativeTab(tabDimStack),
			new ItemPortalAugment(DIM_PIPE).setCreativeTab(tabDimStack)
		);
	}

}
