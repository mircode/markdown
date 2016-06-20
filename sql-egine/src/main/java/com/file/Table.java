package com.file;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

/**
 * 文件到表格的映射对象
 * 
 * @author 魏国兴
 *
 */
public class Table {

	// 表格名称
	protected String name;
	// 文件路径
	protected String input;
	// 文件分隔符
	protected String split;
	// 日志格式
	protected String format;

	// 表格数据
	protected List<String> rows=new ArrayList<String>();

	public Table(){
		
	}
	public Table(String name,String input,String split,String format){
		this(name,input,split,format,null);
	}
	public Table(String name,String input,String split,String format,List<String> rows){
		this.name=name;
		this.input=input;
		this.split=split;
		this.format=format;
		if(rows!=null)
			this.rows=rows;
	}
	

	/**
	 * 添加一行数据
	 * @param row
	 */
	public Table addRow(String row){
		this.rows.add(row);
		return this;
	}
	/**
	 * 添加多行数据
	 * @param row
	 */
	public Table addRows(List<String> rows){
		this.rows.addAll(rows);
		return this;
	}
	/**
	 * 根据index返回line中对应的值
	 * 
	 * @param line
	 * @param index
	 * @return
	 */
	public String getColumn(String line, int index) {
		String[] splits = line.split(getRgxSplit());
		return splits[index];
	}

	
	/**
	 * 返回col中的多个列
	 * 
	 * @param line
	 * @param cols
	 * @return
	 */
	public String getColumns(String line,String cols){
		
		String row = "";
		if(cols.equals("*")){
			row=line;
		}else{
			String splits[]=cols.split(",");
			for (String col : splits) {
				row += this.getColumn(line, col)+split;
			}
			row = row.substring(0, row.length() - 1);
		}
		return row;
	}
	public String toString() {
		String res = this.format + "\n";
		if(rows!=null){
			for (String row : rows) {
				res += row + "\n";
			}
		}
		return res;
	}
	/**
	 * 根据col返回line中对应的值
	 * 
	 * @param line
	 * @param col
	 * @return
	 */
	private String getColumn(String line, String col) {
		String[] splits = format.split(getRgxSplit());
		int index = Arrays.asList(splits).indexOf(col);
		if(index<0){
			if(col.startsWith(name)){
				index = Arrays.asList(splits).indexOf(col.substring((name+".").length()));
			}else{
				index = Arrays.asList(splits).indexOf(name+"."+col);
			}
		}
		return this.getColumn(line, index);
	}
	/**
	 * 返回分隔符的正则表达式形式
	 * 
	 * @return
	 */
	private String getRgxSplit() {
		String regex = split;
		if (regex.equals("|")) {
			regex = "\\|";
		}
		if (regex.equals(" ")) {
			regex = "\\s+";
		}
		return regex;
	}
	/**
	 * 将FileTable序列化为Json串
	 * @return
	 */
	public String serialize() {
		List<String> rows = this.rows;
		this.rows = null;
		ObjectMapper mapper = new ObjectMapper();
		Writer write = new StringWriter();
		try {
			mapper.writeValue(write, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.rows = rows;
		return write.toString();

	}
	/**
	 * Json串反序列化为FileTable
	 * @param json
	 * @return
	 */
	public Table diserialize(String json) {
		Table fileTable = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			fileTable = (Table) mapper.readValue(json,
					TypeFactory.rawClass(Table.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		fileTable.setRows(new ArrayList<String>());
		return fileTable;
	}
	public String getName() {
		return name;
	}
	public Table setName(String name) {
		this.name = name;
		return this;
	}
	public String getInput() {
		return input;
	}
	public Table setInput(String input) {
		this.input = input;
		return this;
	}
	public String getSplit() {
		return split;
	}
	public Table setSplit(String split) {
		this.split = split;
		return this;
	}
	public String getFormat() {
		return format;
	}
	public Table setFormat(String format) {
		this.format = format;
		return this;
	}
	public List<String> getRows() {
		return rows;
	}
	public Table setRows(List<String> rows) {
		this.rows = rows;
		return this;
	}

	
}
