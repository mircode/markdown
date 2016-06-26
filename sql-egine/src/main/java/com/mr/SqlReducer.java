package com.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.conf.SqlConf;
import com.file.Table;
import com.sql.SqlExeEngine;
import com.sql.SqlParse;

/**
 * ִ��SQL��Reducer����
 * 
 * @author κ����
 */
public class SqlReducer extends Reducer<Text, Text, NullWritable, Text> {

	// SQL������
	private SqlParse sqlParse = null;

	// �����л�����table
	private String serialize = null;

	public void setup(Context context) throws IOException, InterruptedException {
		// sql����
		String sql = context.getConfiguration().get(SqlConf.CONF_SQL);
		sqlParse = new SqlParse(sql);

		// main��
		if (sqlParse.get(SqlParse.JOIN) != null) {
			serialize = context.getConfiguration()
					.get(SqlConf.CONF_JOINTABLE);
		} else {
			serialize = context.getConfiguration().get(sqlParse.get(SqlParse.MAIN_TABLE));
		}
	}

	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {

		// ��ʼ����
		Table table = initTable(values);

		// ����SQL����
		SqlExeEngine sqlEngine = new SqlExeEngine(table);

		// ִ�оۺϲ���
		String matrix = sqlParse.get("#mr.reduce.matrix");
		String group = sqlParse.get(SqlParse.GROUP);
		if (matrix != null) {
			sqlEngine.group(matrix, group);
		}
		// ִ�й���
		String select = sqlParse.get("#mr.reduce.select");
		if (select != null) {
			sqlEngine.select(select);
		}

		// ��table�е�����д�뵽hdfs��
		this.writeTable(context, sqlEngine.getTable());
	}

	/**
	 * ��Reduce�е�valuesת����Table
	 * 
	 * @param values
	 * @return
	 */
	private Table initTable(Iterable<Text> values) {

		// �����л�����table
		Table table = new Table().diserialize(serialize);
		table.setFilter(null);
		// �ָ���
		String split = table.getSplit();

		// Mapper�����ʽ��t.id|t.name|count(t.id) as t.count
		// Ŀ���ʽ��t.id|t.name|c#t.id
		// ����matrix��group�����µ�table��ʽ
		String group = sqlParse.get(SqlParse.GROUP);
		String format =sqlParse.get("#mr.reduce.format").replace(",", split);
		if(group!=null){
			 format = group.replace(",", split) + split
					+ sqlParse.get("#mr.reduce.format").replace(",", split);
		}
		table.setFormat(format);
		
		List<String> rows = new ArrayList<String>();
		for (Text t : values) {
			rows.add(t.toString());
		}
		table.setRows(rows);

		return table;
	}

	/**
	 * ����������д�뵽HDFS��
	 * 
	 * @param context
	 * @param table
	 */
	private void writeTable(Context context, Table table) throws IOException,
			InterruptedException {
		for (String row : table.getRows()) {
			context.write(NullWritable.get(), new Text(row));
		}
	}

}
