#include <ilcplex/ilocplex.h>
#include <time.h>
#include <malloc.h>

#define LOG

ILOSTLBEGIN

typedef struct {
	double x;
	double y;
} point;

typedef IloArray<IloNumArray>		doubleMatrix;
typedef IloArray<IloNumVarArray>	NumVarMatrix;
typedef IloArray<NumVarMatrix>		NumVar3DMatrix;
typedef IloArray<NumVar3DMatrix>	NumVar4DMatrix;
typedef IloArray<NumVar4DMatrix>    NumVar5DMatrix;

// Function headers
int generateData();
void prepareAndGenerate(const char *,int,int);
void generateOneInstance(const char *,int,point *,double **,double *,double *,double,double,double,int,int);
int solveData();
int solve(const char *, const char *);
int prepareOutputFile(const char *, const char *);
int solveModel_P1(const char *, const char *);


//Macros for random numbers
//uniform in [0,1]
#define myrand() 1.0*rand()/RAND_MAX
//discrete uniform in [min,max]
#define getrandom_d(min,max)  (min) + ((int)(myrand()*((max)-(min))))   
//continuous uniform in [min, max]
#define getrandom_c(min,max)  (min) + (myrand()*((max)-(min)))
#define MAX(x,y) ((x) > (y) ? (x): (y))


/*  ==================================================================================
	ESTE PROGRAMA FOI MODIFICADO PARA INCLUIR O CALCULO DO LIMITE
	NO NODO RAIZ
	TRATA_SE DO PROGRAMA MODELOS_ENVIADOS_FRANCISCO COM ESTA ALTERAÇÃO
	================================================================================== */





/*  ==================================================================================
	Rotina MAIN
	================================================================================== */
int main() {

	solveData();
	//generateData();

    cout << "\nPrima um tecla\n";
	system ("pause");
	
	return 0;
} // main()


/*  ==================================================================================
	Rotina solveData
	================================================================================== */
