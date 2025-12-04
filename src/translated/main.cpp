//
//  main.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2515 Eliana Fernandes. All rights reserved.
//

#include <iostream>
#include "runaco.h"

using namespace std;

///////// ///////// ///////// /////////          MAIN           ///////// ///////// ///////// /////////
int main()
{

	const char* fileIn;
	const char* fileOut;
#ifdef REP
	//for(int m=0;m<NR_REP;m++){
#endif
	//fileIn="AP20TT_1_1.dat";
	//fileIn="AP20TT_2_1.dat";
	//fileIn="AP20TT_2_2.dat";
	//fileIn="AP20TT_3_1.dat";
	//fileIn="AP20TT_3_2.dat";
	//fileIn="AP20TT_3_3.dat";

	//fileIn="AP20TL_1_1.dat";
	//fileIn="AP20TL_2_1.dat";
	//fileIn="AP20TL_2_2.dat";
	//fileIn="AP20TL_3_1.dat";
	//fileIn="AP20TL_3_2.dat";
	//fileIn="AP20TL_3_3.dat";

	//fileIn="AP20LL_1_1.dat";
	//fileIn="AP20LL_2_1.dat";
	//fileIn="AP20LL_2_2.dat";
	//fileIn="AP20LL_3_1.dat";
	//fileIn="AP20LL_3_2.dat";
	//fileIn="AP20LL_3_3.dat";

	//fileIn="AP20LT_1_1.dat";
	//fileIn="AP20LT_2_1.dat";
	//fileIn="AP20LT_2_2.dat";
	//fileIn="AP20LT_3_1.dat";
	//fileIn="AP20LT_3_2.dat";
	//fileIn="AP20LT_3_3.dat";
	
	fileOut="out.txt";
	//TIME
#ifdef TIME
	clock_t t_readdata1,t_readdata2;  // to get the elapsed time
	t_readdata1=clock();
#endif
	//read data from file
//	data dat=readData(fileIn);
#ifdef TIME
	t_readdata2=clock();
	double diff_readdata ((double)t_readdata2-(double)t_readdata1);
	double time_readdata;
	time_readdata = diff_readdata / CLOCKS_PER_SEC;
	cout <<endl<< "TIME readData: "<<time_readdata<<endl;
#endif

	//TIME
#ifdef TIME
	clock_t t_runaco1,t_runaco2;  // to get the elapsed time
	t_runaco1=clock();
#endif
	// run Ant Colony Optimization
	//runAco(dat,fileIn,fileOut);
#ifdef TIME
	t_runaco2=clock();
	double diff_runaco ((double)t_runaco2-(double)t_runaco1);
	double time_runaco;
	time_runaco = diff_runaco / CLOCKS_PER_SEC;
	cout << endl<<"TIME runAco: "<<time_runaco<<endl;
#endif
#ifdef REP
	//}
#endif
	//// delete
	for(int m=0;m<NR_REP;m++){
		fileIn="AP10TT_1_1.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
	for(int m=0;m<NR_REP;m++){
		fileIn="AP10TT_2_1.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
	for(int m=0;m<NR_REP;m++){
		fileIn="AP10TT_2_2.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
	for(int m=0;m<NR_REP;m++){
	fileIn="AP10TT_3_1.dat";
	fileOut="out.txt";
	//read data from file
data	dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
	for(int m=0;m<NR_REP;m++){
	fileIn="AP10TT_3_2.dat";
	fileOut="out.txt";
	//read data from file
data	dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
	for(int m=0;m<NR_REP;m++){
	fileIn="AP10TT_3_3.dat";
	fileOut="out.txt";
	//read data from file
data	dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10TL_1_1.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
	for(int m=0;m<NR_REP;m++){
	fileIn="AP10TL_2_1.dat";
	fileOut="out.txt";
	//read data from file
data	dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10TL_2_2.dat";
	fileOut="out.txt";
	//read data from file
data	dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10TL_3_1.dat";
	fileOut="out.txt";
	//read data from file
data	dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10TL_3_2.dat";
	fileOut="out.txt";
	//read data from file
data	dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10TL_3_3.dat";
	fileOut="out.txt";
	//read data from file
data	dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){

	fileIn="AP10LL_1_1.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10LL_2_1.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10LL_2_2.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10LL_3_1.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10LL_3_2.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10LL_3_3.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);
}
for(int m=0;m<NR_REP;m++){

	fileIn="AP10LT_1_1.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){

	fileIn="AP10LT_2_1.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10LT_2_2.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10LT_3_1.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10LT_3_2.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}
for(int m=0;m<NR_REP;m++){
	fileIn="AP10LT_3_3.dat";
	fileOut="out.txt";
	//read data from file
	data dat=readData(fileIn);
	// run Ant Colony Optimization
	runAco(dat,fileIn,fileOut);}

	///end delete


	cout << "\nPress any key to continue...\n";
	system ("pause");

	return 0;
	///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// /////////
}
