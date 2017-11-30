# HDFS_Retention_App
This application is used for housekeeping purpose of HDFS data.
1. HDFS_Retention application is a housekeeping application to manage data in HDFS and Zookeeper. It's a java application.
2. This application is responsible to clean: Raw Event files, Ref Data files, Aggregations, ESRs.
3. This application is installed and deployed through SUF playlist.
4. Retention periods and paths of the ‘data to be deleted’ are configurable using config.properties: 
	<app_name>_DIR=<app_location>
	<app_name>_DIR_ZNODE=<path_of_zookeeper>
	<app_name>_DIR_RETENTION_DAYS=<number_of_days_to_retain_in_HDFS>
Example:
	ESR_flattened_DIR=/user/eea/eea_data/flatted/esr_kpi
	ESR_flattened_DIR_ZNODE=/Scheduler/Jobs/AppReady/bucketWriter
	ESR_flattened_DIR_RETENTION_DAYS=30
