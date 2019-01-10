/**
 * 	siccom -- Spatial Interaction in Coral Reef Communities 
 * 	Simulation of a virtual coral reef where corals and algae compete for space.
 *  In the beginning corals and algae are initialized and placed randomly on the simulation area.
 *	Both grow with distinct growth rates which can be altered in the course of interaction with other simulation objects.
 *  Both recruit at certain time intervals 	-- corals every 12 month, algae every 6 month.  
 *  Algae fragtate if they reach a certain height and can produce 0 to 2 fragments per cycle. 
 *  If they fragtate their height is reduced by 1. If they reach a certain age they die.
 *  Disturbance occurs in random time intervals with random sizes. Then all corals and algae within a certain distance of the disturbance center die at once.
 */


package siccom.sim;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Double2D;
import ec.util.MersenneTwisterFast;

public class Siccom extends SimState 
{
	/**
	 * This is the class which contains the main model 
	 * 
	 * @author Andreas Kubicek
	 * @version 2.0
	 */
	private static final long serialVersionUID = -3342324L;
	
	/*
	 * VARIABLES	
	 */

	/**
	 * This simulation
	 */
	Siccom sim = this;		
		
	static long startTime;
	/**
	 * The count for steps (month)
	 */
	private int steps;
	/**
	 * The configuration class in which parameters are read into the program and coral groups are set up
	 */
	Config conf;
	/**
	 * 
	 */
	public static boolean gui = false;						// IF true --> the simulation is started from the GUI

	// Main variables for the simulation field
	public double discretization = 10.0;
	
	public static double areaWidth;			// the simulation area width in meters
	public static double areaHeight;			// the simulation area height in meters
	public static double resolution; 			// how many centimeters per pixel
	public static double meterConv;			// converter from meters to pixels
	public static double growthConv;			// converter from mm/year to pixels/month
	public static double gridWidth;			// the simulation area width in pixels
	public static double gridHeight;			// the simulation area height in pixels
	public static double totalArea;			// = gridWidth * gridHeight

	
	// TEMPERATURE
	public Temperature temp;
	public double overTempPerDay;


	// ArrayLists for groups -- for initialisation
	/**
	 * List of {@link MassiveGroup}s
	 */
	public ArrayList<MassiveGroup> maCoGroups;
	/**
	 * List of {@link BranchingGroup}s
	 */
	public ArrayList<BranchingGroup> braCoGroups;

	
	// Hash tables for corals, algae
	/**
	 * The 2D-Layer for {@link MassiveCoral}s of all Massive Coral Groups
	 */
	public Continuous2D massiveCorals;
	/**
	 * The 2D-Layer for {@link BranchingCoral}s of all Branching Coral Groups
	 */
	public Continuous2D branchingCorals;
	/**
	 * The 2D-Layer for {@link Alga} 
	 */
	public Continuous2D algae;
	
	 
	// CORALS -- GENERAL
	/**
	 * The maximum radius at initialisation of a coral
	 */
	private final double cInitMaxR = 40.0;
	/**
	 * The maximum radius of a massive coral 
	 * -- initialized with the initial maximum radius
	 */
	public double maCoMaxLength = cInitMaxR;
	/**
	 * The maximum radius of a branching coral 
	 * -- initialized with the initial maximum radius
	 */
	public double braCoMaxLength = cInitMaxR;
	
	/** 
	 * The number of {@link MassiveGroup}s
	 */
	public static int maCoGroupNum;
	/**
	 * The number of {@link BranchingGroup}s
	 */
	public static int braCoGroupNum;

	// Parameter files to read
	/**
	 * The parameter file from which to read the parameters
	 * for corals, algae, and the environment, respectively
	 */
	public ParameterFile currentParameterFile = null;

	
	// ALGAE 
	// unlike corals algae are handled as a whole and so their 
	// variable are not stored in a Group class but directly in the main Program		-- maybe needs to be changed
	/**
	 * The number of algal individuals
	 */
	public int numAlgae = 0;
	/**
	 * The fraction of the total area covered by algae 
	 */
	public double algalCoverPercent;					
	

	// GRAZING
	// to define the probability for an alga to be grazed within one time step
	/**
	 * The initial probability for an alga to be grazed
	 */
	public double iniGrazingProb;
	/**
	 * To determine upper and lower boundary of the grazing probability at a given time step
	 */
	public double grazingProbHalfRange=0.04;
	/**
	 * The probability for an alga to be grazed whithin one time step
	 * -- is computed for each time step in relation to algal cover --> see grazing()
	 */
	public double grazingProb;
	/**
	 * The maximum grazing probability
	 */
	public double maxGrazingProb;
	/**
	 * The minimum grazing probability
	 */
	public double minGrazingProb;
	/**
	 * The initial critical threshold of algal cover fraction
	 * -- can be used to adjust the fraction of algal cover
	 */
	public double iniAlgalThreshold;
	/**
	 * The actual critical threshold of algal cover fraction
	 * -- calculated in relation to available space
	 */
	public double algalThreshold = iniAlgalThreshold; 
	/**
	 * The slope of the reaction
	 */
	public double slope = 2.0;
	/**
	 * Combined surface area of all corals in the patch
	 */
	public double totalCoralSurface;
	/**
	 * Reef rugosity
	 */
	public double rugosity;
	/**
	 * The maximum rugosity a reef can have
	 */
	public double maxRugosity = 3.5;
	/**
	 * The time at which grazing rates become dependent on rugosity
	 */
	public int couplingTime;
	/**
	 * The time at which grazing rates become independent again
	 */
	public int decouplingTime;
	/**
	 * Boolean to determine if grazing is coupled with rugosity
	 */
	public boolean coupledGrazing=false;
	/**
	 * The algal threshold that dynamically changes during coupling
	 */
	public double coupledAlgalThreshold;
	/**
	 * The grazing probability that is adjusted when coupled
	 */
	public double coupledGrazingProb;
	
