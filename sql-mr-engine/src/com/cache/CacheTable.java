package com.cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


/**
 * 用于缓存需要Join的表
 * 
 * @author 魏国兴
 *
 */
public class CacheTable {

	// 表名
	private String name;
	// 数据列
	private List<String> rows;

	public CacheTable(FileSystem hdfs, Path path) {

		rows = new ArrayList<String>();
		name = path.getName();
		name = name.substring(0, name.lastIndexOf('.'));

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(hdfs.open(path)));
			String line;
			while ((line = reader.readLine()) != null) {
				rows.add(line);
			}
		} catch (IOException e) {
			throw new RuntimeException("加载"+path+"到内存失败", e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
	}

}
