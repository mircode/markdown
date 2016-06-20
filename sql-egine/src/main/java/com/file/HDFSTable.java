package com.file;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

import com.hdfs.HDFSHelper;


/**
 * �ļ�������ӳ�����
 * 
 * @author κ����
 *
 */
public class HDFSTable extends Table{
	
	public HDFSTable(Configuration conf,Table table){
		this.name=table.getName();
		this.split=table.getSplit();
		this.format=table.getFormat();
		this.input=table.getInput();
		try {
			this.rows=HDFSHelper.readLines(conf, this.input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
