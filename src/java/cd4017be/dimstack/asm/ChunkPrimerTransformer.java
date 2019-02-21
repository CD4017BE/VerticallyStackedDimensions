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

import java.util.Arrays;
import java.util.Collection;

/**
 * This class transformer adds a public IBlockState field into ChunkPrimer and injects code to the beginning of its setBlockState() method which will compare the value of this field with the given IBlockState parameter: if they are equal, the method will immediately return.
 * This will prevent the block currently set in the field from being placed during chunk generation.
 * @author CD4017BE
 */
public class ChunkPrimerTransformer implements IClassTransformer {

	final String cn_ChunkPrimer, jc_ChunkPrimer;
	final String cn_IBlockState, jc_IBlockState;
	final String cn_BlockPredicate;
	final Collection<String> mn_setBlockState;
	final String md_setBlockState;
	final String mn_disableBlock, md_disableBlock;
	final String fn_exclude, fd_exclude;

	public ChunkPrimerTransformer() {
		
		this.cn_ChunkPrimer = "net.minecraft.world.chunk.ChunkPrimer";
		this.jc_ChunkPrimer = cn_ChunkPrimer.replace('.', '/');
		this.cn_IBlockState = "net.minecraft.block.state.IBlockState";
		this.jc_IBlockState = cn_IBlockState.replace('.', '/');
		this.cn_BlockPredicate = "cd4017be.dimstack.api.util.BlockPredicate";
		this.mn_setBlockState = Arrays.asList("setBlockState", "func_177855_a", "func_177436_a", "func_175811_a"); //not sure which of these mappings is the correct one. Just try all of them ...
		this.md_setBlockState = "(IIIL" + jc_IBlockState + ";)V";
		this.mn_disableBlock = "disableBlock";
		this.md_disableBlock = "(L" + jc_ChunkPrimer + ";L" + jc_IBlockState + ";)V";
		this.fn_exclude = "ingnoredBlock";
		this.fd_exclude = "L" + jc_IBlockState + ";";
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (name.equals(cn_ChunkPrimer))
			return transformCP(basicClass);
		if (name.equals(cn_BlockPredicate))
			return transformAcc(basicClass);
		return basicClass;
	}

	private byte[] transformCP(byte[] data) {
		ClassNode cn = new ClassNode();
		ClassReader cr = new ClassReader(data);
		cr.accept(cn, 0);
		
		for (MethodNode mn : cn.methods)
			if (mn.desc.equals(md_setBlockState) && mn_setBlockState.contains(mn.name)) {
				InsnList inj = new InsnList();
				
				LabelNode end = new LabelNode();
				inj.add(new VarInsnNode(ALOAD, 4));
				inj.add(new JumpInsnNode(IFNULL, end));
				
				inj.add(new VarInsnNode(ALOAD, 0));
				inj.add(new FieldInsnNode(GETFIELD, jc_ChunkPrimer, fn_exclude, fd_exclude));
				inj.add(new VarInsnNode(ALOAD, 4));
				inj.add(new JumpInsnNode(IF_ACMPNE, end));
				
				inj.add(new InsnNode(RETURN));
				inj.add(end);
				inj.add(new FrameNode(F_SAME, 5, new Object[] {jc_ChunkPrimer, INTEGER, INTEGER, INTEGER, jc_IBlockState}, 0, new Object[0]));
				
				mn.instructions.insert(inj);
			}
		
		cn.fields.add(new FieldNode(ACC_PUBLIC, fn_exclude, fd_exclude, null, null));
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cn.accept(cw);
		return cw.toByteArray();
	}

	private byte[] transformAcc(byte[] data) {
		ClassNode cn = new ClassNode();
		ClassReader cr = new ClassReader(data);
		cr.accept(cn, 0);
		
		for (MethodNode mn : cn.methods)
			if (mn.name.equals(mn_disableBlock) && mn.desc.equals(md_disableBlock)) {
				InsnList inj = new InsnList();
				
				inj.add(new VarInsnNode(ALOAD, 0));
				inj.add(new VarInsnNode(ALOAD, 1));
				inj.add(new FieldInsnNode(PUTFIELD, jc_ChunkPrimer, fn_exclude, fd_exclude));
				inj.add(new InsnNode(RETURN));
				
				mn.instructions = inj;
			}
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cn.accept(cw);
		return cw.toByteArray();
	}

}