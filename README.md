# HDFS_Retention_App
This application is used for housekeeping purpose of HDFS data.

1. HDFS_Retention application is a housekeeping application to manage data in HDFS and Zookeeper. 
2. This application is responsible to clean any HDFS data which has "expired" according to it's configuration.
3. Retention periods and paths of the ‘data to be deleted’ are configurable using a properties file named config.properties. 
   Syntax of the properties file are below -

	<app_name>_DIR=<app_location>
	
	<app_name>_DIR_ZNODE=<path_of_zookeeper>
	
	<app_name>_DIR_RETENTION_DAYS=<number_of_days_to_retain_in_HDFS>
	
Example:
	
	TestData1_DIR=/test/pralay/userData
	TestData1_DIR_ZNODE=/pralay/userData
	TestData1_DIR_RETENTION_DAYS=10

4. Currently this application supports removal of a folder containing "date" and "timestamp" values present in their name. Any name which has below labels in their name :

	a) date=YYYYMMDD
	
	b) timestamp={10 digit or 13 digit}
	
	c) _YYYYMMDD
	
	d) _{10 digit or 13 digit timestamp}
	
	e) YYYYMMDD
	
	Examples : 
	
	/
	
	
	
5. One can use sample build file(retention_build.xml, attached in this repository) to build jar file from the source code. 

6. config.properties and log4j.xml are the associated file required for this application execution. 

7. Sample execution command has been attached (retention_execution_sample_cmd.txt) in this repository. One can refer to this sample command for running this application.
