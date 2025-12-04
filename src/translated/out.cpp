//
//  out.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include "out.h"

int prepareOutputFile(const char *fichIn, const char *fichOut,double scl_prm,double time_LR) {
    // CHANGE
	//ofstream outFile(fichOut,ios::app);
    ofstream outFile;
    outFile.open (fichOut,ios::app);
    outFile.precision(2);
	outFile.setf(ios::fixed);
	outFile << endl;
    outFile << setw(16) << fichIn << endl;
#ifdef USE_HVIS
    outFile << endl<< "Heuristic Visibility Used" << setw(16) << endl;
#endif
#ifndef USE_HVIS
	outFile << endl<< "Heuristic Visibility NOT Used" << setw(16) << endl;
#endif
#ifdef USE_CPLEX
    outFile << endl<< "Cplex used in each iteration" << setw(16) << endl;
#endif
#ifndef USE_CPLEX
	outFile << endl<< "Cplex used in final iteration" << setw(16) << endl;
#endif
#ifdef UPDATE_BEST
    outFile << endl<< "Pheromone levels only updated for best solutions" << setw(16) << endl;
#endif
#ifndef UPDATE_BEST
	outFile << endl<< "Pheromone levels updated for all solutions" << setw(16) << endl;
#endif
	outFile << setw(15) << "SCAL_PARAM" << setw(15) << SCALING_PARAMETER << endl;
    outFile << endl;
	outFile << setw(9) << "NR_ITER" << setw(11) << "NR_ANTS";
    outFile << setw(8) << "TAU0"<< setw(8) << "TAO" << setw(8) << "BETA";
	outFile << setw(9) << "RHO" << setw(7) << "GAMMA";
    outFile << setw(9) << "Q0" << setw(11) << "scl_param"<<setw(7) << "tLR";
#ifndef UPDATE_PARAM
	outFile << endl;
#endif
#ifdef UPDATE_PARAM
	outFile<< setw(17) << "UPDATE_PARAM" <<endl;
#endif
    outFile << setw(9) << NR_ITER << setw(11) << NR_ANTS;
    outFile << setw(8) << TAU0 << setw(8) << TAO << setw(8) << BETA;
	outFile << setw(9) << RHO << setw(7) << GAMMA;
    outFile << setw(9) << Q0;
    outFile.unsetf(ios::fixed);
    outFile << setw(11) <<scl_prm<<setw(7) << time_LR;
#ifndef UPDATE_PARAM
	outFile << endl;
#endif
#ifdef UPDATE_PARAM
	outFile<< setw(17) << UPDATE_PARAM<<endl;
#endif
    outFile.setf(ios::fixed);
    outFile << endl;
	outFile << setw(11) << "Tot Nr It";
	outFile << setw(12) << "Best It";
	outFile << setw(14) << "vOpt" << setw(14) << "tOpt";
    outFile << setw(14) << "vACO" << setw(14) << "tACO" << setw(14) << "totaltACO";
	outFile << setw(14) << "gap (%)" << endl;
	outFile.close();
    
	return 0;
} // prepareOutputFile(const char *fichIn, const char *fichOut)

int OutputFile(int n_iter, int best_iteration, const char *fichOut,double vOpt, double tOpt, double vACO, double tACO, double totaltACO, double gap){
	ofstream outFile(fichOut,ios::app);
    outFile.precision(2);
    outFile.setf(ios::fixed);
    outFile << endl;
	outFile << setw(11) << n_iter;
	outFile << setw(12) << best_iteration;
	outFile << setw(14) << vOpt << setw(14) << tOpt;
    outFile << setw(14) << vACO << setw(14) << tACO << setw(14) << totaltACO;
	outFile << setw(14) << gap << endl;
	outFile.close();
    
	return 0;
} //  OutputFile(const char *fichOut,double vOpt, double tOpt, double vACO, double tACO, double totaltACO, double gap)