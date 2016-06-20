package com.log;

import java.io.FileInputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.file.Table;

/**
 * 用于读取配置文件
 * 
 * @author 魏国兴
 *
 */
public class LogConf {

	// 配置文件的属性
	public static final String LOG_SQL="log.sql";
	public static final String LOG_SPLIT="log.split";
	public static final String LOG_FORMAT="log.format";
	
	// SQL语句
	private String sql=null;
	
	// 日志表映射
	private Map<String,Table> tables=new HashMap<String,Table>();
		
	
	public LogConf(String path) {

		try{
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(path);
			prop.load(in);
	
			this.sql = prop.getProperty(LogConf.LOG_SQL);
			
			String split = prop.getProperty(LogConf.LOG_SPLIT);
	    	
			Iterator<Map.Entry<Object,Object>> it=prop.entrySet().iterator();
			
			while(it.hasNext()){
				
			    Map.Entry<Object,Object> entry=it.next();
			    String key = entry.getKey().toString();
			    String value = entry.getValue().toString();
			    
			    if(key.contains(LogConf.LOG_FORMAT)){
			    	
			    	String name=key.substring(LogConf.LOG_FORMAT.length()+1);
			    	String input=null;
			    	String format=null;
			    	
			    	int index=value.lastIndexOf(":");
			    	input=value.substring(0,index);
			    	format=value.substring(index+1);
			    		
			    	this.tables.put(name, new Table(name,input,split,format));
			    }
			}
	
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Map<String, Table> getTables() {
		return tables;
	}

	public void setTables(Map<String, Table> tables) {
		this.tables = tables;
	}

	public static void main(String args[]) throws Exception{
		
		
		String path=LogConf.class.getClassLoader().getResource("log.conf").getPath();
		path = URLDecoder.decode(path, "UTF-8");
		LogConf log=new LogConf(path);
		
		
		System.out.println(log.getSql());
		System.out.println(log.getTables());
		
		
	}
	
	
}
