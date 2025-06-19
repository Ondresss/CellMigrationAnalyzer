package cz.vsb.fei.cell_migration_back.utilities;

import java.security.SecureRandom;

public class RandomIDGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static long generateID() {
        return Math.abs(random.nextLong() % 1_000_000_000L);
    }

    public static void main(String[] args) {
        System.out.println(generateID());
        System.out.println(generateID());
    }
}