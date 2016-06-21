package com.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.file.Table;
import com.log.LogConf;
import com.sql.SqlEngine;
import com.sql.SqlParse;

/**
 * 执行SQL的Reducer函数
 * 
 * @author 魏国兴
 */
public class SqlReducer extends Reducer<Text, Text, NullWritable, Text> {

	private Table table = null;
	private SqlParse sqlParse = null;
	

	public void setup(Context context) throws IOException, InterruptedException {
		// sql
		String sql=context.getConfiguration().get(LogConf.LOG_SQL);
		sqlParse=new SqlParse(sql);
	
	}

	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		 
		 
		 String ser=null;
		 // main表
		 if(sqlParse.get("join")!=null){
			 ser=context.getConfiguration().get("join_table_format");
		 }else{
			  ser=context.getConfiguration().get(sqlParse.getMainTable());
		 }
		 this.table=new Table().diserialize(ser);

		table.setFormat("t.id|t.name|t.count");
		table.setRows(getRows(values));
		SqlEngine sqlEngine = new SqlEngine(table);
		
		System.out.println(table);
		// 执行聚合操作
		String matrix = sqlParse.get("matrix");
		String group=sqlParse.get("group by");
		if(matrix!=null){
			sqlEngine.group("count(t.count)",group);
		}
		
		// 执行过滤
		String select = sqlParse.get("select");
		if(select!=null){
			sqlEngine.select("t.name,count(t.count)");
		}
		
		for(String row:sqlEngine.getTable().getRows()){
			context.write(NullWritable.get(),new Text(row));
		}
	}

	private List<String> getRows(Iterable<Text> values) {
		List<String> rows = new ArrayList<String>();
		for (Text t : values) {
			rows.add(t.toString());
		}
		return rows;
	}

}