	//Parameters for mechanical disturbances
	private int count;
	private int dMI1;
	private int dMI2;
	private double dmi1;
	private double dmi2;
	private int disturbSD1;
	private int disturbSD2;
	public double disturbThreshold;
	public boolean coupleDist;
	public double disFreqIncrease;
	
	
	// TURF ALGAE
	/**
	 * The layer for turf algae
	 */
	public SparseGrid2D turf;
	/**
	 * The relative cover per turf cell
	 */
	double cover;
	/**
	 * The growth rate of turf in percent
	 */
	double turfGR = 5;
	/**
	 * The resolution -- how many pixels per rectangle
	 */
	public int turfResolution = 10;
	/**
	 * The number of cells along the width of the simulation area
	 */
	public int cellNumWidth;// = (int)(gridWidth / turfResolution);
	/**
	 * The number of cells along the height of the simulation area
	 */
	public int cellNumHeight;// = (int)(gridHeight / turfResolution);
	/**
	 * The width of a turf cell
	 */
	public int cellWidth; // = turfResolution;
	/**
	 * The height of a turf cell
	 */
	public int cellHeight; // = turfResolution;

	
	// ENVIRONMENTAL / BLEACHING
	// the main temperature of the simulation -- if elNino then higher --> corals bleach
	double temperature = 29;
	/**
	 * The interval of major bleaching events
	 */
	public int bleachInterval;
	/**
	 * Determines, when the first bleaching event happens
	 */
	public int firstBleach;
	/**
	 * The probability of bleaching
	 */
	public double bleachProb;
	/**
	 * The probability mortality during being bleached
	 */
	public double bleachDeathProb;
	/**
	 * The minimal bleaching probability of all corals
	 */
	public double minBleachProb = 0.3;
	/**
	 * The minimal mortality probability
	 */
	public double minBleachDeathProb = 0.2;
	Hashtable<String, Double> bleachProbs;
	Hashtable<String, Double> bleachDeathProbs;
	public double tempSumThreshold;
	public double minDeathTemp;
	public double maxDeathTemp;
	/**
	 * Factor of how many coral recruits migrate from the outside
	 * multiplier of internal larval production 
	 */
	public double coralRecImportFactor;
	
	/**
	 * The probability of breaking if a branching coral stands alone
	 */
	public double breakageProb;
	/**
	 * The probability for a branching coral to fragment
	 */	
	public double fragProb;
	/**
	 * The fraction of overgrowth before a coral dies
	 */
	public double dieOvergrowthMas;
	public double dieOvergrowthBra;

	// variables for disturbance events
	double dRadius ;
	public boolean disser1 = true;
	public double disturbMaxSize1; 
	public double disturbMinSize1;
	public double disturbMaxRadius1;
	public double disturbMinRadius1; 
	public int disturbMeanInterval1;
	public double disturbSDPercent1;
	public int disturbSDInterval1;
	int disturbMaxNumber1;
	
	public boolean disser2 = true;
	public double disturbMaxSize2; 
	public double disturbMinSize2;
	public double disturbMaxRadius2;
	public double disturbMinRadius2; 
	public int disturbMeanInterval2;
	public double disturbSDPercent2;
	public int disturbSDInterval2;
	int disturbMaxNumber2;
	
	ArrayList<double[]> disturbances;
	
	double[] dist1;
	double[] dist2;
	
	double dInterval; 
	
	public Steppable disturber1;
	public Steppable disturber2;


	// OUTPUT
	/**
	 * IF true -- output will be created
	 */
	public static boolean createOutput = true;
	/**
	 * The interval in which output shall be produced in time steps (month)
	 */
	public static int indivOutInter;// = 1;
	/**
	 * The relative path to the Main.class folder
	 */
	private File path = new File(Siccom.class.getProtectionDomain().getCodeSource()
					.getLocation().getPath());
	/**
	 * The relative path to the output folder 
	 */
	String outputPath = path.getParent() +"/output" ; 
	/**
	 * The output writer
	 */
	public OutputWriter outW;




	// FOR CHECKING THE REAL COVER OF DIFFERENT ORGANISM GROUPS
	public boolean checkThatCover = false;
	SparseGrid2D coverGrid;

