package com.conf;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;

import com.file.Table;
import com.sql.SqlExeEngine;
import com.sql.SqlParse;

/**
 * 解析配置文件
 * 
 * @author 魏国兴
 *
 */
public class SqlConf extends Configuration{

	// HDFS地址
	public static final String LOG_HDFS="log.hdfs";
	// 系统分隔符
	public static final String LOG_SPLIT = "log.split";
	// 行过滤器(默认过滤#号开头的行)
	public static final String LOG_REGEX = "log.regex";	
	// 日志格式
	public static final String LOG_FORMAT = "log.format";
	// 结果输出目录
	public static final String LOG_OUTPUT="log.output";

		
	// 配置文件的属性
	public static final String LOG_SQL = "log.sql";
	// sql执行链
    public static final String LOG_CHAIN="log.chain";
	
		
	
	
	// Join临时表
	public static final String JOIN_TABLE="join.table";
	
	
	
	// SQL语句
	public String sql = null;
	// 解析SQL
	public SqlParse sqlParse=null;
	// hdfs地址
	public String hdfs=null;
	// 日志分隔符
	public String split=null;
	
	// 输入目录
	public String input=null;
	// 输出目录
	public String output=null;
	// 临时目录
	public String tmp=null;
	// 是否需要执行排序任务
	public Boolean isSort=false;
	// 是否需要执行分组
	public Boolean isGroup=false;
	
	// 行过滤器(默认过滤#号开头的行)
	public String regex=null;
	
	
	// Table映射
	public Map<String, Table> tables = new HashMap<String, Table>();
		
	public SqlConf(){
		
	}
	/**
	 * 解析配置文件
	 * @param path
	 */
	public SqlConf(String path) {
		
		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(path);
			prop.load(in);

			this.sql = prop.getProperty(SqlConf.LOG_SQL);
			this.hdfs= prop.getProperty(SqlConf.LOG_HDFS);
			this.output=prop.getProperty(SqlConf.LOG_OUTPUT);
			this.regex=prop.getProperty(SqlConf.LOG_REGEX,"^[^#].*");
			this.split = prop.getProperty(SqlConf.LOG_SPLIT,"|");
			
			
			if(this.output!=null){
				if(this.output.endsWith("/")){
					this.output=this.output.substring(0,this.output.length()-1);
				}
				this.tmp=this.output.substring(0,this.output.lastIndexOf("/")+1)+"tmp/";
			}

			Iterator<Map.Entry<Object, Object>> it = prop.entrySet().iterator();

			while (it.hasNext()) {

				Map.Entry<Object, Object> entry = it.next();
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();

				if (key.contains(SqlConf.LOG_FORMAT)) {

					String name = key
							.substring(SqlConf.LOG_FORMAT.length() + 1);
					
					String splits[]=value.split(":");
					
					String input=null;
					String format=null;
					String filter=null;
					
					if(splits.length>=3){
						 input = value.split(":")[0];
						 format = value.split(":")[1];
						 filter = value.split(":")[2];
					}else{
						 input = value.split(":")[0];
						 format = value.split(":")[1];
						 filter=regex;
					}

					this.tables
							.put(name, new Table(name, input, split, format,filter));
				}
			}

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 将必要的配置项加载的Configuration对象中
		initConf();
	}

	/**
	 * 加载SQL和表映射到Context中
	 */
	@SuppressWarnings("static-access")
	public void initConf(){
		
		// HFDS地址
		this.set("fs.defaultFS", this.hdfs);		
		// 解析SQL
		this.sqlParse=new SqlParse(this.sql);
		
		// 加载SQL
		this.set(SqlConf.LOG_SQL,this.sql);
		
		// 加载表映射
		for(Map.Entry<String, Table> entry: this.tables.entrySet()){
			String ser=entry.getValue().serialize();
			this.set(entry.getKey(),ser);
		}
		
		
		// 计算Join之后的表结构
		SqlExeEngine sqlEngine=new SqlExeEngine(tables.get(sqlParse.get("#table.main")));
		String join = sqlParse.get("join");
		
		// 如果有join的表,则Reduce端的表格式需要变更
		if (join != null) {
			for (String en : join.split("\\|")) {
				String table = en.split("on")[0];
				String on = en.split("on")[1];
				String name = sqlParse.getTable(table);
				sqlEngine.join(tables.get(name), on);
			}
			this.set(this.JOIN_TABLE,sqlEngine.getTable().serialize());
		}
		
		
		this.input=this.tables.get(sqlParse.get("#table.main")).getInput();
		
		// 尝试从SQL中解析输出路径
		if(this.output!=null){
			this.output=this.sqlParse.get("#outpath");
			if(this.output.endsWith("/")){
				this.output=this.output.substring(0,this.output.length()-1);
			}
			this.tmp=this.output.substring(0,this.output.lastIndexOf("/")+1)+"tmp/";
		}
		// 尝试从SQL中解析输出路径
		if(output==null){
			output=sqlParse.get("#outpath");
			if(output.endsWith("/")){
				this.output=output.substring(0,output.length()-1);
			}
			this.tmp=output.substring(0,output.lastIndexOf("/")+1)+"tmp/";
		}
		// 根据输入路径确定输出路径
		if(output==null||output.equals("")){
			if(input.endsWith("/")){
				this.output=input+"out/";
				this.tmp=input+"tmp/";
			}else{
				this.output=input.substring(0,input.lastIndexOf("/")+1)+"out/";
				this.tmp=input.substring(0,input.lastIndexOf("/")+1)+"tmp/";
			}
		}
		
		// 判断是否需要执行sortJob
		String distinct = sqlParse.get("distinct");
		String order = sqlParse.get("order by");
		String limit = sqlParse.get("limit");
			
		String matrix = sqlParse.get("matrix");
		String group = sqlParse.get("group by");
		
		if(distinct!=null||order!=null||limit!=null){
			this.isSort=true;
		}else{
			this.tmp=this.output;
		}
		if(matrix!=null||group!=null){
			this.isGroup=true;
		}else{
			this.tmp=this.output;
		}
		
		this.set("#regex",this.regex);
		this.set("#isSort",this.isSort.toString());
		this.set("#tmp",this.tmp);
		this.set("#input",this.input);
		this.set("#output",this.output);
	}
	
	
}
