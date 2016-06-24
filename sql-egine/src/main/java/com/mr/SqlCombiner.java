package com.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class SqlCombiner extends Reducer<Text, Text, Text, Text> {

	// SQL������
	private SqlParse sqlParse = null;

	// �����л�����table
	private String serialize = null;

	public void setup(Context context) throws IOException, InterruptedException {
		// sql����
		String sql = context.getConfiguration().get(SqlConf.LOG_SQL);
		sqlParse = new SqlParse(sql);

		// main��
		if (sqlParse.get("join") != null) {
			serialize = context.getConfiguration()
					.get(SqlConf.JOIN_TABLE);
		} else {
			serialize = context.getConfiguration().get(sqlParse.get("#table.main"));
		}
	}

	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {

		// ��ʼ����
		Table table = initTable(values);
		
		// ����SQL����
		SqlExeEngine sqlEngine = new SqlExeEngine(table);
		// ִ�оۺϲ���
		String matrix = sqlParse.get("matrix");
		String group = sqlParse.get("group by");
		if (matrix != null) {
			sqlEngine.combine(matrix, group);
		}

		
		// ��table�е�����д�뵽hdfs��
		this.writeTable(key, context, sqlEngine.getTable());
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
	private void writeTable(Text key, Context context, Table table)
			throws IOException, InterruptedException {
		for (String row : table.getRows()) {
			context.write(key, new Text(row));
		}
	}
}
