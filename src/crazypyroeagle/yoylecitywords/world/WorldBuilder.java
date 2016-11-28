package crazypyroeagle.yoylecitywords.world;

import java.util.Random;

public class WorldBuilder {
    final int width;
    final int height;
    final long seed;

    int treeRes;
    float slope;

    public WorldBuilder(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.seed = seed;

        treeRes = 2;
        slope = 120;
    }

    public World build() {
        return new World(this, seed);
    }
}
