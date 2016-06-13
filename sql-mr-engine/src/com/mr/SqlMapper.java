package com.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.log.LogConf;
import com.sql.HdfsFileTable;
import com.sql.SqlEngine;
import com.sql.SqlParse;

/**
 * ִ��SQL��Mapper����
 * @author κ����
 */
public class SqlMapper extends Mapper<Object,Text,Text,Text> {

	 private SqlParse sqlParse=null;
	 private HdfsFileTable mainTable=null;
	 private Map<String,HdfsFileTable> joinTables=null; 
	 
	 public void setup(Context context) throws IOException, InterruptedException {
		 
		 // ��ȡ����
		 String sql=context.getConfiguration().get(LogConf.LOG_SQL);
		 // �������ö����SQL������
		 sqlParse=new SqlParse(sql);
		 
		 
		 
		 // ��ѯsql�����漰��ҪJoin�ı�����
		 List<String> tables=sqlParse.getJoinTables();
		 for(String table : tables){
			String line=context.getConfiguration().get(table);
			
			HdfsFileTable fileTable=new HdfsFileTable(context.getConfiguration());
			fileTable.diserialize(line);
			
			joinTables.put(fileTable.getName(),fileTable);
		 }
		 
		 String line=context.getConfiguration().get(sqlParse.getMainTable());
		 
		 HdfsFileTable fileTable=new HdfsFileTable();
		 fileTable.diserialize(line);
			
				 
		 super.setup(context);
	 }
	 public void map(Object key,Text value,Context context) throws IOException, InterruptedException {
		 
		
		
		List<String> rows=new ArrayList<String>(); 
		rows.add(value.toString());
		mainTable.setRows(rows);
		
		SqlEngine sqlEngine=new SqlEngine(mainTable);
		
		// ����Join��
		String join = sqlParse.get("join");
		if (join != null) {

			for (String en : join.split("\\|")) {
				String table = en.split("on")[0];
				String on = en.split("on")[1];

				String name = sqlParse.getTable(table);

				sqlEngine.join(joinTables.get(name), on);
			}
		}
		// ִ��where
		String where = sqlParse.get("where");
		if (where != null) {
			sqlEngine.where(where);
		}
		
		
		// ִ��group by
		/*String group = sqlParse.get("group by");
		if (group != null) {
			String select = sqlParse.get("select");
			sqlEngine.group(select, group);
		} else {
			String select = sqlParse.get("select");
			sqlEngine.select(select);
		}*/
		
		for(String row:sqlEngine.getTable().getRows()){
			context.write(new Text(row),new Text());
		}
		
		//context.write(arg0, arg1);
	 }
	 public String getColumn(String value,String column){
		 return null;
	 }
	 
	 
}
