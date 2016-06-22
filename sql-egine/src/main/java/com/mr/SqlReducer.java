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

		 String split=this.table.getSplit();
		 // 更具matrix和group计算新的table格式 
		 String matrix = sqlParse.get("matrix");
		 String group=sqlParse.get("group by");
		 
		 String format="";
		 String[] matrixs = matrix.split(",");
		 for (String mtrix : matrixs) {

			String func = SqlParse.getMetrix(mtrix, "func");
			String field = SqlParse.getMetrix(mtrix, "field");
			String alias = SqlParse.getMetrix(mtrix, "alias");
			
			format+=func.substring(0,1)+"#"+field+split;
		 }
		 
		 if(group!=null){
				format = group.replace(",", split) + split + format.substring(0,format.length()-1);
		 }
		 
		table.setFormat(format);
		table.setRows(getRows(values));
		SqlEngine sqlEngine = new SqlEngine(table);
		
		matrix=matrix.replaceAll("([s,a,m,n,c])(um|vg|ax|in|ount)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)","$1$2($1#$3) as $4");
		// 执行聚合操作
		//String matrix = sqlParse.get("matrix");
		//String group=sqlParse.get("group by");
		if(matrix!=null){
			sqlEngine.group(matrix,group);
		}
		
		// 执行过滤
		String select = sqlParse.get("select");
		
		select=select.replaceAll("([s,a,m,n,c])(um|vg|ax|in|ount)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)","$1$2($1#$3) as $4");
				
		
		if(select!=null){
			sqlEngine.select(select);
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
