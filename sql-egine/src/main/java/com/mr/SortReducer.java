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
 * 执行SQL的Mapper函数
 * 
 * @author 魏国兴
 */
public class SortReducer extends Reducer<Text, Text, NullWritable, Text> {

	// main主表
	private Table table = null;
	// SQL解析器
	private SqlParse sqlParse = null;

	public void setup(Context context) throws IOException, InterruptedException {

		// sql对象
		String sql = context.getConfiguration().get(LogConf.LOG_SQL);
		sqlParse = new SqlParse(sql);

		super.setup(context);
	}

	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {

		String ser = null;
		// main表
		if (sqlParse.get("join") != null) {
			ser = context.getConfiguration().get("join_table_format");
		} else {
			ser = context.getConfiguration().get(sqlParse.getMainTable());
		}
		this.table = new Table().diserialize(ser);

		String select = sqlParse.get("select");

		select = select.replaceAll(
				"(sum|avg|max|min|count)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)",
				"$3");

		table.setFormat(select.replace(",", this.table.getSplit()));
		table.setRows(getRows(values));

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
		for (String row : sqlEngine.getTable().getRows()) {
			context.write(NullWritable.get(), new Text(row));
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
