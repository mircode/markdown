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
		// SQL����
		SqlMrEngine engine = new SqlMrEngine();
		// ִ��SQL
		engine.execute();
		// �ر�����
		engine.close();
	}
	// ִ��SQL
	public void execute() throws IOException {
		
		// Hadoop���ö���
		Configuration mainConf = new Configuration();
		Job mainJob = Job.getInstance(mainConf,SqlMrEngine.class.getName());
		
		// ��־���ö���
		LogConf logConf=new LogConf(conf);
		
		// ����Ҫִ�е�sql���ص�context��
		mainConf.set(LogConf.LOG_SQL,logConf.getSql());

		
		
		//������ʱĿ¼
		String tmpDir = new String("/tmp");
		HDFSHelper.createDirectory(mainConf,tmpDir);
		
		
		// ����Ҫjoin�ı��浽cachefile��
		Map<String,FileTable> tables=logConf.getTables();
		// ������Ҫjoin�ı�
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
	// �ر�����
	public void close() {
		
	}
}
