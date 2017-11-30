# HDFS_Retention_App
This application is used for housekeeping purpose of HDFS data.
1. HDFS_Retention application is a housekeeping application to manage data in HDFS and Zookeeper. It's a java application.
2. This application is responsible to clean any HDFS data which has "expired" according to application configuration.
3. This application is installed and deployed through SUF playlist.
4. Retention periods and paths of the ‘data to be deleted’ are configurable using config.properties: 

	<app_name>_DIR=<app_location>
	
	<app_name>_DIR_ZNODE=<path_of_zookeeper>
	
	<app_name>_DIR_RETENTION_DAYS=<number_of_days_to_retain_in_HDFS>
	
Example:
	
	TestData1_DIR=/test/pralay/userData
	TestData1_DIR_ZNODE=/pralay/userData
	TestData1_DIR_RETENTION_DAYS=10

5. Currently this application supports removal of a folder containing "date" and "timestamp" values present in their name. Any name which has below types - 
	a) date=YYYYMMDD
	b) timestamp={10 digit or 13 digit}
	c) _YYYYMMDD
	d) _{10 digit or 13 digit timestamp}
	e) YYYYMMDD
	
