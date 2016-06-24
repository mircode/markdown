package com.mr;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.conf.SqlConf;
import com.file.HDFSTable;
import com.file.Table;
import com.sql.SqlExeEngine;
import com.sql.SqlParse;

/**
 * ִ��SQL��Mapper����
 * @author κ����
 */
public class SqlMapper extends Mapper<Object,Text,Text,Text> {

	 // main����
	 private Table table=null;
	 // join��
	 private Map<String,Table> joins=new HashMap<String,Table>();
	 // SQL������
	 private SqlParse sqlParse=null;
	 
	 // ��Ҫ�����е���������
	 private String regex=null;
	 
	 public void setup(Context context) throws IOException, InterruptedException {
		 
		 // sql����
		 String sql=context.getConfiguration().get(SqlConf.LOG_SQL);
		 sqlParse=new SqlParse(sql);
		 
		 // main��
		 String json=context.getConfiguration().get(sqlParse.get("#table.main"));
		 this.table=new Table().diserialize(json);
		 
		
		 // join��
		 String join = sqlParse.get("join");
		  if (join != null) {
			 String joins[]=sqlParse.get("#table.join").split(",");
			 for(String t : joins){
				json=context.getConfiguration().get(t);
				Table table=new HDFSTable(context.getConfiguration(), new Table().diserialize(json));
				this.joins.put(table.getName(),table);
			 }
		 }
		 this.regex=context.getConfiguration().get("#regex");
		 super.setup(context);
	 }
	
	 public void map(Object key,Text value,Context context) throws IOException, InterruptedException {
		
		// ���˲���Ҫ����
		if(regex!=null&&!value.toString().matches(regex)) return;
		table.setRow(value.toString());
		if(table.getRows()==null||table.getRows().size()==0) return;
		
		// ����SQL����
		SqlExeEngine sqlEngine=new SqlExeEngine(table);
		
		// ����Join��
		String join = sqlParse.get("join");
		if (join != null) {
			for (String en : join.split("\\|")) {
				String table = en.split("on")[0];
				String on = en.split("on")[1];
				String name = SqlParse.getTable(table);
				sqlEngine.join(joins.get(name), on);
			}
		}
		// ִ��where
		String where = sqlParse.get("where");
		if (where != null) {
			sqlEngine.where(where);
		}
		
		// ִ�з���
		String group = sqlParse.get("group by");
		for(String row:sqlEngine.getTable().getRows()){
			String ky="id";// Ĭ�ϰ���id����
			if(group!=null){
				ky = sqlEngine.getTable().getColumns(row, group);
			}
			context.write(new Text(ky),new Text(row));
		}
		
	 }
	 
}