	private double totalMaCoCov;
	private double totalBraCoCov;
	public double availAreaPerc;

	// VARIABLES FOR MASON INSPECTORS
	// MODEL
	/**
	 * Displays the checkbox for createOutput in the GUI's Model Tab
	 * @return the value of createOutput
	 */
	public boolean getCreateOutput() { return createOutput; }
	/**
	 * Allows to set the value of createOutput 
	 * @param b the value of createOutput -- true or false
	 */
	public void setCreateOutput(boolean b) 
	{ 
		createOutput = b;
		
		outW = new OutputWriter(this);
//		schedule.scheduleOnce(Schedule.EPOCH, outW);
//		setupOutput();
	}
	/**
	 * Displays the output interval in the GUI's Model Tab
	 * @return the value of outputInterval
	 */
	public int getOutputInterval() { return indivOutInter; }
	/**
	 * allows to set the output interval
	 * @param val the value for outputInterval
	 */
	public void setOutputInterval( int val ) {if (val>0) indivOutInter = val; }
	public double getIniAlgalThreshold() { return iniAlgalThreshold; }
	public void setIniAlgalThreshold(double val) { if (val >= 0 && val <= 10000) {iniAlgalThreshold = val; algalThreshold = val;}}
	public double getIniGrazingProb() { return iniGrazingProb; }
	public void setIniGrazingProb(double val) 
	{
		if (val >= 0 && val <= 1) 
		{
			iniGrazingProb = val; 
			grazingProb = iniGrazingProb;
			minGrazingProb = iniGrazingProb - grazingProbHalfRange;
			if (minGrazingProb < 0.0) minGrazingProb = 0;
			maxGrazingProb = iniGrazingProb + grazingProbHalfRange;
		}
	}
	
	public boolean getCheckThatCover() { return checkThatCover; }
	/**
	 * Allows to set the value of createOutput 
	 * @param b the value of createOutput -- true or false
	 */
	public void setCheckThatCover(boolean b) 
	{ 
		checkThatCover = b;
		
	}

	
	
	/**
	 * Contructor
	 * creates the CRPS main frame
	 * -- if started in its own main() --> direct initialisation and start of the program
	 * @param seed the random seed for the random generator
	 */
	public Siccom(long seed) 
	{
		super(new MersenneTwisterFast(seed), new Schedule());
		
		startTime = System.currentTimeMillis();
		
		conf = new Config(this);
		
		if(!gui) initMain();
	}
	
	/**
	 * Initializes the parameters for the different coral groups from parameter files (*.inf) in the specified folder
	 */
	public void initMain()
	{
		conf.readMainFile("mainParam");
		conf.readEnvironmentFile("environment");
	
		totalArea = gridWidth*gridHeight;
	}
	



