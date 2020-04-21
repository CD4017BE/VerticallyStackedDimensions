package cd4017be.dimstack.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import cd4017be.dimstack.util.ChunkIterator;
import cd4017be.lib.TickRegistry;
import cd4017be.lib.TickRegistry.ITickReceiver;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;


/**
 * command for manually regenerating certain world features
 * @author CD4017BE
 */
public class Regen extends CommandBase implements ITickReceiver {

	public static final HashMap<String, IWorldGenerator> generators = new HashMap<>();

	IWorldGenerator[] gens;
	ICommandSender sender;
	LongIterator chunks;
	int dim, processed, total;
	long t;

	@Override
	public String getName() {
		return "ds_regen";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.ds_regen.usage";
	}

	@Override
	public List<String> getTabCompletions(
		MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos
	) {
		int i = 1;
		switch(args.length) {
		case 0: return Collections.emptyList();
		case 1: return getListOfStringsMatchingLastWord(args, "help", "cancel", "~");
		case 2:
			List <String> list = getListOfStringsMatchingLastWord(args, "loaded");
			list.addAll(getTabCompletionCoordinateXZ(args, i, targetPos));
			return list;
		case 4: case 5: i = 3;
		case 3:
			if (!args[1].equals("loaded")) return getTabCompletionCoordinateXZ(args, i, targetPos);
		default: return getListOfStringsMatchingLastWord(args, generators.keySet());
		}
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) throw new WrongUsageException("commands.ds_regen.usage");
		String s = args[0];
		if (s.equals("help")) {
			sender.sendMessage(new TextComponentTranslation("commands.ds_regen.info", generators.keySet()));
			return;
		}
		if (s.equals("cancel")) {
			chunks = null;
			gens = null;
			return;
		}
		if (args.length < 3) throw new WrongUsageException("commands.blockrepl.usage");
		if (this.sender != null || chunks != null) throw new CommandException("commands.ds_regen.running");
		World world;
		if (s.equals("~")) world = sender.getEntityWorld();
		else world = server.getWorld(parseInt(s));
		if (world == null) throw new CommandException("commands.ds_regen.invalid_dim");
		int i = 1;
		if (args[i].equals("loaded")) {
			i++;
			long[] a = ((WorldServer)world).getChunkProvider().id2ChunkMap.keySet().toLongArray();
			total = a.length;
			chunks = LongIterators.wrap(a);
		} else if (args.length < 6) throw new WrongUsageException("commands.blockrepl.usage");
		else {
			BlockPos pos = sender.getPosition();
			chunks = new ChunkIterator(
				(int)parseDouble(pos.getX(), args[i++], false) >> 4,
				(int)parseDouble(pos.getZ(), args[i++], false) >> 4,
				(int)parseDouble(pos.getX(), args[i++], false) + 15 >> 4,
				(int)parseDouble(pos.getZ(), args[i++], false) + 15 >> 4
			);
			total = ((ChunkIterator)chunks).size();
			if (total < 0) total = 0;
		}
		gens = new IWorldGenerator[args.length - i];
		for (int j = 0; i < args.length; i++, j++) {
			if ((gens[j] = generators.get(args[i])) == null) {
				chunks = null;
				gens = null;
				throw new CommandException("commands.ds_regen.invalid_gen", s);
			}
		}
		this.sender = sender;
		processed = 0;
		dim = world.provider.getDimension();
		TickRegistry.instance.add(this);
	}

	@Override
	public boolean tick() {
		if (sender == null) return false;
		if (chunks == null) {
			sender.sendMessage(new TextComponentTranslation("commands.ds_regen.cancel", processed, total));
			sender = null;
			gens = null;
			return false;
		}
		long t = System.currentTimeMillis();
		if (t - this.t > 1000) {
			sender.sendMessage(new TextComponentTranslation("commands.ds_regen.progress", processed, total, total == 0 ? 100 : processed * 100 / total));
			this.t = t;
		}
		World world = sender.getServer().getWorld(dim);
		IChunkProvider provider = world.getChunkProvider();
		long worldSeed = world.getSeed();
		Random rand = new Random(worldSeed);
		long xSeed = rand.nextLong() >> 2 + 1L;
		long zSeed = rand.nextLong() >> 2 + 1L;
		do {
			if (!chunks.hasNext()) {
				sender.sendMessage(new TextComponentTranslation("commands.ds_regen.complete"));
				sender = null;
				chunks = null;
				gens = null;
				return false;
			}
			processed++;
			long p = chunks.nextLong();
			int x = (int)p, z = (int)(p >> 32);
			if (!provider.isChunkGeneratedAt(x, z)) continue;
			long chunkSeed = (xSeed * x + zSeed * z) ^ worldSeed;				
			for (IWorldGenerator gen : gens) {
				rand.setSeed(chunkSeed);
				gen.generate(rand, x, z, world, null, provider);
			}
		} while(System.currentTimeMillis() - t < 50); //lets be friendly to the tickrate
		return true;
	}

}
