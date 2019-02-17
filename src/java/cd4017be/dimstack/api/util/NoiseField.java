package cd4017be.dimstack.api.util;

import java.util.Arrays;

import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

/**
 * Handler that provides noise field data from a common noise generator for different height ranges in a chunk.<dl>
 * Use {@link #provideRange(int, int)} to register the height ranges, where you need noise data for.<br>
 * Or use {@link #provideLayer(int)} to register individual 2D planes in the noise field.
 * @author CD4017BE
 */
public class NoiseField {

	/** horizontal / vertical noise scale */
	public final double hScale, vScale;
	/** horizontal / vertical grid interval */
	public final int hGrid, vGrid;
	/** the noise generator */
	public NoiseGenerator gen;
	/** keeps track of which height regions require noise data */
	private int[] ranges;
	/** array of generated noise fields for different height regions */
	private double[][] fieldBuffers;
	/** Y-coordinate offset */
	private int offsetY = 0;

	/**
	 * @param hGrid horizontal grid interval in blocks (may be 1, 2, 4, 8 or 16)
	 * @param vGrid vertical grid interval in blocks (if <= 0 creates 2D field instead)
	 * @param hScale horizontal noise scale
	 * @param vScale vertical noise scale
	 */
	public NoiseField(int hGrid, int vGrid, double hScale, double vScale) {
		if (hGrid <= 0 || hGrid > 16 || (hGrid & -hGrid) != hGrid)
			throw new IllegalArgumentException("invalid grid interval!");
		this.hScale = hScale;
		this.vScale = vScale;
		this.hGrid = hGrid;
		this.vGrid = vGrid;
	}

	/**
	 * register the given range to be included in later generated noise field arrays
	 * @param y0 lower Y-bound (inclusive)
	 * @param y1 upper Y-bound (exclusive)
	 * @return this
	 */
	public NoiseField provideRange(int y0, int y1) {
		int g = vGrid;
		if (g <= 0) {
			if (ranges == null) provideLayer(0);
			return this;
		}
		if (y0 >= y1) throw new IllegalArgumentException("upper bound must be greater than lower bound!");
		y0 += offsetY;
		y1 += offsetY;
		if (g != 1) {
			y0 = Math.floorDiv(y0, g);
			y1 = Math.floorDiv(y1 - 1, g) + 2;
		}
		if (ranges == null) {
			ranges = new int[] {y0, y1};
			return this;
		}
		int i0 = segIndex(y0),
			i1 = segIndex(y1);
		int l = ranges.length, l1 = l - i1, l0 = i0 + (~i0 & 1) + (~i1 & 1);
		int[] na = new int[l0 + l1];
		if ((i0 & 1) == 0) na[i0] = y0;
		if ((i1 & 1) == 0) na[l0 - 1] = y1;
		System.arraycopy(ranges, 0, na, 0, i0);
		System.arraycopy(ranges, i1, na, l0, l1);
		ranges = na;
		return this;
	}

	/**
	 * register the given layer to be included in later generated noise field arrays.
	 * @param y Y-layer
	 * @return this
	 */
	public NoiseField provideLayer(int y) {
		int g = vGrid;
		if (g > 0) return provideRange(y, y + 1);
		y += offsetY;
		if (ranges == null) {
			ranges = new int[] {y};
			return this;
		}
		int l = ranges.length;
		ranges = Arrays.copyOf(ranges, l + 1);
		ranges[l] = y;
		return this;
	}

	/**
	 * @param ofsY new Y-offset in blocks by which to shift the underlying noise-field down
	 * @return this
	 */
	public NoiseField setOffsetY(int ofsY) {
		this.offsetY = ofsY;
		return this;
	}

	/**
	 * @param gen a new generator source for this noise field
	 * @return this
	 */
	public NoiseField setGenerator(NoiseGenerator gen) {
		this.gen = gen;
		return this;
	}

	/**
	 * @param y grid Y-coord (= blockY / vGrid)
	 * @return {@code (segment index) * 2 + 1} if inside segment or {@code (insert point) * 2} if not inside any segment
	 */
	private int segIndex(int y) {
		int i = Arrays.binarySearch(ranges, y);
		if (i >= 0) return i + 1;
		else return -1 - i;
	}