	/**
	 * Sets the simulation time to zero, starts the simulation and repeats a specified Steppable().
	 */
	public void start()
	{
		super.start();
		
		System.out.println("Random Seed: " + this.seed());

		coupledGrazing=false;
		count=0;
		
		availAreaPerc = 100;
		totalMaCoCov = 0;
		totalBraCoCov = 0;
		
		maCoGroups = new ArrayList<MassiveGroup>();
		braCoGroups = new ArrayList<BranchingGroup>();		
	
		if(!gui)	conf.initOrganisms();
		else		conf.initGUI();															// <-- SERVER TURN OFF
		
		coverGrid = new SparseGrid2D((int)gridWidth, (int)gridHeight);
		
		//SET UP THE HASH TABLES
		massiveCorals = new Continuous2D(discretization, gridWidth, gridHeight);
		branchingCorals = new Continuous2D(discretization, gridWidth, gridHeight);
		algae  = new Continuous2D(discretization, gridWidth, gridHeight);
		turf = new SparseGrid2D(cellNumWidth,cellNumHeight);

		initTurf();
		initMassiveCorals();
		initBranchingCorals();
		initAlgae();
		
		// setup and initialize the hashtable where the bleaching 
		// probabilities for the different CoralGroups are stored in
		bleachProbs = new Hashtable<String, Double>();
		initBleachProbs();
		bleachDeathProbs = new Hashtable<String, Double>();
		initBleachDeathProbs();
		
		// Determine first bleaching event
		firstBleach = random.nextInt((bleachInterval/12));
				
		dMI1 = (int) dist1[2];
		dMI2 = (int) dist2[2];
		dmi1 = dist1[2];
		dmi2 = dist2[2];
		disturbSD1 = (int) dist1[3];
		disturbSD2 = (int) dist2[3];
				
		// Setup the temperature routine
		temp = new Temperature("TemperatureData_Chumbe", this);
		temp.readTempFile();
		schedule.scheduleOnce(Schedule.EPOCH, temp);
		
		// SETUP OUTPUT WRITER: 
		// at first just the parameters are written to the screen
		outW = new OutputWriter(this);
		
		/**
		 *  MONTHLIES
		 *  -- methods that are repeated for each month 
		 */
		Steppable monthlies = new Steppable()
		{
			private static final long serialVersionUID = 3564764645L;

			@Override
			public void step(SimState state) 
			{
				if (!createOutput) steps = (int) schedule.getSteps();
				
				calculateBleachProbs();
				calcRugosity();
				getMax();
				grazing();
				coupledAlgalThreshold = iniAlgalThreshold;
				coupledGrazingProb = iniGrazingProb;
				
				
				if (((int) schedule.getSteps()) == couplingTime ) coupledGrazing=true;
				
	
				if (((int) schedule.getSteps()) == decouplingTime ) 
				{
					coupledGrazing=false;

					minGrazingProb = iniGrazingProb - grazingProbHalfRange;
					if (minGrazingProb < 0.0) minGrazingProb = 0;
					maxGrazingProb = iniGrazingProb + grazingProbHalfRange;
					

					if(coupleDist) resetDisturbance();
					
				}
				
				
				if (coupledGrazing)
				{
					adjustGrazing();
					if(coupleDist)
					{	
						if(rugosity <= disturbThreshold) count+=1; 
						else resetDisturbance();
	
						if(count!=0 && count%12==0) adjustDisturbance();
					}
				}
			}
		};
		schedule.scheduleRepeating(Schedule.EPOCH, 2, monthlies, 1);
	

		if(createOutput == true) 
		{
			outW.initOutput(this);
			
			Steppable output = new Steppable()
			{
				private static final long serialVersionUID = 1L;
	
				@Override
				public void step(SimState state) 
				{
					steps = (int) schedule.getSteps();
					
					outW.groupedOutput(steps);
					outW.phaseOutput(steps, rugosity, grazingProb, coupledGrazingProb, minGrazingProb, maxGrazingProb);
				}
			};
			schedule.scheduleRepeating(Schedule.EPOCH, 2, output, 1);
		}
		
		

		/**
		 * GET THE DATA
		 */
		Steppable dataGetter = new Steppable()
		{
			private static final long serialVersionUID = 3875019604046987874L;

			public void step(SimState state)
			{
				massiveCoralCover();
				massiveCoralCounter();
				branchingCoralCover();
				branchingCoralCounter();
				algaeCover();
				algCounter();
	
			}
		};
		schedule.scheduleRepeating(Schedule.EPOCH, 1, dataGetter, 1);
		
		
		/**
		 * 	DISTURBANCE 
		 */
		if (disser1)
		{
			disturber1 = new Steppable()
			{
				private static final long serialVersionUID = 3564764645L;

				@Override
				public void step(SimState state) 
				{
					disturbance1(dist1);
				}
			};
			schedule.scheduleOnce(Schedule.EPOCH+dist1[5], disturber1);
		}
		
		if (disser2)
		{
			disturber2 = new Steppable()
			{
				private static final long serialVersionUID = 3564764645L;

				@Override
				public void step(SimState state) 
				{
					 disturbance2(dist2);
				}
			};
			schedule.scheduleOnce(Schedule.EPOCH+dist2[5], disturber2);
		}

		
		/**
		 * 	CORAL RECRUITMENT
		 */
		for (final BranchingGroup bG : braCoGroups )
		{
			Steppable braCoRecruitment = new Steppable()
			{
				private static final long serialVersionUID = 3432L;

				@Override
				public void step(SimState state) 
				{
					bG.recruitBranchingCorals();
				}
				
			};
			schedule.scheduleRepeating(Schedule.EPOCH+bG.recFirst, 1, braCoRecruitment, bG.recInterval);
		}
		
		for (final MassiveGroup mG : maCoGroups )
		{
			Steppable maCoRecruitment = new Steppable()
			{
				private static final long serialVersionUID = 3432L;

				@Override
				public void step(SimState state) 
				{
					mG.recruitMassiveCorals();
				}
			};
			schedule.scheduleRepeating(Schedule.EPOCH + mG.recFirst, 1, maCoRecruitment, mG.recInterval);
		}
		
		/**
		 * 	ALGAL RECRUITMENT
		 */
		Steppable aRecruitment = new Steppable()
		{
			private static final long serialVersionUID = 43095843L;

			@Override
			public void step(SimState state) 
			{
				recruitAlgae();
			}
		};
		schedule.scheduleRepeating(Schedule.EPOCH + conf.algaRecFirst, 1, aRecruitment, conf.algaRecInterval);	

	}

