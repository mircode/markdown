package com.file;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	// ��Ԫ��Ӧ�÷��ϵ�������ʽ
	protected String filter;
	// �������
	protected List<String> rows=new ArrayList<String>();

	public Table(){
		
	}
	
	public Table(String name,String input,String split,String format,String filter){
		this(name,input,split,format,filter,null);
	}
	public Table(String name,String input,String split,String format,String filter,List<String> rows){
		this.name=name;
		this.input=input;
		this.split=split;
		this.format=format;
		this.filter=filter;
		if(rows!=null)
			this.setRows(rows);
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
				row += this.getColumn(line, col,String.class)+split;
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
	@SuppressWarnings("unchecked")
	public <T> T getColumn(String line,String col,Class<T> type){
		String[] splits = format.split(getRgxSplit());
		int index = Arrays.asList(splits).indexOf(col);
		if(index<0){
			if(col.startsWith(name)){
				index = Arrays.asList(splits).indexOf(col.substring((name+".").length()));
			}else{
				index = Arrays.asList(splits).indexOf(name+"."+col);
			}
		}
		String val=this.getColumn(line, index);
		
		String name=type.getSimpleName();
		
		Object res=null;
		if(name.toLowerCase().contains("double")){
			res=Double.parseDouble(val);
		}else if(name.toLowerCase().contains("float")){
			res=Float.parseFloat(val);
		}else if(name.toLowerCase().contains("integer")){
			res=Integer.parseInt(val);
		}else if(name.toLowerCase().contains("boolean")){
			res=Integer.parseInt(val);
		}else{
			res=val;
		}
		return (T)res;
		
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
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	public List<String> getRows() {
		return rows;
	}
	
	
	public Table setRows(List<String> rows) {
		this.rows = this.filterRows(rows,this.filter);
		return this;
	}
	public Table setRow(String row) {
		List<String> rows=new ArrayList<String>();
		rows.add(row);
		this.setRows(rows);
		return this;
	}
	/**
	 * ���һ������
	 * @param row
	 */
	public Table addRow(String r){
		String row=this.filterRow(r,this.filter);
		if(row!=null) this.rows.add(row);
		return this;
	}
	/**
	 * ��Ӷ�������
	 * @param row
	 */
	public Table addRows(List<String> rows){
		this.rows.addAll(this.filterRows(rows,this.filter));
		return this;
	}

	private List<String> filterRows(List<String> rs,String filter){
		List<String> rows=new ArrayList<String>();
		if(filter!=null&&rs!=null){
			for(String r:rs){
				String nw=this.filterRow(r, filter);
				if(nw!=null) rows.add(nw);
			}
			return rows;
		}
		return rs;
	}
	private String filterRow(String r,String filter){
		if(filter!=null){
			// ���SQL
			Pattern p = Pattern.compile(filter);
			Matcher m = p.matcher(r);
			String row="";
			
			// ������ƥ��Ľ����ŵ�Map��
			while (m.find()) {
				int count=m.groupCount();
				if(!m.group().equals("")){
					for(int i=1;i<=count;i++){
						String n=m.group(i);
						if(n.endsWith(split)){
							row+=n;
						}else{
							row+=n+split;
						}
					}
				}
			}
			if(!row.equals("")){
				if(row.endsWith(split)){
					row=row.substring(0,row.length()-1);
				}
			}else{
				row=null;
			}
			return row;
		}
		return r;
	}
	
	
}
