package com.sql;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.file.Table;

/**
 * SQLִ�е�Ԫ����
 * 
 * @author κ����
 *
 */
public class SqlExeEngine {

	// ������
	private Table table = null;

	public SqlExeEngine() {
	}

	public SqlExeEngine(Table table) {
		this.table = table;
	}

	public SqlExeEngine join(Table join, String on) {
		return this.join(join, on, false);
	}

	public SqlExeEngine join(Table join, String on, boolean leftJoin) {

		// ��������
		class On {
			public String mainCol;// �����ֶ�
			public String joinCol;// join���ֶ�

			public On(String mainCol, String joinCol) {
				this.mainCol = mainCol;
				this.joinCol = joinCol;
			}
		}

		// �����ӵ�����
		Table main = this.table;

		String[] splits = on.trim().split("and");

		List<On> ons = new ArrayList<On>();

		for (String wherecase : splits) {
			String[] joincases = wherecase.replaceAll("\\s+", "").split("=");

			String mainCol;// �����ֶ�
			String joinCol;// join���ֶ�
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
		// ƴ�������к�join�����
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
					combineRow.add(row + this.table.getSplit() + joinRow);
				}
			}
			if (leftJoin && leftFlag) {
				combineRow.add(row);
			}

		}

		// �ϲ���ı���ʽ
		String name1 = main.getName();
		String name2 = join.getName();
		String format1 = main.getFormat();
		String format2 = join.getFormat();

		String split = this.table.getSplit();
		format1 = (name1 + "." + format1).replace(split, split + name1 + ".");
		format2 = (name2 + "." + format2).replace(split, split + name2 + ".");

		String format = format1 + split + format2;

		this.table = new Table(name1 + "," + name2, null,
				this.table.getSplit(), format, null,combineRow);

		return this;
	}

	public SqlExeEngine where(String where) {
		// �Կհ��ַ��з�where���
		String arry[] = where.split("\\s+");
		// ��Ų�����
		Stack<String> opt = new Stack<String>();
		// ��ź�׺���ʽ
		List<String> output = new ArrayList<String>();

		// ����׺���ʽת��Ϊ��׺���ʽ
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
		// ����table�е���
		for (String row : table.getRows()) {
			// ��Ų�����
			Stack<Boolean> opts = new Stack<Boolean>();
			// ������׺���ʽ��������
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
					opts.push(this.filter(table, row, v));
				}
			}
			// ȡ��������
			boolean res = opts.pop();
			if (res) {
				filters.add(row);
			}
		}

		// ���˺����
		table.setRows(filters);

		return this;
	}
	/**
	 * �ж�ĳ��֪������wherecase�������
	 * @param t
	 * @param row
	 * @param wherecase
	 * @return
	 */
	private boolean filter(Table t, String row, String wherecase) {

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
		if (v.matches("\\d+\\.?\\d+")) {
			Double v1 = Double.parseDouble(v);
			Double v2 = Double.parseDouble(val);
			if (opt.equals(">=")) {
				res = v1.compareTo(v2) >= 0;
			} else if (opt.equals("<=")) {
				res = v1.compareTo(v2) <= 0;
			} else if (opt.equals("=")) {
				res = v.equals(val);
			} else if (opt.equals("!=")) {
				res = !v.equals(val);
			} else if (opt.equals("<")) {
				res = v1.compareTo(v2) < 0;
			} else if (opt.equals(">")) {
				res = v1.compareTo(v2) > 0;
			} else if (opt.equals("like")) {
				res = v.matches(val);
			}
		}else{
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
	/**
	 * ����ָ������
	 * 
	 * @param limit
	 * @return
	 */
	public SqlExeEngine limit(String limit) {

		String splits[] = limit.split(",");

		int start = Integer.parseInt(splits[0]);
		int end = Integer.parseInt(splits[1]);

		List<String> rows = new ArrayList<String>();
		for (int i = start; i < end; i++) {
			if(i>=table.getRows().size()){
				break;
			}
			rows.add(table.getRows().get(i));
		}
		table.setRows(rows);

		return this;
	}

	/**
	 * �������н�������
	 * 
	 * @param order
	 * @return
	 */
	public SqlExeEngine order(String order) {

		// ��ȡҪ������ֶ�
		String splits[] = order.split(",");
		table.getRows().sort(new Comparator<String>() {
			@Override
			public int compare(String row1, String row2) {
				int res = 0;
				for (String cols : splits) {
					// ������ֶκ�����ķ�ʽ
					String col = cols.split("\\s+")[0];
					String type = cols.split("\\s+")[1];
					String val1 = table.getColumns(row1, col);
					String val2 = table.getColumns(row2, col);
					// ����
					if (val1.matches("\\d+\\.?\\d+")) {
						Integer v1 = Integer.parseInt(val1);
						Integer v2 = Integer.parseInt(val2);
						if (type.toLowerCase().equals("asc")) {
							res = v1.compareTo(v2);
						} else {
							res = v2.compareTo(v1);
						}
						// �ַ���
					} else {
						if (type.toLowerCase().equals("asc")) {
							res = val1.compareTo(val2);
						} else {
							res = val2.compareTo(val1);
						}
					}
				}
				return res;
			}
		});

		return this;
	}

	/**
	 * ����select����
	 * 
	 * @param select
	 * @return
	 */
	public SqlExeEngine select(String select) {

		// ����select*�����
		String split = this.table.getSplit();
		if (select.equals("*")) {
			select = this.table.getFormat().replace(split, ",");
		}

		String format = "";
		List<String> rows = new ArrayList<String>();

		// ѡȡselect�е���
		String splits[] = select.split(",");
		for (String r : table.getRows()) {
			String row = "";
			format = "";

			for (String col : splits) {
				if (col.matches(".*(sum|avg|max|min|count).*")) {
					col = SqlParse.getMetrix(col, "alias");
				}
				if (col.contains("distinct")) {
					col = SqlParse.getDistinct(col);
				}
				format += col + split;
				row += table.getColumns(r, col) + split;
			}

			format = format.substring(0, format.length() - 1);
			row = row.substring(0, row.length() - 1);
			rows.add(row);
		}
		table.setFormat(format);
		table.setRows(rows);

		return this;
	}

	public SqlExeEngine group(String matrix, String group) {
		return this.group(matrix, group, null);
	}

	public SqlExeEngine combine(String matrix, String group) {
		return this.group(matrix, group, "#");
	}

	/**
	 * group����
	 * 
	 * @param matrix
	 * @param group
	 * @param sp
	 * @return
	 */
	public SqlExeEngine group(String matrix, String group, String combine) {

		// table�ָ���
		String split = this.table.getSplit();

		// group������
		Map<String, List<String>> context = new HashMap<String, List<String>>();

		if (group == null) {
			context.put("nogroup", this.getTable().getRows());// group
																// Ϊnullʱ,���ǰ�id����
		} else {
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
		Map<String, String> collects = new HashMap<String, String>();
		String format = "";

		for (Map.Entry<String, List<String>> entry : context.entrySet()) {

			// �����key
			String key = entry.getKey();
			List<String> values = entry.getValue();

			format = "";
			String aggregate = "";

			// ��Ҫ�ۺϵ�ָ��
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
					if (combine != null) {
						aggregate += this.sum(field, values) + combine
								+ this.count(field, values) + split;
					} else {
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
					if (combine != null) {
						aggregate += combine + this.count(field, values)
								+ split;
					} else {
						aggregate += this.count(field, values) + split;
					}
				}

			}

			format = format.substring(0, format.length() - 1);
			aggregate = aggregate.substring(0, aggregate.length() - 1);

			collects.put(key, aggregate);
		}

		List<String> rows = new ArrayList<String>();
		for (Map.Entry<String, String> entry : collects.entrySet()) {
			String key = entry.getKey();
			if (key.equals("nogroup")) {
				rows.add(entry.getValue());
			} else {
				rows.add(key + split + entry.getValue());
			}
		}
		if (group != null) {
			format = group.replace(",", split) + split + format;
		}
		this.table.setFormat(format);
		this.table.setRows(rows);

		return this;
	}

	public SqlExeEngine distinct(String distinct) {

		distinct = SqlParse.getDistinct(distinct);

		Map<String, String> map = new HashMap<String, String>();
		for (String row : this.table.getRows()) {
			map.put(this.table.getColumns(row, distinct), row);
		}
		this.table.setRows(new ArrayList<String>(map.values()));
		return this;
	}

	public String count(String field) {
		return this.count(field, this.table.getRows());
	}

	/**
	 * ��ָ���е�����
	 * 
	 * @param field
	 * @param rows
	 * @return
	 */
	public String count(String field, List<String> rows) {
		Double count = new Double(0);
		if(field.equals("*")){
			count+=rows.size();
		}else{
			for (String row : rows) {
				String col = this.table.getColumns(row, field);
				if (col.startsWith("#")) {
					count += Double.parseDouble(col.substring(1));
				} else {
					count += 1;
				}
			}
		}
		return Db2Str(count);
	}

	public String max(String field) {
		return this.max(field, this.table.getRows());
	}

	/**
	 * ��ָ���е����ֵ
	 * 
	 * @param field
	 * @param rows
	 * @return
	 */
	public String max(String field, List<String> rows) {
		Double max = Double.MIN_VALUE;
		for (String row : rows) {
			Double value = this.table.getColumn(row, field, Double.class);
			if (value > max) {
				max = value;
			}
		}
		return Db2Str(max);
	}

	public String min(String field) {
		return this.min(field, this.table.getRows());
	}

	/**
	 * ��ָ���е���Сֵ
	 * 
	 * @param field
	 * @param rows
	 * @return
	 */
	public String min(String field, List<String> rows) {
		Double min = Double.MAX_VALUE;
		for (String row : rows) {
			Double value = this.table.getColumn(row, field, Double.class);
			if (value < min) {
				min = value;
			}
		}
		return Db2Str(min);
	}

	public String avg(String field) {
		return this.avg(field, this.table.getRows());
	}

	/**
	 * ��ָ�����е�ƽ����
	 * 
	 * @param field
	 * @param rows
	 * @return
	 */
	public String avg(String field, List<String> rows) {
		Double sum = new Double(0);
		Double count = new Double(0);
		Double avg = new Double(0);
		for (String row : rows) {
			String col = this.table.getColumns(row, field);
			if (col.contains("#")) {
				sum += Double.parseDouble(col.split("#")[0]);
				count += Double.parseDouble(col.split("#")[1]);
			} else {
				sum += Double.parseDouble(col);
				count += 1;
			}
		}
		avg = sum / count;
		return Db2Str(avg);
	}

	public String sum(String field) {
		return this.sum(field, this.table.getRows());
	}

	/**
	 * ����row��ָ��field�еĺ�
	 * 
	 * @param field
	 * @param rows
	 * @return
	 */
	public String sum(String field, List<String> rows) {
		Double sum = new Double(0);
		for (String row : rows) {
			sum += this.table.getColumn(row, field, Double.class);
		}
		return Db2Str(sum);
	}

	
	private String Db2Str(Double res) {
		return res.toString().matches("\\d+.0+") ? res.intValue() + "" : res
				.toString();
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

}
