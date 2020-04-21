package cd4017be.dimstack.util;

import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.util.math.ChunkPos;


/**
 * @author CD4017BE
 *
 */
public class ChunkIterator implements LongIterator {

	final int x0, x1, z1;
	int x, z;

	public ChunkIterator(int x0, int z0, int x1, int z1) {
		this.x0 = this.x = x0;
		this.x1 = x1;
		this.z = z0;
		this.z1 = z1;
	}

	@Override
	public boolean hasNext() {
		return z < z1;
	}

	@Override
	public Long next() {
		return nextLong();
	}

	@Override
	public long nextLong() {
		long p = ChunkPos.asLong(x, z);
		if (++x >= x1) {
			x = x0;
			z++;
		}
		return p;
	}

	@Override
	public int skip(int n) {
		n = Math.min(n, size());
		x += n - x0;
		int l = x1 - x0;
		z += x / l;
		x = x % l + x0; 
		return n;
	}

	public int size() {
		return x1 - x + (z1 - z - 1) * (x1 - x0);
	}

}
