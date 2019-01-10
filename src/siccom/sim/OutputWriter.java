package siccom.sim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import sim.util.Bag;

/**
 * Writes the output files to the output folder
 * @author Andreas Kubicek
 * @version 2.0
 *
 */

public class OutputWriter
{

	public static final long serialVersionUID = 6948161854909261168L;

	Siccom sim;
	
	// Format for output data 
	// -- always in the US format
	DecimalFormatSymbols usFS = new DecimalFormatSymbols(Locale.US);
	public DecimalFormat numform2 = new DecimalFormat("#000.00", usFS);
	public DecimalFormat numform3 = new DecimalFormat("#000.###", usFS);
	public DecimalFormat numform8 = new DecimalFormat("0000.0000", usFS);
	public DecimalFormat percform = new DecimalFormat("00.0", usFS);
	
	/**
	 * File for individual output
	 */
	private File indOutFile;
	/**
	 * The print writer for the individual output file
	 */
	private PrintWriter indWriter;
	/**
	 * File for grouped output
	 */
	private File groupOutFile;
	/**
	 * The print writer for the grouped output file
	 */
	private PrintWriter groupWriter;
	/**
	 * The month as name
	 */
	private String month;
	/**
	 * The count for years
	 */
	private double year;
	/**
	 * The file for disturbance output data
	 */
	private File disOutFile;
	/**
	 * The print writer for disturbance data
	 */
	private PrintWriter disWriter;

	private PrintWriter phaseWriter;
	private File  phaseOutFile;

	/**
	 * This class
	 */
	Config conf;


	public OutputWriter(Siccom sim)
	{
		this.sim = sim;
		
		this.conf = sim.conf;

		writeParameters();
		
	}

