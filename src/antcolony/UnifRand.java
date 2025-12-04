package antcolony;

/**
 * UnifRand.java
 * Uniform [0,1) random number generator
 * Translated from unifrand.cpp + unifrand.h
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
import java.util.Random;

public final class UnifRand {

    // Thread-safe, high-quality random generator (same as C++ rand() behavior if seeded)
    private static final Random rand = new Random();

    // Optional: set seed for reproducibility (like srand(time(NULL)) in C++)
    static {
        rand.setSeed(System.currentTimeMillis());
        // Or use fixed seed for debugging:
        // rand.setSeed(12345L);
    }

    /**
     * Returns a random double uniformly distributed in [0.0, 1.0)
     * Exactly equivalent to C++: rand() / double(RAND_MAX)
     */
    public static double next() {
        return rand.nextDouble();
    }

    /**
     * Convenience method — same as next()
     */
    public static double unifRand() {
        return next();
    }

    /**
     * Optional: reseed with current time (like C++ srand(time(NULL)))
     */
    public static void reseed() {
        rand.setSeed(System.currentTimeMillis());
    }

    /**
     * Set fixed seed for reproducible runs (useful for benchmarking)
     */
    public static void setSeed(long seed) {
        rand.setSeed(seed);
    }

    // Prevent instantiation
    private UnifRand() {}
}