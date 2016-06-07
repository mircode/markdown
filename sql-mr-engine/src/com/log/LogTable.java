package com.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class LogTable {

	// 表格名称
	private String name;
	// 输入路径
	private String input;
	// 切分符号
	private String split;
	// 日志格式
	private String format;

	// 表格数据
	private List<String> rows;
	
	public LogTable(String name,String input,String split,String format,List<String> rows){
		this.name=name;
		this.input=input;
		this.split=split;
		this.format=format;
		this.rows=rows;
	}
	public LogTable(String name,String input,String split,String format){
		this(name,input,split,format,null);
	}
	public LogTable(String line){
		String splits[]=line.split(",");
		this.name=splits[0];
		this.input=splits[1];
		this.split=splits[2];
		this.format=splits[3];
	}
	public LogTable(FileSystem hdfs,String line){
		String splits[]=line.split(",");
		this.name=splits[0];
		this.input=splits[1];
		this.split=splits[2];
		this.format=splits[3];
		
		
		Path path=new Path(input);
		rows = new ArrayList<String>();
		name = path.getName();
		name = name.substring(0, name.lastIndexOf('.'));

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(hdfs.open(path)));
			String txt;
			while ((txt = reader.readLine()) != null) {
				rows.add(txt);
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
	
	/**
	 * 根据index返回line中对应的值
	 * 
	 * @param line
	 * @param index
	 * @return
	 */
	public String getColumn(String line, int index) {
		String[] splits = line.split(split);
		return splits[index];
	}
	/**
	 * 根据col返回line中对应的值
	 * 
	 * @param line
	 * @param col
	 * @return
	 */
	public String getColumn(String line, String col) {
		String[] splits = format.split(split);
		int index = Arrays.asList(splits).indexOf(col);
		return this.getColumn(line, index);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInput() {
		return input;
	}
	public void setInput(String input) {
		this.input = input;
	}
	public String getSplit() {
		return split;
	}
	public void setSplit(String split) {
		this.split = split;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public List<String> getRows() {
		return rows;
	}
	public void setRows(List<String> rows) {
		this.rows = rows;
	}
	public String toString(){
		return this.name+","+this.input+","+this.split+","+this.format;
	}
}
