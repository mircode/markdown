package com.conf;

import java.util.HashMap;
import java.util.Map;

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
public class SqlConf extends Configuration {

	// �����ļ�������
	public static final String CONF_SQL = "#sql";

	// ����,���,��ʱĿ¼
	public static final String CONF_TMP = "#tmp";
	public static final String CONF_INPUT = "#input";
	public static final String CONF_OUTPUT = "#output";

	// �Ƿ���Ҫ����
	public static final String CONF_SORT = "#isSort";
	// �Ƿ���Ҫ����
	public static final String CONF_GROUP = "#isGroup";

	// Join��ʱ��
	public static final String CONF_JOINTABLE = "#joinTable";

	// HDFS·��
	public static final String CONF_HDFS = "fs.defaultFS";

	// ����SQL
	public SqlParse sqlParse = null;

	// SQL���
	private String sql = null;
	// hdfs��ַ
	private String hdfs = null;

	// ����Ŀ¼
	private String input = null;
	// ���Ŀ¼
	private String output = null;
	// ��ʱĿ¼
	private String tmp = null;

	// �Ƿ���Ҫִ����������
	private Boolean isSort = false;
	// �Ƿ���Ҫִ�з���
	private Boolean isGroup = false;
	// Join֮���ṹ
	private String joinTable = null;
	// Tableӳ��
	private Map<String, Table> tables = new HashMap<String, Table>();

	public SqlConf() {
	}

	public SqlConf(String hdfs, String sql, String output,
			Map<String, Table> tables) {

		// ����������������Ŀ¼
		this.sqlParse = new SqlParse(sql);

		this.hdfs = hdfs;
		this.sql = sql;
		this.output = output;
		this.tables = tables;

		// ��ʼ����ʱĿ¼
		initInput();

		// ����Join֮��ı�ṹ
		initJoin();

		// �ж��Ƿ���Ҫ��������
		initSort();
		initGroup();

		// �������Ҫ����ͷ���,��ô��ʱĿ¼�������Ŀ¼
		if (!isSort || !isGroup) {
			this.tmp = this.output;
		}

		// ���������ص�Context��
		loadContext();

	}

	/**
	 * ����SQL�ͱ�ӳ�䵽Context��
	 */
	public void loadContext() {

		// ����SQL
		this.set(CONF_SQL, this.sql);

		// ִ��SQL�е���ʱ,����,���Ŀ¼
		this.set(CONF_TMP, this.tmp);
		this.set(CONF_INPUT, this.input);
		this.set(CONF_OUTPUT, this.output);

		// �Ƿ���Ҫ����ͷ���
		this.setBoolean(CONF_GROUP, this.isGroup);
		this.setBoolean(CONF_SORT, this.isSort);

		// Joinʱ�ı�ṹ
		if(joinTable!=null){
			this.set(CONF_JOINTABLE, this.joinTable);
		}

		// ����HFDS��ַ
		this.set(CONF_HDFS, this.hdfs);

		// ����������ṹ
		for (Map.Entry<String, Table> entry : this.tables.entrySet()) {
			String ser = entry.getValue().serialize();
			this.set(entry.getKey(), ser);
		}
	}

	// �Ƿ���Ҫ����
	private void initSort() {
		this.isSort = false;
		String distinct = sqlParse.get(SqlParse.DISTINCT);
		String order = sqlParse.get(SqlParse.ORDER);
		String limit = sqlParse.get(SqlParse.LIMIT);
		if (distinct != null || order != null || limit != null)
			this.isSort = true;
	}

	// �Ƿ���Ҫ����
	private void initGroup() {
		this.isGroup = false;
		String matrix = sqlParse.get(SqlParse.MATRIX);
		String group = sqlParse.get(SqlParse.GROUP);
		if (matrix != null || group != null)
			this.isGroup = true;
	}

	private void initJoin() {
		SqlExeEngine sqlEngine = new SqlExeEngine(tables.get(sqlParse
				.get(SqlParse.MAIN_TABLE)));
		String join = sqlParse.get(SqlParse.JOIN);
		// �����join�ı�,��Reduce�˵ı��ʽ��Ҫ���
		if (join != null) {
			for (String en : join.split("\\|")) {
				String table = en.split("on")[0];
				String on = en.split("on")[1];
				String name = SqlParse.getTable(table);
				sqlEngine.join(tables.get(name), on);
			}
			this.joinTable = sqlEngine.getTable().serialize();
		}
	}

	/**
	 * ��ʼ������Ŀ¼
	 */
	private void initInput() {
		this.input = this.tables.get(sqlParse.get(SqlParse.MAIN_TABLE)).getInput();
		if (output != null) {
			if (output.endsWith("/")) {
				output = output.substring(0, this.output.length() - 1);
			}
			this.tmp = output.substring(0, output.lastIndexOf("/") + 1)
					+ "tmp/";
		} else {
			if (input.endsWith("/")) {
				this.output = input + "out/";
				this.tmp = input + "tmp/";
			} else {
				this.output = input.substring(0, input.lastIndexOf("/") + 1)
						+ "out/";
				this.tmp = input.substring(0, input.lastIndexOf("/") + 1)
						+ "tmp/";
			}
		}
	}

}
