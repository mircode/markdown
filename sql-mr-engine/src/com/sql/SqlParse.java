package com.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL������
 * @author κ����
 * 
 */
public class SqlParse {

	/**
	 * ���SQL�����Ľ��
	 */
	private Map<String, String> SQL = new HashMap<String, String>();

	/**
	 * ����SQL
	 * @param sql
	 */
	public SqlParse(String sql) {

		// ��ʽ��SQL
		sql = sql
				.trim()
				.replaceAll("\\s*(select|from|join|where|group by|order by|limit)\\s+","}$0{")
				.substring(1)
				.concat("}")
				.replaceAll("\\s*(,|>|<|=|!=|>=|<=|like)\\s*", "$1");

		// ���SQL
		Pattern p = Pattern
				.compile("(select|from|join|where|group by|order by|limit)\\s+\\{(.*?)\\}");
		Matcher m = p.matcher(sql);

		// ������ƥ��Ľ����ŵ�Map��
		while (m.find()) {
			String func = m.group(1);
			String param = m.group(2);

			// ������join��ʱ��
			SQL.put(func, SQL.get(func) == null ? param : SQL.get(func) + "|"
					+ param);
		}
	}

	/**
	 * ��ȡSQL�е�value
	 * @param key
	 * @return
	 */
	public String get(String key) {
		return SQL.get(key);
	}

	/**
	 * ��table���н�������
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
	 * ��table���н�����·��
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


}
