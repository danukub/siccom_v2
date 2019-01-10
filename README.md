## siccom_v2
# SICCOM - Spatial Interaction in Coral Reef Communities



### Version 2.0 - Mechanics of feedbacks in benthic coral reef communities
	
An article in which this model is applied and described in more detail can be found at:
https://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1002791

author: Andreas Kubicek


#### RUN THE PROGRAM
  
  - Minimum requirement is a Java Runtime Environment installed on the computer.
  
  - Under Windows extract the folder into the 'home'-directory (Windows 7).
  
  - the siccom_OS.jar file and the inf-folder have to be placed in the same directory.

  - then either double click the jar-file or to start from the command line type
      #~> java -jar siccom_OS_v2.0.jar

 



#### SAVING OUTPUT

  To save output you can open the 'Model' tab and check 'create output'.

  It will create a new 'output' directory which contains three different text files. 
      - disturbance.dat 	disturbance events are logged
      - groupedOutput.dat 	population data is logged
      - individualOutput.dat 	data for each individual is logged

  For the reason that individual  data output is quite storage intensive, one can specify the interval (months) at which this output shall be created.



#### SIMULATION SETTINGS	

  The 'environment' tab allows to set many of the main settings, such as:
	- timing and size of the two different mechanical disturbances,
	- timing of bleaching events,
	- the grazing probability, and
	- from when on grazing depends on 3D-structure (rugosity) and when it becomes independent again (couplingTime, decouplingTime)

  Coral species settings and settings for algae can be adjusted in the respective tabs.



#### SOURCE CODE AND DOCUMENTATION

  The model was developed with Java and makes use of the MASON (Multi Agent Simulation) toolkit (http://cs.gmu.edu/~eclab/projects/mason/)

  To run the source code in e.g. the Eclipse development environment you need to include MASON into the project file.
