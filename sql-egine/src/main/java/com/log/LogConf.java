package com.log;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.file.Table;

/**
 * 解析配置文件
 * 
 * @author 魏国兴
 *
 */
public class LogConf {

	// 配置文件的属性
	public static final String LOG_SQL = "log.sql";
	public static final String LOG_SPLIT = "log.split";
	public static final String LOG_FORMAT = "log.format";

	// SQL语句
	public String sql = null;
	// Table映射
	public Map<String, Table> tables = new HashMap<String, Table>();

	/**
	 * 
	 * @param path
	 */
	public LogConf(String path) {

		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(path);
			prop.load(in);

			this.sql = prop.getProperty(LogConf.LOG_SQL);

			String split = prop.getProperty(LogConf.LOG_SPLIT);

			Iterator<Map.Entry<Object, Object>> it = prop.entrySet().iterator();

			while (it.hasNext()) {

				Map.Entry<Object, Object> entry = it.next();
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();

				if (key.contains(LogConf.LOG_FORMAT)) {

					String name = key
							.substring(LogConf.LOG_FORMAT.length() + 1);
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
	}

}
