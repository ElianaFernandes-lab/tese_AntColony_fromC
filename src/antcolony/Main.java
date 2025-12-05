package antcolony;

/**
 * Main.java
 * Translated from main.cpp
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
import java.util.Scanner;

import antcolony.ReadData.Data;
import antcolony.constants.AcoVar;
import antcolony.tempfiles.RunAco;

public class Main {

    public static void main(String[] args) {
        String fileIn;
        String fileOut = "out.txt";

        // For multiple repetitions if REP is enabled
        int nrRep = AcoVar.REP ? AcoVar.NR_REP : 1;

        // List of instances to run (from the repeated blocks in original)
        String[] instances = {
                "AP10TT_1_1.dat", "AP10TT_2_1.dat", "AP10TT_2_2.dat", "AP10TT_3_1.dat", "AP10TT_3_2.dat", "AP10TT_3_3.dat",
                "AP10TL_1_1.dat", "AP10TL_2_1.dat", "AP10TL_2_2.dat", "AP10TL_3_1.dat", "AP10TL_3_2.dat", "AP10TL_3_3.dat",
                "AP10LL_1_1.dat", "AP10LL_2_1.dat", "AP10LL_2_2.dat", "AP10LL_3_1.dat", "AP10LL_3_2.dat", "AP10LL_3_3.dat",
                "AP10LT_1_1.dat", "AP10LT_2_1.dat", "AP10LT_2_2.dat", "AP10LT_3_1.dat", "AP10LT_3_2.dat", "AP10LT_3_3.dat"
                // Add more from original if needed, e.g., AP20* instances
        };

        for (String instance : instances) {
            fileIn = instance;

            for (int m = 0; m < nrRep; m++) {
                long t_readdata1 = 0, t_readdata2 = 0;
                if (AcoVar.TIME) {
                    t_readdata1 = System.nanoTime();
                }

                // Read data from file
                Data dat = ReadData.readData(fileIn);

                if (AcoVar.TIME) {
                    t_readdata2 = System.nanoTime();
                    double time_readdata = (t_readdata2 - t_readdata1) / 1_000_000_000.0;
                    System.out.println("TIME readData: " + time_readdata);
                }

                long t_runaco1 = 0, t_runaco2 = 0;
                if (AcoVar.TIME) {
                    t_runaco1 = System.nanoTime();
                }

                // Run Ant Colony Optimization
                RunAco.runAco(dat, fileIn, fileOut);

                if (AcoVar.TIME) {
                    t_runaco2 = System.nanoTime();
                    double time_runaco = (t_runaco2 - t_runaco1) / 1_000_000_000.0;
                    System.out.println("TIME runAco: " + time_runaco);
                }
            }
        }

        System.out.println("\nPress Enter to continue...");
        new Scanner(System.in).nextLine();
    }
}
