package io.shardingsphere.shardingjdbc.yaml.userAlgo;


public class HashUtil {


    public static int consistentHash(long input, int buckets) {
        if (buckets <= 0) {
            throw new IllegalArgumentException("buckets must be positive");
        }
        LinearCongruentialGenerator generator = new LinearCongruentialGenerator(input);
        int candidate = 0;
        int next;

        while (true) {
            next = (int) ((candidate + 1) / generator.nextDouble());
            if (next >= 0 && next < buckets) {
                candidate = next;
            } else {
                return candidate;
            }
        }
    }

    private static final class LinearCongruentialGenerator {
        private long state;

        public LinearCongruentialGenerator(long seed) {
            this.state = seed;
        }

        public double nextDouble() {
            state = 2862933555777941757L * state + 1;
            return ((double) ((int) (state >>> 33) + 1)) / (0x1.0p31);
        }
    }
}