	/**
	 * setup of output folder and output files
	 * @param sim
	 */
	public void initOutput(Siccom sim) {
		File f = new File(sim.outputPath);
		f.mkdir();
		
		indOutFile = new File(sim.outputPath + "/individualOutput.dat");
		groupOutFile = new File(sim.outputPath + "/groupedOutput.dat");
		
		disOutFile = new File(sim.outputPath + "/disturbance.dat");
		
		phaseOutFile = new File(sim.outputPath + "/phaseShift.dat");
		

		try 
		{
			indWriter = new PrintWriter( new BufferedWriter(new FileWriter(indOutFile)) );
			indWriter.write("Step\tMonth\tYear\tName\txPos\tyPos\tRadius\tDiameter\tSize\tAge\n");

			groupWriter = new PrintWriter( new BufferedWriter(new FileWriter(groupOutFile)));
			groupWriter.write("Step\tMonth\tYear\tName\tAbundance\tRelativeCover\n");			
			
			disWriter = new PrintWriter( new BufferedWriter(new FileWriter(disOutFile)));
			disWriter.write("Step\tMode\txLoc\tyLoc\tDiameter\n");
			disWriter.flush();
			
			phaseWriter = new PrintWriter( new BufferedWriter(new FileWriter(phaseOutFile)));
			phaseWriter.write("Step\tRugosity\tGrazingRate\tadjGR\tGRmin\tGRmax\n");
			phaseWriter.flush();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * write disturbance output
	 * @param steps
	 * @param mode
	 * @param xLoc
	 * @param yLoc
	 * @param size
	 */
	public void disturbanceOutput(int steps, String mode, double xLoc, double yLoc, double size)
	{
		disWriter.append( steps + "\t"  );  
		disWriter.append( mode + "\t"  );  
		disWriter.append( numform2.format(xLoc*Siccom.meterConv) + "\t");
		disWriter.append( numform2.format(yLoc*Siccom.meterConv) + "\t");
		disWriter.append( numform2.format(size*2*Siccom.meterConv) + "\n");
		disWriter.flush();
	}
	/**
	 * Writes the output for rugosity and grazing rate parameters
	 */
	public void phaseOutput(int steps, double rugosity, double grazingRate, double adjustedGrazingRate, double minGrazingRate, double maxGrazingRate)
	{
		phaseWriter.append( steps + "\t"  );  
		phaseWriter.append( rugosity + "\t"  );  
		phaseWriter.append( grazingRate + "\t");
		phaseWriter.append( adjustedGrazingRate + "\t");
		phaseWriter.append( minGrazingRate + "\t");
		phaseWriter.append( maxGrazingRate + "\n");
		phaseWriter.flush();
	}
	
	/**
	 * write individual output
	 * @param steps
	 */
	public void individualOutput(int steps)
	{
		Bag m = sim.massiveCorals.getAllObjects();
		for (int i=0; i<m.size(); i++ )
		{	
//			steps = sim.steps;
			MassiveCoral mc = (MassiveCoral) m.objs[i];
			indWriter.append( steps + "\t"  );  
			indWriter.append( getMonth(steps) +"\t"  );  
			indWriter.append(  getYear(steps) + "\t" );
			indWriter.append( mc.getName() + "\t");
			indWriter.append( numform2.format(mc.xPos) + "\t");
			indWriter.append( numform2.format(mc.yPos) + "\t");
			indWriter.append( numform2.format(mc.radius*Siccom.resolution) + "\t");
			indWriter.append( numform2.format(mc.diameter*Siccom.resolution) + "\t");
			indWriter.append( numform8.format(mc.getSize()*Math.pow(Siccom.resolution, 2.0)) + "\t");
			indWriter.append( mc.getAge() + "\n");
		}
		
		Bag b = sim.branchingCorals.getAllObjects();
		for (int i=0; i<b.size(); i++ )
		{	
			BranchingCoral bc = (BranchingCoral) b.objs[i];
			indWriter.append( steps + "\t"  );  
			indWriter.append( getMonth(steps) +"\t"  );  
			indWriter.append(  getYear(steps) + "\t" );
			indWriter.append( bc.getName() + "\t");
			indWriter.append( numform2.format(bc.xPos) + "\t");
			indWriter.append( numform2.format(bc.yPos) + "\t");
			indWriter.append( numform2.format(bc.radius*Siccom.resolution) + "\t");
			indWriter.append( numform2.format(bc.diameter*Siccom.resolution) + "\t");
			indWriter.append( numform8.format(bc.getSize()*Math.pow(Siccom.resolution, 2.0)) + "\t");
			indWriter.append( bc.getAge() + "\n");
			
		}
		indWriter.flush();
	}
	/**
	 * Determine the right month
	 * @param steps
	 * @return
	 */
	private String getMonth(int steps) 
	{
		if (steps%12  == 0) month = "January  ";
		if (steps%12  == 1) month = "February ";
		if (steps%12  == 2) month = "March    ";
		if (steps%12  == 3) month = "April    ";
		if (steps%12  == 4) month = "May      ";
		if (steps%12  == 5) month = "June     ";
		if (steps%12  == 6) month = "July     ";
		if (steps%12  == 7) month = "August   ";
		if (steps%12  == 8) month = "September";
		if (steps%12  == 9) month = "October  ";
		if (steps%12  == 10) month = "November";
		if (steps%12  == 11) month = "December";
		return month;
	}
	
	private double getYear(int steps) 
	{
		year = steps/12.0;
		return year;
	}
	
	/**
	 * Writes the grouped output file
	 */
	public void groupedOutput(int steps)
	{
		for (MassiveGroup mG : sim.maCoGroups)
		{
			groupWriter.append( steps + "\t"  );  
			groupWriter.append( getMonth(steps) +"\t"  );  
			groupWriter.append(  numform3.format(getYear(steps)) + "\t" );
			if (mG.name.length() >= 8) groupWriter.append( mG.name + "\t");
			else 	groupWriter.append( mG.name + "\t");
			groupWriter.append(mG.getNum() + "\t");
			groupWriter.append(percform.format(mG.getCover()) + "\n");
//			groupWriter.append(sim.rugosity + "\n");
			
		}
		
		for (BranchingGroup bG : sim.braCoGroups)
		{
			groupWriter.append( steps + "\t"  );  
			groupWriter.append( getMonth(steps) +"\t"  );  
			groupWriter.append(  numform3.format(getYear(steps)) + "\t" );
			if (bG.name.length() >= 8) groupWriter.append( bG.name + "\t");
			else 	groupWriter.append( bG.name + "\t");
			groupWriter.append(bG.getNum() + "\t");
			groupWriter.append(percform.format(bG.getCover()) + "\n");
//			groupWriter.append(sim.rugosity + "\n");
		}
		
		groupWriter.append( steps + "\t"  );  
		groupWriter.append( getMonth(steps) +"\t"  );  
		groupWriter.append(  numform3.format(getYear(steps)) + "\t" );
		groupWriter.append( "Algae" + "\t");
		groupWriter.append( sim.numAlgae + "\t");
		groupWriter.append(percform.format(sim.algalCoverPercent) + "\n");
//		groupWriter.append(sim.rugosity + "\n");
		groupWriter.flush();
	}
	
	/**
	 * Write out the parameter settings to the simulation output file
	 */
	private void writeParameters() 
	{
		System.out.println();
		System.out.println("---------------  PARAMETERS  ---------------");
		System.out.println();
		System.out.println("Main Parameters");
		System.out.println( (int)(Siccom.gridWidth* Siccom.meterConv) + "\t|\t" + "areaWidth" + "\t|\t ---" ) ;
		System.out.println( (int)(Siccom.gridHeight* Siccom.meterConv) + "\t|\t" + "areaHeight" + "\t|\t ---");
		System.out.println(Siccom.resolution + "\t|\t" + "resolution" + "\t|\t ---");
		System.out.println(Siccom.maCoGroupNum + "\t|\t" + "maCoNum " + "\t|\t ---");
		System.out.println(Siccom.braCoGroupNum + "\t|\t" + "braCoNum" + "\t|\t ---");
		System.out.println(Siccom.indivOutInter + "\t|\t" + "indivOutInter" + "\t|\t ---");
		System.out.println();
		
		System.out.println("Environment Parameters");
		System.out.println( sim.disturbMaxSize1*Siccom.meterConv + "\t|\t" + "disturbMaxSize1\t" + "\t|\t ---" );
		System.out.println( sim.disturbMinSize1*Siccom.meterConv + "\t|\t" + "disturbMinSize1\t" + "\t|\t ---" );
		System.out.println( sim.disturbMeanInterval1 + "\t|\t" + "disturbMeanInterval1" + "\t|\t ---" );
		System.out.println( sim.disturbSDPercent1 + "\t|\t" + "disturbSDInterval1" + "\t|\t ---" );
		System.out.println( sim.disturbMaxNumber1 + "\t|\t" + "disturbMaxNumber1" + "\t|\t ---" );
		System.out.println( sim.disturbMaxSize2*Siccom.meterConv + "\t|\t" + "disturbMaxSize2\t" + "\t|\t ---" );
		System.out.println( sim.disturbMinSize2*Siccom.meterConv + "\t|\t" + "disturbMinSize2\t" + "\t|\t ---" );
		System.out.println( sim.disturbMeanInterval2 + "\t|\t" + "disturbMeanInterval2" + "\t|\t ---" );
		System.out.println( sim.disturbSDPercent2 + "\t|\t" + "disturbSDInterval2" + "\t|\t ---" );
		System.out.println( sim.disturbMaxNumber2 + "\t|\t" + "disturbMaxNumber2" + "\t|\t ---" );
		System.out.println( sim.tempSumThreshold + "\t|\t" + "tempSumThreshold" + "\t|\t ---" );
		System.out.println( sim.bleachInterval / 12 + "\t|\t" + "bleachInterval\t" + "\t|\t ---" );
		System.out.println( sim.coralRecImportFactor + "\t|\t" + "coralRecImportFactor" + "\t|\t ---" );
		System.out.println( sim.grazingProb + "\t|\t" + "grazingProb\t" + "\t|\t ---" );
		System.out.println( sim.iniAlgalThreshold + "\t|\t" + "iniAlgalThreshold" + "\t|\t ---" );
		System.out.println( sim.couplingTime + "\t|\t" + "couplingTime" + "\t\t|\t ---" );
		System.out.println( sim.decouplingTime + "\t|\t" + "decouplingTime" + "\t\t|\t ---" );
		System.out.println( sim.maxRugosity + "\t|\t" + "maxRugosity" + "\t\t|\t ---" );
		System.out.println( sim.disturbThreshold + "\t|\t" + "disturbThreshold" + "\t|\t ---" );
		System.out.println( (Math.round((1 - sim.disFreqIncrease)*100)) + "\t|\t" + "disFreqIncrease" + "\t\t|\t ---" );
		System.out.println( (int)(sim.turfResolution*Siccom.meterConv) + "\t|\t" + "turfResolution\t" + "\t|\t ---" );
		System.out.println();
		
		System.out.println("Algae Parameters");
		System.out.println("Alga\t|\tname\t\t" + "\t|\t ---" );
		System.out.println( conf.aMaxRadius*Siccom.resolution + "\t|\t" + "aMaxRadius\t" + "\t|\t ---" );
		System.out.println( conf.aMaxHeight*Siccom.resolution + "\t|\t" + "aMaxHeight\t" + "\t|\t ---" );
		System.out.println( conf.algaMaxAge + "\t|\t" + "algaMaxAge\t" + "\t|\t ---" );
		System.out.println( conf.aGrowthRate*Siccom.resolution*10 + "\t|\t" + "aGrowthRate\t" + "\t|\t ---" );
		System.out.println( conf.aFragmentationHeight*Siccom.resolution + "\t|\t" + "aFragmentationHeight" + "\t|\t ---" );
		System.out.println( conf.aMaxFragNum + "\t|\t" + "aMaxFragNum\t" + "\t|\t ---" );
		System.out.println( conf.aFragmentSize*Siccom.resolution + "\t|\t" + "aFragmentSize\t" + "\t|\t ---" );
		System.out.println( conf.aFragRange*Siccom.meterConv + "\t|\t" + "aFragRange\t" + "\t|\t ---" );
		System.out.println( conf.algalCoverPercent + "\t|\t" + "algalCoverPercent" + "\t|\t ---" );
		System.out.println( conf.algaRecRad*Siccom.resolution + "\t|\t" + "algaRecRad\t" + "\t|\t ---" );
		System.out.println( conf.algaRecFirst + "\t|\t" + "algaRecFirst\t" + "\t|\t ---" );
		System.out.println( conf.algaRecInterval + "\t|\t" + "algaRecInterval\t" + "\t|\t ---" );
		System.out.println( conf.algaRecNumPerSqM + "\t|\t" + "algaRecNumPerSqM" + "\t|\t ---" );
		System.out.println();
		
		for(MassiveGroup mg : sim.maCoGroups)
		{
			System.out.println("Coral Parameters " + mg.name);
			System.out.println( mg.name + "\t\t|\t" + "name\t\t" + "\t|\t ---" );
			System.out.println( mg.colorString + "\t\t\t|\t" + "color\t\t" + "\t|\t ---" );
			System.out.println( mg.CI + "\t\t\t|\t" + "CI\t\t" + "\t|\t ---" );
			System.out.println( mg.maxIniRadius*Siccom.resolution + "\t\t\t|\t" + "maxIniRadius\t" + "\t|\t ---" );
			System.out.println( mg.maxRadius*Siccom.resolution + "\t\t\t|\t" + "maxRadius\t" + "\t|\t ---" );
			System.out.println( numform2.format(mg.growthRate/Siccom.growthConv) + "\t\t\t|\t" + "growthRate\t" + "\t|\t ---" );
			System.out.println( numform2.format(mg.coverPercent) + "\t\t\t|\t" + "coveredArea\t" + "\t|\t ---" );
			System.out.println( mg.diamAtMaturity*Siccom.resolution + "\t\t\t|\t" + "diamAtMaturity\t" + "\t|\t ---" );
			System.out.println( mg.surfaceFactor + "\t\t\t|\t" + "surfaceFactor\t" + "\t|\t ---" );
			System.out.println( mg.propagulesPerSqCm + "\t\t\t|\t" + "propagulesPerSqCm" + "\t|\t ---" );
			System.out.println( mg.retainFactor + "\t\t\t|\t" + "retainFactor\t" + "\t|\t ---" );
			System.out.println( mg.recRad*Siccom.resolution + "\t\t\t|\t" + "recRad\t\t" + "\t|\t ---" );
			System.out.println( mg.recFirst + "\t\t\t|\t" + "recFirst\t" + "\t|\t ---" );
			System.out.println( mg.recInterval + "\t\t\t|\t" + "recInterval\t" + "\t|\t ---" );
			System.out.println( mg.fixRecImport/(Siccom.areaWidth*Siccom.areaHeight) + "\t\t\t|\t" + "recNumberPerSqM\t" + "\t|\t ---" );
			System.out.println( mg.minBleachTemp + "\t\t\t|\t" + "minBleachTemp\t" + "\t|\t ---" );
			System.out.println( mg.maxBleachTemp + "\t\t\t|\t" + "maxBleachTemp\t" + "\t|\t ---" );
			System.out.println( mg.minDeathTemp + "\t\t\t|\t" + "minDeathTemp\t" + "\t|\t ---" );
			System.out.println( mg.maxDeathTemp + "\t\t\t|\t" + "maxDeathTemp\t" + "\t|\t ---" );
			System.out.println();
		}
		
		for(BranchingGroup mg : sim.braCoGroups)
		{
			System.out.println("Coral Parameters " + mg.name);
			System.out.println( mg.name + "\t|\t" + "name\t\t" + "\t|\t ---" );
			System.out.println( mg.colorString + "\t\t\t|\t" + "color\t\t" + "\t|\t ---" );
			System.out.println( mg.CI + "\t\t\t|\t" + "CI\t\t" + "\t|\t ---" );
			System.out.println( mg.maxIniRadius*Siccom.resolution + "\t\t\t|\t" + "maxIniRadius\t" + "\t|\t ---" );
			System.out.println( mg.maxRadius*Siccom.resolution + "\t\t\t|\t" + "maxRadius\t" + "\t|\t ---" );
			System.out.println( numform2.format(mg.growthRate/Siccom.growthConv) + "\t\t\t|\t" + "growthRate\t" + "\t|\t ---" );
			System.out.println( numform2.format(mg.coverPercent) + "\t\t\t|\t" + "coveredArea\t" + "\t|\t ---" );
			System.out.println( mg.diamAtMaturity*Siccom.resolution + "\t\t\t|\t" + "diamAtMaturity\t" + "\t|\t ---" );
			System.out.println( mg.surfaceFactor + "\t\t\t|\t" + "surfaceFactor\t" + "\t|\t ---" );
			System.out.println( mg.propagulesPerSqCm + "\t\t\t|\t" + "propagulesPerSqCm" + "\t|\t ---" );
			System.out.println( mg.retainFactor + "\t\t\t|\t" + "retainFactor\t" + "\t|\t ---" );
			System.out.println( mg.recRad*Siccom.resolution + "\t\t\t|\t" + "recRad\t\t" + "\t|\t ---" );
			System.out.println( mg.recFirst + "\t\t\t|\t" + "recFirst\t" + "\t|\t ---" );
			System.out.println( mg.recInterval + "\t\t\t|\t" + "recInterval\t" + "\t|\t ---" );
			System.out.println( mg.fixRecImport/(Siccom.areaWidth*Siccom.areaHeight) + "\t\t\t|\t" + "recNumberPerSqM\t" + "\t|\t ---" );
			System.out.println( mg.minBleachTemp + "\t\t\t|\t" + "minBleachTemp\t" + "\t|\t ---" );
			System.out.println( mg.maxBleachTemp + "\t\t\t|\t" + "maxBleachTemp\t" + "\t|\t ---" );
			System.out.println( mg.minDeathTemp + "\t\t\t|\t" + "minDeathTemp\t" + "\t|\t ---" );
			System.out.println( mg.maxDeathTemp + "\t\t\t|\t" + "maxDeathTemp\t" + "\t|\t ---" );
			System.out.println( mg.numBranches + "\t\t\t|\t" + "numBranches\t" + "\t|\t ---" );
			System.out.println();
		}
		System.out.println();
		System.out.println("--------------- --------- ---------------");
		System.out.println();
	}
	
	
	
}