	/**
	 * generate noise data for the given chunk that can be acquired via {@link #getField(int)} afterwards.
	 * @param x chunk X-coord
	 * @param z chunk Z-coord
	 * @see #setGenerator(NoiseGeneratorOctaves)
	 */
	public void prepareFor(int x, int z) {
		final double hScale = this.hScale, yScale = this.vScale;
		final int g = hGrid, dh = g == 1 ? 16 : 16 / g + 1;
		x = (x << 4) / g; z = (z << 4) / g;
		boolean flat = vGrid <= 0;
		int[] ranges = this.ranges;
		if (ranges == null) return;
		int l = ranges.length >> (flat ? 0 : 1);
		double[][] fields = this.fieldBuffers;
		if (fields == null) this.fieldBuffers = fields = new double[l][];
		for (int i = 0; i < l; i++) {
			double[] f = fields[i];
			if (flat) {
				int y = ranges[i];
				if (gen instanceof NoiseGeneratorOctaves)
					f = ((NoiseGeneratorOctaves)gen).generateNoiseOctaves(f, x, y, z, dh, 1, dh, hScale, yScale, hScale);
				else if (gen instanceof NoiseGeneratorPerlin)
					f = ((NoiseGeneratorPerlin)gen).getRegion(f, x, z, dh, dh, hScale, hScale, yScale);
				else if (f == null) f = new double[dh * dh];
			} else {
				int y = ranges[i<<1], dy = ranges[i<<1 | 1] - y;
				if (gen instanceof NoiseGeneratorOctaves)
					f = ((NoiseGeneratorOctaves)gen).generateNoiseOctaves(f, x, y, z, dh, dy, dh, hScale, yScale, hScale);
				else if (f == null) f = new double[dh * dh * dy];
			}
			fields[i] = f;
		}
	}

	/**
	 * @param y block Y-coord
	 * @return the noise field index for given height
	 * @see #provideRange(int, int)
	 * @see #provideLayer(int)
	 */
	public int getIndex(int y) {
		y += offsetY;
		int g = vGrid;
		if (g <= 0) {
			int i = Arrays.binarySearch(ranges, y);
			if (i >= 0) return i;
			return Math.min(-i, ranges.length) - 1;
		}
		int i = segIndex(y / g);
		if ((i & 1) == 0) return -1;
		return i >> 1;
	}

	/**
	 * @param i noise field index as from {@link #getIndex(int)}
	 * @return the Y-coord at which the given noise field array starts
	 */
	public int getYOffset(int i) {
		int g = vGrid;
		return (g <= 0 ? ranges[i] : ranges[i << 1] * g) - offsetY;
	}

	/**
	 * @param i noise field index as from {@link #getIndex(int)}
	 * @return the noise field array for given index
	 * @see #getYOffset(int)
	 */
	public double[] getField(int i) {
		return fieldBuffers[i];
	}

	/**
	 * calls the given placer function for each block in the currently generating chunk with the corresponding noise field value.
	 * @param y0 minimum Y-level (inclusive)
	 * @param y1 maximum Y-level (exclusive)
	 * @param p block placer function
	 */
	public void generate(int y0, int y1, BlockPlacer p) {
		int idx = getIndex(y0);
		if (idx < 0) throw new IllegalStateException("height range not registered: " + y0 + " to " + y1);
		double[] f = getField(idx);
		if (vGrid > 0)
			interpolate3D(f, hGrid, vGrid, getYOffset(idx), y0, y1, p);
		else
			interpolate2D(f, hGrid, y0, y1, p);
	}

