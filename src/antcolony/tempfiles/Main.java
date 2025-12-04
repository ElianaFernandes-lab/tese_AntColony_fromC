package antcolony.tempfiles;

import antcolony.ReadData;
import antcolony.ReadData.Data;

/**
 * Main.java
 * 
 * Created by Eliana Fernandes
 * Copyright (c) 2015 Eliana Fernandes. All rights reserved.
 * 
 * Translated to Java
 */

public class Main {
    
    public static void main(String[] args) {
        String fileIn;
        String fileOut;
        
        // All previous test cases are commented out in the original
        // Only the last one is active
        
        // for(int i=0; i<9; i++) {
            fileIn = "AP25LT_3_3.dat";
            fileOut = "out.txt";
            
            // TIME - Read data
            long tReaddata1 = 0, tReaddata2 = 0;
            boolean timeEnabled = System.getenv("TIME") != null;
            
            if (timeEnabled) {
                tReaddata1 = System.currentTimeMillis();
            }
            
            // Read data from file
            Data dat = ReadData.readData(fileIn);
            
            if (timeEnabled) {
                tReaddata2 = System.currentTimeMillis();
                double timeReaddata = (tReaddata2 - tReaddata1) / 1000.0;
                System.out.println("\nTIME readData: " + timeReaddata);
            }
            
            // TIME - Run ACO
            long tRunaco1 = 0, tRunaco2 = 0;
            
            if (timeEnabled) {
                tRunaco1 = System.currentTimeMillis();
            }
            
            // Run Ant Colony Optimization
            RunAco.runAco(dat, fileIn, fileOut);
            
            if (timeEnabled) {
                tRunaco2 = System.currentTimeMillis();
                double timeRunaco = (tRunaco2 - tRunaco1) / 1000.0;
                System.out.println("\nTIME runAco: " + timeRunaco);
            }
        // }
        
        System.out.println("\nPress Enter to continue...");
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}