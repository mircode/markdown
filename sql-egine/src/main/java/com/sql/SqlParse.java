package com.sql;

import java.util.HashMap;
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
				.replaceAll("\\s*(create|select|from|join|where|group by|order by|limit)\\s+","}$0{")
				.substring(1)
				.concat("}")
				.replaceAll("\\s*(,|>|<|=|!=|>=|<=|like)\\s*", "$1");

		// 拆分SQL
		Pattern p = Pattern
				.compile("(create|select|from|join|where|group by|order by|limit)\\s+\\{(.*?)\\}");
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
		
		// 格式化where语句
		String where=SQL.get("where");
		if(where!=null){
			where = where.replace("&&", "and").replace("||", "or")
					.replaceAll("\\(|\\)", " $0 ");
	
			String[] compare = { ">=", "<=", "=", "!=", "<", ">", "like" };
			for (int i = 0; i < compare.length; i++) {
				where = where.replaceAll("\\s*" + compare[i] + "\\s*", compare[i]);
			}
			SQL.put("where",where);
		}
		
		
		
		
		
		// 主表和join表
		SQL.put("#table.main",this.getMainTable());
		SQL.put("#table.join",this.getJoinTables());
		SQL.put("#outpath", this.getOutPath());
		
		
		// 用于MapReduce中
		String mtxReg="([s,a,m,n,c])(um|vg|ax|in|ount)\\s*\\((.*?)\\)\\s+as\\s+([\\w|\\.]+)";
		if(matrix!=null){
			SQL.put("#mr.reduce.format",matrix.replaceAll(mtxReg,"$1#$3"));
			SQL.put("#mr.matrix",matrix.replaceAll(mtxReg,"$1$2($1#$3) as $4"));
		}
		SQL.put("#mr.select",select.replaceAll(mtxReg,"$1$2($1#$3) as $4"));
		SQL.put("#mr.sort.format",select.replaceAll(mtxReg,"$4"));
		
		
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
	 * 解析distinct字段
	 * @param distinct
	 * @return
	 */
	public static String getDistinct(String distinct){
		
		String regx="distinct\\s+\\((.*?)\\)";
		
		Pattern p = Pattern.compile(regx);
		Matcher m = p.matcher(distinct);
		String col = null;
		if (m.find()) {
			col = m.group(1);
		}
		
		return col;
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
	 * 从table串中解析表名
	 * 
	 * @param table  eg:classpath:student.txt s
	 * @return
	 */
	public static String getTable(String table) {
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
	public static String getPath(String table) {
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
	private String getJoinTables(){
		String joins="";
		String join = this.get("join");
		if (join != null) {
			for (String en : join.split("\\|")) {
				String table = en.split("on")[0];
				String name = SqlParse.getTable(table);
				joins=name+",";
			}
		}
		if(!joins.equals("")){
			joins=joins.substring(0,joins.length()-1);
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
		return SqlParse.getTable(from);
	}

	/**
	 * 返回输出目录
	 * @return
	 */
	private String getOutPath(){
		String create =this.get("create");
		if(create!=null){
			Pattern p = Pattern.compile("(.*)\\s+as.*");
			Matcher m = p.matcher(create);
			if (m.find()) {
				create=m.group(1);
			}
		}
		return create;
	}
	
}
