package com.sql;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	
	public String table=null;
	public String path=null;
	
	public List<JoinTable> join=null;
	
	public SqlParse(String sql){
		
		sql=sql.trim().replaceAll("\\s*(create|select|from|join|where|group by|order by|limit)\\s+","}$0{").substring(1).concat("}");
		Pattern p = Pattern.compile("(create|select|from|join|where|group by|order by|limit)\\s+\\{(.*?)\\}"); 
		Matcher m = p.matcher(sql); 
		
		// 将正则匹配的结果存放到Map中
		while(m.find()){
			
			String func=m.group(1);
			String param=m.group(2);
			
			// 处理多个join的时候
			SQL.put(func, SQL.get(func)==null?param:SQL.get(func)+":"+param);
		}
		
		// 解析table和path
		String from=this.get("from");
		if(from!=null){
			this.table=getTable(from);
			this.path=getPath(from);
		}
		
		// 解析join表
		String joins=this.get("join");
		if(joins!=null){
			this.join=new ArrayList<JoinTable>();
			p = Pattern.compile("join\\s+(\\w+)\\s+on\\s+(\\w+)\\s+"); 
			m = p.matcher(joins.trim()); 
			while(m.find()){
				String join=getTable(m.group(1));
				String path=getPath(m.group(1));
				String on=m.group(2);
				this.join.add(new JoinTable(join,path,on));
			}
		}
		
	}
	
	private String getTable(String str){
		String[] splits=str.trim().split("\\s+");
		if(splits.length==1){
			return splits[0];
		}else{
			return splits[1];
		}
	}
	private String getPath(String str){
		String[] splits=str.trim().split("\\s+");
		if(splits.length==1){
			return null;
		}else{
			return splits[0];
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
	/**
	 * 返回SQL中的主表
	 * @return
	 */
	public String getMainTable() {
		String mainTable=this.get("from");
		String[] res=parseTable(mainTable); 
		return res[0];
	}
	
	public List<String> getJoinTables(){
		List<String> res=new ArrayList<String>();
		// 解析join表
		String joinTables=this.get("join");
		if(joinTables!=null){
			Pattern p = Pattern.compile("join\\s+(\\w+)\\s+on"); 
			Matcher m = p.matcher(joinTables.trim()); 
			while(m.find()){
				String joinTable=m.group(1);
				String[] table=parseTable(joinTable);
				res.add(table[0]);
			}
		}
		return res;
	}
	/**
	 * 返回SQL中涉及的表
	 * @return
	 */
	public Map<String,String> getTables(){
		// 返回结果
		Map<String,String> res=new HashMap<String,String>();
		
		// 解析主表
		String mainTable=this.get("from");
		String[] table=parseTable(mainTable);
		res.put(table[0],table[1]);
		
		// 解析join表
		String joinTables=this.get("join");
		if(joinTables!=null){
			Pattern p = Pattern.compile("join\\s+(\\w+)\\s+on"); 
			Matcher m = p.matcher(joinTables.trim()); 
			while(m.find()){
				String joinTable=m.group(1);
				table=parseTable(joinTable);
				res.put(table[0],table[1]);
			}
		}
		return res;
	}
	/**
	 * 将表相关的串拆分成表名和路径放到map中
	 * @param table
	 * @return
	 */
	private String[] parseTable(String table){
		
		String res[]=new String[2];
		
		String[] splits=table.trim().split("\\s+");
		
		if(splits.length==1){
			// 文件路径
			if(splits[0].indexOf("/")>-1){
				File file=new File(splits[0]);
				if(file.isDirectory()){
					String name="table";
					res[0]=name;
					res[1]=splits[0];
				}else{
					String name=file.getName();
					res[0]=name;
					res[1]=splits[0];
				}
			}else{
				res[0]=splits[0];
				res[1]=null;
			}
		}else if(splits.length==2){
			res[0]=splits[1];
			res[1]=splits[2];
		}
		
		return res;
		
	}

	
	public static void main(String args[]){
		
		String sql="create t1 as select user,service,ip,code from t2 join t3 on t2.user=t3.user join t4 on t2.user=t4.user where t2.user=2 group by t2.user order by t2.date desc limit 0,10";
		
		SqlParse parse=new SqlParse(sql);
   		String select=parse.get("select");
   		
		System.out.println(select);
	}
	
}
