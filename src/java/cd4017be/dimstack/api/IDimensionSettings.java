package cd4017be.dimstack.api;

import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.util.INBTSerializable;


/**
 * API for dimension settings that store custom data alongside a dimension.<br>
 * Implementors should provide an empty constructor for instantiation.
 * @see IDimension#getSettings(Class, boolean)
 * @author CD4017BE
 */
public interface IDimensionSettings extends INBTSerializable<NBTBase> {}
