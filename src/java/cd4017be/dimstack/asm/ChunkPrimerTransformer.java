package cd4017be.dimstack.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import static org.objectweb.asm.Opcodes.*;
import static cd4017be.dimstack.asm.CorePlugin.LOG;
import static cd4017be.dimstack.asm.Name.*;

import java.util.Arrays;

/**
 * This class transformer adds a public IBlockState field into ChunkPrimer and injects code to the beginning of its setBlockState() method which will compare the value of this field with the given IBlockState parameter: if they are equal, the method will immediately return.
 * This will prevent the block currently set in the field from being placed during chunk generation.
 * @author CD4017BE
 */
public class ChunkPrimerTransformer implements IClassTransformer {

	final String c_IBlockState;
	final String c_ChunkPrimer, n_ChunkPrimer;
	final String m_setBlockState, md_setBlockState;
	final String f_exclude, fd_exclude;
	final String n_BlockPredicate;
	final String m_disableBlock, md_disableBlock;

	public ChunkPrimerTransformer() {
		this.c_IBlockState = type("net.minecraft.block.state.IBlockState", "awt");
		
		this.c_ChunkPrimer = type("net.minecraft.world.chunk.ChunkPrimer", "ayw");
		this.n_ChunkPrimer = name(c_ChunkPrimer);
		this.m_setBlockState = method("setBlockState", "a");
		this.md_setBlockState = m_desc(VOID, INT, INT, INT, c_IBlockState);
		this.f_exclude = "ingnoredBlock";
		this.fd_exclude = f_desc(c_IBlockState);
		
		this.n_BlockPredicate = "cd4017be.dimstack.api.util.BlockPredicate";
		this.m_disableBlock = "disableBlock";
		this.md_disableBlock = m_desc(VOID, "net.minecraft.world.chunk.ChunkPrimer", "net.minecraft.block.state.IBlockState");
		
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (name.equals(n_ChunkPrimer))
			return transformCP(basicClass);
		if (name.equals(n_BlockPredicate))
			return transformAcc(basicClass);
		return basicClass;
	}

	private byte[] transformCP(byte[] data) {
		ClassNode cn = new ClassNode();
		ClassReader cr = new ClassReader(data);
		cr.accept(cn, 0);
		
		LOG.debug("patching ChunkPrimer as {} ...", cn.name);
		boolean found = false;
		for (MethodNode mn : cn.methods) {
			if (mn.desc.equals(md_setBlockState) && m_setBlockState.contains(mn.name)) {
				LOG.debug("patching method setBlockState() as {}{}", mn.name, mn.desc);
				InsnList inj = new InsnList();
				
				LabelNode end = new LabelNode();
				inj.add(new VarInsnNode(ALOAD, 4));
				inj.add(new JumpInsnNode(IFNULL, end));
				
				inj.add(new VarInsnNode(ALOAD, 0));
				inj.add(new FieldInsnNode(GETFIELD, c_ChunkPrimer, f_exclude, fd_exclude));
				inj.add(new VarInsnNode(ALOAD, 4));
				inj.add(new JumpInsnNode(IF_ACMPNE, end));
				
				inj.add(new InsnNode(RETURN));
				inj.add(end);
				inj.add(new FrameNode(F_SAME, 5, new Object[] {c_ChunkPrimer, INTEGER, INTEGER, INTEGER, c_IBlockState}, 0, new Object[0]));
				
				mn.instructions.insert(inj);
				found = true;
			}
		}
		if (!found) {
			String[] names = new String[cn.methods.size()];
			for (int i = 0; i < names.length; i++)
				names[i] = cn.methods.get(i).name;
			LOG.error("can't find ChunkPrimer.setBlockState() in {}", Arrays.toString(names));
			LOG.info("method name = {}, descriptor = {}", m_setBlockState, md_setBlockState);
		}
		
		LOG.debug("adding field ingnoredBlock");
		cn.fields.add(new FieldNode(ACC_PUBLIC, f_exclude, fd_exclude, null, null));
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cn.accept(cw);
		return cw.toByteArray();
	}

	private byte[] transformAcc(byte[] data) {
		ClassNode cn = new ClassNode();
		ClassReader cr = new ClassReader(data);
		cr.accept(cn, 0);
		
		LOG.debug("patching BlockPredicate ...");
		for (MethodNode mn : cn.methods)
			if (mn.name.equals(m_disableBlock) && mn.desc.equals(md_disableBlock)) {
				LOG.debug("patching method disableBlock() as {}{}", mn.name, mn.desc);
				InsnList inj = new InsnList();
				
				inj.add(new VarInsnNode(ALOAD, 0));
				inj.add(new VarInsnNode(ALOAD, 1));
				inj.add(new FieldInsnNode(PUTFIELD, c_ChunkPrimer, f_exclude, fd_exclude));
				inj.add(new InsnNode(RETURN));
				
				mn.instructions = inj;
			}
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cn.accept(cw);
		return cw.toByteArray();
	}

}
