package com.conf;

import java.io.FileInputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.file.Table;
import com.sql.SqlParse;

public class SqlChain {

	// ����ǰ׺
	public static final String LOG_PREFIX = "log";
	// ����ǰ׺
	public static final String LOG_VAR_PREFIX = "#";
	// HDFS·��
	public static final String LOG_HDFS = "log.hdfs";
	// SQLִ����
	public static final String LOG_CHAIN = "log.chain";

	public static List<SqlConf> getChain(String path) {

		if(path.startsWith("classpath:")){
			path=getResource(path);
		}
		Properties prop = new Properties();

		// ��ȡ�����ļ�
		try {
			FileInputStream in = new FileInputStream(path);
			prop.load(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ������������
		String hdfs = prop.getProperty(SqlChain.LOG_HDFS);
		String chain = prop.getProperty(SqlChain.LOG_CHAIN,
				SqlChain.LOG_VAR_PREFIX + "sql");

		// ������������Ϣ
		List<SqlConf> confs = new ArrayList<SqlConf>();

		// ��������
		for (String sql : chain.split(",")) {
			
			if(!sql.startsWith("#")) sql="#"+sql;
			sql=SqlChain.get(prop, sql);
			
			// ����SQL
			SqlParse sqlParse = new SqlParse(sql);

			// ����SQL�ı�ṹ
			Map<String, Table> tables = new HashMap<String, Table>();

			// ����SQL�����·��
			String output = sqlParse.get(SqlParse.OUTPUT);
			output = SqlChain.get(prop, output);

			// ����SQL������·��
			String inputs = sqlParse.get(SqlParse.INPUT);
			// input:name��name
			for (String input : inputs.split(",")) {

				String name = null;
				// ����������
				if (input.contains(":")) {
					name = input.split(":")[1];
					input = input.replace(":", ".");
				} else {
					name = input;
					input = "*." + input;
				}
				// ��������
				String value = SqlChain.get(prop, input);
				String[] splits = value.split(":");

				String in = null;
				if (splits.length >= 1) {
					in = SqlChain.get(prop, splits[0]);
				}
				String format = null;
				if (splits.length >= 2) {
					format = SqlChain.get(prop, splits[1]);
				}
				String split = null;
				if (splits.length >= 3) {
					split = SqlChain.get(prop, splits[2]);
				}
				String filter = null;
				if (splits.length >= 4) {
					filter = SqlChain.get(prop, splits[3]);
				}
				tables.put(name, new Table(name, in, split, format, filter));
			}

			// ����Conf����
			SqlConf conf = new SqlConf(hdfs, sql, output, tables);
			confs.add(conf);

		}

		return confs;

	}

	/**
	 * ��ȡ������ֵ,���key���Ǳ���,��ֱ�ӷ���key
	 * 
	 * @param prop
	 * @param key
	 *            #input.t��*.t
	 * @return
	 */
	private static String get(Properties prop, String key) {
		if (key.startsWith(LOG_VAR_PREFIX)) {
			return prop.getProperty(
					LOG_PREFIX + "." + key.substring(1), key);
		} else if (key.startsWith("*")) {

			String value = null;
			String regex = key.replace(".", "\\.").replace("*", ".*");
			regex = LOG_PREFIX + "\\." + regex;
			Iterator<Map.Entry<Object, Object>> it = prop.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Object, Object> entry = it.next();
				String k = entry.getKey().toString();
				String v = entry.getValue().toString();
				if (k.matches(regex)) {
					value = v;
					break;
				}
			}
			return value;
		} else {
			return key;
		}
	}

	public static String getResource(String resource) {
		String path = SqlConf.class.getClassLoader().getResource(resource.substring("classpath:".length()))
				.getPath();
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return path;
	}

	

}