int solveData() {

	//const char* fileIn = "AP10TT_3_2.dat";
	//const char* fileOut = "output.dat";
	//solve(fileIn,fileOut);

	const char* fileIn;
	const char* fileOut;
	
	fileOut="output.dat";



 	//fileIn="exemplo1_NETS.dat";
	 //   prepareOutputFile(fileIn,fileOut);
	 //   solveModel_P1(fileIn,fileOut);  
 	//fileIn="exemplo2_NETS.dat";
	 //   prepareOutputFile(fileIn,fileOut);
	 //   solveModel_P1(fileIn,fileOut);  
 	//fileIn="exemplo3_NETS.dat";
	 //   prepareOutputFile(fileIn,fileOut);
	 //   solveModel_P1(fileIn,fileOut);  

 


 
 	fileIn="AP10TT_1_1.dat";
	    prepareOutputFile(fileIn,fileOut);
	    solveModel_P1(fileIn,fileOut);
    fileIn="AP10TT_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
	    solveModel_P1(fileIn,fileOut);
 	fileIn="AP10TT_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10TT_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10TT_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10TT_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    

 	fileIn="AP10TL_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10TL_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP10TL_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10TL_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10TL_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10TL_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);

 	fileIn="AP10LL_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10LL_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP10LL_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10LL_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10LL_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10LL_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);

 	fileIn="AP10LT_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10LT_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP10LT_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10LT_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10LT_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP10LT_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);



 	fileIn="AP20TT_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20TT_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP20TT_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20TT_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20TT_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20TT_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    

 	fileIn="AP20TL_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20TL_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP20TL_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20TL_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20TL_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20TL_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);

 	fileIn="AP20LL_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20LL_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP20LL_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20LL_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20LL_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20LL_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);


 	fileIn="AP20LT_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20LT_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP20LT_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20LT_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20LT_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP20LT_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);



	fileIn="AP25TT_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25TT_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP25TT_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25TT_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25TT_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25TT_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    

 	fileIn="AP25TL_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25TL_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP25TL_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25TL_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25TL_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25TL_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);

 	fileIn="AP25LL_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25LL_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP25LL_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25LL_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25LL_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25LL_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);


 	fileIn="AP25LT_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25LT_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP25LT_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25LT_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25LT_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP25LT_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);

 	fileIn="AP40TT_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40TT_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP40TT_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40TT_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40TT_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40TT_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    

 	fileIn="AP40TL_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40TL_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP40TL_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40TL_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40TL_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40TL_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);

 	fileIn="AP40LL_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40LL_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP40LL_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40LL_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40LL_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40LL_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);


 	fileIn="AP40LT_1_1.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40LT_2_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
 	fileIn="AP40LT_2_2.dat";
		prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40LT_3_1.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40LT_3_2.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);
    fileIn="AP40LT_3_3.dat";
	    prepareOutputFile(fileIn,fileOut);
		solveModel_P1(fileIn,fileOut);	


	// Ficheiros para o problema multi-produto
	/*  
	solve("AP10TL_1_1.dat",fileOut);
	solve("AP10TL_2_1.dat",fileOut);
	solve("AP10TL_2_2.dat",fileOut);
	solve("AP10TL_3_1.dat",fileOut);
	solve("AP10TL_3_2.dat",fileOut);
	solve("AP10TL_3_3.dat",fileOut);
	solve("AP10TT_1_1.dat",fileOut);
	solve("AP10TT_2_1.dat",fileOut);
	solve("AP10TT_2_2.dat",fileOut);
	solve("AP10TT_3_1.dat",fileOut);
	solve("AP10TT_3_2.dat",fileOut);
	solve("AP10TT_3_3.dat",fileOut);
	solve("AP20TL_1_1.dat",fileOut);
	solve("AP20TL_2_1.dat",fileOut);
	solve("AP20TL_2_2.dat",fileOut);
	solve("AP20TL_3_1.dat",fileOut);
	solve("AP20TL_3_2.dat",fileOut);
	solve("AP20TL_3_3.dat",fileOut);
	solve("AP20TT_1_1.dat",fileOut);
	solve("AP20TT_2_1.dat",fileOut);
	solve("AP20TT_2_2.dat",fileOut);
	solve("AP20TT_3_1.dat",fileOut);
	solve("AP20TT_3_2.dat",fileOut);
	solve("AP20TT_3_3.dat",fileOut);
	solve("AP25TL_1_1.dat",fileOut);
	solve("AP25TL_2_1.dat",fileOut);
	solve("AP25TL_2_2.dat",fileOut);
	solve("AP25TL_3_1.dat",fileOut);
	solve("AP25TL_3_2.dat",fileOut);
	solve("AP25TL_3_3.dat",fileOut);
	solve("AP25TT_1_1.dat",fileOut);
	solve("AP25TT_2_1.dat",fileOut);
	solve("AP25TT_2_2.dat",fileOut);
	solve("AP25TT_3_1.dat",fileOut);
	solve("AP25TT_3_2.dat",fileOut);
	solve("AP25TT_3_3.dat",fileOut);
	solve("AP40TL_1_1.dat",fileOut);
	solve("AP40TL_2_1.dat",fileOut);
	solve("AP40TL_2_2.dat",fileOut);
	solve("AP40TL_3_1.dat",fileOut);
	solve("AP40TL_3_2.dat",fileOut);
	solve("AP40TL_3_3.dat",fileOut);
	solve("AP40TT_1_1.dat",fileOut);
	solve("AP40TT_2_1.dat",fileOut);
	solve("AP40TT_2_2.dat",fileOut);
	solve("AP40TT_3_1.dat",fileOut);
	solve("AP40TT_3_2.dat",fileOut);
	solve("AP40TT_3_3.dat",fileOut);
	solve("AP50TL_1_1.dat",fileOut);
	solve("AP50TL_2_1.dat",fileOut);
	solve("AP50TL_2_2.dat",fileOut);
	solve("AP50TL_3_1.dat",fileOut);
	solve("AP50TL_3_2.dat",fileOut);
	solve("AP50TL_3_3.dat",fileOut); 
    solve("AP50TT_1_1.dat",fileOut);
	solve("AP50TT_2_1.dat",fileOut);
	solve("AP50TT_2_2.dat",fileOut);
	solve("AP50TT_3_1.dat",fileOut);
	solve("AP50TT_3_2.dat",fileOut);
	solve("AP50TT_3_3.dat",fileOut);
	*/ 

	return 0;
} // solveData()


/*  ==================================================================================
	Rotina prepareOutputFile
	================================================================================== */
int prepareOutputFile(const char *fichIn, const char *fichOut) {

	ofstream outFile(fichOut,ios::app);
	outFile.setf(ios::fixed);
	outFile << endl;
	outFile << setw(16) << fichIn;
	outFile << setw(14) << "vOptLR" << setw(14) << "timeLR";
	outFile << setw(14) << "vOpt" << setw(14) << "tOpt";
	outFile << setw(14) << "gap RL (%)" << endl; 
	outFile.close();

	return 0;
} // prepareOutputFile(const char *fichIn, const char *fichOut) 




/*  ==================================================================================
	Rotina solveModel_P1
	//  Restricoes n. minimo serviços 
	================================================================================== */
