package com.sql;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.file.FileTable;
import com.file.Table;

/**
 * SQL引擎
 * 
 * @author 魏国兴
 *
 */
public class SqlEngine {

	// 操作表
	private Table table = null;
	// 操作SQL
	private SqlParse sqlParse = null;

	public SqlEngine() {
	}

	public SqlEngine SQL(String sql) {

		this.sqlParse = new SqlParse(sql);

		// 获取到主表
		String from = sqlParse.get("from");
		if (from != null) {

			String name = sqlParse.getTable(from);
			String input = sqlParse.getPath(from);

			// 从input中解析出format和split
			this.table = new Table(name, null, null, input);

		}

		// 执行join
		String join = sqlParse.get("join");
		if (join != null) {

			for (String en : join.split("\\|")) {
				String table = en.split("on")[0];
				String on = en.split("on")[1];

				String name = sqlParse.getTable(table);
				String input = sqlParse.getPath(table);

				this.join(new Table(name, null, null, input), on);
			}
		}

		// 执行where
		String where = sqlParse.get("where");
		if (where != null) {
			this.where(where);
		}
		
		// 执行group by
		String matrix=sqlParse.get("matrix");
		String group = sqlParse.get("group by");
		if (group != null || matrix!=null) {
			this.group(matrix, group);
		} 
		
		// 执行过滤
		String select = sqlParse.get("select");
		if(select!=null){
			this.select(select);
		}
		String distinct=sqlParse.get("distinct");
		if (distinct != null) {
			this.distinct(distinct);
		}
		// 执行order by
		String order = sqlParse.get("order by");
		if (order != null) {
			this.order(order);
		}
		// 执行limit
		String limit = sqlParse.get("limit");
		if (limit != null) {
			this.limit(limit);
		}
		
		

		return this;
	}

	public SqlEngine(Table table) {
		this.table = table;
	}

	public SqlEngine join(Table join, String on) {
		return this.join(join, on, false);
	}

	public SqlEngine join(Table join, String on, boolean leftJoin) {

		// 内部类开始 ==============================================
		// 链接条件
		class On {
			public String mainCol;// 主表字段
			public String joinCol;// join表字段

			public On(String mainCol, String joinCol) {
				this.mainCol = mainCol;
				this.joinCol = joinCol;
			}
		}
		// 内部类结束 ==============================================

		// 表连接的主表
		Table main = this.table;

		String[] splits = on.trim().split("and");

		List<On> ons = new ArrayList<On>();

		for (String wherecase : splits) {
			String[] joincases = wherecase.replaceAll("\\s+", "").split("=");

			String mainCol;// 主表字段
			String joinCol;// join表字段
			if (joincases[0].indexOf(this.table.getName()) > -1) {
				mainCol = joincases[0].substring(joincases[0].indexOf(".") + 1);
				joinCol = joincases[1].substring(joincases[1].indexOf(".") + 1);
			} else {
				joinCol = joincases[0].substring(joincases[0].indexOf(".") + 1);
				mainCol = joincases[1].substring(joincases[1].indexOf(".") + 1);
			}
			ons.add(new On(mainCol, joinCol));
		}

		List<String> combineRow = new ArrayList<String>();
		// 拼接主表行和join表的行
		for (String row : main.getRows()) {

			Map<String, String> condition = new HashMap<String, String>();
			for (On o : ons) {
				String column = main.getColumns(row, o.mainCol);
				condition.put(o.joinCol, column);
			}
			boolean leftFlag = true;
			for (String joinRow : join.getRows()) {
				boolean flag = true;
				for (Map.Entry<String, String> entry : condition.entrySet()) {
					String col = entry.getKey();
					String val = entry.getValue();
					if (!join.getColumns(joinRow, col).equals(val)) {
						flag = false;
					}
				}
				if (flag) {
					leftFlag = false;
					combineRow.add(row + this.table.getSplit()
							+ joinRow);
				}
			}
			if (leftJoin && leftFlag) {
				combineRow.add(row);
			}

		}

		// 合并后的表格格式
		String name1 = main.getName();
		String name2 = join.getName();
		String format1 = main.getFormat();
		String format2 = join.getFormat();

		String split = this.table.getSplit();
		format1 = (name1 + "." + format1).replace(split, split + name1 + ".");
		format2 = (name2 + "." + format2).replace(split, split + name2 + ".");

		String format = format1 + split + format2;

		this.table = new Table(name1 + "," + name2,null, this.table.getSplit(), format, combineRow);
				

		return this;
	}

