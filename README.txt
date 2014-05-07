==== COURSE ==== 

Operating System and Multicore programming (1DT089) - Spring 2014

Department of Information Technology 
Uppsala University


==== GROUP ==== 

4


==== PROJECT NAME ==== 

The Lost Vaults - Uneasy Alliance


==== PROJECT DESCRIPTION ==== 

Add a short description of your project here. 


==== GROUP MEMBERS ====
910804-0628 anna.fm.nilsson@gmail.com   (Anna Nilsson)
911225-4678 felix.farjsjo@gmail.com     (Felix Färsjö)
920508-1335 philip.akerfeldt@gmail.com  (Philip Åkerfeldt)
890422-0590 fredrik.mejl@gmail.com      (Fredrik Larsson)
870928-0138 senilica@gmail.com          (Jimmy Holm)


==== MAY THE SOURCE BE WITH YOU ==== 

Everything you need to compile and run the system is included in the
directory tree. 

However, you might want to get the most up to date version of this
directory with

git clone https://github.com/senilica/LostVaults

==== SCALA VERSION ====

This software was developed and tested using scala 2.10 + akka 2.3.2 (Relevant libraries included in git repo.)
     	      	  	    	       
==== MAKE IT HAPPEN ====  

ant build => Compile the source files
ant build_jars => Compile the source files and create runnable jar files client.jar and server.jar
ant client => Compile and build only the client jar file.
ant server => Compile and build only the server jar file.
ant run_client => Compile, build and execute the client jar file.
ant run_server => Compile, build and execute the server jar file.
ant doc => Compile code documentation
ant run_tests => Compile, build and execute the scalatest test suites.


==== TO COMPILE ==== 

Running ant will build both client and server jar files.  

==== TO RUN AND TEST THE SYSTEM ==== 

The build targets run_client and run_server have been provided in order to rapidly build and run 
the client and server jar files respectively.
build target run_tests compiles, builds and executes the scalatest test suites.