	/**
	 * initializes the storage for the bleaching probabilities
	 */
	 private void initBleachProbs()
	 {
		 for(MassiveGroup mG : maCoGroups)
		 {
			 bleachProbs.put(mG.name, 0.0);
		 }
		 for(BranchingGroup bG : braCoGroups)
		 {
			 bleachProbs.put(bG.name, 0.0);
		 }
	 }
	/**
	 * Calculation of specific bleaching probs
	 */
	 public void calculateBleachProbs()
	 {
		 double actualTemp = overTempPerDay+tempSumThreshold;

		 for(MassiveGroup mG : maCoGroups)
		 {
			 if (actualTemp >= mG.minBleachTemp) 
			 {
				 bleachProb = ((1-minBleachProb)/(mG.maxBleachTemp-mG.minBleachTemp) * (actualTemp-mG.minBleachTemp)) + minBleachProb;	
				 if (bleachProb>1) bleachProb = 1;
				 if (bleachProb<0) bleachProb = 0;
				 bleachDeathProb = 1/(mG.maxDeathTemp-mG.minDeathTemp) * (actualTemp-mG.minDeathTemp);	
				 if (bleachDeathProb>1) bleachDeathProb = 1;
				 if (bleachDeathProb<0) bleachDeathProb = 0;
				 bleachDeathProbs.put(mG.name, bleachDeathProb);
			 }
			 else bleachProb = 0;
			 
			 bleachProbs.put(mG.name, bleachProb);
		 }
		 
		 for(BranchingGroup bG : braCoGroups)
		 {
			 if (actualTemp >= bG.minBleachTemp)
			 {
				 bleachProb = ((1-minBleachProb)/(bG.maxBleachTemp-bG.minBleachTemp) * (actualTemp-bG.minBleachTemp)) + minBleachProb;
				 if (bleachProb>1) bleachProb = 1;
				 if (bleachProb<0) bleachProb = 0;
				 bleachDeathProb = 1/(bG.maxDeathTemp-bG.minDeathTemp) * (actualTemp-bG.minDeathTemp);
				 if (bleachDeathProb>1) bleachDeathProb = 1;
				 if (bleachDeathProb<0) bleachDeathProb = 0;				 
				 bleachDeathProbs.put(bG.name, bleachDeathProb);
			 }
			 else bleachProb = 0;
			 
			 bleachProbs.put(bG.name, bleachProb);
		 }
	 }
	
	 /**
	  * Initializes the storage for bleach death probabilities
	  */
	 private void initBleachDeathProbs()
	 {
		 for(MassiveGroup mG : maCoGroups)
		 {
			 bleachDeathProbs.put(mG.name, 0.0);
		 }
		 for(BranchingGroup bG : braCoGroups)
		 {
			 bleachDeathProbs.put(bG.name, 0.0);
		 }
	 }

	 /**
	  * Calculates the rugosity of the reef patch
	  */
	 private void calcRugosity()
	 {
		totalCoralSurface = 0;
		 
		for (MassiveGroup mG : maCoGroups )
	 	{	
	 		totalCoralSurface += mG.calculateSurfaceArea();
		}
		for (BranchingGroup bG : braCoGroups )
 		{	
			totalCoralSurface += bG.calculateSurfaceArea();
		}
		 double uncovered = totalArea - totalMaCoCov - totalBraCoCov;
		 if(uncovered<0) uncovered=0;
		 rugosity = Math.sqrt((totalCoralSurface+uncovered) / (totalArea));
	 }
	 /**
	  * Adjusts grazing to the new rugosity
	  */
	 private void adjustGrazing()
	 {
		coupledGrazingProb = iniGrazingProb * (rugosity-1) / (maxRugosity-1) ;	//ORIGINAL

		minGrazingProb = coupledGrazingProb - grazingProbHalfRange;
		if (minGrazingProb < 0.0) minGrazingProb = 0;
		maxGrazingProb = coupledGrazingProb + grazingProbHalfRange;
	 }
	 /**
	  * Adjusting the disturbance if destructive fishing takes place
	  */
	 private void adjustDisturbance()
	 {
		 dmi1 = dmi1 * disFreqIncrease;
		 dmi2 = dmi2 * disFreqIncrease;
		 dMI1 = (int) Math.round(dmi1);
		 dMI2 = (int) Math.round(dmi2);
		 disturbSD1 = 0;
		 disturbSD2 = 0;
	 }
	 /**
	  * Resets the disturbance regime once the rugosity exceeds the threshold again
	  */
	 private void resetDisturbance()
	 {
		 count=0;
		 
		 dMI1 = (int)dist1[2];
		 dMI2 = (int)dist2[2];
		 
		 disturbSD1 = (int) dist1[3];
		 disturbSD2 = (int) dist2[3];
		 
		 
	 }
	 
		


	/* 
	 * 	CORALS	
	 */
		
	/**
	 *  Initializes massive coral agents from the massive groups and stores them into the massiveCorals-Continuous2D
	 */
 	public synchronized void initMassiveCorals()
	{
 		for (MassiveGroup mG : maCoGroups )
 		{	
 			mG.initMassiveCorals();
		}
	}

	/**
	 *  Initializes branching coral agents from the branching groups and stores them into the branchingCorals-Continuous2D
	 */
	public synchronized void initBranchingCorals()
	{
		for (BranchingGroup bG : braCoGroups )
 		{	
 			bG.initBranchingCorals();
		}
	}
	
 
  	/*
	 * 	ALGAE
	 */
	