	public SqlEngine where(String where) {

		// 内部类开始 ==============================================

		// 用于过滤某一行
		class RowFilter {
			// 判断某行知否满足wherecase这个条件
			public boolean filter(Table t, String row,
					String wherecase) {

				if (wherecase.equals("true") || wherecase.equals("false")) {
					return Boolean.parseBoolean(wherecase);
				}

				String splits[] = wherecase.split(">=|<=|=|!=|<|>|like");
				String col = splits[0];
				String val = splits[1];
				String opt = wherecase.substring(col.length(),
						wherecase.length() - val.length());

				String v = t.getColumns(row, col);

				boolean res = false;

				try {
					Integer v1 = Integer.parseInt(v);
					Integer val1 = Integer.parseInt(val);
					if (opt.equals(">=")) {
						res = v1.compareTo(val1) >= 0;
					} else if (opt.equals("<=")) {
						res = v1.compareTo(val1) <= 0;
					} else if (opt.equals("=")) {
						res = v.equals(val);
					} else if (opt.equals("!=")) {
						res = !v.equals(val);
					} else if (opt.equals("<")) {
						res = v1.compareTo(val1) < 0;
					} else if (opt.equals(">")) {
						res = v1.compareTo(val1) > 0;
					} else if (opt.equals("like")) {
						res = v.matches(val);
					}
				} catch (Exception e) {
					if (opt.equals(">=")) {
						res = v.compareTo(val) >= 0;
					} else if (opt.equals("<=")) {
						res = v.compareTo(val) <= 0;
					} else if (opt.equals("=")) {
						res = v.equals(val);
					} else if (opt.equals("!=")) {
						res = !v.equals(val);
					} else if (opt.equals("<")) {
						res = v.compareTo(val) < 0;
					} else if (opt.equals(">")) {
						res = v.compareTo(val) > 0;
					} else if (opt.equals("like")) {
						res = v.matches(val);
					}
				}
				return res;

			}
		}
		// 内部类结束 ==============================================

		// 格式化where条件
		where = where.replace("&&", "and").replace("||", "or")
				.replaceAll("\\(|\\)", " $0 ");
		String[] compare = { ">=", "<=", "=", "!=", "<", ">", "like" };
		for (int i = 0; i < compare.length; i++) {
			where = where.replaceAll("\\s*" + compare[i] + "\\s*", compare[i]);
		}

		// 以空白字符切分where语句
		String arry[] = where.split("\\s+");

		// 存放操作符
		Stack<String> opt = new Stack<String>();
		// 存放后缀表达式
		List<String> output = new ArrayList<String>();

		// 将中缀表达式转换为后缀表达式
		for (int i = 0; i < arry.length; i++) {
			String it = arry[i];
			if (it.equals("and") || it.equals("or")) {
				if (opt.isEmpty() || opt.peek().equals("(")) {
					opt.push(it);
				} else {
					output.add(opt.pop());
				}
			} else if (it.equals("(")) {
				opt.push(it);
			} else if (it.equals(")")) {
				String el = opt.pop();
				while (el.equals("(") == false) {
					output.add(el);
					el = opt.pop();
				}
			} else {
				output.add(it);
			}
		}
		while (!opt.isEmpty()) {
			output.add(opt.pop());
		}

		List<String> filters = new ArrayList<String>();
		// 过滤table中的列
		for (String row : table.getRows()) {
			// 存放操作符
			Stack<Boolean> opts = new Stack<Boolean>();
			// 解析后缀表达式并运算结果
			for (String v : output) {
				if (v.equals("or")) {
					boolean v1 = opts.pop();
					boolean v2 = opts.pop();
					opts.push(v1 || v2);
				} else if (v.equals("and")) {
					boolean v1 = opts.pop();
					boolean v2 = opts.pop();
					opts.push(v1 && v2);
				} else {
					opts.push(new RowFilter().filter(table, row, v));
				}
			}
			// 取出计算结果
			boolean res = opts.pop();
			if (res) {
				filters.add(row);
			}
		}

		// 过滤后的列
		table.setRows(filters);

		return this;
	}

	public SqlEngine limit(String limit) {

		String splits[] = limit.split(",");

		int start = Integer.parseInt(splits[0]);
		int end = Integer.parseInt(splits[1]);

		List<String> rows = new ArrayList<String>();
		for (int i = start; i < end; i++) {
			rows.add(table.getRows().get(i));
		}
		table.setRows(rows);

		return this;
	}

	public SqlEngine order(String order) {

		// 获取要排序的字段
		String splits[] = order.split(",");

		table.getRows().sort(new Comparator<String>() {

			@Override
			public int compare(String row1, String row2) {

				for (String cols : splits) {

					// 排序的字段和排序的方式
					String col = cols.split("\\s+")[0];
					String type = cols.split("\\s+")[1];

					String val1 = table.getColumns(row1, col);
					String val2 = table.getColumns(row2, col);

					try {
						Integer v1 = Integer.parseInt(val1);
						Integer v2 = Integer.parseInt(val2);

						if (v1.compareTo(v2) != 0) {
							if (type.toLowerCase().equals("asc")) {
								return v1.compareTo(v2);
							} else {
								return v2.compareTo(v1);
							}
						}
					} catch (Exception e) {
						if (val1.compareTo(val2) != 0) {
							if (type.toLowerCase().equals("asc")) {
								return val1.compareTo(val2);
							} else {
								return val2.compareTo(val1);
							}
						}
					}
				}

				return 0;
			}

		});

		return this;
	}

