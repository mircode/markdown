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
 * 执行SQL的Reducer函数
 * 
 * @author 魏国兴
 */
public class SqlReducer extends Reducer<Text, Text, NullWritable, Text> {

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
	}

	public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {

		// 初始化表
		Table table = initTable(values);

		// 构建SQL引擎
		SqlEngine sqlEngine = new SqlEngine(table);

		// 执行聚合操作
		String matrix = sqlParse.get("#matrix");
		String group = sqlParse.get("group by");
		if (matrix != null) {
			sqlEngine.group(matrix, group);
		}
		// 执行过滤
		String select = sqlParse.get("#select");
		if (select != null) {
			sqlEngine.select(select);
		}

		// 将table中的内容写入到hdfs中
		this.writeTable(context, sqlEngine.getTable());
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

		// Mapper输入格式：t.id|t.name|count(t.id) as t.count
		// 目标格式：t.id|t.name|c#t.id
		// 更具matrix和group计算新的table格式
		String group = sqlParse.get("group by");
		String format = group.replace(",", split) + split
				+ sqlParse.get("#reduce_format").replace(",", split);

		table.setFormat(format);

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
	private void writeTable(Context context, Table table) throws IOException,
			InterruptedException {
		for (String row : table.getRows()) {
			context.write(NullWritable.get(), new Text(row));
		}
	}

}