	/**
	 *  Initializes alga agents into the algae-Continuous2D
	 */
	public synchronized void initAlgae()
	{
		double sumSize = 0;
		
		while (sumSize <= conf.algalCover)
		{
			double xPos = random.nextDouble()*gridWidth;
			double yPos = random.nextDouble()*gridHeight;
			
			Alga a = new Alga(	sim,									// the simulation, the agent acts in
								xPos, 									// x position
								yPos,									// y position
								random.nextDouble()*conf.aMaxRadius,			// radius
								random.nextInt(conf.algaMaxAge),				// age
								random.nextDouble()*conf.aMaxHeight);		// height
			
			sim.algae.setObjectLocation(a, 
					new Double2D(xPos, yPos));	
			schedule.scheduleOnce(Schedule.EPOCH, a);
			sumSize = sumSize + a.getSize();
		}
	}
	
	
	/**
	 * Creates new alga agents and stores them into the algae-Continuous2D
	 */
 	public void recruitAlgae()
  	{
   		for (int i=0; i<conf.algaRecNum; i++)
  		{
  			double xPos = random.nextDouble()*gridWidth;
  			double yPos = random.nextDouble()*gridHeight;
  				
  			Alga a = new Alga(		sim,							// the simulation, the agent acts in
  									xPos,							// x position
  									yPos,							// y position
									conf.algaRecRad,				// radius
									0,								// age
									conf.algaRecRad);				// height of the recruit equals the radius
  				
  			sim.algae.setObjectLocation(a, new Double2D(xPos, yPos));
  			schedule.scheduleOnce(a);
   		}
  	}
	
 	/*
 	 *  TURF
 	 */
 	/**
 	 * Initializes turf algae and stores them into the turf-SparseGrid2D
 	 */
	private void initTurf() {
		for (int i = 0; i < cellNumWidth; i++)
		{
			double x = (i * cellWidth);
			for (int j = 0; j < cellNumHeight; j++)
			{
				double y = (j * cellHeight);
				cover = random.nextDouble()*100.0;
				
				TurfCell tC = new TurfCell( 	sim,
												x, 
												y,
												cellWidth,
												cellHeight,
												cover,
												turfGR			);
				
				schedule.scheduleOnce(Schedule.EPOCH, tC);
				turf.setObjectLocation(tC, i, j);
			}
		}
	}
 
 	/* GRAZING */
	/**
	 *  At first the grazing probability is computed in relation to the algal density.
	 *  Then it checks the whole algae-Continuous2D and removes an alga with a certain probability
	 */
	public void grazing()
	{

		// calculate available area for algal dispersal 
		//-- the total area of massive and branching corals is substracted from the total area
//		availAreaPerc =	100; //	(totalArea - ((totalMaCoCov + (totalBraCoCov))))/totalArea * 100; 
//		algalThreshold = (coupledAlgalThreshold / 100) * availAreaPerc;

//		System.out.println("availAreaPerc:\t" + availAreaPerc);
		
		algalThreshold = iniAlgalThreshold;
//		
//		// calculate the probability for grazing depending on the algal density
		grazingProb = (maxGrazingProb-minGrazingProb) * ( 1 - ( 1 / Math.pow((algalCoverPercent / algalThreshold), slope))) + minGrazingProb; 

		
		if (grazingProb < 0) grazingProb = 0; 
		else if (grazingProb > 1) grazingProb = 1;
		
		Bag a = algae.getAllObjects();
		for(int i=0; i<a.numObjs; i++)					//	ORIGINAL
		{
			if (random.nextBoolean(grazingProb)) 		//	ORIGINAL
			((Alga) a.objs[i]).die();
		}
		
		Bag t = turf.getAllObjects();
		for (int j=0; j<t.numObjs; j++)
		{
			if(random.nextBoolean(grazingProb)) ((TurfCell) t.objs[j]).cover = ((TurfCell) t.objs[j]).cover - 50;
			if (((TurfCell) t.objs[j]).cover < 0) ((TurfCell) t.objs[j]).cover = 0;
		}
	}
	

  	/* FIND THE MAXIMUM RADIUS OF CORALS */
  	/**
	 * 	Finds the maximum radius of corals.	
	 */
	public void getMax()
	{
		maCoMaxLength = 25;						// in case that the larger coral dies, maxR is reset to the initial value
		braCoMaxLength = 10;
		Bag m = massiveCorals.getAllObjects();
		for (int i=0; i<m.numObjs; i++)
		{
			 if ((!((MassiveCoral) m.objs[i]).bleached) && (((MassiveCoral) m.objs[i]).maximumBranchLength > maCoMaxLength))
			 {
				maCoMaxLength = ((MassiveCoral) m.objs[i]).getRadius(); 
			 }
		}
		
		Bag b = branchingCorals.getAllObjects();
		for (int i=0; i<b.numObjs; i++)
		{
			 if ((!((BranchingCoral) b.objs[i]).bleached) && (((BranchingCoral) b.objs[i]).maximumBranchLength > braCoMaxLength))
			 {
				braCoMaxLength = ((BranchingCoral) b.objs[i]).getRadius(); 
			 }
		}
	}
	
