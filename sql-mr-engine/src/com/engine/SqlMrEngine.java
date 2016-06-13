package com.engine;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

import com.hdfs.HDFSHelper;
import com.log.LogConf;
import com.sql.FileTable;
import com.sql.SqlParse;

public class SqlMrEngine {

	
	private String conf;
	
	public static void main(String[] args) throws Exception {
		// SQL引擎
		SqlMrEngine engine = new SqlMrEngine();
		// 执行SQL
		engine.execute();
		// 关闭引擎
		engine.close();
	}
	// 执行SQL
	public void execute() throws IOException {
		
		// Hadoop配置对象
		Configuration mainConf = new Configuration();
		Job mainJob = Job.getInstance(mainConf,SqlMrEngine.class.getName());
		
		// 日志配置对象
		LogConf logConf=new LogConf(conf);
		
		// 将需要执行的sql加载到context中
		mainConf.set(LogConf.LOG_SQL,logConf.getSql());

		
		
		//创建临时目录
		String tmpDir = new String("/tmp");
		HDFSHelper.createDirectory(mainConf,tmpDir);
		
		
		// 将需要join的表缓存到cachefile中
		Map<String,FileTable> tables=logConf.getTables();
		// 解析需要join的表
		SqlParse sqlParse=new SqlParse(logConf.getSql());
				
		for (String table : sqlParse.getJoinTables()) {
			
			FileTable joinTable=tables.get(table);
			FileStatus[] fileStatus = HDFSHelper.listStatus(mainConf,joinTable.getInput());
			
			if(fileStatus.length == 1) {
				mainJob.addCacheFile(fileStatus[0].getPath().toUri());
			} else {
				
				String tmpFile = new String(tmpDir+"/"+joinTable.getName());
				HDFSHelper.mergeFiles(mainConf,joinTable.getInput(), tmpFile);
				joinTable.setInput(tmpDir+"/"+joinTable.getName());
				
				mainJob.addCacheFile(new Path(tmpFile).toUri());
			}
		}
		for(Map.Entry<String, FileTable> entry: tables.entrySet()){
			mainConf.set(entry.getKey(),entry.getValue().serialize());
		}
	}
	// 关闭引擎
	public void close() {
		
	}
}
