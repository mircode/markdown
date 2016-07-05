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
 * ����Reducer
 * 
 * @author κ����
 */
public class SortReducer extends Reducer<Text, Text, NullWritable, Text> {

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

		super.setup(context);
	}

	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {

		// ��ʼ����
		Table table = initTable(values);

		// ����SQL����
		SqlExeEngine sqlEngine = new SqlExeEngine(table);

		String distinct = sqlParse.get(SqlParse.DISTINCT);
		if (distinct != null) {
			sqlEngine.distinct(distinct);
		}
		// ִ��order by
		String order = sqlParse.get(SqlParse.ORDER);
		if (order != null) {
			sqlEngine.order(order);
		}
		// ִ��limit
		String limit = sqlParse.get(SqlParse.LIMIT);
		if (limit != null) {
			sqlEngine.limit(limit);
		}
		
		writeTable(context, sqlEngine.getTable());
	}

	private List<String> getRows(Iterable<Text> values) {
		List<String> rows = new ArrayList<String>();
		for (Text t : values) {
			rows.add(t.toString());
		}
		return rows;
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
		// �ָ���
		String split = table.getSplit();
		table = new Table().diserialize(serialize);
		table.setFilter(null);
		String format = sqlParse.get("#mr.sort.format");
		if(!format.equals("*")){
			table.setFormat(format.replace(",", split));
		}
		table.setRows(getRows(values));

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
