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
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.file.Table;
import com.hdfs.HDFSHelper;
import com.log.LogConf;
import com.mr.SortMapper;
import com.mr.SortReducer;
import com.mr.SqlCombiner;
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
		
		// SQL引擎
		SqlMrEngine engine = new SqlMrEngine(path);
		// 执行SQL
		engine.execute();
		// 关闭引擎
		engine.close();
	}
	// 执行SQL
	public void execute() throws IOException, ClassNotFoundException, InterruptedException {
		
		
		
		// Hadoop配置对象
		Configuration mainConf = new Configuration();
		
		
		// 日志配置对象
		LogConf logConf=new LogConf(conf);
		// 将需要执行的sql加载到context中
		mainConf.set(LogConf.LOG_SQL,logConf.sql);

		
		// 保存表的映射关系到context中
		Map<String,Table> tables=logConf.tables;
		for(Map.Entry<String, Table> entry: tables.entrySet()){
			String ser=entry.getValue().serialize();
			mainConf.set(entry.getKey(),ser);
		}
		// 计算Join之后的表结构
		SqlParse sqlParse=new SqlParse(logConf.sql);
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
		
		// 清除输出目录
		HDFSHelper.deleteOnExit(mainConf,"/sql/tmp/");
				
		Job mainJob = Job.getInstance(mainConf,SqlMrEngine.class.getName());
		
		// 设置输入
		Path in = new Path("/sql/student.txt");
		Path out = new Path("/sql/tmp");
		
		FileInputFormat.setInputPaths(mainJob, in);
		FileOutputFormat.setOutputPath(mainJob, out);
		
		mainJob.setMapperClass(SqlMapper.class);
		mainJob.setReducerClass(SqlReducer.class);
		mainJob.setCombinerClass(SqlCombiner.class);
		
		mainJob.setMapOutputKeyClass(Text.class);
		mainJob.setMapOutputValueClass(Text.class);
		
		mainJob.setOutputKeyClass(NullWritable.class);
		mainJob.setOutputValueClass(Text.class);
		
		// 7.等待程序运行退出
		//System.exit(mainJob.waitForCompletion(true)?0:1);
		
		// Hadoop配置对象
		Configuration sortConf = new Configuration();
		sortConf.set(LogConf.LOG_SQL,logConf.sql);
		sortConf.set("join_table_format",sqlEngine.getTable().serialize());
		
		// 清除输出目录
		HDFSHelper.deleteOnExit(sortConf,"/sql/out/");
				
		Job sortJob = Job.getInstance(sortConf,SqlMrEngine.class.getName()+"sort");
		
		// 设置输入
		Path sortIn = new Path("/sql/tmp/part-r-00000");
		Path sortOut = new Path("/sql/out");
		
		FileInputFormat.setInputPaths(sortJob, sortIn);
		FileOutputFormat.setOutputPath(sortJob, sortOut);
		
		sortJob.setMapperClass(SortMapper.class);
		sortJob.setReducerClass(SortReducer.class);
		
		sortJob.setMapOutputKeyClass(Text.class);
		sortJob.setMapOutputValueClass(Text.class);
		
		sortJob.setOutputKeyClass(NullWritable.class);
		sortJob.setOutputValueClass(Text.class);
		
		JobControl jobControl = new JobControl("JobChain");
		
		ControlledJob cmainJob = new ControlledJob(mainJob.getConfiguration());
		cmainJob.setJob(mainJob);
		
		ControlledJob csortJob = new ControlledJob(sortJob.getConfiguration());
		csortJob.setJob(sortJob);
		
		csortJob.addDependingJob(cmainJob);
		
		jobControl.addJob(cmainJob);
		jobControl.addJob(csortJob);
		
		new Thread(jobControl).start();
        while (!jobControl.allFinished()) {
            Thread.sleep(500);
        }
        jobControl.stop();
	}
	// 关闭引擎
	public void close() {
		
	}
}