	/**
	 * sends maxR
	 * @return maxR the maximum radius
	 */
	public double sendMaxR()
	{
		return maCoMaxLength;
	}


	/* DISTURBANCE */
	
	// Disturbance No. 1
	/**
	 * Clears a field of the simulation area of all objects
	 */
	public void disturbance1(double[] disList)
	{
		double disturbMaxRadius = disList[0];
		double disturbMinRadius = disList[1];
//		dMI1 = (int) disList[2];
//		disturbSD1 = (int) disList[3];
		int disturbPerEvent = (int) disList[4]; 					// random.nextInt(disturbMaxNumber1);
		
		if (disturbPerEvent != 0)
		{

			for (int j=0; j<disturbPerEvent; j++)
			{
				dRadius = random.nextDouble()* (disturbMaxRadius - disturbMinRadius +1) + disturbMinRadius;
				if (dRadius<0.5) dRadius = 0;
				
				
				double dX = random.nextDouble() *  ((gridWidth+dRadius) - (0-dRadius) + 1)  + (0-dRadius);
				double dY = random.nextDouble() *  ((gridWidth+dRadius) - (0-dRadius) + 1)  + (0-dRadius);	
				Double2D dCentre = new Double2D(	dX, dY );
				
				if (createOutput) outW.disturbanceOutput(steps, "small", dX, dY, dRadius);
				
				Bag m = massiveCorals.getObjectsExactlyWithinDistance(dCentre, dRadius);
				for (int i=0; i<m.numObjs; i++) ((MassiveCoral) m.objs[i]).die();
				
				Bag b = branchingCorals.getObjectsExactlyWithinDistance(dCentre, dRadius);
				for (int i=0; i<b.numObjs; i++) ((BranchingCoral) b.objs[i]).die();
				
				Bag a = algae.getObjectsExactlyWithinDistance(dCentre, dRadius);
				for (int i=0; i<a.numObjs; i++) ((Alga) a.objs[i]).die();
			}
		}
		// sets the time for the first disturbance event to occur
		if (dMI1<=1) 
		{
			dInterval = 1;
			schedule.scheduleOnce(disturber1);
		}
		else
		{
			dInterval = Math.round(random.nextGaussian() * disturbSD1 + dMI1) + 1;
			schedule.scheduleOnce(steps+dInterval, disturber1);			// reschedule the disturbance event after the time interval
		}
	}
	
	// Disturbance No. 2
	/**
	 * Clears a field of the simulation area of all objects
	 */
	public void disturbance2(double[] disList)
	{
		double disturbMaxRadius = disList[0];
		double disturbMinRadius = disList[1];
		int disturbPerEvent = (int) disList[4];

		if (disturbPerEvent != 0)
		{

			for (int j=0; j<disturbPerEvent; j++)
			{
				dRadius = random.nextDouble()* (disturbMaxRadius - disturbMinRadius +1) + disturbMinRadius;
				double dX = random.nextDouble() *  ((gridWidth+dRadius) - (0-dRadius) + 1)  + (0-dRadius);
				double dY = random.nextDouble() *  ((gridWidth+dRadius) - (0-dRadius) + 1)  + (0-dRadius);	
				Double2D dCentre = new Double2D(	dX, dY );
				
				if (createOutput) outW.disturbanceOutput(steps, "large", dX, dY, dRadius);
				
				Bag m = massiveCorals.getObjectsExactlyWithinDistance(dCentre, dRadius);
				for (int i=0; i<m.numObjs; i++) ((MassiveCoral) m.objs[i]).die();
				
				Bag b = branchingCorals.getObjectsExactlyWithinDistance(dCentre, dRadius);
				for (int i=0; i<b.numObjs; i++)
				{
					BranchingCoral braco = ((BranchingCoral) b.objs[i]);
					if (sim.random.nextBoolean(braco.fragProb)) braco.fragtate();
					braco.die();
				}
				
				Bag a = algae.getObjectsExactlyWithinDistance(dCentre, dRadius);
				for (int i=0; i<a.numObjs; i++) ((Alga) a.objs[i]).die();
			}
		}

		
		if (dMI2<=1) 
		{
			dInterval = 1;
			schedule.scheduleOnce(disturber2);
		}
		else
		{
			dInterval = Math.round(random.nextGaussian() * disturbSD2 + dMI2) + 1;
			schedule.scheduleOnce(steps+dInterval, disturber2);			// reschedule the disturbance event after the time interval
		}	
	}
	
	
	/**
	 * Counts massive coral agents of each group and stores them in separate lists for each group
	 */
	public void massiveCoralCounter()
	{
		for (MassiveGroup mG : maCoGroups )
		{
			int num = 0;
			Bag m = massiveCorals.getAllObjects();
			
			for (int i=0; i<m.numObjs; i++)
			{
				MassiveCoral mC = (MassiveCoral)m.objs[i];
				if (mC.getName().equals(mG.name))
				num++;
			}
			mG.numMaCo = num;
		}
	}
	
