package com.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.log.LogConf;
import com.sql.FileTable;
import com.sql.SqlEngine;
import com.sql.SqlParse;

/**
 * 执行SQL的Mapper函数
 * @author 魏国兴
 */
public class SqlMapper extends Mapper<Object,Text,Text,Text> {

	 private SqlParse sqlParse=null;
	 private Map<String,FileTable> tableMap=null; 
	 
	 public void setup(Context context) throws IOException, InterruptedException {
		 
		/* // 读取配置
		 String sql=context.getConfiguration().get(LogConf.LOG_SQL);
		 // 构造配置对象和SQL解析器
		 sqlParse=new SqlParse(sql);
		 
		 
		 FileSystem hdfs = FileSystem.get(context.getConfiguration());
		 
		 // 查询sql中所涉及的要Join的表名称
		 List<String> tables=sqlParse.getJoinTables();
		 for(String table : tables){
			String line=context.getConfiguration().get(table);
			FileTable fileTable=new FileTable();
			tableMap.put(fileTable.getName(),fileTable);
		 }
		 
		 super.setup(context);*/
	 }
	 public void map(Object key,Text value,Context context) throws IOException, InterruptedException {
		 
		
		
		List<String> rows=new ArrayList<String>(); 
		rows.add(value.toString());
		FileTable main=new FileTable();
		
		
		// 表连接
		new SqlEngine(main).join(new FileTable(), "");
		// 条件查询
		new SqlEngine(main).where(sqlParse.get("where")).select(sqlParse.get("select"));
		
		// 分组查询
		new SqlEngine(main).where(sqlParse.get("where")).group(sqlParse.get("select"),sqlParse.get("group"));
		
		
	 }
	 public String getColumn(String value,String column){
		 return null;
	 }
	 
	 
}
