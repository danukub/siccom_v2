package siccom.sim;

import java.awt.Color;

import sim.engine.Schedule;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * This class defines the main parameters for a branching coral species
 * 
 * For parameter description of individual colonies see {@link BranchingCoral}
 * 
 * For parameter which are important for all coral groups see {@link CoralGroup}
 * 
 * 
 * @author Andreas Kubicek
 * @version 2.0
 */
public class BranchingGroup extends CoralGroup
{
	/**
	 * number of branches a coral colony produces
	 */
	int numBranches;
	
	/**
	 * FRAGMENTING
	 */
	double fragSize;
	double fragRange;
	
	/**
	 * The number of coral individuals of this group
	 */
	int numBraCo;
	/**
	 * The percentage of covered area for the group
	 */
	public double braCoPercentCov;
	/**
	 * The actual relative cover of the species
	 */
	public double relCover;
	/**
	 * The number of recruits produced in the stock-recruitment relationship
	 */
	public int selfMadeRecs;
	/**
	 * The number of imported recruits from outside
	 */
	public int importedRecs;
	/**
	 * The total number of recruits entering the system
	 */
	public int totalRecs;
	/**
	 * The age of a colony
	 */
	private int age;
	

	/**
	 * Constructor: for parameter description see {@link CoralGroup}
	 * @param sim
	 * @param name
	 * @param color
	 * @param CI
	 * @param maxIniRadius
	 * @param maxRadius
	 * @param growthRate
	 * @param coverPercent
	 * @param recRad
	 * @param recFirst
	 * @param recInterval
	 * @param fixRecImport
	 * @param minBleachTemp
	 * @param maxBleachTemp
	 * @param numBranches	The number of branches the coral colony produces
	 */
	public BranchingGroup(	Siccom sim, 
							String name, 
							String colorString,
							Color color, 
							double CI,
							double maxIniRadius, 
							double maxRadius, 
							double growthRate,
							double coverPercent, 
							double diamAtMaturity,
							double surfaceFactor,
							double propagulesPerSqCm,
							double retainFactor,
							double recRad, 
							int recFirst, 
							int recInterval, 
							int fixRecImport,
							double minBleachTemp,
							double maxBleachTemp,
							double minDeathTemp,
							double maxDeathTemp,
							int numBranches,
							double fragSize,
							double fragRange) 
	{
		super(	sim, 
				name, 
				colorString,
				color, 
				CI, 
				maxIniRadius, 
				maxRadius, 
				growthRate, 
				coverPercent, 
				diamAtMaturity,
				surfaceFactor,
				propagulesPerSqCm,
				retainFactor,
				recRad,
				recFirst, 
				recInterval, 
				fixRecImport, 
				minBleachTemp,
				maxBleachTemp,
				minDeathTemp,
				maxDeathTemp);
		
		this.numBranches = numBranches;
		this.fragSize = fragSize;
		this.fragRange = fragRange;
	}

	/**
	 * Initializes {@link BranchingCoral} individuals until the desired percentage of cover is reached
	 */
	public void initBranchingCorals()
	{
		double sumSize = 0;
		
		while (sumSize < coveredArea)
		{
			double xPos = sim.random.nextDouble()* Siccom.gridWidth;
			double yPos = sim.random.nextDouble()* Siccom.gridHeight;
			
			age = 10;
			
			BranchingCoral mC = new BranchingCoral( sim,										// the simulation, the agent acts in
													this,
													name,
													color,
													CI,
													maxRadius,
													xPos, 										// x position
													yPos,										// y position
													sim.random.nextDouble()* maxIniRadius,		// radius
													growthRate,									// growthRate
													numBranches,
													age, 
													fragSize,
													fragRange);								
			
			sim.branchingCorals.setObjectLocation(mC, 
					new Double2D(xPos, yPos));							// random location
			
			sim.schedule.scheduleOnce(Schedule.EPOCH, mC);					// schedule once in the beginning 
																		// -- then Coral reschedules itself if alive			
			sumSize = sumSize + mC.getSize();
		}
		
	}
	
	/**
  	 *  Creates recruits and appends them to branchingCorals
  	 */
 	public void recruitBranchingCorals()
  	{
 		selfMadeRecs = selfMadeRecruits();
 		importedRecs = importedRecruits();
 		totalRecs = selfMadeRecs + importedRecs;
 		
 		
 		for (int i = 0 ; i<totalRecs; i++)
		{
			double xPos = sim.random.nextDouble()*Siccom.gridWidth;
  			double yPos = sim.random.nextDouble()*Siccom.gridHeight;
  				
  			BranchingCoral mC = new BranchingCoral(	sim,										// the simulation, the agent acts in
  													this,
  													name,
  													color,
  													CI,
  													maxRadius,
   													xPos, 										// x position
  													yPos,										// y position
  													recRad,										// radius
  													growthRate,									// growthRate
													numBranches,
													0,											//age
													fragSize,
													fragRange);																							
  			
  			sim.branchingCorals.setObjectLocation(mC, new Double2D(xPos, yPos));
  			sim.schedule.scheduleOnce(mC);
  		}
 	}
 	
 	/**
 	 * Calculates the amount of recruits produced with the stock-recruitment relationship
 	 * @return self made recruits
 	 */
 	public int selfMadeRecruits()
 	{
		// the number of recruits that come directly from the focal reef
 		double inRecNum = 0;
 		double surfaceArea = 0;

 		// collect all massive corals into a bag to make them iterable
		Bag bB = sim.branchingCorals.getAllObjects();
		for(int i = 0; i<bB.size(); i++)
		{
			BranchingCoral bC = (BranchingCoral)bB.objs[i];
			
			// check, if coral is of the desired species, mature and can produce propagules 
			if(bC.getName().equals(name) && bC.diameter >= diamAtMaturity && 
					(bC.xPos > 0 || bC.xPos < Siccom.gridWidth || bC.yPos > 0 || bC.yPos < Siccom.gridHeight))
			{
					surfaceArea += bC.calculateHemisphereRecruitment();
			}
		}
		// calculate the total propagule output
		inRecNum = (surfaceArea * surfaceFactor * propagulesPerSqCm);		
		
		return (int) (inRecNum * retainFactor);
		
 	}
 	
 	/**
 	 * Calculates the amount of imported recruits
 	 * @return imported recruits
 	 */
 	private int importedRecruits()
 	{
 		return (int)((selfMadeRecs  * sim.coralRecImportFactor) + fixRecImport);
 	}
 	
 	
 	/**
 	 * Calculates the surface area for the rugosity calculations
 	 * @return total surface area for a coral species
 	 */
	public double calculateSurfaceArea()
 	{
 		double surfaceArea = 0;
 		
 		Bag bB = sim.branchingCorals.getAllObjects();
		for(int i = 0; i<bB.size(); i++)
		{
			BranchingCoral bC = (BranchingCoral)bB.objs[i];
			
				// calculate area of a hemisphere (massive coral head)
//				if (sim.random.nextBoolean(0.5)) 
			surfaceArea += bC.calculateHemisphere();
		}
		return surfaceArea * surfaceFactor;
 	}
 	
 	
 	
 	/**
 	 * Passes the number of branching coral individuals in this group
 	 * @return number of agents
 	 */
 	public int getNum() {return numBraCo;}

 	/**
 	 * Passes the name of the coral species
 	 * @return name
 	 */
 	public String getName() {return name;}
	
 	/**
 	 * Passes the percentage of covered area for this group
 	 * @return percentage of cover
 	 */
 	public double getCover() {return braCoPercentCov;}
}
