										Readme.txt
									      ---------------

ENVIRONMENT
------------
	Programming Language: Java


FOLDER STRUCTURE
----------------
The Project consists of 2 folders (consistency and extlib) and 3 unix executable files.

        	consistency: Contains the below java class files.

			ConsistencyChecker.java
			KVClient.java
			KVStore.java
			SequenceNumberGenerator.java
			SequenceNumberRequestHandler.java
			SequenceService.java


        	extlib : Contains the below libraries used in the java class files.

			commons-collections4-4.1.jar
			json-simple-1.1.1.jar
			libthrift-0.9.3.jar
			slf4j-api-1.7.21.jar
			slf4j-simple-1.6.1.jar


		compile_all: Command to compile the java class files

		sequence_server: Command to run the sequence server

		consistency_test: Command to run the KVClient


HOW TO RUN:
-----------
	1) Windows
	
	Steps:
        	1. Open Command Prompt (cmd.exe).
		2. Navigate to the main folder which contains the above two folders.
      
        	3. Compile the java classes within the consistency folder using the below commmand

			javac -cp ;./extlib/commons-collections4-4.1.jar;./extlib/json-simple-1.1.1.jar;./extlib/libthrift-0.9.3.jar;./extlib/slf4j-api-1.7.21.jar;./extlib/slf4j-simple-1.6.1.jar ./consistency/*.java  


		4. First run the Sequence Server using the below command. The Sequence Server will run on port 5432.

			java -cp ;./extlib/commons-collections4-4.1.jar;./extlib/json-simple-1.1.1.jar;./extlib/libthrift-0.9.3.jar;./extlib/slf4j-api-1.7.21.jar;./extlib/slf4j-simple-1.6.1.jar consistency.SequenceNumberGenerator


        	5. Next, run the KVCLient using the below command. This takes the KVServer host and the port number as parameters. 

			java -cp ;./extlib/commons-collections4-4.1.jar;./extlib/json-simple-1.1.1.jar;./extlib/libthrift-0.9.3.jar;./extlib/slf4j-api-1.7.21.jar;./extlib/slf4j-simple-1.6.1.jar consistency.KVClient kvserver 192.168.1.5:9634



	2) Linux

	Steps:
		1) Open Terminal.
		2) Navigate to the main folder which contains the above two folders.
		3) To compile all the java files,run the script Compile_all.

			$ ./compile_all

		4) To run the sequence server, run the script SequenceServer. This will run on the port 5432.

			$ ./sequence_server

		5) To runt he KVClient, run the script

			$ ./consistency_test -server 192.168.1.5:9634

