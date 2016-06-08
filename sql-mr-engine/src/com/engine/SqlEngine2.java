package com.engine;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

import com.hdfs.HDFSHelper;
import com.log.LogConf;
import com.log.LogTable;
import com.sql.SqlParse;

public class SqlEngine2 {

	
	private String confile;
	
	public static void main(String[] args) throws Exception {
		
		
		// SQL引擎
		SqlEngine2 engine = new SqlEngine2();
		// 执行SQL
		engine.execute();
		// 关闭引擎
		engine.close();
	}
	// 执行SQL
	public void execute() throws IOException {
		
		/*// Hadoop配置对象
		Configuration mainConf = new Configuration();
		
		// 日志配置对象
		LogConf logConf=new LogConf(confile);
		
		// 将log映射table的映射关系加载到context中
		Map<String,LogTable> tables=logConf.getTableMap();
		for(Map.Entry<String, LogTable> entry: tables.entrySet()){
			mainConf.set(entry.getKey(),entry.getValue().toString());
		}
		// 将需要执行的sql加载到context中
		mainConf.set(LogConf.LOG_SQL,logConf.getSql());
		
		SqlParse sqlParse=new SqlParse(logConf.getSql());
		List<String> joinTables=sqlParse.getJoinTables();
		Map<String,LogTable> tableMap=logConf.getTableMap();
		
		//创建临时目录
		String tmpDir = new String("/tmp");
		HDFSHelper.createDirectory(mainConf,tmpDir);

		Job mainJob = Job.getInstance(mainConf,SqlEngine2.class.getName());
		
		// 将需要join的表缓存到cachefile中
		for (String table : joinTables) {
			
			LogTable joinTable=tableMap.get(table);
			FileStatus[] fileStatus = HDFSHelper.listStatus(mainConf,joinTable.getInput());
			
			if(fileStatus.length == 1) {
				mainJob.addCacheFile(fileStatus[0].getPath().toUri());
			} else {
				String tmpFile = new String(tmpDir+"/"+joinTable.getName());
				HDFSHelper.mergeFiles(mainConf,joinTable.getInput(), tmpFile);
				mainJob.addCacheFile(new Path(tmpFile).toUri());
			}
		}*/
		
	}
	// 关闭引擎
	public void close() {
		
	}
}
