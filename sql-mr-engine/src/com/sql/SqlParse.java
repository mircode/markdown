package com.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL解析器
 * 
 * @author 魏国兴
 * 
 */
public class SqlParse {

	/**
	 * 存放SQL解析的结果
	 */
	private Map<String, String> SQL = new HashMap<String, String>();

	/**
	 * 解析SQL
	 * 
	 * @param sql
	 */
	public SqlParse(String sql) {

		// 格式化SQL
		sql = sql
				.trim()
				.replaceAll("\\s*(select|from|join|where|group by|order by|limit)\\s+","}$0{")
				.substring(1)
				.concat("}")
				.replaceAll("\\s*(,|>|<|=|!=|>=|<=|like)\\s*", "$1");

		// 拆分SQL
		Pattern p = Pattern
				.compile("(select|from|join|where|group by|order by|limit)\\s+\\{(.*?)\\}");
		Matcher m = p.matcher(sql);

		// 将正则匹配的结果存放到Map中
		while (m.find()) {
			String func = m.group(1);
			String param = m.group(2);

			// 处理多个join的时候
			SQL.put(func, SQL.get(func) == null ? param : SQL.get(func) + "|"
					+ param);
		}
	}

	/**
	 * 获取SQL中的value
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		return SQL.get(key);
	}

	/**
	 * 从table串中解析表名
	 * 
	 * @param table  eg:classpath:student.txt s
	 * @return
	 */
	public String getTable(String table) {
		String[] splits = table.trim().split("\\s+");
		if (splits.length == 1) {
			return splits[0];
		} else {
			return splits[1];
		}
	}

	/**
	 * 从table串中解析表路径
	 * 
	 * @param table eg:classpath:student.txt s
	 * @return
	 */
	public String getPath(String table) {
		String[] splits = table.trim().split("\\s+");
		if (splits.length == 1) {
			return null;
		} else {
			return splits[0];
		}
	}
	/**
	 * 返回需要join的表的表名
	 * 
	 * @return
	 */
	public List<String> getJoinTables(){
		List<String> joins=new ArrayList<String>();
		String join = this.get("join");
		if (join != null) {
			for (String en : join.split("\\|")) {
				String table = en.split("on")[0];
				String name = this.getTable(table);
				joins.add(name);
			}
		}
		 return joins;
	}
	/**
	 * 返回主表名称
	 * 
	 * @return
	 */
	public String getMainTable(){
		String from = this.get("from");
		return this.getTable(from);
	}

}
