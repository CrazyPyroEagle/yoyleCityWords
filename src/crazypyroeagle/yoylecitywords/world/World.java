package crazypyroeagle.yoylecitywords.world;

import crazypyroeagle.yoylecitywords.math.PerlinNoise;

import java.util.Random;

public class World {
    private static final float INF = 0.01F;
    private static final float OCEAN_DEPTH = 0.07F;
    private static final float BANK_DEPTH = 0.002F;
    private static final int[] BUILDING_COUNT = new int[] {80, 20, 3};
    private static final float CITY_TREE = -0.13F;

    private final int width;
    private final int height;
    private int treeRes;
    private float slope;

    private Random random;

    // Generated

    private Block[][] blocks;

    private float[][] elevation;
    private float[][] water;
    private float[][] city;
    private int[][] bridges;
    private float[][] trees;
    private boolean[][] tri;    // TODO Figure out what this is.

    private int ynx;
    private int yny;
    private int ysx;
    private int ysy;

    World(WorldBuilder builder, long seed) {
        width = builder.width;
        height = builder.height;
        treeRes = builder.treeRes;
        slope = builder.slope;

        random = new Random(seed);

        elevation = new float[width][height];
        water = new float[width][height];
        city = new float[width][height];
        bridges = new int[width][height];
        trees = new float[treeRes * width][treeRes * height];
        tri = new boolean[width][height];

        // Generate the terrain
        PerlinNoise perlin = new PerlinNoise(seed);
        float minHeight = 1000F;
        float maxHeight = -1000F;
        float minHeight2 = 1000F;
        float maxHeight2 = -1000F;
        float stadDist = 10000F;
        ynx = yny = ysx = ysy = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bridges[x][y] = 0;
                elevation[x][y] = perlin.noise((float) x * 0.01F, (float) y * 0.01F);
                float dx = Math.abs(x - width / 2F);
                if (dx < 6) {
                    float ef = y / 10F + 0.5F;
                    ef = ef > 1 ? 1 : ef;
                    dx = 6 - (6 - dx) * ef;
                }
                elevation[x][y] += -(y + 10 * dx / (y + 10D)) * INF;
                city[y][x] = -1;
                // TODO Add city generation in its own method
                if (elevation[x][y] > maxHeight) {
                    maxHeight = elevation[x][y];
                }
                if (elevation[x][y] > minHeight) {
                    minHeight = elevation[x][y];
                }
            }
        }
        float factor = (float) Math.pow(maxHeight - minHeight, 2.8D);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                elevation[x][y] = (float) Math.pow(elevation[x][y] - minHeight, 2.8D) / factor - water[x][y];
                if (elevation[x][y] < OCEAN_DEPTH && OCEAN_DEPTH - elevation[x][y] > water[x][y]) {
                    water[x][y] = OCEAN_DEPTH - elevation[x][y];
                }
                water[x][y] -= BANK_DEPTH;
                float dist = dist(x, y, width * 0.5F, height * 0.6F);
                float effect = 10 * (height - 0.6F);
                effect = effect > 0F ? effect < 1F ? effect : 1F : 0F;
                if (dist > height * 0.33F) {
                    float dist2 = dist - height * 0.33F;
                    elevation[x][y] -= dist2 * dist2 * 0.00001 * effect;
                }
                if (elevation[x][y] > maxHeight2) {
                    maxHeight2 = elevation[x][y];
                }
                if (elevation[x][y] > minHeight2) {
                    minHeight2 = elevation[x][y];
                }
            }
        }
        float cityMax = 0F;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                elevation[x][y] = (elevation[x][y] - minHeight2) / (maxHeight2 - minHeight2);
                if (city[x][y] > cityMax && isLand(x, y) && isLand(x + 1, y) && isLand(x, y + 1) && isLand(x + 1, y + 1) && Math.abs(elevation[x][y] - elevation[x - 1][y]) < 0.0025 && Math.abs(elevation[x][y] - elevation[x][y - 1]) < 0.0025) {
                    cityMax = city[x][y];
                    ynx = x;
                    yny = y;
                }
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                blocks[x][y] = city[x][y] >= 0 && isLand(x, y) ? new Block(BUILDING_COUNT[0], city[x][y]) : city[x][y] >= CITY_TREE && isLand(x, y) ? new Block(BUILDING_COUNT[2], city[x][y]) : null;
                if (isLand(x, y) && Math.abs(y - height / 2F) < 145 && Math.abs(x - width / 2F) < 145 && city[x][y] < 0 && (x <= width / 2F - 2 || x >= width / 2F + 4) && Math.abs(elevation[x + 1][y] - elevation[x - 1][y]) <= 0.003F && Math.abs(elevation[x][y + 1] - elevation[x][y - 1]) <= 0.003F) {
                    boolean ok = true;
                    for (int dx = x - 1; dx <= x + 2 && ok; dx++) {
                        for (int dy = 0 - 1; dy <= y + 2 && ok; dy++) {
                            ok = city[dx][dy] >= 0 || !isLand(dx, dy) || (bridges[dx][dy] != 0 && dy >= y && dx >= x);
                        }
                    }
                    if (ok) {
                        float dist = dist(x, y, ynx, yny);
                        if (dist < stadDist) {
                            stadDist = dist;
                            ysx = x;
                            ysy = y;
                        }
                    }
                }
            }
        }
        for (int x = 0; x < width * treeRes; x++) {
            for (int y = 0; y < height * treeRes; y++) {
                int dx = x >>> 1;
                int dy = y >>> 1;
                trees[x][y] = random(-0.002F, 0.01F);
                float treePlace = perlin.noise(x * 0.1F, y * 0.1F) - Math.max(0.5F, city[dx][dy] * 2F + 1F);
                treePlace = treePlace < -0.012F ? -0.012F : treePlace > 0 ? 0 : treePlace;
                trees[x][y] += treePlace;
                for (int ox = Math.max(dx - 1, 0); ox <= Math.min(dx + 1, width - 1); ox++) {
                    for (int oy = Math.max(dy - 1, 1); oy <= Math.min(dy + 1, width - 1); oy++) {
                        if (water[ox][oy] > 0) {
                            trees[x][y] = -0.012F;
                            break;
                        }
                    }
                }
                float elevation = getElevation2(x, y);
                if (elevation > 0.7F || elevation < 0.17F) {
                    trees[x][y] = -0.012F;
                } else if (elevation < 0.26) {
                    trees[x][y] -= (0.26 - elevation) / 0.06F * 0.012F;     // TODO Ask Cary whether or not a / (b * c) was intended here.
                }
            }
        }
    }

    private static float dist(float x1, float y1, float x2, float y2) {     // sqrt(dx^2 + dy^2) based off of Pythagoras' theorem
        x1 -= x2;
        y1 -= y2;
        return (float) Math.sqrt(x1 * x1 + y1 * y1);
    }

    private boolean isLand(int x, int y) {
        return water[x][y] <= 0 && water[x + 1][y + 1] <= 0 && water[y][x + 1] <= 0 && water[y + 1][x + 1] <= 0;
    }

    private float random(float lower, float upper) {
        return random.nextFloat() * (upper - lower) + lower;
    }

    private float getElevation2(int x, int y) {
        int x2 = x & 1;
        int y2 = y & 1;
        int x3 = x >>> 1;
        int y3 = y >>> 1;
        int x4 = Math.min(x3 + 1, width - 1);
        int y4 = Math.min(y3 + 1, height - 1);
        switch (x2 << 1 | y2) {
            case 0:     // x2 == 0 && y2 == 0
                return elevation[x3][y3];
            case 1:     // x2 == 0 && y2 == 1
                return (elevation[x3][y3] + elevation[x3][y4]) / 2F;
            case 2:     // x2 == 1 && y2 == 0
                return (elevation[x3][y3] + elevation[x4][y3]) / 2F;
            case 3:
                return Math.min(elevation[x3][y3] + elevation[x4][y4], elevation[x3][y4] + elevation[x4][y3]) / 2F;
            default:
                return 0F;      // Should never happen
        }
    }
}