int solveModel_P1(const char *fichIn, const char *fichOut) {

	IloEnv 	env;

	// Problem parameters
	int		nbNodes,nbProducts;
	point	*coordenadas;
	double	***w,**d,*g,**f,**Gamma,**O,**D;
	int		*L;
	double	*chi,*delta,*alpha;

	// Counters
	int		i,j,k,l,p,nz;

	// Auxiliar variables
	double	aux, auxd;

	// Cplex results
	double	vOpt, tOpt;
	double	vOpt_LR, tOpt_LR;

	// Variables for the enhanced models
	int	qLim;
	int	q;
	int R,maxR;
	double	prodDemand;
	double	totalP,maxGamma;
	int		*check;
	int		checkL,sumR;
	int		index;
	double	sumO;

	// Concert timer
	IloTimer timer(env);

	// Log file if LOG is defined
#ifdef LOG
	const char* fLog = "output.log";
#endif

	// Input and output files
	ifstream inFile(fichIn);
	ofstream outFile(fichOut,ios::app);
	outFile.close();

	// Log file if LOG is defined
#ifdef LOG
	ofstream logFile(fLog,ios::app);
	logFile.setf(ios::fixed);
	logFile << setw(12) << fichIn << endl;
	logFile.close();
#endif

	// Check input file
	if ( !inFile ) {
		cerr << "Input file could not be opened\n";
		system ("pause");
		exit(1);
	}

	// Read number of nodes and number of products
	inFile >> nbNodes >> nbProducts;

	// Memory allocation
	coordenadas = (point *) malloc(sizeof(point)*nbNodes);
	w = (double ***) malloc(sizeof(double)*nbNodes*nbNodes*nbProducts);
	for (i=0; i<nbNodes; ++i) {
		w[i]=(double **) malloc(sizeof(double)*nbNodes*nbProducts);
		for (j=0; j<nbNodes; ++j)
			w[i][j]=(double *) malloc(sizeof(double)*nbProducts);
	}
	chi = (double *) malloc (sizeof(double)*(nbProducts));
	alpha = (double *) malloc (sizeof(double)*(nbProducts));
	delta = (double *) malloc (sizeof(double)*(nbProducts));
	d = (double **) malloc(sizeof(double)*nbNodes*nbNodes);
		for (i=0; i<nbNodes; ++i) 
			d[i]=(double *) malloc(sizeof(double)*nbNodes);
	g = (double *) malloc (sizeof(double)*(nbNodes));
	L = (int *) malloc (sizeof(int)*(nbNodes));
	f = (double **) malloc(sizeof(double)*nbNodes*nbProducts);
		for (i=0; i<nbNodes; ++i) 
			f[i]=(double *) malloc(sizeof(double)*nbProducts);
	Gamma = (double **) malloc(sizeof(double)*nbNodes*nbProducts);
		for (i=0; i<nbNodes; ++i) 
			Gamma[i]=(double *) malloc(sizeof(double)*nbProducts);
	O = (double **) malloc(sizeof(double)*nbNodes*nbProducts);
		for (i=0; i<nbNodes; ++i) 
			O[i]=(double *) malloc(sizeof(double)*nbProducts);
	D = (double **) malloc(sizeof(double)*nbNodes*nbProducts);
		for (i=0; i<nbNodes; ++i) 
			D[i]=(double *) malloc(sizeof(double)*nbProducts);

	// Read coordinates
	for (i=0; i<nbNodes; i++)
		inFile >> coordenadas[i].x >> coordenadas[i].y;

	// Calculate distances
	for (i=0; i<nbNodes; i++)
		for (j=0; j<nbNodes; j++) { 
			aux=(coordenadas[i].x-coordenadas[j].x)*(coordenadas[i].x-coordenadas[j].x);
			aux+=(coordenadas[i].y-coordenadas[j].y)*(coordenadas[i].y-coordenadas[j].y);
			d[i][j]=sqrt(aux)/1000.0;
		}

	// Read flows
	for (p=0; p<nbProducts; ++p)
		for (i=0; i<nbNodes; ++i)
			for (j=0; j<nbNodes; ++j)
				inFile >> w[i][j][p];

	// Read collection, discount and distribution costs
	for (p=0; p<nbProducts; ++p) {
		inFile >> chi[p];
	}
	for (p=0; p<nbProducts; ++p) 
		inFile >> alpha[p];
	for (p=0; p<nbProducts; ++p) {
		inFile >> delta[p];
	} 
	// Read set-up costs
	for (i=0; i<nbNodes; i++){
		inFile >> g[i];
	}

	// Read maximum numbder of products that can be handled in each node
	for (i=0; i<nbNodes; i++)
		inFile >> L[i];

	// Read capacities
	for (i=0; i<nbNodes; i++)
		for (p=0; p<nbProducts; ++p)
			inFile >> Gamma[i][p];
	  
	// Read set up cots per product
	for (i=0; i<nbNodes; i++){
		for (p=0; p<nbProducts; ++p) {
			inFile >> f[i][p];
		}
	}

// ATENCAO    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//	Depois de lidos os g[i] nos ficheiros de dados, eles devem ser transformados de acordo com as instrucoes a seguir. So assim ficam de acordo
//    com o que vem no paper do Networks and Spatial Economics
//	Isto é necessario porque depois de ter gerado os dados foi necessario alterar apenas os g[i].

//	A rotina que a seguir se apresenta corresponde ao modelo PEK+(17)+(20) do paper
//  É gerado um ficheiro output.log com informação de controle.
//  É também gerado um ficheiro output.out com a informação sintetizada: valor optimo da relaxação linear e respectivo tempo optimo, valor optimo
//	do problema e respectivo tempo e gap




	// Update the g[i] values according with the paper
	for (i=0; i<nbNodes; i++){
		g[i]=0.0;
		for (p=0; p<nbProducts; ++p) 
			g[i] = g[i] + f[i][p];
		g[i]= 2*g[i]/(nbProducts*1.0);
	}



	// Close file for reading
	inFile.close();

	// Write data if LOG defined
#ifdef LOG
	logFile.open(fLog,ios::app);
	logFile.setf(ios::fixed);
	logFile << setw(4) << nbNodes << setw(4) << nbProducts <<endl;
	for (i=0; i<nbNodes; i++) {
		logFile << setw(15) << setprecision(6) << coordenadas[i].x << "  ";
		logFile << setw(15) << setprecision(6) << coordenadas[i].y << endl;
	}
	for (p=0; p<nbProducts; ++p)
		for (i=0; i<nbNodes; ++i) {
			for (j=0; j<nbNodes; ++j)
				logFile << setw(15) << setprecision(6) << w[i][j][p] << " ";
			logFile << endl;
		}
	for (p=0; p<nbProducts; ++p)
		logFile << setw(15) << setprecision(6) << chi[p] << " ";
	logFile << endl;
	for (p=0; p<nbProducts; ++p)
		logFile << setw(15) << setprecision(6) << alpha[p] << " ";
	logFile << endl;
	for (p=0; p<nbProducts; ++p)
		logFile << setw(15) << setprecision(6) << delta[p] << " ";
	logFile << endl;
	for (i=0; i<nbNodes; ++i)
		logFile << setw(15) << setprecision(6) << g[i] << endl;
	for (i=0; i<nbNodes; i++)
		logFile << setw(4) << L[i] << endl;
	for (i=0; i<nbNodes; ++i) {
		for (p=0; p<nbProducts; ++p)
			logFile << setw(15) << setprecision(6) << f[i][p] << " ";
		logFile << endl;
	}
	for (i=0; i<nbNodes; i++) {
		for (p=0; p<nbProducts; ++p)
			logFile << setw(15) << setprecision(6) << Gamma[i][p];
		logFile << endl;
	}
	logFile << endl;
	// Closing file
	logFile.close();
#endif

	// Calculate total flows originated at the nodes
	for (i=0; i<nbNodes; ++i)
		for (p=0; p<nbProducts; ++p) {
			O[i][p]=0.0;
			for (j=0; j<nbNodes; ++j) 
				O[i][p]+=w[i][j][p];
	}

	// Calculate total flows destined to the nodes
	for (i=0; i<nbNodes; ++i)
		for (p=0; p<nbProducts; ++p) {
			D[i][p]=0.0;
			for (j=0; j<nbNodes; ++j) 
				D[i][p]+=w[j][i][p];
	}

	// Variables x_{ik}^p
	NumVar3DMatrix x(env,nbNodes);
	for (i=0; i<nbNodes; i++) {
		x[i] = NumVarMatrix (env,nbNodes);
		for (k=0; k<nbNodes; ++k) {
			x[i][k] = IloNumVarArray(env, nbProducts, 0, 1, ILOFLOAT);
		}
	}

	// Variables z
	IloNumVarArray z(env,nbNodes,0,1,ILOFLOAT);

	// Variables y_{ikl}^p
	NumVar4DMatrix y(env,nbNodes);
	for (i=0; i<nbNodes; i++) {
		y[i] = NumVar3DMatrix (env,nbNodes);
		for (k=0; k<nbNodes; k++) {
			y[i][k] = NumVarMatrix (env,nbNodes);
			for (l=0; l<nbNodes; ++l)
				y[i][k][l] = IloNumVarArray(env, nbProducts, 0, IloInfinity, ILOFLOAT);
		}
	}

	// Ambiente CPLEX
	IloCplex cplex(env);

	// CPLEX parameters
	cplex.setParam(IloCplex::TiLim, 21600);
	cplex.setParam(IloCplex::ClockType,  1);   // CPU time is measured
	//cplex.setParam(IloCplex::ParallelMode, 1);
//	cplex.setParam(IloCplex::EpGap, 0.00001);
//	cplex.setParam(IloCplex::EpInt, 0.00001);
	// cplex.setParam(IloCplex::NumericalEmphasis, 1);
	// tolerance = cplex.getParam(IloCplex::EpInt); // Get current tolerance defined
	
	/* --------------------------------------------------------------
	   Build and solve model P1
	   -------------------------------------------------------------- */
	try {

		IloModel model_P1(env);

		// Single-allocation
		for (i=0; i<nbNodes; i++)
			for (p=0; p<nbProducts; ++p) {
				IloExpr v(env);
				for (k=0; k<nbNodes; ++k)
					v+=x[i][k][p];
				model_P1.add(v == 1);
				v.end();
			}

		// Non-hub nodes can only be allocated to open hubs
		for (i=0; i<nbNodes; i++) 
			for (k=0; k<nbNodes; k++) 
				for (p=0; p<nbProducts; ++p)
					model_P1.add(x[i][k][p] <= x[k][k][p]);

		// Maximum number of products handled in each location
		for (k=0; k<nbNodes; ++k) {
			IloExpr v(env);
			for (p=0; p<nbProducts; ++p)
				v+=x[k][k][p];
			model_P1.add(v <= ((double)L[k]*z[k]) );
			v.end();
		}

		// Flow divergence
		for (p=0; p<nbProducts; ++p)		
			for (i=0; i<nbNodes; i++)
				for (k=0; k<nbNodes; k++) {
					IloExpr v1(env);
					IloExpr v2(env);
					for (l=0; l<nbNodes; l++)
						v1 += y[i][k][l][p];
					for (l=0; l<nbNodes; l++)
						v1 -= y[i][l][k][p];
					v2 += O[i][p]*x[i][k][p];
					for (j=0; j<nbNodes; j++)
						v2 -= w[i][j][p]*x[j][k][p];
					model_P1.add(v1 == v2);
					v1.end();
					v2.end();
				}

		// Missing cuts
		for (i=0; i<nbNodes; i++)
			for (k=0; k<nbNodes; k++)
				for (p=0; p<nbProducts; ++p) {
					IloExpr v(env);
					for (l=0; l<nbNodes; ++l) {
						if (l != k) v+=y[i][k][l][p];
					}
					model_P1.add(v <= O[i][p]*x[i][k][p]);
				}

		// Enhanced capacity constraints
		for (k=0; k<nbNodes; k++) {
			for (p=0; p<nbProducts; ++p) {
			IloExpr v(env);
			for (i=0; i<nbNodes; ++i) 
					v += ((double)O[i][p])*x[i][k][p];
			model_P1.add(v <= Gamma[k][p]*x[k][k][p]);
			v.end();
			}
		}

		// Minimum number of hubs to open for each category
		// Minimum number of hubs to open
		check = (int *) malloc (sizeof(int)*nbNodes);
		maxR=0;
		sumR=0;
		for (p=0; p<nbProducts; p++) {
			prodDemand=0.0;
			for(i=0; i<nbNodes; i++) {
				prodDemand+=O[i][p];
				check[i]=0;
			}
			R=0;
			totalP=0.0;
			do {
				maxGamma=0.0;
				for (i=0; i<nbNodes; i++)
//
// Na instrucao abaixo deveriamos juntar O[i][p]<Gamma[i][p] pois caso contrario
// no local i nao pode ser aberto um hub do tipo p e nao devemos contar com ele
//
					if ((check[i]==0 && Gamma[i][p]>maxGamma)&&(O[i][p]<Gamma[i][p])) {
						maxGamma = Gamma[i][p];
						index = i;
					}
				totalP+=maxGamma;
				check[index]=1;
				++R;
			} while (totalP < prodDemand);
			IloExpr v(env);
			for (k=0; k<nbNodes; k++)
				v+=x[k][k][p];
			model_P1.add (v >= R);
			v.end();
			maxR = MAX(maxR,R);
			sumR+=R;
		}
		free(check);
		// We now check if all the Lk's are equal or not for using the appropriate inequality
		checkL=1;
		for (k=1; k<nbNodes; ++k)
			if (L[k]!=L[0]) {
				checkL=0;
				break;
			}
		// If not all the Lk's are equal
		if (checkL==0)
			model_P1.add ( IloSum(z) >= maxR);
		// If all the Lk's are equal
		else {
			model_P1.add ( IloSum(z) >= MAX(maxR,ceil(1.0*sumR/L[0])) );
		}


		// Objective function
		IloExpr obj(env);
		for (p=0; p<nbProducts; ++p)
			for (i=0;  i<nbNodes; i++)
				for (k=0; k<nbNodes; k++) {
					obj += (d[i][k]*(chi[p]*O[i][p]+delta[p]*D[i][p])*x[i][k][p]);
			}
		for (p=0; p<nbProducts; ++p) 
			for (i=0;  i<nbNodes; i++)
				for (k=0; k<nbNodes; k++)
					for (l=0; l<nbNodes; l++) {
						obj += (alpha[p] * d[k][l] * y[i][k][l][p]);
				}
		for (k=0; k<nbNodes; k++)
			obj += (g[k]*z[k]);
		for (p=0; p<nbProducts; ++p)
			for (k=0; k<nbNodes; k++)
				obj += (f[k][p]*x[k][k][p]);

		model_P1.add(IloMinimize(env,obj));
		obj.end();
		
		cplex.extract(model_P1);
		cplex.exportModel("P1_LR.lp");

		// Turn off displays on screen when CPLEX runs
		//cplex.setOut(env.getNullStream());


		timer.restart();
		cplex.solve();
		timer.stop();

		vOpt_LR=cplex.getObjValue();
		tOpt_LR=timer.getTime();

//		// Write in the output file
		outFile.open(fichOut,ios::app);
		outFile.setf(ios::fixed);
		outFile << "  P1" << "                 ";
		outFile << setw(10) << setprecision(2) << vOpt_LR << "  ";
		outFile << setw(12) << setprecision(2) << tOpt_LR << "  "; 
		outFile.close();

#ifdef LOG
//		// Write in the log file
		logFile.open(fLog,ios::app);
		logFile.setf(ios::fixed);
		logFile << "MODEL P1 - LINEAR RELAXATION" << endl;
		logFile << "Optimal value : ";
		logFile << setw(7) << setprecision(2) << vOpt_LR << endl;
		logFile << "CPU           : ";
		logFile << setw(7) << setprecision(2) << tOpt_LR << " seconds" << endl;
		logFile.close();
#endif

		model_P1.add(IloConversion(env, z, ILOINT));
		for (i=0; i<nbNodes; ++i)
			for (k=0; k<nbNodes; ++k)
				model_P1.add(IloConversion(env, x[i][k], ILOINT));
	
		cplex.extract(model_P1);
		cplex.exportModel("P1_INT.lp") ;
		timer.restart();
		cplex.solve();
		timer.stop();

		vOpt=cplex.getObjValue();
		auxd=cplex.getBestObjValue();
		tOpt=timer.getTime();

		// Write in the output file
		outFile.open(fichOut,ios::app);
		outFile.setf(ios::fixed);
        //outFile << setw(12) << setprecision(2) << cplex.getBestObjValue() << "  ";
		outFile << setw(12) << setprecision(2) << vOpt << "  ";
		outFile << setw(12) << setprecision(2) << tOpt << "  ";
		nz=0;
		for (k=0; k<nbNodes; k++)
			if ( (int)floor(0.5+cplex.getValue(z[k])) == 1)
				nz=nz+1;
        outFile << setw(12) << nz << "  ";
//		outFile << setw(12) << setprecision(2) << 100.0*(vOpt-vOpt_LR)/vOpt << "  ";
//		outFile << setw(12) << setprecision(3) << 100.0*cplex.getMIPRelativeGap() << "  ";
//      outFile << cplex.getNnodes() << "  ";
		outFile << endl;
		outFile.close();

#ifdef LOG
		// Write in the log file
		logFile.open(fLog,ios::app);
		logFile.setf(ios::fixed);
		logFile << endl;
		logFile << "MODEL P1" << endl;
		logFile << "Optimal value : ";
		logFile << setw(7) << setprecision(2) << vOpt << endl;
		logFile << "CPU           : ";
		logFile << setw(7) << setprecision(2) << tOpt << " seconds" << endl;
		logFile << endl;
		logFile << "Variables z equal to 1: " << endl;
		for (k=0; k<nbNodes; k++)
			if ( (int)floor(0.5+cplex.getValue(z[k])) == 1)
				logFile << "z[" << k+1 << "]" << endl;
		logFile << endl;
		logFile << "Variables x equal to 1: " << endl;
		for (i=0; i<nbNodes; i++)
			for (k=0; k<nbNodes; k++) 
				for (p=0; p<nbProducts; ++p) {
					if ( (int)floor(0.5+cplex.getValue(x[i][k][p])) == 1)
						logFile << "x[" << i+1 << "][" << k+1 << "][" << p+1 << "]" << endl;
				}
		logFile << endl;
		logFile << "Variables y: " << endl;
		for (i=0; i<nbNodes; i++) 
			for (k=0; k<nbNodes; k++) 
				for (l=0; l<nbNodes; l++) 
					for(p=0; p<nbProducts; ++p) {
						if (l != k) {
							if ( cplex.getValue(y[i][k][l][p]) > 0.000001) {
								logFile << "y[" << i+1 << "][" << k+1 << "][" << l+1 << "][" << p+1 << "] = ";
								logFile << setw(7) << setprecision(2) << cplex.getValue(y[i][k][l][p]) << endl ;
							}
						}
					}
		logFile << endl;
		logFile.close();
#endif

		cplex.clearModel();
		model_P1.end();
	
	}
	catch (IloException& e) {
		timer.stop();
		cerr << " Error: " << e << endl;
		outFile.open(fichOut,ios::app);
		outFile << "model P1               " << e ;
		outFile.close();
		cplex.clearModel();
	}
	catch (...) {
		timer.stop();
		cerr << " Error: " << endl;
		outFile.open(fichOut,ios::app);
		outFile << "model P1               " << "Error" ;
		outFile.close();
		cplex.clearModel();
	}

	// Free memory
	free(coordenadas);
	for (i=0; i<nbNodes; ++i) 
		for (j=0; j<nbNodes; ++j)
			free(w[i][j]);
	free(chi);
	free(alpha);
	free(delta);
	for (i=0; i<nbNodes; ++i) 
			free(d[i]);
	free(g);
	free(L);
	for (i=0; i<nbNodes; ++i) 
			free(f[i]);
	for (i=0; i<nbNodes; ++i) 
			free(Gamma[i]);
	for (i=0; i<nbNodes; ++i) 
			free(O[i]);
	for (i=0; i<nbNodes; ++i) 
			free(D[i]);

	env.end();

	return 0;

} // solveModel_P1(const char *fichIn, const char *fichOut)




