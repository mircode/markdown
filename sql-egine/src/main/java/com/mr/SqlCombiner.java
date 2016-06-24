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
 * 执行SQL的Reducer函数
 * 
 * @author 魏国兴
 */
public class SqlCombiner extends Reducer<Text, Text, Text, Text> {

	// SQL解析器
	private SqlParse sqlParse = null;

	// 反序列化生成table
	private String serialize = null;

	public void setup(Context context) throws IOException, InterruptedException {
		// sql对象
		String sql = context.getConfiguration().get(SqlConf.LOG_SQL);
		sqlParse = new SqlParse(sql);

		// main表
		if (sqlParse.get("join") != null) {
			serialize = context.getConfiguration()
					.get(SqlConf.JOIN_TABLE);
		} else {
			serialize = context.getConfiguration().get(sqlParse.get("#table.main"));
		}
	}

	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {

		// 初始化表
		Table table = initTable(values);
		
		// 构建SQL引擎
		SqlExeEngine sqlEngine = new SqlExeEngine(table);
		// 执行聚合操作
		String matrix = sqlParse.get("matrix");
		String group = sqlParse.get("group by");
		if (matrix != null) {
			sqlEngine.combine(matrix, group);
		}

		
		// 将table中的内容写入到hdfs中
		this.writeTable(key, context, sqlEngine.getTable());
	}

	/**
	 * 将Reduce中的values转化成Table
	 * 
	 * @param values
	 * @return
	 */
	private Table initTable(Iterable<Text> values) {

		// 反序列化生成table
		Table table = new Table().diserialize(serialize);

		List<String> rows = new ArrayList<String>();
		for (Text t : values) {
			rows.add(t.toString());
		}
		table.setRows(rows);

		return table;
	}

	/**
	 * 将表格的内容写入到HDFS中
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
