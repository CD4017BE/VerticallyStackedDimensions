package cd4017be.dimstack.api;

import java.util.HashSet;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;

/**
 * 
 * @author CD4017BE
 */
@SuppressWarnings("serial")
public class DisabledPortals extends HashSet<String> implements IDimensionSettings {

	@Override
	public NBTBase serializeNBT() {
		NBTTagList list = new NBTTagList();
		for (String s : this)
			list.appendTag(new NBTTagString(s));
		return list;
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		this.clear();
		for (NBTBase tag : (NBTTagList)nbt)
			this.add(((NBTTagString)tag).getString());
	}

	public static boolean allowNetherPortal(World world) {
		return !API.INSTANCE.getSettings(DisabledPortals.class, true).contains("nether");
	}

}
