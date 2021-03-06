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
	
	b) _YYYYMMDD
	
	c) YYYYMMDD
	
	d) timestamp={10 digit or 13 digit}
	
	e) _{10 digit or 13 digit timestamp}
	
	f) yyyyMMddHHmmssSSS
	
	g) _yyyyMMddHHmmssSSS
	
	Examples : 
	
	/testdata/pralay/date=20171130
	
	/testdata/pralaySample/timestamp=1511977202
	
	/test/pralay/transaction_1511977202326_1511976081373.txt
	
	/home/pralay/testdata/user_info_20171201.parquet
	
	/user/pralay/pralay_data/20170920
	
	
5. One can use sample build file(retention_build.xml, attached in this repository) to build jar file from the source code. 

6. config.properties and log4j.xml are the associated files required for this application execution. 

7. Sample execution command has been attached (retention_execution_sample_cmd.txt) in this repository. One can refer to this sample command for running this application.
