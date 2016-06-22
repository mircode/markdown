package com.mr;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class SortMapper extends Mapper<Object,Text,Text,Text> {
	
	 public void map(Object key,Text value,Context context) throws IOException, InterruptedException {
		context.write(new Text("sort"),new Text(value));
	 }
}
