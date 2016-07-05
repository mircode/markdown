package com.thread;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * 将文件分块
 * @author 魏国兴
 *
 */
public class FileIndex {

	// 文件分的总块数
	private int count;
	// 块大小
	private int size;
	// 每块的起始索引
	private int blocks[];
	
	
	
	// 文件名称,文件大小,文件总行数
	private String filePath;
	private String fileSize;
	private String fileLines;
	
	
	
	public FileIndex(String filePath,int count) throws FileNotFoundException{
		RandomAccessFile file=new RandomAccessFile(filePath,"r");
	}
	
	// 返回指定索引块的内容
	public List<String> getBlock(int i){
		return null;
	}
}