/*  ===================================================================================
	Routine "generateData"
	Generates data for the CSAHLP
	=================================================================================== */
int generateData() {
 
	int	nbProducts,paramL;
	const char* dataIn;

	dataIn = "AP10TL.dat";
	nbProducts=1;
	paramL=1;
	prepareAndGenerate(dataIn,nbProducts,paramL);
	nbProducts=2;
	paramL=1;
	prepareAndGenerate(dataIn,nbProducts,paramL);
	paramL=2;
	prepareAndGenerate(dataIn,nbProducts,paramL);
	nbProducts=3;

	paramL=1;
	prepareAndGenerate(dataIn,nbProducts,paramL);
	paramL=2;
	prepareAndGenerate(dataIn,nbProducts,paramL);
	paramL=3;
	prepareAndGenerate(dataIn,nbProducts,paramL); 

	return 0;
} // generateData()

/*  ===================================================================================
	Routine "prepareANDgenerate"
	Prepares the information for generating one instance of the CSAHLP
	=================================================================================== */
void prepareAndGenerate(const char *fich, int nbProducts, int paramL) {

	// Problem AP parameters
	int		nbNodes;
	point	*coordenadas;
	double	**wAP,*fAP,*GammaAP;
	double	chiAP,deltaAP,alphaAP;

	// counters
	int		i,j;

	// READ AP DATA 
	ifstream inFile(fich);
	if ( !inFile ) {
		cerr << "Input file could not be opened\n";
		system ("pause");
		exit(1);
	}

	// Read number of nodes and number of products
	inFile >> nbNodes;

	// Memory allocation
	if ( !(coordenadas = (point *) malloc(sizeof(point)*nbNodes))) goto EXIT;
	if ( !(wAP = (double **) malloc(sizeof(double)*nbNodes*nbNodes))) goto EXIT;
	for (i=0; i<nbNodes; ++i) 
		if ( !(wAP[i]=(double *) malloc(sizeof(double)*nbNodes))) goto EXIT;
	if ( (fAP = (double *) malloc (sizeof(double)*(nbNodes)))==NULL) goto EXIT;
	if ( (GammaAP = (double *) malloc (sizeof(double)*(nbNodes)))==NULL) goto EXIT;

	// Read coordinates
	for (i=0; i<nbNodes; i++)
		inFile >> coordenadas[i].x >> coordenadas[i].y;
	// Read flows
	for (i=0; i<nbNodes; ++i)
		for (j=0; j<nbNodes; ++j)
			inFile >> wAP[i][j];
	// Read collection, discount and distribution costs
	inFile >> chiAP >> alphaAP >> deltaAP;
	// Read capacities
	for (i=0; i<nbNodes; i++)
		inFile >> GammaAP[i];
	// Read set up costs
	for (i=0; i<nbNodes; i++)
		inFile >> fAP[i];
	// Close file for reading
	inFile.close();

	// Generate data
	generateOneInstance(fich,nbNodes,coordenadas,wAP,fAP,GammaAP,chiAP,alphaAP,deltaAP,nbProducts,paramL);

	// Free memory
	free(coordenadas);
	for (i=0; i<nbNodes; ++i) 
		free(wAP[i]);
	free(fAP);
	free(GammaAP);

	return;

EXIT: 
	cerr << "Error in memory allocation\n";
	system ("pause");
	exit(1);
} // prepareAndGenerate(const char *fich, int nbProducts, int paramL)


