//
//  main.cpp
//
//  Created by Eliana Fernandes
//  Copyright (c) 2015 Eliana Fernandes. All rights reserved.
//

#include <iostream>
#include "runaco.h"

using namespace std;

///////// ///////// ///////// /////////          MAIN           ///////// ///////// ///////// /////////
int main()
{

	const char* fileIn;
	const char* fileOut;

//	for(int i=0;i<9;i++){
//		fileIn="AP25TT_1_1.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25TT_2_1.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25TT_2_2.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25TT_3_1.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25TT_3_2.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25TT_3_3.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25TL_1_1.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25TL_2_1.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25TL_2_2.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25TL_3_1.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25TL_3_2.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25TL_3_3.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25LL_1_1.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25LL_2_1.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25LL_2_2.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25LL_3_1.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25LL_3_2.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25LL_3_3.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25LT_1_1.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25LT_2_1.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25LT_2_2.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25LT_3_1.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
//	for(int i=0;i<9;i++){
//		fileIn="AP25LT_3_2.dat";
//		fileOut="out.txt";
//		//TIME
//#ifdef TIME
//		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
//		t_readdata1=clock();
//#endif
//		//read data from file
//		data dat=readData(fileIn);
//#ifdef TIME
//		t_readdata2=clock();
//		double diff_readdata ((double)t_readdata2-(double)t_readdata1);
//		double time_readdata;
//		time_readdata = diff_readdata / CLOCKS_PER_SEC;
//		cout <<endl<< "TIME readData: "<<time_readdata<<endl;
//#endif
//
//		//TIME
//#ifdef TIME
//		clock_t t_runaco1,t_runaco2;  // to get the elapsed time
//		t_runaco1=clock();
//#endif
//		// run Ant Colony Optimization
//		runAco(dat,fileIn,fileOut);
//#ifdef TIME
//		t_runaco2=clock();
//		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
//		double time_runaco;
//		time_runaco = diff_runaco / CLOCKS_PER_SEC;
//		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
//#endif
//	}
//
//
	//for(int i=0;i<9;i++){
		fileIn="AP25LT_3_3.dat";
		fileOut="out.txt";
		//TIME
#ifdef TIME
		clock_t t_readdata1,t_readdata2;  // to get the elapsed time
		t_readdata1=clock();
#endif
		//read data from file
		data dat=readData(fileIn);
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
		runAco(dat,fileIn,fileOut);
#ifdef TIME
		t_runaco2=clock();
		double diff_runaco ((double)t_runaco2-(double)t_runaco1);
		double time_runaco;
		time_runaco = diff_runaco / CLOCKS_PER_SEC;
		cout << endl<<"TIME runAco: "<<time_runaco<<endl;
#endif
//	}


	cout << "\nPress any key to continue...\n";
	system ("pause");

	return 0;
	///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// ///////// /////////
}