	/**
	 * interpolates the given noise field over a chunk and calls the given placer function for each block.
	 * @param field noise field in [x, z, y] order
	 * @param hGrid horizontal grid interval
	 * @param vGrid vertical grid interval
	 * @param yOffset noise field array Y-offset
	 * @param y0 minimum Y-level (inclusive)
	 * @param y1 maximum Y-level (exclusive)
	 * @param p block placer function
	 */
	public static void interpolate3D(double[] field, int hGrid, int vGrid, int yOffset, int y0, int y1, BlockPlacer p) {
		int Y0 = (y0 - yOffset) / vGrid;
		if (hGrid == 1) {
			int nY = field.length / 256, i = 0;
			for (int bx = 0; bx < 16; bx++)
				for (int bz = 0; bz < 16; bz++, i++) {
					int by = y0, y = (y0 - yOffset) % vGrid;
					for (int Y = Y0; by < y1; Y++, y = 0) {
						double f = field[i * nY + Y + 0];
						double d = (field[i * nY + Y + 1] - f) / (double)vGrid;
						for (; y < vGrid && by < y1; y++, by++) {
							p.place(bx, by, bz, f);
							f += d;
						}
					}
				}
		} else {
			int nh = 16 / hGrid, nH = nh + 1;
			int nY = field.length / nH / nH;
			for (int X = 0; X < nh; X++)
				for (int Z = 0; Z < nh; Z++) {
					int by = y0, y = (y0 - yOffset) % vGrid;
					for (int Y = Y0; by < y1; Y++, y = 0) {
						double f00 = field[((X + 0) * nH + Z + 0) * nY + Y + 0];
						double f01 = field[((X + 0) * nH + Z + 1) * nY + Y + 0];
						double f10 = field[((X + 1) * nH + Z + 0) * nY + Y + 0];
						double f11 = field[((X + 1) * nH + Z + 1) * nY + Y + 0];
						double d00 = (field[((X + 0) * nH + Z + 0) * nY + Y + 1] - f00) / (double)vGrid;
						double d01 = (field[((X + 0) * nH + Z + 1) * nY + Y + 1] - f01) / (double)vGrid;
						double d10 = (field[((X + 1) * nH + Z + 0) * nY + Y + 1] - f10) / (double)vGrid;
						double d11 = (field[((X + 1) * nH + Z + 1) * nY + Y + 1] - f11) / (double)vGrid;
						for (; y < vGrid && by < y1; y++, by++) {
							double f0 = f00;
							double f1 = f01;
							double d0 = (f10 - f00) / (double)hGrid;
							double d1 = (f11 - f01) / (double)hGrid;
							for (int x = 0; x < hGrid; ++x) {
								int bx = x + X * hGrid;
								double f = f0;
								double d = (f1 - f0) / (double)hGrid;
								for (int z = 0; z < hGrid; ++z) {
									int bz = z + Z * hGrid;
									p.place(bx, by, bz, f);
									f += d;
								}
								f0 += d0;
								f1 += d1;
							}
							f00 += d00;
							f01 += d01;
							f10 += d10;
							f11 += d11;
						}
					}
				}
		}
	}

	/**
	 * interpolates the given noise field over a chunk and calls the given placer function for each block.
	 * @param field noise field in [x, z] order
	 * @param hGrid horizontal grid interval
	 * @param y0 minimum Y-level (inclusive)
	 * @param y1 maximum Y-level (exclusive)
	 * @param p block placer function
	 */
	public static void interpolate2D(double[] field, int hGrid, int y0, int y1, BlockPlacer p) {
		if (hGrid == 1) {
			for (int i = 0, x = 0; x < 16; x++)
				for (int z = 0; z < 16; z++) {
					double f = field[i++];
					for (int y = y0; y < y1; y++)
						p.place(x, y, z, f);
				}
		} else {
			int nh = 16 / hGrid, nH = nh + 1;
			for (int X = 0; X < nh; X++)
				for (int Z = 0; Z < nh; Z++) {
					double f0 = field[(X + 0) * nH + Z + 0];
					double f1 = field[(X + 0) * nH + Z + 1];
					double d0 = (field[(X + 1) * nH + Z + 0] - f0) / (double)hGrid;
					double d1 = (field[(X + 1) * nH + Z + 1] - f1) / (double)hGrid;
					for (int x = 0; x < hGrid; ++x) {
						int bx = x + X * hGrid;
						double f = f0;
						double d = (f1 - f0) / (double)hGrid;
						for (int z = 0; z < hGrid; ++z) {
							int bz = z + Z * hGrid;
							for (int by = y0; by < y1; by++)
								p.place(bx, by, bz, f);
							f += d;
						}
						f0 += d0;
						f1 += d1;
					}
				}
		}
	}

}