/*  ===================================================================================
	Routine "generateOneInstance"
	Generates one instance of the CSAHLP
	=================================================================================== */
void generateOneInstance(const char *fich, int nbNodes, point *coordenadas,
		 double **wAP, double *fAP, double *GammaAP, 
		 double chiAP, double alphaAP, double deltaAP, 
		 int nbProducts, int paramL) {

	// Variable for file name
	char fData[14];
	int nChar;

	// Factors
	double gFactor=1.5;
	double factorMin=0.05;
	double factorMax=0.10;

	// Auxiliary variables
	double	avg;

	// counters
	int		i,j,k,p;

	// Problem parameters
	int		*L;
	double	***w,*g,**f,**Gamma;
	double	*chi,*delta,*alpha;

	// Memory allocation
	w = (double ***) malloc(sizeof(double)*nbNodes*nbNodes*nbProducts);
	for (i=0; i<nbNodes; ++i) {
		w[i]=(double **) malloc(sizeof(double)*nbNodes*nbProducts);
		for (j=0; j<nbNodes; ++j)
			w[i][j]=(double *) malloc(sizeof(double)*nbProducts);
	}
	chi = (double *) malloc (sizeof(double)*(nbProducts));
	alpha = (double *) malloc (sizeof(double)*(nbProducts));
	delta = (double *) malloc (sizeof(double)*(nbProducts));
	g = (double *) malloc (sizeof(double)*(nbNodes));
	L = (int *) malloc (sizeof(int)*(nbNodes));
	f = (double **) malloc(sizeof(double)*nbNodes*nbProducts);
		for (i=0; i<nbNodes; ++i) 
			f[i]=(double *) malloc(sizeof(double)*nbProducts);
	Gamma = (double **) malloc(sizeof(double)*nbNodes*nbProducts);
		for (i=0; i<nbNodes; ++i) 
			Gamma[i]=(double *) malloc(sizeof(double)*nbProducts);

	// Give name ot the file
	strcpy_s (fData,fich); 
	nChar = strlen (fData);
	nChar += sprintf_s(fData+nChar-4,10,"_%d_%d.dat",nbProducts,paramL);
	
	// Generate parameters
	for (i=0; i<nbNodes; ++i) {
		for (k=0; k<nbNodes; ++k)
			w[i][k][0]=wAP[i][k];
		f[i][0]=fAP[i];
		Gamma[i][0]=GammaAP[i];
	}
	chi[0]=chiAP;
	alpha[0]=alphaAP;
	delta[0]=deltaAP;

	// Randomize seed for random numbers generation
	srand( (unsigned) time(NULL) );

	for (p=1; p<nbProducts; ++p) 
		for (i=0; i<nbNodes; ++i) {
			for (k=0; k<nbNodes; ++k) {
				w[i][k][p]=w[i][k][p-1]*(1+(float)getrandom_c(factorMin,factorMax));
			}
			f[i][p]=f[i][p-1]*(1+(float)getrandom_c(factorMin,factorMax));
			Gamma[i][p]=Gamma[i][p-1]*(1+(float)getrandom_c(factorMin,factorMax));
		}	
	for (i=0; i<nbNodes; ++i) {
		avg=0.0;
		for (p=0; p<nbProducts; ++p)
			avg+= (double)(1.0/nbProducts)*f[i][p];
		g[i]=gFactor*avg;
	}
	if (paramL==1)
		for (i=0; i<nbNodes; ++i)
			L[i]=1;
	else if (paramL==2 && nbProducts>2)
		for (i=0; i<nbNodes; ++i)
			L[i]=(int)floor(0.5+nbProducts/2.0);
	else if (paramL==2 && nbProducts==2)
		for (i=0; i<nbNodes; ++i)
			L[i]=2;
	else if (paramL==3)
		for (i=0; i<nbNodes; ++i)
			L[i]=nbProducts;
	for (p=1; p<nbProducts; ++p) {
		chi[p]=chi[p-1];
		alpha[p]=alpha[p-1];
		delta[p]=delta[p-1];
	}

	// Write data
	ofstream outFile(fData);
	outFile.setf(ios::fixed);
	outFile << setw(4) << nbNodes << setw(4) << nbProducts <<endl;
	for (i=0; i<nbNodes; i++) {
		outFile << setw(15) << setprecision(6) << coordenadas[i].x << "  ";
		outFile << setw(15) << setprecision(6) << coordenadas[i].y << endl;
	}
	for (p=0; p<nbProducts; ++p)
		for (i=0; i<nbNodes; ++i) {
			for (j=0; j<nbNodes; ++j)
				outFile << setw(15) << setprecision(6) << w[i][j][p] << " ";
			outFile << endl;
		}
	for (p=0; p<nbProducts; ++p)
		outFile << setw(15) << setprecision(6) << chi[p] << " ";
	outFile << endl;
	for (p=0; p<nbProducts; ++p)
		outFile << setw(15) << setprecision(6) << alpha[p] << " ";
	outFile << endl;
	for (p=0; p<nbProducts; ++p)
		outFile << setw(15) << setprecision(6) << delta[p] << " ";
	outFile << endl;
	for (i=0; i<nbNodes; ++i)
		outFile << setw(15) << setprecision(6) << g[i] << endl;
	for (i=0; i<nbNodes; i++)
		outFile << setw(4) << L[i] << endl;
	for (i=0; i<nbNodes; i++) {
		for (p=0; p<nbProducts; ++p)
			outFile << setw(15) << setprecision(6) << Gamma[i][p];
		outFile << endl;
	}
	for (i=0; i<nbNodes; ++i) {
		for (p=0; p<nbProducts; ++p)
			outFile << setw(15) << setprecision(6) << f[i][p] << " ";
		outFile << endl;
	}
	// Closing file
	outFile.close();

		// Free memory
	for (i=0; i<nbNodes; ++i) 
		for (j=0; j<nbNodes; ++j)
			free(w[i][j]);
	free(g);
	for (i=0; i<nbNodes; ++i) 
			free(f[i]);
	for (i=0; i<nbNodes; ++i) 
			free(Gamma[i]);
	free(L);

	return;
} // generateOneInstance()


