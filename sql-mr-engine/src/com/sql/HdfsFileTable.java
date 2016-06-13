package com.sql;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;

import com.hdfs.HDFSHelper;

public class HdfsFileTable extends FileTable {

	private Configuration conf;

	public HdfsFileTable(){}
	public HdfsFileTable(Configuration conf) {
		this.conf = conf;
	}

	public List<String> loadHdfs(String path) {
		List<String> rows=null;
		try {
			 rows=HDFSHelper.readLines(conf, path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rows;
	}
}
