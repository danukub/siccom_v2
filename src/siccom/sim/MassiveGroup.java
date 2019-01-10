package siccom.sim;

import java.awt.Color;

import sim.engine.Schedule;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * This class defines the main parameters for a massive coral species
 * 
 * For parameter description of individual colonies see {@link MassiveCoral}
 * 
 * For parameter which are important for all coral groups see {@link CoralGroup}
 * 
 * 
 * @author Andreas Kubicek
 * @version 2.0
 */
public class MassiveGroup extends CoralGroup
{
	/**
	 * The number of coral individuals of this group
	 */
	public int numMaCo;
	/**
	 * The percentage of covered area for the group
	 */
	public double maCoPercentCov;
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
	 */
	public MassiveGroup(	Siccom sim, 
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
							double maxDeathTemp)
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
	}
	
	/**
	 * Initializes {@link MassiveCoral} individuals until the desired percentage of cover is reached
	 */
	public void initMassiveCorals()
	{
		double sumSize = 0;
		
		while (sumSize < coveredArea)
		{
			double xPos = sim.random.nextDouble()* Siccom.gridWidth;
			double yPos = sim.random.nextDouble()* Siccom.gridHeight;
			
			age = 10;
			
			MassiveCoral mC = new MassiveCoral( 	sim,										// the simulation, the agent acts in
													name,
													color,
													CI,
													maxRadius,
													xPos, 										// x position
													yPos,										// y position
													sim.random.nextDouble()*maxIniRadius,		// radius
													growthRate,									// growthRate
													age);																	
			
			sim.massiveCorals.setObjectLocation(mC, 
					new Double2D(xPos, yPos));							// random location
			
			sim.schedule.scheduleOnce(Schedule.EPOCH, mC);					// schedule once in the beginning 
																		// -- then Coral reschedules itself if alive			
			sumSize = sumSize + mC.getSize();
		}
	}
	
	/**
  	 *  Creates recruits and appends them to massiveCorals
  	 */
 	public void recruitMassiveCorals()
  	{
 		selfMadeRecs = selfMadeRecruits();
 		importedRecs = importedRecruits();
 		totalRecs = selfMadeRecs + importedRecs;
 		
 		
 		for (int i = 0 ; i<totalRecs; i++)
// 		for (int i = 0 ; i<recNumber; i++)
		{
			double xPos = sim.random.nextDouble()*Siccom.gridWidth;
  			double yPos = sim.random.nextDouble()*Siccom.gridHeight;
  				
  			MassiveCoral mC = new MassiveCoral(	sim,										// the simulation, the agent acts in
  												name,
  												color,
  												CI,
  												maxRadius,
  												xPos, 										// x position
  												yPos,										// y position
  												recRad,										// radius
  												growthRate,
  												0); 										// age
  						
  			sim.massiveCorals.setObjectLocation(mC, new Double2D(xPos, yPos));
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
		Bag mB = sim.massiveCorals.getAllObjects();
		for(int i = 0; i<mB.size(); i++)
		{
			MassiveCoral mC = (MassiveCoral)mB.objs[i];
			
			// check, if coral is mature and can produce propagules 
			if(mC.getName().equals(name) && mC.diameter >= diamAtMaturity)
			{
				// calculate area of a hemisphere (massive coral head)
					surfaceArea += mC.calculateHemisphereRecruitment();
			}
			
		}
		// calculate the total propagule output
		inRecNum = (surfaceArea * surfaceFactor * propagulesPerSqCm);	
		int selfRecs = (int) (inRecNum * retainFactor);
				
		return selfRecs;
		
 	}

 	/**
 	 * Calculates the amount of imported recruits
 	 * @return imported recruits
 	 */
 	private int importedRecruits()
 	{
 		return (int)((selfMadeRecs * sim.coralRecImportFactor) + fixRecImport);
 	}

 	
 	public double calculateSurfaceArea()
 	{
 		double surfaceArea = 0;
 		
 		Bag mB = sim.massiveCorals.getAllObjects();
		for(int i = 0; i<mB.size(); i++)
		{
			MassiveCoral mC = (MassiveCoral)mB.objs[i];
			
				// calculate area of a hemisphere (massive coral head)
//				if (sim.random.nextBoolean(0.5)) 
			surfaceArea += mC.calculateHemisphere();
			
		}
		
		return surfaceArea * surfaceFactor;
 	}
 	
	
 	/**
 	 * Passes the number of massive coral individuals in this group
 	 * @return number of agents
 	 */
 	public int getNum() {return numMaCo;}
 	/**
 	 * Passes the name of the coral species
 	 * @return name
 	 */
 	public String getName() {return name;}
 	/**
 	 * Passes the percentage of covered area for this group
 	 * @return percentage of cover
 	 */
 	public double getCover() {return maCoPercentCov;}
}