	public SqlEngine select(String select) {

		String split = this.table.getSplit();

		if (select.equals("*")) {
			select = this.table.getFormat().replace(split, ",");
		}
		String splits[] = select.split(",");

		List<String> rows = new ArrayList<String>();
		String format = "";
		for (String row : table.getRows()) {
			String newRow = "";
			format = "";
			for (String col : splits) {
				if (col.matches(".*(sum|avg|max|min|count).*")) {
					col = SqlParse.getMetrix(col, "alias");
				}
				format+=col+split;
				newRow += table.getColumns(row, col) + split;
			}
			format = format.substring(0, format.length() - 1);
			newRow = newRow.substring(0, newRow.length() - 1);
			rows.add(newRow);
		}

		table.setFormat(format);
		table.setRows(rows);

		return this;
	}
	public SqlEngine group(String matrix, String group) {
		return this.group(matrix, group,null);
	}
	public SqlEngine group(String matrix, String group,String sp) {

		// table分隔符
		String split = this.table.getSplit();

		// group上下文
		Map<String, List<String>> context = new HashMap<String, List<String>>();

		
		if(group==null){
			context.put("main", this.getTable().getRows());
		}else{
			// mapper
			for (String row : this.table.getRows()) {
	
				
				String key = table.getColumns(row, group);
	
				List<String> rows = context.get(key);
				rows = rows == null ? new ArrayList<String>() : rows;
	
				rows.add(row);
				context.put(key, rows);
	
			}
		}

		// reducer
		Map<String, String> keyVal = new HashMap<String, String>();
		String format = "";

		for (Map.Entry<String, List<String>> entry : context.entrySet()) {

			String key = entry.getKey();
			List<String> values = entry.getValue();

			format = "";
			String aggregate = "";

			String[] matrixs = matrix.split(",");
			for (String mtrix : matrixs) {

				String func = SqlParse.getMetrix(mtrix, "func");
				String field = SqlParse.getMetrix(mtrix, "field");
				String alias = SqlParse.getMetrix(mtrix, "alias");

				format += alias + split;

				if (func.equals("sum")) {
					aggregate += this.sum(field, values) + split;
				}
				if (func.equals("avg")) {
					if(sp!=null){
						aggregate += this.sum(field, values)+sp+this.count(field,values) + split;
					}else{
						aggregate += this.avg(field, values) + split;
					}
				}
				if (func.equals("max")) {
					aggregate += this.max(field, values) + split;
				}
				if (func.equals("min")) {
					aggregate += this.min(field, values) + split;
				}
				if (func.equals("count")) {
					if(sp!=null){
						aggregate += sp+this.count(field, values) + split;
					}else{
						aggregate += this.count(field, values) + split;
					}
				}

			}

			format = format.substring(0, format.length() - 1);
			aggregate = aggregate.substring(0, aggregate.length() - 1);

			keyVal.put(key, aggregate);
		}

		List<String> rows = new ArrayList<String>();
		for (Map.Entry<String, String> entry : keyVal.entrySet()) {
			String key=entry.getKey();
			if(key.equals("main")){
				rows.add(entry.getValue());
			}else{
				rows.add(key + split + entry.getValue());
			}
		}
		if(group!=null){
			format = group.replace(",", split) + split + format;
		}
		this.table.setFormat(format);
		this.table.setRows(rows);

		return this;
	}

	public SqlEngine distinct(String distinct) {
		Pattern p = Pattern.compile("distinct\\s+\\((.*?)\\)");
		Matcher m = p.matcher(distinct);
		String col = null;
		if (m.find()) {
			col = m.group(1);
		}
		Map<String, String> rowMap = new HashMap<String, String>();
		for (String row : this.table.getRows()) {
			rowMap.put(this.table.getColumns(row, col), null);
		}
		this.table.setFormat(col);
		this.table.setRows(new ArrayList<String>(rowMap.keySet()));
		return this;
	}

	public String count(String field) {
		return this.count(field, this.table.getRows());
	}

	public String count(String field, List<String> rows) {
		Integer count = 0;
		for(String row:rows){
			String col=this.table.getColumns(row, field);
			
			if(col.startsWith("#")){
				count+=Integer.parseInt(col.substring(1));
			}else{
				count+=1;
			}
		}
		return count.toString();
	}

