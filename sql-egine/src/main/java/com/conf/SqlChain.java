package com.conf;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.file.Table;

public class SqlChain {

	public static List<SqlConf> getChain(String path) {
		List<SqlConf> confs = new ArrayList<SqlConf>();
		try {
			// 加载配置文件
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(path);
			prop.load(in);

			// 读取配置
			// String sql = prop.getProperty(SqlConf.LOG_SQL);
			String hdfs = prop.getProperty(SqlConf.LOG_HDFS);
			String output = prop.getProperty(SqlConf.LOG_OUTPUT);

			String chain = prop.getProperty(SqlConf.LOG_CHAIN, "sql");
			String regex = prop.getProperty(SqlConf.LOG_REGEX, "^[^#].*");
			String split = prop.getProperty(SqlConf.LOG_SPLIT, "|");

			Map<String, Table> tables = new HashMap<String, Table>();

			String tmp = null;
			if (output != null) {
				if (output.endsWith("/")) {
					output = output.substring(0, output.length() - 1);
				}
				tmp = output.substring(0, output.lastIndexOf("/") + 1) + "tmp/";
			}

			// 解析format和sql
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
						 filter=prop.getProperty("log."+filter.substring(1));
					}else{
						 input = value.split(":")[0];
						 format = value.split(":")[1];
						 filter=regex;
					}

					tables.put(name, new Table(name, input, split, format,filter));

				}
			}
			for (String s : chain.split(",")) {
				String sql = prop.getProperty("log." + s);

				SqlConf conf = new SqlConf();
				conf.sql = sql;
				conf.hdfs = hdfs;
				conf.split=split;
				conf.output = output;
				conf.regex = regex;
				conf.tmp = tmp;
				conf.tables=tables;
				
				conf.initConf();

				confs.add(conf);
			}
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return confs;

	}
}
