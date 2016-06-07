package com.sql;

import java.util.Arrays;

/**
 * 日志格式类
 * @author 魏国兴
 *
 */
public class TableFormat {
	// 切分符号
	private String split;
	// 日志格式
	private String format;
	
	public TableFormat(String split,String format){
		this.split=split;
		this.format=format;
	}
	/**
	 * 根据index返回line中对应的值
	 * 
	 * @param line
	 * @param index
	 * @return
	 */
	public String getColumn(String line, int index) {
		String[] splits = line.split(getSplitFormat());
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
		String[] splits = format.split(getSplitFormat());
		int index = Arrays.asList(splits).indexOf(col);
		return this.getColumn(line, index);
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
	
	private String getSplitFormat(){
		if(split.equals("|")){
			return "\\"+this.split;
		}else{
			return this.split;
		}
	}
	
}
