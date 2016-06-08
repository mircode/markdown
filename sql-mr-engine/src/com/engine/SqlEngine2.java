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
		
		
		// SQL����
		SqlEngine2 engine = new SqlEngine2();
		// ִ��SQL
		engine.execute();
		// �ر�����
		engine.close();
	}
	// ִ��SQL
	public void execute() throws IOException {
		
		/*// Hadoop���ö���
		Configuration mainConf = new Configuration();
		
		// ��־���ö���
		LogConf logConf=new LogConf(confile);
		
		// ��logӳ��table��ӳ���ϵ���ص�context��
		Map<String,LogTable> tables=logConf.getTableMap();
		for(Map.Entry<String, LogTable> entry: tables.entrySet()){
			mainConf.set(entry.getKey(),entry.getValue().toString());
		}
		// ����Ҫִ�е�sql���ص�context��
		mainConf.set(LogConf.LOG_SQL,logConf.getSql());
		
		SqlParse sqlParse=new SqlParse(logConf.getSql());
		List<String> joinTables=sqlParse.getJoinTables();
		Map<String,LogTable> tableMap=logConf.getTableMap();
		
		//������ʱĿ¼
		String tmpDir = new String("/tmp");
		HDFSHelper.createDirectory(mainConf,tmpDir);

		Job mainJob = Job.getInstance(mainConf,SqlEngine2.class.getName());
		
		// ����Ҫjoin�ı��浽cachefile��
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
	// �ر�����
	public void close() {
		
	}
}
