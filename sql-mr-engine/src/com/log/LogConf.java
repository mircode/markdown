package com.log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.sql.SqlParse;

/**
 * ���ڶ�ȡ�����ļ�
 * 
 * @author κ����
 *
 */
public class LogConf {

	// �����ļ�������
	public static final String LOG_SPLIT="log.split";
	public static final String LOG_FORMAT="log.format";
	public static final String LOG_SQL="log.sql";
	
	// ��־��ӳ��
	private Map<String,LogTable> tableMap=new HashMap<String,LogTable>();
	
	// ϵͳ�ָ���
	private String split="|";// Ĭ��Ϊ|
	// ��־������sql
	private String sql;

	public LogConf(String path) throws IOException {

		/*Properties prop = new Properties();
		FileInputStream in = new FileInputStream(path);
		prop.load(in);

		this.sql = prop.getProperty(LogConf.LOG_SQL);
		this.split = prop.getProperty(LogConf.LOG_SPLIT,this.split);
		
		// ���Դ�sql�н��������Ϣ
    	SqlParse sqlParse=new SqlParse(this.sql);
    	
		Iterator<Map.Entry<Object,Object>> it=prop.entrySet().iterator();
		while(it.hasNext()){
		    Map.Entry<Object,Object> entry=it.next();
		    String key = entry.getKey().toString();
		    String value = entry.getValue().toString();
		    if(key.indexOf(LogConf.LOG_FORMAT)>-1){
		    	
		    	String name=key.substring(LogConf.LOG_FORMAT.length());
		    	String input=null;
		    	String format=null;
		    	String split=null;
		    	
		    	
		    	if(name==null||name.equals("")){
			    	name=sqlParse.getMainTable();
		    	}
		    	
		    	// ���Դ�log.format�н����� input format split
		    	String[] splits=value.split(":");
		    	if(splits.length==1){
		    		input=sqlParse.getTables().get(name);
		    		format=splits[0];
		    		split=this.split;
		    	}else if(splits.length==2){
		    		input=splits[0];
		    		format=splits[1];
		    		split=this.split;
		    	}else if(splits.length==3){
		    		input=splits[0];
		    		format=splits[1];
		    		split=splits[2];
				}
		    	this.tableMap.put(name, new LogTable(name,input,split,format));
		    }
		}

		in.close();
*/
	}
	public Map<String, LogTable> getTableMap() {
		return tableMap;
	}
	public void setTableMap(Map<String, LogTable> tableMap) {
		this.tableMap = tableMap;
	}
	public String getSplit() {
		return split;
	}

	public void setSplit(String split) {
		this.split = split;
	}
	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
	
}
