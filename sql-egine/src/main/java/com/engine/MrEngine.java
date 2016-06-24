package com.engine;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.conf.SqlChain;
import com.conf.SqlConf;
import com.hdfs.HDFSHelper;
import com.mr.SortMapper;
import com.mr.SortReducer;
import com.mr.SqlCombiner;
import com.mr.SqlMapper;
import com.mr.SqlReducer;

public class MrEngine {

	public static void main(String[] args) throws Exception {

		// �����ļ�·��
		String path = SqlConf.class.getClassLoader().getResource("chain.conf")
				.getPath();
		path = URLDecoder.decode(path, "UTF-8");

		// SQL����
		MrEngine engine = new MrEngine();
		// ִ��SQL
		engine.execute(path);

	}

	// ִ��SQL
	public void execute(String path) throws IOException,
			ClassNotFoundException, InterruptedException {

		// ִ�ж���SQL
		List<SqlConf> confs = SqlChain.getChain(path);

		// ��ʽִ��SQL
		for (SqlConf conf : confs) {
			execute(conf);
		}
		// ��ӡִ�н��
		for (SqlConf conf : confs) {
			System.out.println(conf.sql);
			// ��ӡִ�н��
			System.out.println(conf.sqlParse.get("#mr.sort.format").replace(
					",", conf.split));
			List<String> result = HDFSHelper.readLines(conf, conf.get("#output")
					+ "/part-r-00000",20);
			for(String row:result)
				System.out.println(row);
		}
	}

	public void execute(SqlConf conf) throws IOException,
			ClassNotFoundException, InterruptedException {

		// #########################################
		// Job ������
		// #########################################

		// Job������
		JobControl jobControl = new JobControl(MrEngine.class.getName()
				+ "_JobChain");

		if (conf.isGroup) {
			Job mainJob = this.groupJob(conf);
			// ��Job
			ControlledJob cmainJob = new ControlledJob(
					mainJob.getConfiguration());
			cmainJob.setJob(mainJob);
			jobControl.addJob(cmainJob);

			Job sortJob = this.sortJob(conf);
			Boolean isSort = Boolean.parseBoolean(conf.get("#isSort"));
			if (isSort) {
				// ����Job
				ControlledJob csortJob = new ControlledJob(
						sortJob.getConfiguration());
				csortJob.setJob(sortJob);
				csortJob.addDependingJob(cmainJob);
				jobControl.addJob(csortJob);
			}
		} else {
			Job filterJob = this.filterJob(conf);
			// ��Job
			ControlledJob cfilterJob = new ControlledJob(
					filterJob.getConfiguration());
			cfilterJob.setJob(filterJob);
			jobControl.addJob(cfilterJob);
		}
		// ����Job
		new Thread(jobControl).start();

		// �ȴ�ִ�����,����ӡִ�н���
		while (!jobControl.allFinished()) {
			Thread.sleep(500);
			for (ControlledJob job : jobControl.getRunningJobList()) {
				this.display(job.getJob());
			}
		}
		jobControl.stop();
	}

	public void display(Job job) {
		try {
			// JobConf conf=(JobConf)job.getConfiguration();
			System.out.printf("Job " + job.getJobName()
					+ ": map: %.1f%% reduce %.1f%%\n",
					100.0 * job.mapProgress(), 100.0 * job.reduceProgress());
			// System.out.println("Job total maps = " + conf.getNumMapTasks());
			// System.out.println("Job total reduces = " +
			// conf.getNumReduceTasks());
			// System.out.flush();
		} catch (Exception e) {
		}

	}

	public Job filterJob(Configuration conf) throws IOException {
		// #########################################
		// ����Job
		// #########################################

		// ������Ŀ¼
		HDFSHelper.deleteOnExit(conf, conf.get("#output"));

		// ��������
		Path in = new Path(conf.get("#input"));
		Path out = new Path(conf.get("#output"));

		Job job = Job.getInstance(conf, MrEngine.class.getName() + "_filter");
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setMapperClass(SqlMapper.class);
		job.setReducerClass(SortReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);

		// System.exit(mainJob.waitForCompletion(true)?0:1);

		return job;
	}

	public Job groupJob(Configuration conf) throws IOException {
		// #########################################
		// ����Job
		// #########################################

		// ������Ŀ¼
		HDFSHelper.deleteOnExit(conf, conf.get("#tmp"));

		// ��������
		Path in = new Path(conf.get("#input"));
		Path out = new Path(conf.get("#tmp"));

		Job job = Job.getInstance(conf, MrEngine.class.getName() + "_main");
		FileInputFormat.setInputPaths(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setMapperClass(SqlMapper.class);
		job.setReducerClass(SqlReducer.class);
		job.setCombinerClass(SqlCombiner.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);

		// System.exit(mainJob.waitForCompletion(true)?0:1);

		return job;
	}

	public Job sortJob(Configuration conf) throws IOException {

		// #########################################
		// ���� Job
		// #########################################

		// ������Ŀ¼
		HDFSHelper.deleteOnExit(conf, conf.get("#output"));

		// ��������
		Path sortIn = new Path(conf.get("#tmp"));
		Path sortOut = new Path(conf.get("#output"));

		Job job = Job.getInstance(conf, MrEngine.class.getName() + "_sort");

		FileInputFormat.setInputPaths(job, sortIn);
		FileOutputFormat.setOutputPath(job, sortOut);

		job.setMapperClass(SortMapper.class);
		job.setReducerClass(SortReducer.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);

		return job;
	}

}