	/**
	 * Computes the relative cover of massive coral groups and stores them separately for each group
	 */
	public void massiveCoralCover()
	{
		totalMaCoCov = 0;
		
		
		for ( MassiveGroup mG : maCoGroups ) 
		{
			double cSize = 0;
			Bag m = massiveCorals.getAllObjects();
		
			for (int i=0; i<m.numObjs; i++) 
			{
				MassiveCoral mC = (MassiveCoral)m.objs[i];
				if (mC.getName().equals(mG.name))
					cSize += ((MassiveCoral) m.objs[i]).size;
			}		
			mG.maCoPercentCov = cSize/totalArea*100;
			
			
			totalMaCoCov += cSize;
		}
		
		
	}
	
	/**
	 * Counts branching coral agents of each group and stores them in separate lists for each group
	 */
	public void branchingCoralCounter()
	{
		for (BranchingGroup bG : braCoGroups)
		{
			int num = 0;
			Bag m = branchingCorals.getAllObjects();
			
			for (int i=0; i<m.numObjs; i++)
			{
				BranchingCoral bC = (BranchingCoral)m.objs[i];
				if (bC.getName().equals(bG.name) && (bC.xPos > 0 || bC.xPos < Siccom.gridWidth || bC.yPos > 0 || bC.yPos < Siccom.gridHeight))
				num++;
			}
			bG.numBraCo = num;
		}
	}
	
	/**
	 * Computes the relative cover of branching coral groups and stores them separately for each group
	 */
	public void branchingCoralCover()
	{
		totalBraCoCov = 0;
		
		for ( BranchingGroup bG : braCoGroups ) 
		{
			double cSize = 0;
			Bag m = branchingCorals.getAllObjects();
		
			for (int i=0; i<m.numObjs; i++) 
			{
				BranchingCoral bC = (BranchingCoral)m.objs[i];
				if (bC.getName().equals(bG.name) && (bC.xPos > 0 || bC.xPos < Siccom.gridWidth || bC.yPos > 0 || bC.yPos < Siccom.gridHeight))	
					cSize += ((BranchingCoral) m.objs[i]).size;
			}
			bG.braCoPercentCov = cSize/totalArea*100;
			
			totalBraCoCov += cSize;
		}
	}
	
	/**
	 * Counts macroalgal agents
	 */
	public void algCounter()
	{
		numAlgae = 0;
		Bag a = algae.getAllObjects();
		for (int i=0; i<a.numObjs; i++)
		{
			Alga alg = (Alga) a.objs[i];
			if (alg.alive && (alg.xPos > 0 || alg.xPos < Siccom.gridWidth || alg.yPos > 0 || alg.yPos < Siccom.gridHeight)) numAlgae++;
		}
	}

	/**
	 * Computes the relative cover of macroalgae
	 */
	public double algaeCover()
	{
		double aSize = 0;
		Bag a = algae.getAllObjects();
		for (int i=0; i<a.numObjs; i++)
		{
			Alga alg = (Alga) a.objs[i];
			
			if (alg.alive && (alg.xPos > 0 || alg.xPos < Siccom.gridWidth || alg.yPos > 0 || alg.yPos < Siccom.gridHeight)) 
				aSize = aSize + alg.getSize();
			
		}
		algalCoverPercent = aSize/totalArea*100;
		
		return algalCoverPercent;
		
	}


	/**
	 * The main method
	 * @param args
		-repeat R Long value > 0: Runs the job R times. The random seed for
		each job is the provided -seed plus the job# (starting at 0).
		Default: runs once only: job number is 0.
		
		-checkpoint C String: loads the simulation from file C for
		job# 0. Further jobs are started new using -seed as normal.
		Default: starts a new simulation rather than loading one.

		-until U Double value >= 0: the simulation must stop when the
		simulation time U has been reached or exceeded.
		Default: don't stop.

		-for N Long value >= 0: the simulation must stop when N
		simulation steps have transpired.
		Default: don't stop.
		
		-seed S Long value not 0: the random number generator seed.
		Default: the system time in milliseconds.

		-time T Long value >= 0: print a timestamp every T simulation steps.
		If 0, nothing is printed.
		Default: auto-chooses number of steps based on how many
		appear to fit in one second of wall clock time. Rounds to
		one of 1, 2, 5, 10, 25, 50, 100, 250, 500, 1000, 2500, etc.

		-docheckpoint D Long value > 0: checkpoint every D simulation steps.
		Default: never.
		Checkpoints files named
		<steps>.<job#>.Siccom.checkpoint
	 */
	public static void main (String[] args)
	{
		doLoop (Siccom.class, args);    
		
		DecimalFormatSymbols usFS = new DecimalFormatSymbols(Locale.US);
		DecimalFormat numform = new DecimalFormat("00", usFS);
		
		long endTime = System.currentTimeMillis();
		long extTime = endTime - startTime;
		
		double mins = (int)(extTime / 60000);
		double secs = (int)((extTime -(mins*60000)) /1000);
		
		System.out.println("Execution Time: " + numform.format(mins) + " min " + numform.format(secs) + " sec");
		
		System.exit(0);
		
	}
}
