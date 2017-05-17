#Hive

### install hive 

1. Download hive

	[Download Hive](http://apache.stu.edu.tw/hive/)
	
	tar -xzvf file.zip

2.	Set ENV 

設定Hive環境變數
	
		export HIVE_HOME=/{you path}
		export PATH=$PATH:$HIVE_HOME/bin
		export CLASSPATH=$CLASSPATH:/Hadoop/lib/*:.
		export CLASSPATH=$CLASSPATH:/hive/lib/*:.
	
3.	Set Hive hdfs 權限
set authority Folders


		hadoop fs -mkdir /tmp
		hadoop fs -mkdir /user/hive/warehouse
		hadoop fs -chmod g+w /tmp
		hadoop fs -chmod g+w /user/hive/warehouse
		
4. Set hive-stie.xml


設定hive-site.xml
 	
		 //注意這個一定要設定false不然沒辦法使用
		<property>
			<name>hive.server2.enable.doAs</name>
			<value>false</value>
		</property>
	
		<property>
			<name>hive.server2.authentication</name>
			<value>CUSTOM</value>
		</property>
		
		<property>
			<name>hive.server2.custom.authentication.class</name>
			<value>com.lxw1234.hive.auth.CustomHiveServer2Auth</value>
		</property>
		
		//指定讀取user帳號 conf檔案
		<property>
			<name>hive.server2.custom.authentication.file</name>
			<value>/usr/local/apache-hive-0.13.1-bin/conf/hive.server2.users.conf</value>
		</property> 	
		
		
需要設定以下四個路徑，不然啟動hive會發生錯誤

	<property>
		<name>hive.metastore.warehouse.dir</name>
		<value>hdfs://xyz-slave3:9000/user/hive/warehouse</value>
		<description>location of default database for the warehouse</description>
	</property>

	 <property>
        <name>hive.exec.local.scratchdir</name>
        <value>/home/hadoop/hive-1.2.1/iotmp</value>
        <description>Local scratch space for Hive jobs</description>
    </property>
    <property>
        <name>hive.downloaded.resources.dir</name>
        <value>/home/hadoop/hive-1.2.1/_resources</value>
        <description>Temporary local directory for added resources in the remote file system.</description>
    </property>
    <property>
        <name>hive.querylog.location</name>
        <value>/home/hadoop/hive-1.2.1/logs</value>
        <description>Location of Hive run time structured log file</description>
    </property>
    <property>
        <name>hive.server2.logging.operation.log.location</name>
        <value>/home/test/hive-1.2.1/logs/operation_logs</value>
        <description>Top level directory where operation logs are stored if logging functionality is enabled</description>
    </property>
		
		
set jdbc save path

	<property>
		<name>javax.jdo.option.ConnectionURL</name>
		<value>jdbc:derby:;databaseName={yourpath}metastore_db;create=true</value>
		<description>
		JDBC connect string for a JDBC metastore.
		To use SSL to encrypt/authenticate the connection, provide database-specific SSL flag in the connection URL.
		For example, jdbc:postgresql://myhost/db?ssl=true for postgres database.
		</description>
	</property>	


5. set hadoop core-site.xml
set hadoop authority Folders in hive

		<property>
			<name>hadoop.proxyuser.hive.hosts</name>
			<value>*</value>
		</property>
		
		<property>
			<name>hadoop.proxyuser.hive.groups</name>
			<value>*</value>
		</property>

6. set hive login Authenticate
set hive-site.xml


		<property>
			<name>hive.server2.custom.authentication.file</name>
			<value>/home/hadoop/hive-2.1.1/conf/hive.server2.users.conf</value>
			<description>
			the user account username password , password is md5
			</description>
		</property>


在hive/conf建立一個檔案為 hive.server2.users.conf
將帳號輸入注意是password為`md5` 

	neil,8yz7z80454y00ii80409x83yri6r2416
	xyz,04pix95i207y6ix1194rr253yi81prp3

新增一個專案放入以下code，打包好將jar放在hive/lib底下，密碼為加密md5，如需要加入新的帳號密碼，密碼要轉成md5。

	package com.xyzprinting.hive.auth;
	
	
	import java.io.BufferedReader;
	import java.io.File;
	import java.io.FileReader;
	import java.io.IOException;
	import java.security.MessageDigest;
	import java.security.NoSuchAlgorithmException;
	
	import javax.security.sasl.AuthenticationException;
	
	import org.apache.hadoop.conf.Configuration;
	import org.apache.hadoop.hive.conf.HiveConf;
	import org.apache.hive.service.auth.PasswdAuthenticationProvider;
	
	public class HiveAuthenticator implements PasswdAuthenticationProvider  {
	
	    @Override
	    public void Authenticate(String username, String password)
	            throws AuthenticationException {
	
	        boolean ok = false;
	        String passMd5 = new MD5().md5(password);
	        HiveConf hiveConf = new HiveConf();
	        Configuration conf = new Configuration(hiveConf);
	        String filePath = conf.get("hive.server2.custom.authentication.file");
	        System.out.println("hive.server2.custom.authentication.file [" + filePath + "] ..");
	        File file = new File(filePath);
	        BufferedReader reader = null;
	        try {
	            reader = new BufferedReader(new FileReader(file));
	            String passwordData = null;
	            while ((passwordData = reader.readLine()) != null) {
	                String[] datas = passwordData.split(",", -1);
	                if(datas.length != 2) continue;
	                //check user account is OK
	                if(datas[0].equals(username) && datas[1].equals(passMd5)) {
	                    ok = true;
	                    break;
	                }
	            }
	            reader.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	            throw new AuthenticationException("read authentication config file error, [" + filePath + "] ..", e);
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {}
	            }
	        }
	        if(ok) {
	            System.out.println("user account [" + username + "] authentication check ok .. ");
	        } else {
	            System.out.println("user account [" + username + "] authentication check fail .. ");
	            throw new AuthenticationException("user account [" + username + "] authentication check fail .. ");
	        }
	    }
	
	    //MD5加密
	    class MD5 {
	        private MessageDigest digest;
	        private char hexDigits[] = {'0','1','2','3','4','5','6','7','8','9','x','y','z','p','r','i','n','t'};//可自行修改
	        public MD5() {
	            try {
	                digest = MessageDigest.getInstance("MD5");
	            } catch (NoSuchAlgorithmException e) {
	                throw new RuntimeException(e);
	            }
	        }
	
	        public String md5(String str) {
	            byte[] btInput = str.getBytes();
	            digest.reset();
	            digest.update(btInput);
	            byte[] md = digest.digest();
	            // change password to 16 byte
	            int j = md.length;
	            char strChar[] = new char[j * 2];
	            int k = 0;
	            for (int i = 0; i < j; i++) {
	                byte byte0 = md[i];
	                strChar[k++] = hexDigits[byte0 >>> 4 & 0xf];
	                strChar[k++] = hexDigits[byte0 & 0xf];
	            }
	            return new String(strChar);
	        }
	    }
	
	}



7. run hive server
use jdbc,copy hive-jdbc to lib

		cd hive-2.1.1
		cp jdbc/hive-jdbc-2.1.1-standalone.jar lib/
		
		//add env 
		vi .bashrc
		export CLASSPATH=$CLASSPATH:/home/hadoop/hive-2.1.1/lib/*:.
		export CLASSPATH=$CLASSPATH:/home/hadoop/hadoop-2.6.4/lib/*:.


Stpe1 初始化Schema 
 
	schematool -initSchema -dbType derby

Stpe2 使用Hiveserver2

	nohup bin/hiveserver2 &

注意不能一次啟動兩個程式會有問題 例如 hive --service metastore or hiveserver2
	
	//save data schema
	bin/hive --service metastore > metastore.log 2>&1 &
	//jdbc connect
	bin/hive --service hiveserver2 > hiveserver2.log 2>&1 &
	
	//查看log
	hive --service hiveserver2 --hiveconf hive.server2.thrift.port=10000 --hiveconf hive.root.logger=INFO,console
	
	//show log
	-hiveconf hive.root.logger=DEBUG,console
	

hiveserver2 server起啟動使用beeline 下hive sql
start 'beeline'

connect hive databases, 連接:url,user,pw

	!connect jdbc:hive2://xyz-slave3:10000/ neil neillin
	
	//return message
	Connected to: Apache Hive (version 2.1.1)
	Driver: Spark Project Core (version 1.6.2)
	Transaction isolation: TRANSACTION_REPEATABLE_READ
	
	
	//select database;	
	show databases;

	//return message
	+----------------+--+
	| database_name  |
	+----------------+--+
	| default        |
	| xyzprinting    |
	+----------------+--+



### hive HA配置

[說明文件 http://lxw1234.com/archives/2016/05/675.htm](http://lxw1234.com/archives/2016/05/675.htm)


### 從sql server 匯入 hive
use sqoop toole 從sql server data table to hive table;
create hive

	sqoop create-hive-table --connect "jdbc:sqlserver://v2f9jotfte.database.windows.net:1433;databaseName=xyzprinterlog" --table FilamentLog --username cloud.xyzprinting@v2f9jotfte -P --hive-table FilamentLog ;

	sqoop import --connect "jdbc:sqlserver://v2f9jotfte.database.windows.net:1433;databaseName=xyzprinterlog" --username cloud.xyzprinting@v2f9jotfte -P --table FilamentLog --hive-import --hive-table FilamentLog --warehouse-dir /user/hive/warehouse;



### hive insert timestamp or select timestamp or datetime
日期存成bigint 時間要轉成timestamp 以下為查詢語法
	
	//查詢DB ，info.birthday 為日期，後面為時間格式
	from_unixtime(info.birthday, 'yyyy-MM-dd hh:mm:ss') > '2017-01-01'

	//寫入DB
	unix_timestamp('2017-01-01 12:44:22','yyyy-MM-dd HH:mm:ss'),

timestamp type 

	//寫入DB now time fromat '2017-04-19 16:54:49.865'
	current_timestamp()  
	//指定時間
	cast('2017-04-20 17:45:30.865' as timestamp)

	//查詢DB
	from_unixtime(unix_timestamp(info.birthday))

date type

	//寫入DB
	Date('日期')
	
	//查詢 
	info.birthday > '日期'


resource 
	
[時間格式說明 http://www.cloudera.com/documentation/cdh](http://www.cloudera.com/documentation/cdh/5-1-x/Impala/Installing-and-Using-Impala/ciiu_datetime_functions.html)


### load csv data to hive
將資料寫入hdfs

	load data local inpath '/home/hadoop/employs.csv'
	overwrite into table employs;
	
寫入完成後使用select

	select * from employs;
id  | name| product	
--- | --- | ---
1| neil |good
2 |neil |bad
3| peter| dog
4| peter| cat


### 寫入 STRUCT , MAP , Array data
使用STRUCT , MAP , Array Type 寫入方法，

	create table test (life_events STRUCT<event:STRING> )
	
	//struct key為'event'
	insert into table test select NAMED_STRUCT('key','info') as life_events from dummy_hive

	//Map
	create table test ( a MAP<STRING, INT>)
	insert into test SELECT MAP('neil',3)  from dummy_hive


	//Array
	create table arrayDemo( list array<String> );
	insert into arraydemo select ARRAY('Paperino', 'Topolino') as list from dummy;

在查詢時欄位時直接指定需要的Key欄位(life_events.event = 'info')，Array可使用like去比對

注意： 在寫入這幾種type時需先建立'假的table 欄位'

	//欄位只需要一個即可，寫入內容都可
	create table dummy_hive (event STRING);

	insert into table dummy_hive values ( 'neil' ) ; 


###  Database 指定path
set database path 
	
	create table user
	{
		id int,
		name String,
		age int
	} 
	ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' 
	STORED AS TEXTFILE
	LOCATION  '/user/hive/db/tableName';



### set hive ACID using update or delete


	// change tmp
	hadoop dfs -chmod 700 /tmp
	
	//set hive-site.xml
	export HADOOP_USER_CLASSPATH_FIRST=true
	set hive.support.concurrency = true;
	set hive.enforce.bucketing = true;
	set hive.exec.dynamic.partition.mode = nonstrict;
	set hive.txn.manager = org.apache.hadoop.hive.ql.lockmgr.DbTxnManager;
	set hive.compactor.initiator.on = true;
	set hive.compactor.worker.threads = 1;


1. 一般table cretae table

		CREATE TABLE college(clg_id int,clg_name string,clg_loc string) 
		clustered by (clg_id) into 5 buckets stored as orc TBLPROPERTIES('transactional'='true');
		
		//insert
		INSERT INTO table college values
		(1,'nec','nlr'),
		(2,'vit','vlr'),
		(3,'srm','chen'),
		(4,'lpu','del'),
		(5,'stanford','uk'),
		(6,'JNTUA','atp'),
		(7,'cambridge','us');
		
		//update
		update college set clg_name = 'neil' where clg_id = 5;
	
		//delete 
		delete from college where clg_id = 5;


2. Type 使用Array,Map ,STRUCT 

		CREATE TABLE usertest1(
		life_events 
			STRUCT < event:STRING >,
		living 	
			STRUCT < location:String,
				hometown:String,
				move_here_total:STRUCT < move_here:ARRAY < STRING >>>,
		id INT
		)
		CLUSTERED BY (id) into 3 buckets
		ROW FORMAT DELIMITED
		FIELDS TERMINATED BY ','
		STORED as orc tblproperties('transactional'='true');
		
		//需要create 假table
		create table dummy_hive (event STRING);
		insert into table dummy_hive values ( 'neil' ) ; 
		
		
		insert INTO TABLE usertest1 SELECT
		NAMED_STRUCT( 'event','hello' ) as life_events,  
		NAMED_STRUCT( 'location','taiwan', 'hometown','taipie','move_here_total',NAMED_STRUCT('move_here',Array('data','good'))  ) as living ,
		11223 as id FROM dummy_hive
		
		select * from usertest1
		
		delete from usertest1 where id = 11223

resource 

[ACID 說明 https://hortonworks.com/hadoop-tutorial](https://hortonworks.com/hadoop-tutorial/using-hive-acid-transactions-insert-update-delete-data/)

### table Stored to json format
將資料儲存為csv檔案格式,這沒辦法跟ACID同時使用

	// add hive/lib
	https://mvnrepository.com/artifact/org.apache.hive.hcatalog/hive-hcatalog-core
	
	
	//create table to json format
	CREATE TABLE my_table
	(
		id int,
		name bigint
	)
	ROW FORMAT SERDE 'org.apache.hive.hcatalog.data.JsonSerDe'
	STORED AS TEXTFILE;
	
	insert into my_table select 11233, 'neil'










	