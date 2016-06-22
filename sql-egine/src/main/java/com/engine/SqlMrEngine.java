package com.engine;

import java.io.IOException;
import java.net.URLDecoder;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.hdfs.HDFSHelper;
import com.log.SqlEngineConf;
import com.mr.SortMapper;
import com.mr.SortReducer;
import com.mr.SqlCombiner;
import com.mr.SqlMapper;
import com.mr.SqlReducer;

public class SqlMrEngine {

	public static void main(String[] args) throws Exception {
		
		String path=SqlEngineConf.class.getClassLoader().getResource("log.conf").getPath();
		path = URLDecoder.decode(path, "UTF-8");
		
		// SQL引擎
		SqlMrEngine engine = new SqlMrEngine();
		// 执行SQL
		engine.execute(path);
		
	}
	// 执行SQL
	public void execute(String conf) throws IOException, ClassNotFoundException, InterruptedException {
		
		//#########################################
		// 主 Job
		//#########################################
		
		//配置对象
		Configuration mainConf = new SqlEngineConf(conf);
		// 清除输出目录
		HDFSHelper.deleteOnExit(mainConf,"/sql/tmp/");
			
		// 设置输入
		Path in = new Path("/sql/student.txt");
		Path out = new Path("/sql/tmp");
		
		
		Job mainJob = Job.getInstance(mainConf,SqlMrEngine.class.getName()+"_main");
		FileInputFormat.setInputPaths(mainJob, in);
		FileOutputFormat.setOutputPath(mainJob, out);
		
		mainJob.setMapperClass(SqlMapper.class);
		mainJob.setReducerClass(SqlReducer.class);
		mainJob.setCombinerClass(SqlCombiner.class);
		
		mainJob.setMapOutputKeyClass(Text.class);
		mainJob.setMapOutputValueClass(Text.class);
		
		mainJob.setOutputKeyClass(NullWritable.class);
		mainJob.setOutputValueClass(Text.class);
		
		//System.exit(mainJob.waitForCompletion(true)?0:1);
		
		
		//#########################################
		// 排序 Job
		//#########################################
		
		// 配置对象
		Configuration sortConf = new SqlEngineConf(conf);
		// 清除输出目录
		HDFSHelper.deleteOnExit(sortConf,"/sql/out/");
				
		// 设置输入
		Path sortIn = new Path("/sql/tmp/part-r-00000");
		Path sortOut = new Path("/sql/out");
				
		Job sortJob = Job.getInstance(sortConf,SqlMrEngine.class.getName()+"_sort");
		
		FileInputFormat.setInputPaths(sortJob, sortIn);
		FileOutputFormat.setOutputPath(sortJob, sortOut);
		
		sortJob.setMapperClass(SortMapper.class);
		sortJob.setReducerClass(SortReducer.class);
		
		sortJob.setMapOutputKeyClass(Text.class);
		sortJob.setMapOutputValueClass(Text.class);
		
		sortJob.setOutputKeyClass(NullWritable.class);
		sortJob.setOutputValueClass(Text.class);
		
		
		//#########################################
		// Job 控制器
		//#########################################
		
		// Job控制器
		JobControl jobControl = new JobControl(SqlMrEngine.class.getName()+"_JobChain");
		
		// 主Job
		ControlledJob cmainJob = new ControlledJob(mainJob.getConfiguration());
		cmainJob.setJob(mainJob);
		jobControl.addJob(cmainJob);
		
		// 排序Job
		ControlledJob csortJob = new ControlledJob(sortJob.getConfiguration());
		csortJob.setJob(sortJob);
		csortJob.addDependingJob(cmainJob);
		jobControl.addJob(csortJob);
		
		// 运行Job
		new Thread(jobControl).start();
        while (!jobControl.allFinished()) {
            Thread.sleep(500);
        }
        jobControl.stop();
	}
	
}
