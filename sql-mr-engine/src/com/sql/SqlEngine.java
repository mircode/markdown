package com.sql;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlEngine {

	private FileTable table = null;

	public SqlEngine(FileTable table) {
		this.table = table;
	}

	public SqlEngine join(FileTable join, String on) {
		return this.join(join, on, false);
	}

	public SqlEngine join(FileTable join, String on, boolean leftJoin) {

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
		FileTable main = this.table;

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
				String column = main.getFormat().getColumn(row, o.mainCol);
				condition.put(o.joinCol, column);
			}
			boolean leftFlag = true;
			for (String joinRow : join.getRows()) {
				boolean flag = true;
				for (Map.Entry<String, String> entry : condition.entrySet()) {
					String col = entry.getKey();
					String val = entry.getValue();
					if (!join.getFormat().getColumn(joinRow, col).equals(val)) {
						flag = false;
					}
				}
				if (flag) {
					leftFlag = false;
					combineRow.add(row + this.table.getFormat().getSplit()
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
		String format1 = main.getFormat().getFormat();
		String format2 = join.getFormat().getFormat();

		String split = this.table.getFormat().getSplit();
		format1 = (name1 + "." + format1).replace(split, split + name1 + ".");
		format2 = (name2 + "." + format2).replace(split, split + name2 + ".");

		String format = format1 + split + format2;

		this.table = new FileTable(name1 + "," + name2, this.table.getFormat()
				.getSplit(), format, combineRow);

		return this;
	}

	public SqlEngine where(String where) {

		// 内部类开始 ==============================================

		// 用于过滤某一行
		class RowFilter {
			// 判断某行知否满足wherecase这个条件
			public boolean filter(TableFormat format, String row,
					String wherecase) {

				if (wherecase.equals("true") || wherecase.equals("false")) {
					return Boolean.parseBoolean(wherecase);
				}

				String splits[] = wherecase.split(">=|<=|=|!=|<|>|like");
				String col = splits[0];
				String val = splits[1];
				String opt = wherecase.substring(col.length(),
						wherecase.length() - val.length());

				String v = format.getColumn(row, col);

				boolean res = false;
				
				
				try{
					Integer v1=Integer.parseInt(v);
					Integer val1=Integer.parseInt(val);
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
				}catch(Exception e){
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
					opts.push(new RowFilter().filter(table.getFormat(), row, v));
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

					String val1 = table.getFormat().getColumn(row1, col);
					String val2 = table.getFormat().getColumn(row2, col);

					try{
						Integer v1=Integer.parseInt(val1);
						Integer v2=Integer.parseInt(val2);
						
						if (v1.compareTo(v2) != 0) {
							if (type.toLowerCase().equals("asc")) {
								return v1.compareTo(v2);
							} else {
								return v2.compareTo(v1);
							}
						}
					}catch(Exception e){
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

		String splits[] = select.split(",");

		List<String> rows = new ArrayList<String>();
		String split = this.table.getFormat().getSplit();
		String format = select.replace(",", split);
		for (String row : table.getRows()) {
			String newRow = "";
			for (String col : splits) {
				newRow += table.getFormat().getColumn(row, col) + split;
			}
			newRow = newRow.substring(0, newRow.length() - 1);
			rows.add(newRow);
		}

		table.getFormat().setFormat(format);
		table.setRows(rows);

		return this;
	}

	public SqlEngine group(String select, String group) {

		Map<String, List<String>> groupmap = new HashMap<String, List<String>>();

		String splits[] = group.split(",");
		String split = this.table.getFormat().getSplit();
		for (String row : this.table.getRows()) {

			String newRow = "";
			for (String col : splits) {
				newRow += table.getFormat().getColumn(row, col) + split;
			}
			newRow = newRow.substring(0, newRow.length() - 1);

			List<String> rows = groupmap.get(newRow);
			if (rows == null) {
				rows = new ArrayList<String>();
				rows.add(row);
				groupmap.put(newRow, rows);
			} else {
				rows.add(row);
			}
		}

		List<String> matrix = new ArrayList<String>();

		String filter = "";
		String distinct = null;

		splits = select.split(",");
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
		for (Map.Entry<String, List<String>> entry : groupmap.entrySet()) {

			String key = entry.getKey();
			List<String> rows = entry.getValue();

			String aggregate = "";
			format = "";
			// sum(id) as id
			for (String mtrix : matrix) {
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
				}
				Double sum = new Double(0);
				Double count = new Double(0);
				Double avg = new Double(0);
				Double max = Double.MIN_VALUE;
				Double min = Double.MAX_VALUE;

				for (String row : rows) {
					Double value = Double.parseDouble(this.table.getFormat()
							.getColumn(row, param));
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
				format += alias != null ? alias : func + "(" + param + ")";
				format += split;
			}
			aggregate = aggregate.substring(0, aggregate.length() - 1);
			format = format.substring(0, format.length() - 1);
			newRows.add(key + split + aggregate);
		}

		String newFormat = group.replace(",", split) + split + format;

		this.table.getFormat().setFormat(newFormat);
		this.table.setRows(newRows);

		if (!filter.equals("")) {
			this.select(filter + "," + format.replace(split, ","));
		}

		if (distinct != null) {
			this.distinct(distinct);
		}
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
			rowMap.put(this.table.getFormat().getColumn(row, col), null);
		}
		this.table.getFormat().setFormat(col);
		this.table.setRows(new ArrayList<String>(rowMap.keySet()));
		return this;
	}

	public String toString() {
		return this.table.toString();
	}

	public static void main(String args[]) {

		FileTable student=null;
		FileTable teacher=null;
		
		// 简单查询
		// select name,grade from student where id>10 order by grade desc  limit 0,10;
		
		student = new FileTable("id|name|grade|tid", "classpath:student.txt");
		
		SqlEngine sql=new SqlEngine(student).where("id>10").order("grade desc").limit("0,10").select("id,name,grade");
		System.out.println(sql);
		
		
		// 表连接
		// select s.id,s.name,s.grade,t.id,t.name from student s join teacher t where s.tid=t.id
		
		teacher = new FileTable("t","id|name", "classpath:teacher.txt");
		student = new FileTable("s","id|name|grade|tid", "classpath:student.txt");
		
		sql=new SqlEngine(student).join(teacher, "s.tid=t.id").select("s.id,s.name,s.grade,t.id,t.name");
		System.out.println(sql);
		
		// 分组查询
		// select count(tid) from student group by tid
		
		student = new FileTable("s","id|name|grade|tid", "classpath:student.txt");
		
		sql=new SqlEngine(student).group("count(tid) as tid", "tid");
		System.out.println(sql);
		
		// 表连接分组查询(查询那个老师对应的学生比较多)
		
		// select t.name,count(s.tid) as t.count from student s join teacher t on s.tid=t.id group by s.tid order by t.count desc 
		teacher = new FileTable("t","id|name", "classpath:teacher.txt");
		student = new FileTable("s","id|name|grade|tid", "classpath:student.txt");
		
		sql=new SqlEngine(student).join(teacher, "s.tid=t.id").select("s.id,s.name,s.grade,t.id,t.name").group("t.name,count(t.id) as t.count", "t.id,t.name").order("t.count desc");
		System.out.println(sql);
		
		
	}
}
