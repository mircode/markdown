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
 * ���������ļ�
 * 
 * @author κ����
 *
 */
public class SqlConf extends Configuration{

	// HDFS��ַ
	public static final String LOG_HDFS="log.hdfs";
	// ϵͳ�ָ���
	public static final String LOG_SPLIT = "log.split";
	// �й�����(Ĭ�Ϲ���#�ſ�ͷ����)
	public static final String LOG_REGEX = "log.regex";	
	// ��־��ʽ
	public static final String LOG_FORMAT = "log.format";
	// ������Ŀ¼
	public static final String LOG_OUTPUT="log.output";

		
	// �����ļ�������
	public static final String LOG_SQL = "log.sql";
	// sqlִ����
    public static final String LOG_CHAIN="log.chain";
	
		
	
	
	// Join��ʱ��
	public static final String JOIN_TABLE="join.table";
	
	
	
	// SQL���
	public String sql = null;
	// ����SQL
	public SqlParse sqlParse=null;
	// hdfs��ַ
	public String hdfs=null;
	// ��־�ָ���
	public String split=null;
	
	// ����Ŀ¼
	public String input=null;
	// ���Ŀ¼
	public String output=null;
	// ��ʱĿ¼
	public String tmp=null;
	// �Ƿ���Ҫִ����������
	public Boolean isSort=false;
	// �Ƿ���Ҫִ�з���
	public Boolean isGroup=false;
	
	// �й�����(Ĭ�Ϲ���#�ſ�ͷ����)
	public String regex=null;
	
	
	// Tableӳ��
	public Map<String, Table> tables = new HashMap<String, Table>();
		
	public SqlConf(){
		
	}
	/**
	 * ���������ļ�
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
		
		// ����Ҫ����������ص�Configuration������
		initConf();
	}

	/**
	 * ����SQL�ͱ�ӳ�䵽Context��
	 */
	@SuppressWarnings("static-access")
	public void initConf(){
		
		// HFDS��ַ
		this.set("fs.defaultFS", this.hdfs);		
		// ����SQL
		this.sqlParse=new SqlParse(this.sql);
		
		// ����SQL
		this.set(SqlConf.LOG_SQL,this.sql);
		
		// ���ر�ӳ��
		for(Map.Entry<String, Table> entry: this.tables.entrySet()){
			String ser=entry.getValue().serialize();
			this.set(entry.getKey(),ser);
		}
		
		
		// ����Join֮��ı�ṹ
		SqlExeEngine sqlEngine=new SqlExeEngine(tables.get(sqlParse.get("#table.main")));
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
		
		
		this.input=this.tables.get(sqlParse.get("#table.main")).getInput();
		
		// ���Դ�SQL�н������·��
		if(this.output!=null){
			this.output=this.sqlParse.get("#outpath");
			if(this.output.endsWith("/")){
				this.output=this.output.substring(0,this.output.length()-1);
			}
			this.tmp=this.output.substring(0,this.output.lastIndexOf("/")+1)+"tmp/";
		}
		// ���Դ�SQL�н������·��
		if(output==null){
			output=sqlParse.get("#outpath");
			if(output.endsWith("/")){
				this.output=output.substring(0,output.length()-1);
			}
			this.tmp=output.substring(0,output.lastIndexOf("/")+1)+"tmp/";
		}
		// ��������·��ȷ�����·��
		if(output==null||output.equals("")){
			if(input.endsWith("/")){
				this.output=input+"out/";
				this.tmp=input+"tmp/";
			}else{
				this.output=input.substring(0,input.lastIndexOf("/")+1)+"out/";
				this.tmp=input.substring(0,input.lastIndexOf("/")+1)+"tmp/";
			}
		}
		
		// �ж��Ƿ���Ҫִ��sortJob
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
