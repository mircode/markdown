package com.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于解析SQL语句
 * 
 * @author 魏国兴
 *
 */
public class SqlParse {

	/**
	 * 存放SQL解析的结果
	 */
	private Map<String,String> SQL=new HashMap<String,String>();
	
	
	public SqlParse(String sql){
		
		sql=sql.trim().replaceAll("\\s*(create|select|from|join|on|where|group by|order by|limit)\\s+","}$0{").substring(1).concat("}");
		Pattern p = Pattern.compile("(create|select|from|join|on|where|group by|order by|limit)\\s+\\{(.*?)\\}"); 
		Matcher m = p.matcher(sql); 
		while(m.find()){
			String func=m.group(1);
			String param=m.group(2);
			
			SQL.put(func, param);
		}
		
	}
	/**
	 * 获取SQL中的value
	 * @param key
	 * @return
	 */
	public String get(String key){
		return SQL.get(key);
	}
	public static void main(String args[]){
		
		String sql="create t1 as select user,service,ip,code from t2 join t3 on t2.user=t3.user where t2.user=2 group by t2.user order by t2.date desc limit 0,10";
		
		SqlParse parse=new SqlParse(sql);
   		String select=parse.get("select");
		System.out.println(select);
	}
}
