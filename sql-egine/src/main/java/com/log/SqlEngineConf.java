package com.log;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;

import com.file.Table;
import com.sql.SqlEngine;
import com.sql.SqlParse;

/**
 * 解析配置文件
 * 
 * @author 魏国兴
 *
 */
public class SqlEngineConf extends Configuration{

	// 配置文件的属性
	public static final String LOG_SQL = "log.sql";
	public static final String LOG_SPLIT = "log.split";
	public static final String LOG_FORMAT = "log.format";

	// Join临时表
	public static final String JOIN_TABLE="join.table";
	
	
	// SQL语句
	public String sql = null;
	// Table映射
	public Map<String, Table> tables = new HashMap<String, Table>();
	// 解析SQL
	public SqlParse sqlParse=null;
	
	/**
	 * 解析配置文件
	 * @param path
	 */
	public SqlEngineConf(String path) {

		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(path);
			prop.load(in);

			this.sql = prop.getProperty(SqlEngineConf.LOG_SQL);

			String split = prop.getProperty(SqlEngineConf.LOG_SPLIT);

			Iterator<Map.Entry<Object, Object>> it = prop.entrySet().iterator();

			while (it.hasNext()) {

				Map.Entry<Object, Object> entry = it.next();
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();

				if (key.contains(SqlEngineConf.LOG_FORMAT)) {

					String name = key
							.substring(SqlEngineConf.LOG_FORMAT.length() + 1);
					String input = null;
					String format = null;

					int index = value.lastIndexOf(":");
					input = value.substring(0, index);
					format = value.substring(index + 1);

					this.tables
							.put(name, new Table(name, input, split, format));
				}
			}

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 将必要的配置项加载的Configuration对象中
		initContext();
	}

	/**
	 * 加载SQL和表映射到Context中
	 */
	@SuppressWarnings("static-access")
	public void initContext(){
		
		// 解析SQL
		this.sqlParse=new SqlParse(this.sql);
		
		// 加载SQL
		this.set(SqlEngineConf.LOG_SQL,this.sql);
		
		// 加载表映射
		for(Map.Entry<String, Table> entry: this.tables.entrySet()){
			String ser=entry.getValue().serialize();
			this.set(entry.getKey(),ser);
		}
		
		
		// 计算Join之后的表结构
		SqlEngine sqlEngine=new SqlEngine(tables.get(sqlParse.get("#main_table")));
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
		
	}
	
}
