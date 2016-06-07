package com.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件到表格的映射对象
 * @author 魏国兴
 *
 */
public class FileTable {

	// 表格名称
	private String name;
	// 输入路径
	private String input;
	// 日志格式
	private TableFormat format;
	// 表格数据
	private List<String> rows;
	
	
	public FileTable(String name,String split,String format,String input){
		
		if(input.startsWith("classpath:")){
			try {
				String filePath=input.substring("classpath:".length());
				filePath=FileTable.class.getClassLoader().getResource(filePath).getPath();
				filePath = URLDecoder.decode(filePath, "UTF-8");
	
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
				String str = null;
				this.rows=new ArrayList<String>();
				while ((str = reader.readLine()) != null) {
					this.rows.add(str);
				}
				reader.close();
				
				if(name==null){
					File file=new File(filePath);
					this.name=file.getName().substring(0,file.getName().lastIndexOf("."));
				}else{
					this.name=name;
				}
				if(split==null){
					split="|";
				}
				this.input=input;
				this.format=new TableFormat(split,format);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public FileTable(String name,String format,String input){
		this(name,null,format,input);
	}
	public FileTable(){}
	public FileTable(String format,String input){
		this(null,null,format,input);
	}
	public FileTable(String name,String split,String format,List<String> rows){
		this(name,null,split,format,rows);
	}
	public FileTable(String name,String input,String split,String format,List<String> rows){
		this.name=name;
		this.input=input;
		this.format=new TableFormat(split,format);
		this.rows=rows;
	}
	public FileTable(String name,String input,TableFormat format,List<String> rows){
		
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
	public TableFormat getFormat() {
		return format;
	}
	public void setFormat(TableFormat format) {
		this.format = format;
	}
	public List<String> getRows() {
		return rows;
	}
	public void setRows(List<String> rows) {
		this.rows = rows;
	}
	public String toString(){
		String res=this.format.getFormat()+"\n";
		for(String row:rows){
			res+=row+"\n";
		}
		return res;
	}
}
