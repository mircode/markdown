package com.engine;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.file.Table;
import com.hdfs.HDFSHelper;
import com.log.LogConf;
import com.mr.SqlMapper;
import com.mr.SqlReducer;
import com.sql.SqlEngine;
import com.sql.SqlParse;

public class SqlMrEngine {

	
	private String conf;
	
	public SqlMrEngine(String conf){
		this.conf=conf;
	}
	public static void main(String[] args) throws Exception {
		
		String path=LogConf.class.getClassLoader().getResource("log.conf").getPath();
		path = URLDecoder.decode(path, "UTF-8");
		
		// SQL����
		SqlMrEngine engine = new SqlMrEngine(path);
		// ִ��SQL
		engine.execute();
		// �ر�����
		engine.close();
	}
	// ִ��SQL
	public void execute() throws IOException, ClassNotFoundException, InterruptedException {
		
		// Hadoop���ö���
		Configuration mainConf = new Configuration();
		
		
		// ��־���ö���
		LogConf logConf=new LogConf(conf);
		// ����Ҫִ�е�sql���ص�context��
		mainConf.set(LogConf.LOG_SQL,logConf.getSql());

		
		// ������ӳ���ϵ��context��
		Map<String,Table> tables=logConf.getTables();
		for(Map.Entry<String, Table> entry: tables.entrySet()){
			String ser=entry.getValue().serialize();
			mainConf.set(entry.getKey(),ser);
		}
		// ����Join֮��ı�ṹ
		SqlParse sqlParse=new SqlParse(logConf.getSql());
		SqlEngine sqlEngine=new SqlEngine(tables.get(sqlParse.getMainTable()));
		String join = sqlParse.get("join");
		if (join != null) {
			for (String en : join.split("\\|")) {
				String table = en.split("on")[0];
				String on = en.split("on")[1];
				String name = sqlParse.getTable(table);
				sqlEngine.join(tables.get(name), on);
			}
		}
		mainConf.set("join_table_format",sqlEngine.getTable().serialize());
		
		// ������Ŀ¼
		HDFSHelper.deleteOnExit(mainConf,"/sql/out/");
				
		Job mainJob = Job.getInstance(mainConf,SqlMrEngine.class.getName());
		
		// ��������
		Path in = new Path("/sql/student.txt");
		Path out = new Path("/sql/out");
		
		FileInputFormat.setInputPaths(mainJob, in);
		FileOutputFormat.setOutputPath(mainJob, out);
		
		mainJob.setMapperClass(SqlMapper.class);
		mainJob.setReducerClass(SqlReducer.class);

		mainJob.setMapOutputKeyClass(Text.class);
		mainJob.setMapOutputValueClass(Text.class);
		
		mainJob.setOutputKeyClass(NullWritable.class);
		mainJob.setOutputValueClass(Text.class);
		
		
		System.exit(mainJob.waitForCompletion(true)?0:1);
	}
	// �ر�����
	public void close() {
		
	}
}