	public String max(String field) {
		return this.max(field, this.table.getRows());
	}

	public String max(String field, List<String> rows) {

		Double max = Double.MIN_VALUE;
		for (String row : rows) {

			Double value = Double.parseDouble(this.table.getColumns(
					row, field));

			if (value > max) {
				max = value;
			}
		}

		return max.intValue()+"";
	}

	public String min(String field) {
		return this.min(field, this.table.getRows());
	}

	public String min(String field, List<String> rows) {
		Double min = Double.MAX_VALUE;

		for (String row : rows) {
			Double value = Double.parseDouble(this.table.getColumns(
					row, field));
			if (value < min) {
				min = value;
			}
		}
		return min.intValue()+"";
	}

	public String avg(String field) {
		return this.avg(field, this.table.getRows());
	}

	public String avg(String field, List<String> rows) {

		Double sum = new Double(0);
		Double count = new Double(0);
		Double avg = new Double(0);

		for (String row : rows) {
			
			String col=this.table.getColumns(row, field);
					
			if(col.contains("#")){
				
				String v1=col.split("#")[0];
				String v2=col.split("#")[1];
				
				sum+=Double.parseDouble(v1);
				count+=Double.parseDouble(v2);
			}else{
				Double value = Double.parseDouble(col);
				sum += value;
				count++;
			}

		}
		avg = sum / count;

		return avg.toString();
	}

	public String sum(String field) {
		return this.sum(field, this.table.getRows());
	}

	public String sum(String field, List<String> rows) {
		Double sum = new Double(0);
		for (String row : rows) {
			Double value = Double.parseDouble(this.table.getColumns(
					row, field));
			sum += value;
		}
		return sum.intValue()+"";
	}

	public String toString() {
		return this.table.toString();
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public SqlParse getSqlParse() {
		return sqlParse;
	}

	public void setSqlParse(SqlParse sqlParse) {
		this.sqlParse = sqlParse;
	}

	public static void main(String args[]) {

		// 声明变量
		String sql = null;
		SqlEngine sqlEngine = null;

		Table student = null;
		Table teacher = null;

		// 简单查询
		sql = "select id,name,grade from classpath:student.txt student where id>10 order by grade desc limit 0,10";

		sqlEngine = new SqlEngine().SQL(sql);
		System.out.println(sqlEngine);

		student = new FileTable("classpath:student.txt");
		sqlEngine = new SqlEngine(student).where("id>10").order("grade desc")
				.limit("0,10").select("id,name,grade");
		
		System.out.println(sqlEngine);

		// 查询最高的成绩
		sql = "select max(grade) as grade from classpath:student.txt s";

		
		sqlEngine = new SqlEngine().SQL(sql);
		System.out.println(sqlEngine);
		
		student = new FileTable("classpath:student.txt");
		String max= new SqlEngine(student).max("grade");
		System.out.println("grade");
		System.out.println(max);	
		
		// 表连接
		sql = "select s.id,s.name,s.grade,t.id,t.name from classpath:student.txt s join classpath:teacher.txt t on s.tid=t.id limit 0,10";

		sqlEngine = new SqlEngine().SQL(sql);
		System.out.println(sqlEngine);

		teacher = new FileTable("t", "classpath:teacher.txt");
		student = new FileTable("s", "classpath:student.txt");

		sqlEngine = new SqlEngine(student).join(teacher, "s.tid=t.id")
				.select("s.id,s.name,s.grade,t.id,t.name").limit("0,10");
		System.out.println(sqlEngine);

		// 分组查询
		sql = "select tid,sum(grade) as grade from classpath:student.txt student group by tid limit 0,10";

		sqlEngine = new SqlEngine().SQL(sql);
		System.out.println(sqlEngine);

		student = new FileTable("s", "classpath:student.txt");
		sqlEngine = new SqlEngine(student).group("sum(grade) as grade", "tid")
				.limit("0,10");
		System.out.println(sqlEngine);

		// 表连接分组查询(查询那个老师对应的学生比较多)
		sql = "select t.name,count(t.id) as t.count  from classpath:student.txt s join classpath:teacher.txt t on s.tid=t.id group by t.id,t.name order by t.count desc limit 0,10";

		sqlEngine = new SqlEngine().SQL(sql);
		System.out.println(sqlEngine);

		teacher = new FileTable("t", "classpath:teacher.txt");
		student = new FileTable("s", "classpath:student.txt");

		sqlEngine = new SqlEngine(student).join(teacher, "s.tid=t.id")
				.group("count(t.id) as t.count", "t.id,t.name")
				.select("t.name,count(t.id) as t.count")
				.order("t.count desc").limit("0,10");
		System.out.println(sqlEngine);

		

	}
}
