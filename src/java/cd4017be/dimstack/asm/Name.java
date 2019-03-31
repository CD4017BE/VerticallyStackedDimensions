package cd4017be.dimstack.asm;

import net.minecraft.launchwrapper.Launch;
import static cd4017be.dimstack.asm.CorePlugin.LOG;

/**
 * @author CD4017BE
 *
 */
public class Name {

	public static final boolean OBFUSCATED;
	public static final char
		BOOL = 'Z',
		INT = 'I',
		VOID = 'V';

	static {
		boolean obf;
		try {obf = Launch.classLoader.getClassBytes("net.minecraft.world.World") == null;}
		catch (Exception e) {obf = true;}
		OBFUSCATED = obf;
		LOG.debug("running in {} environment", obf ? "obfuscated" : "deobfuscated");
	}

	public static String type(String name) {
		return name.replace('.', '/');
	}

	public static String type(String deobf, String obf) {
		return type(OBFUSCATED ? obf : deobf);
	}

	public static String name(String type) {
		return type.replace('/', '.');
	}

	public static String method(String deobf, String obf) {
		return OBFUSCATED ? obf : deobf;
	}

	public static String m_desc(Object ret, Object... args) {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (Object arg : args)
			sb.append(f_desc(arg));
		sb.append(')');
		sb.append(f_desc(ret));
		return sb.toString();
	}

	public static String f_desc(Object type) {
		if (type instanceof Character) return type.toString();
		else if (type instanceof Class) {
			String s = ((Class<?>)type).getName().replace('.', '/');
			return s.charAt(0) == '[' ? s : 'L' + s + ';';
		} else return 'L' + type.toString().replace('.', '/') + ';';
	}

}
