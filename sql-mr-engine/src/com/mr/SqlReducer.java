package com.mr;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.log.LogConf;
import com.sql.SqlParse;

/**
 * 执行SQL的Reducer函数
 * @author 魏国兴
 */
public class SqlReducer extends Reducer<Text,Text,Text,Text> {
	
	 private LogConf logConf=null;
	 private SqlParse sqlParse=null;
	 
	 public void setup(Context context) throws IOException, InterruptedException {
		 String sql=context.getConfiguration().get(LogConf.LOG_SQL);
		 String split=context.getConfiguration().get(LogConf.LOG_SPLIT);
		 String format=context.getConfiguration().get(LogConf.LOG_FORMAT);
		 
		 //logConf=new LogConf(split,format);
		 //sqlParse=new SqlParse(sql);
	 }
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		
	}
}
