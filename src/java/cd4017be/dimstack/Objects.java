package cd4017be.dimstack;

import cd4017be.dimstack.block.Portal;
import cd4017be.dimstack.block.ProgressionBarrier;
import cd4017be.dimstack.core.WorldProviderCustom;
import cd4017be.dimstack.item.ItemPortalAugment;
import cd4017be.dimstack.tileentity.DimensionalPipe;
import cd4017be.lib.block.AdvancedBlock;
import cd4017be.lib.item.BaseItemBlock;
import cd4017be.lib.item.ItemVariantBlock;
import cd4017be.lib.templates.TabMaterials;
import cd4017be.lib.util.TooltipUtil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.DimensionType;
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

	public static DimensionType CUSTOM_DIM_TYPE = DimensionType.register("dimstack_custom", "_custom", 2, WorldProviderCustom.class, false);
	public static TabMaterials tabDimStack = new TabMaterials(Main.ID);
	public static Material M_PORTAL;
	public static Material M_BEDROCK;

	//Blocks
	public static final Portal PORTAL = null;
	public static final AdvancedBlock DIM_PIPE = null;
	public static final ProgressionBarrier BEDROCK;

	//ItemBlocks
	public static final BaseItemBlock portal = null;
	public static final ItemPortalAugment dim_pipe = null;
	public static final ItemVariantBlock bedrock = null;

	//Items

	//required before init
	static {
		M_PORTAL = Material.PORTAL;
		M_BEDROCK = new Material(MapColor.BLACK) {{
			setRequiresTool();
			setImmovableMobility();
		}};
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		BEDROCK = new ProgressionBarrier("bedrock", M_BEDROCK);
	}

	static void init() {
		tabDimStack.item = new ItemStack(portal);
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> ev) {
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		ev.getRegistry().registerAll(
			new Portal("portal", M_PORTAL).setCreativeTab(tabDimStack),
			new AdvancedBlock("dim_pipe", M_PORTAL, SoundType.METAL, 0, DimensionalPipe.class).setBlockUnbreakable().setResistance(Float.POSITIVE_INFINITY).setCreativeTab(tabDimStack),
			BEDROCK.setCreativeTab(tabDimStack)
		);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> ev) {
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		ev.getRegistry().registerAll(
			new BaseItemBlock(PORTAL),
			new ItemPortalAugment(DIM_PIPE),
			new ItemVariantBlock(BEDROCK)
		);
	}

}
