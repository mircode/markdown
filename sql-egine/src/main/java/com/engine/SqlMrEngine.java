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
		
		// SQL����
		SqlMrEngine engine = new SqlMrEngine();
		// ִ��SQL
		engine.execute(path);
		
	}
	// ִ��SQL
	public void execute(String conf) throws IOException, ClassNotFoundException, InterruptedException {
		
		//#########################################
		// �� Job
		//#########################################
		
		//���ö���
		Configuration mainConf = new SqlEngineConf(conf);
		// ������Ŀ¼
		HDFSHelper.deleteOnExit(mainConf,"/sql/tmp/");
			
		// ��������
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
		// ���� Job
		//#########################################
		
		// ���ö���
		Configuration sortConf = new SqlEngineConf(conf);
		// ������Ŀ¼
		HDFSHelper.deleteOnExit(sortConf,"/sql/out/");
				
		// ��������
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
		// Job ������
		//#########################################
		
		// Job������
		JobControl jobControl = new JobControl(SqlMrEngine.class.getName()+"_JobChain");
		
		// ��Job
		ControlledJob cmainJob = new ControlledJob(mainJob.getConfiguration());
		cmainJob.setJob(mainJob);
		jobControl.addJob(cmainJob);
		
		// ����Job
		ControlledJob csortJob = new ControlledJob(sortJob.getConfiguration());
		csortJob.setJob(sortJob);
		csortJob.addDependingJob(cmainJob);
		jobControl.addJob(csortJob);
		
		// ����Job
		new Thread(jobControl).start();
        while (!jobControl.allFinished()) {
            Thread.sleep(500);
        }
        jobControl.stop();
	}
	
}
