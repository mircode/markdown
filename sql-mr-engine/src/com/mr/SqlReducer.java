package com.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.log.LogConf;
import com.sql.HdfsFileTable;
import com.sql.SqlParse;

/**
 * 执行SQL的Reducer函数
 * @author 魏国兴
 */
public class SqlReducer extends Reducer<Text,Text,Text,Text> {
	
	private SqlParse sqlParse=null;
	private HdfsFileTable mainTable=null;
	public void setup(Context context) throws IOException, InterruptedException {
		 
		 // 读取配置
		 String sql=context.getConfiguration().get(LogConf.LOG_SQL);
		 // 构造配置对象和SQL解析器
		 sqlParse=new SqlParse(sql);
	}
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		
		
		String split=mainTable.getFormat().getSplit();
		
		String select=sqlParse.get("select");
		
		List<String> matrix = new ArrayList<String>();

		String filter = "";
		String distinct = null;

		String splits[] = select.split(",");
		for (String col : splits) {
			if (col.matches(".*(sum|avg|max|min|count).*")) {
				matrix.add(col);
			} else if (col.matches(".*distinct.*")) {
				distinct = col;
			} else {
				filter += col + ",";
			}
		}
		if (!filter.equals("")) {
			filter = filter.substring(0, filter.length() - 1);
		}

		// 计算各个分组的sum|avg|max|min|count
		List<String> newRows = new ArrayList<String>();

		String format = "";

			

		String aggregate = "";
		format = "";
		// sum(id) as id
		for (String mtrix : matrix) {

			if (!mtrix.contains("as")) {
				mtrix += " as matrix_name";
			}
			Pattern p = Pattern
					.compile("(sum|avg|max|min|count)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)");
			Matcher m = p.matcher(mtrix);

			String func = null;
			String param = null;
			String alias = null;
			if (m.find()) {
				func = m.group(1);
				param = m.group(2);
				alias = m.group(3).trim();
				if (alias.equals("matrix_name")) {
					alias = func + "(" + param + ")";
				}
			}
			Double sum = new Double(0);
			Double count = new Double(0);
			Double avg = new Double(0);
			Double max = Double.MIN_VALUE;
			Double min = Double.MAX_VALUE;

			for (Text row : values) {
				Double value = Double.parseDouble(mainTable.getFormat().getColumn(row.toString(), param));
						
				sum += value;
				count++;
				if (value > max) {
					max = value;
				}
				if (value < min) {
					min = value;
				}
			}
			avg = sum / count;

			if (func.equals("sum")) {
				aggregate += sum.intValue() + split;
			}
			if (func.equals("avg")) {
				aggregate += avg + split;
			}
			if (func.equals("max")) {
				aggregate += max.intValue() + split;
			}
			if (func.equals("min")) {
				aggregate += min.intValue() + split;
			}
			if (func.equals("count")) {
				aggregate += count.intValue() + split;
			}
			format += alias;
			format += split;
		}
		aggregate = aggregate.substring(0, aggregate.length() - 1);
		format = format.substring(0, format.length() - 1);
		newRows.add(key + split + aggregate);

		for(String row:mainTable.getRows()){
			context.write(new Text(row),new Text());
		}
	}
}
