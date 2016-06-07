package com.sql;

import java.util.List;

/**
 * 表连接对象
 * @author 魏国兴
 */
public class JoinTable {
	
	public String table;
	public String path;
	public List<On> on=null;
	
	public JoinTable(String table,String path,String on){
		this.table=table;
		this.path=path;
		
		this.initOn(on);
		
	}
	
	private void initOn(String str){
		
		String[] splits=str.trim().split("and");
		for(String where : splits){
			String[] sps = where.replaceAll("\\s+","").split("=");
			
			String mainCol=null;
			String joinCol=null;
			if(sps[0].indexOf(this.table)>-1){
				joinCol=sps[0].substring(sps[0].indexOf("."));
				mainCol=sps[1].substring(sps[1].indexOf("."));
			}else{
				mainCol=sps[0].substring(sps[0].indexOf("."));
				joinCol=sps[1].substring(sps[1].indexOf("."));
			}
			this.on.add(new On(mainCol,joinCol));
		}
		
	}
	// 内部类
	public class On{
		public String mCol;// 主表字段
		public String jCol;// join表字段
		
		public On(String mCol,String jCol){
			this.mCol=mCol;
			this.jCol=jCol;
		}
	}
}
