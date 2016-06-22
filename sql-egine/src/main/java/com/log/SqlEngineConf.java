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
 * ���������ļ�
 * 
 * @author κ����
 *
 */
public class SqlEngineConf extends Configuration{

	// �����ļ�������
	public static final String LOG_SQL = "log.sql";
	public static final String LOG_SPLIT = "log.split";
	public static final String LOG_FORMAT = "log.format";

	// Join��ʱ��
	public static final String JOIN_TABLE="join.table";
	
	
	// SQL���
	public String sql = null;
	// Tableӳ��
	public Map<String, Table> tables = new HashMap<String, Table>();
	// ����SQL
	public SqlParse sqlParse=null;
	
	/**
	 * ���������ļ�
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
		
		// ����Ҫ����������ص�Configuration������
		initContext();
	}

	/**
	 * ����SQL�ͱ�ӳ�䵽Context��
	 */
	@SuppressWarnings("static-access")
	public void initContext(){
		
		// ����SQL
		this.sqlParse=new SqlParse(this.sql);
		
		// ����SQL
		this.set(SqlEngineConf.LOG_SQL,this.sql);
		
		// ���ر�ӳ��
		for(Map.Entry<String, Table> entry: this.tables.entrySet()){
			String ser=entry.getValue().serialize();
			this.set(entry.getKey(),ser);
		}
		
		
		// ����Join֮��ı�ṹ
		SqlEngine sqlEngine=new SqlEngine(tables.get(sqlParse.get("#main_table")));
		String join = sqlParse.get("join");
		
		// �����join�ı�,��Reduce�˵ı��ʽ��Ҫ���
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
