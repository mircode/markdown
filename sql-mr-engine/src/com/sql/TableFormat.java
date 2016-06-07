package com.sql;

import java.util.Arrays;

/**
 * ��־��ʽ��
 * @author κ����
 *
 */
public class TableFormat {
	// �зַ���
	private String split;
	// ��־��ʽ
	private String format;
	
	public TableFormat(String split,String format){
		this.split=split;
		this.format=format;
	}
	/**
	 * ����index����line�ж�Ӧ��ֵ
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
	 * ����col����line�ж�Ӧ��ֵ
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
