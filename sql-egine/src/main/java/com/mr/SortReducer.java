package com.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.file.Table;
import com.log.SqlEngineConf;
import com.sql.SqlEngine;
import com.sql.SqlParse;

/**
 * 排序Reducer
 * 
 * @author 魏国兴
 */
public class SortReducer extends Reducer<Text, Text, NullWritable, Text> {

	// SQL解析器
	private SqlParse sqlParse = null;

	// 反序列化生成table
	private String serialize = null;

	public void setup(Context context) throws IOException, InterruptedException {
		// sql对象
		String sql = context.getConfiguration().get(SqlEngineConf.LOG_SQL);
		sqlParse = new SqlParse(sql);

		// main表
		if (sqlParse.get("join") != null) {
			serialize = context.getConfiguration()
					.get(SqlEngineConf.JOIN_TABLE);
		} else {
			serialize = context.getConfiguration().get(sqlParse.get("#main_table"));
		}

		super.setup(context);
	}

	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {

		// 初始化表
		Table table = initTable(values);

		// 构建SQL引擎
		SqlEngine sqlEngine = new SqlEngine(table);

		String distinct = sqlParse.get("distinct");
		if (distinct != null) {
			sqlEngine.distinct(distinct);
		}
		// 执行order by
		String order = sqlParse.get("order by");
		if (order != null) {
			sqlEngine.order(order);
		}
		// 执行limit
		String limit = sqlParse.get("limit");
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
	 * 将Reduce中的values转化成Table
	 * 
	 * @param values
	 * @return
	 */
	private Table initTable(Iterable<Text> values) {

		// 反序列化生成table
		Table table = new Table().diserialize(serialize);
		// 分隔符
		String split = table.getSplit();
		table = new Table().diserialize(serialize);
		String format = sqlParse.get("#sort_format");
		table.setFormat(format.replace(",", split));
		table.setRows(getRows(values));

		return table;
	}

	/**
	 * 将表格的内容写入到HDFS中
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
