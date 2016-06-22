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
		
		// 拆分select语句
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
		
		
		
		// 主表和join表
		SQL.put("#main_table",this.getMainTable());
		
		// 用于MapReduce中
		SQL.put("#reduce_format",matrix.replaceAll("([s,a,m,n,c])(um|vg|ax|in|ount)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)","$1#$3"));
		SQL.put("#matrix",matrix.replaceAll("([s,a,m,n,c])(um|vg|ax|in|ount)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)","$1$2($1#$3) as $4"));
		SQL.put("#select",select.replaceAll("([s,a,m,n,c])(um|vg|ax|in|ount)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)","$1$2($1#$3) as $4"));
		SQL.put("#sort_format",select.replaceAll("(sum|avg|max|min|count)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)","$3"));
		
		
		
		
	}
	/**
	 * 解析Select中的聚合函数
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
	private String getMainTable(){
		String from = this.get("from");
		return this.getTable(from);
	}

	
}
