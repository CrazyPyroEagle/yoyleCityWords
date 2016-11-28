package crazypyroeagle.yoylecitywords.math;

import java.util.Random;

public class PerlinNoise {
    private final float[] perlin;
    private static final float[] perlin_cosTable = new float[720];
    private final int perlin_PI;
    private final int perlin_TWOPI;
    private Random perlinRandom;

    static {
        for (int index = 0; index < 720; index++) {
            perlin_cosTable[index] = (float) Math.cos((double) ((float) index * 0.017453292F * 0.5F));
        }
    }

    public PerlinNoise() {
        this.perlin = new float[4096];
        for(int xi = 0; xi < 4096; ++xi) {
            this.perlin[xi] = this.perlinRandom.nextFloat();
        }
        this.perlin_TWOPI = 720;
        this.perlin_PI = 360;
        this.perlinRandom = new Random();
    }

    public PerlinNoise(long seed) {
        this();
        perlinRandom.setSeed(seed);
    }

    public float noise(float x) {
        return this.noise(x, 0.0F, 0.0F);
    }

    public float noise(float x, float y) {
        return this.noise(x, y, 0.0F);
    }

    public float noise(float x, float y, float z) {
        int xi;
        if(x < 0.0F) {
            x = -x;
        }

        if(y < 0.0F) {
            y = -y;
        }

        if(z < 0.0F) {
            z = -z;
        }

        xi = (int)x;
        int yi = (int)y;
        int zi = (int)z;
        float xf = x - (float)xi;
        float yf = y - (float)yi;
        float zf = z - (float)zi;
        float r = 0.0F;
        float ampl = 0.5F;

        int perlin_octaves = 4;
        for(int i = 0; i < perlin_octaves; ++i) {
            int of = xi + (yi << 4) + (zi << 8);
            float rxf = this.noise_fsc(xf);
            float ryf = this.noise_fsc(yf);
            float n1 = this.perlin[of & 4095];
            n1 += rxf * (this.perlin[of + 1 & 4095] - n1);
            float n2 = this.perlin[of + 16 & 4095];
            n2 += rxf * (this.perlin[of + 16 + 1 & 4095] - n2);
            n1 += ryf * (n2 - n1);
            of += 256;
            n2 = this.perlin[of & 4095];
            n2 += rxf * (this.perlin[of + 1 & 4095] - n2);
            float n3 = this.perlin[of + 16 & 4095];
            n3 += rxf * (this.perlin[of + 16 + 1 & 4095] - n3);
            n2 += ryf * (n3 - n2);
            n1 += this.noise_fsc(zf) * (n2 - n1);
            r += n1 * ampl;
            float perlin_amp_falloff = 0.5F;
            ampl *= perlin_amp_falloff;
            xi <<= 1;
            xf *= 2.0F;
            yi <<= 1;
            yf *= 2.0F;
            zi <<= 1;
            zf *= 2.0F;
            if(xf >= 1.0F) {
                ++xi;
                --xf;
            }

            if(yf >= 1.0F) {
                ++yi;
                --yf;
            }

            if(zf >= 1.0F) {
                ++zi;
                --zf;
            }
        }

        return r;
    }

    private float noise_fsc(float i) {
        return 0.5F * (1.0F - perlin_cosTable[(int)(i * (float)this.perlin_PI) % this.perlin_TWOPI]);
    }
}
