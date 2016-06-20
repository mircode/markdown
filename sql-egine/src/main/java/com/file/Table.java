package com.file;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

/**
 * �ļ�������ӳ�����
 * 
 * @author κ����
 *
 */
public class Table {

	// �������
	protected String name;
	// �ļ�·��
	protected String input;
	// �ļ��ָ���
	protected String split;
	// ��־��ʽ
	protected String format;

	// �������
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
	 * ���һ������
	 * @param row
	 */
	public Table addRow(String row){
		this.rows.add(row);
		return this;
	}
	/**
	 * ��Ӷ�������
	 * @param row
	 */
	public Table addRows(List<String> rows){
		this.rows.addAll(rows);
		return this;
	}
	/**
	 * ����index����line�ж�Ӧ��ֵ
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
	 * ����col�еĶ����
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
	 * ����col����line�ж�Ӧ��ֵ
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
	 * ���طָ�����������ʽ��ʽ
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
	 * ��FileTable���л�ΪJson��
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
	 * Json�������л�ΪFileTable
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
