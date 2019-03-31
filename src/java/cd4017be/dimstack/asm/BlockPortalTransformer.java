package cd4017be.dimstack.asm;

import net.minecraft.launchwrapper.IClassTransformer;

import static cd4017be.dimstack.asm.CorePlugin.LOG;
import static cd4017be.dimstack.asm.Name.*;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author CD4017BE
 *
 */
public class BlockPortalTransformer implements IClassTransformer {

	final String c_World, c_BlockPos;
	final String c_BlockPortal, n_BlockPortal;
	final String m_trySpawnPortal, md_trySpawnPortal;
	
	final String c_DisabledPortals;
	final String m_allowNetherPortal, md_allowNetherPortal;

	public BlockPortalTransformer() {
		c_World = type("net.minecraft.world.World", "amu");
		c_BlockPos = type("net.minecraft.util.math.BlockPos", "et");
		
		c_BlockPortal = type("net.minecraft.block.BlockPortal", "ass");
		n_BlockPortal = name(c_BlockPortal);
		m_trySpawnPortal = method("trySpawnPortal", "b");
		md_trySpawnPortal = m_desc(BOOL, c_World, c_BlockPos);
		
		c_DisabledPortals = type("cd4017be.dimstack.api.DisabledPortals");
		m_allowNetherPortal = "allowNetherPortal";
		md_allowNetherPortal = m_desc(BOOL, "net.minecraft.world.World");
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (name.equals(n_BlockPortal))
			return transformBP(basicClass);
		return basicClass;
	}

	private byte[] transformBP(byte[] data) {
		ClassNode cn = new ClassNode();
		ClassReader cr = new ClassReader(data);
		cr.accept(cn, 0);
		
		LOG.debug("patching BlockPortal as {} ...", cn.name);
		boolean found = false;
		for (MethodNode mn : cn.methods) {
			if (mn.desc.equals(md_trySpawnPortal) && mn.name.equals(m_trySpawnPortal)) {
				LOG.debug("patching method trySpawnPortal() as {}{}", mn.name, mn.desc);
				InsnList inj = new InsnList();
				
				LabelNode end = new LabelNode();
				inj.add(new VarInsnNode(ALOAD, 1));
				inj.add(new MethodInsnNode(INVOKESTATIC, c_DisabledPortals, m_allowNetherPortal, md_allowNetherPortal, false));
				inj.add(new JumpInsnNode(IFNE, end));
				inj.add(new InsnNode(ICONST_0));
				inj.add(new InsnNode(IRETURN));
				inj.add(end);
				inj.add(new FrameNode(F_SAME, 3, new Object[] {c_BlockPortal, c_World, c_BlockPos}, 0, new Object[0]));
				
				mn.instructions.insert(inj);
				found = true;
			}
		}
		if (!found) {
			String[] names = new String[cn.methods.size()];
			for (int i = 0; i < names.length; i++) {
				MethodNode mn = cn.methods.get(i);
				names[i] = mn.name + mn.desc;
			}
			LOG.error("can't find BlockPortal.trySpawnPortal() in {}", Arrays.toString(names));
			LOG.info("method name = {}, descriptor = {}", m_trySpawnPortal, md_trySpawnPortal);
		}
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cn.accept(cw);
		return cw.toByteArray();
	}

}
