package antcolony;

/**
 * Output.java
 * Translated from out.cpp + out.h
 * Original by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import antcolony.constants.AcoVar;

public class Out {

    private static final DecimalFormat df = new DecimalFormat("0.00", 
            DecimalFormatSymbols.getInstance(Locale.US));

    /**
     * Prepares the output file header with instance name and algorithm settings
     */
    public static void prepareOutputFile(String fichIn, String fichOut, double scl_prm, double time_LR) {
        try (PrintWriter out = new PrintWriter(new FileWriter(fichOut, true))) {

            out.println();
            out.println(padRight(fichIn, 16));

            if (AcoVar.USE_HVIS) {
                out.println("Heuristic Visibility Used");
            } else {
                out.println("Heuristic Visibility NOT Used");
            }

            if (AcoVar.USE_CPLEX) {
                out.println("Cplex used in each iteration");
            } else {
                out.println("Cplex used in final iteration");
            }

            if (AcoVar.UPDATE_BEST) {
                out.println("Pheromone levels only updated for best solutions");
            } else {
                out.println("Pheromone levels updated for all solutions");
            }

            out.printf("%-15s %-15s%n", "SCAL_PARAM", AcoVar.SCALING_PARAMETER);
            out.println();

            out.print(padRight("NR_ITER", 9));
            out.print(padRight("NR_ANTS", 11));
            out.print(padRight("TAU0", 8));
            out.print(padRight("TAO", 8));
            out.print(padRight("BETA", 8));
            out.print(padRight("RHO", 9));
            out.print(padRight("GAMMA", 7));
            out.print(padRight("Q0", 9));
            out.print(padRight("scl_param", 11));
            out.print(padRight("tLR", 7));
            if (AcoVar.UPDATE_PARAM > 0) {
                out.print(padRight("UPDATE_PARAM", 17));
            }
            out.println();

            out.print(padRight(String.valueOf(AcoVar.NR_ITER), 9));
            out.print(padRight(String.valueOf(AcoVar.NR_ANTS), 11));
            out.print(padRight(df.format(AcoVar.TAU0), 8));
            out.print(padRight(df.format(AcoVar.TAO), 8));
            out.print(padRight(df.format(AcoVar.BETA), 8));
            out.print(padRight(df.format(AcoVar.RHO), 9));
            out.print(padRight(df.format(AcoVar.GAMMA), 7));
            out.print(padRight(df.format(AcoVar.Q0), 9));
            out.print(padRight(df.format(scl_prm), 11));
            out.print(padRight(df.format(time_LR), 7));
            if (AcoVar.UPDATE_PARAM > 0)  {
                out.print(padRight(String.valueOf(AcoVar.UPDATE_PARAM), 17));
            }
            out.println();

            out.println();
            out.print(padRight("Tot Nr It", 11));
            out.print(padRight("Best It", 12));
            out.print(padRight("vOpt", 14));
            out.print(padRight("tOpt", 14));
            out.print(padRight("vACO", 14));
            out.print(padRight("tACO", 14));
            out.print(padRight("totaltACO", 14));
            out.print(padRight("gap (%)", 14));
            out.println();

        } catch (IOException e) {
            System.err.println("Error writing to output file: " + fichOut);
            e.printStackTrace();
        }
    }

    /**
     * Appends one result line to the output file
     */
    public static void outputFile(int n_iter, int best_iteration, String fichOut,
                                  double vOpt, double tOpt,
                                  double vACO, double tACO, double totaltACO, double gap) {
        try (PrintWriter out = new PrintWriter(new FileWriter(fichOut, true))) {

            out.println();
            out.print(padRight(String.valueOf(n_iter), 11));
            out.print(padRight(String.valueOf(best_iteration), 12));
            out.print(padRight(df.format(vOpt), 14));
            out.print(padRight(df.format(tOpt), 14));
            out.print(padRight(df.format(vACO), 14));
            out.print(padRight(df.format(tACO), 14));
            out.print(padRight(df.format(totaltACO), 14));
            out.print(padRight(df.format(gap), 14));
            out.println();

        } catch (IOException e) {
            System.err.println("Error writing result to output file: " + fichOut);
            e.printStackTrace();
        }
    }

    // Helper: right-pad string to given width
    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}
