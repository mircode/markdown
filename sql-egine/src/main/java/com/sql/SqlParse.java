package com.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL������
 * 
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
	 * 
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
		
		// ���select���
		String select=SQL.get("select");
		String[] splits = select.split(",");
		
		String matrix="";
		String distinct=null;
		
		for (String field : splits) {
			if (field.matches(".*(sum|avg|max|min|count).*")) {
				matrix +=field+",";
			} else if (field.matches(".*distinct.*")) {
				distinct = field;
			} 
		}
		
		if (!matrix.equals("")) {
			matrix = matrix.substring(0,matrix.length()-1);
		}else{
			matrix=null;
		}
		
		
		
		SQL.put("matrix",matrix);
		SQL.put("distinct",distinct);
		
		
		
		// �����join��
		SQL.put("#main_table",this.getMainTable());
		
		// ����MapReduce��
		SQL.put("#reduce_format",matrix.replaceAll("([s,a,m,n,c])(um|vg|ax|in|ount)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)","$1#$3"));
		SQL.put("#matrix",matrix.replaceAll("([s,a,m,n,c])(um|vg|ax|in|ount)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)","$1$2($1#$3) as $4"));
		SQL.put("#select",select.replaceAll("([s,a,m,n,c])(um|vg|ax|in|ount)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)","$1$2($1#$3) as $4"));
		SQL.put("#sort_format",select.replaceAll("(sum|avg|max|min|count)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)","$3"));
		
		
		
		
	}
	/**
	 * ����Select�еľۺϺ���
	 * @param mtrix
	 * @param flag
	 * @return
	 */
	public static String getMetrix(String mtrix,String flag){
		
		if (!mtrix.contains("as")) {
			mtrix += " as matrix_name";
		}
		Pattern p = Pattern
				.compile("(sum|avg|max|min|count)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)");
		Matcher m = p.matcher(mtrix);

		String func = null;
		String field = null;
		String alias = null;
		if (m.find()) {
			func = m.group(1);
			field = m.group(2);
			alias = m.group(3).trim();
			if (alias.equals("matrix_name")) {
				alias = func + "(" + field + ")";
			}
		}
		
		Map<String,String> res=new HashMap<String,String>();
		res.put("func",func);
		res.put("field",field);
		res.put("alias",alias);
	
		return res.get(flag);
	}

	/**
	 * ��ȡSQL�е�value
	 * 
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
	/**
	 * ������Ҫjoin�ı�ı���
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
	 * ������������
	 * 
	 * @return
	 */
	private String getMainTable(){
		String from = this.get("from");
		return this.getTable(from);
	}

	
}
