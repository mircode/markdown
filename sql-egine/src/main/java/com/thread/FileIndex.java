package com.thread;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * ���ļ��ֿ�
 * @author κ����
 *
 */
public class FileIndex {

	// �ļ��ֵ��ܿ���
	private int count;
	// ���С
	private int size;
	// ÿ�����ʼ����
	private int blocks[];
	
	
	
	// �ļ�����,�ļ���С,�ļ�������
	private String filePath;
	private String fileSize;
	private String fileLines;
	
	
	
	public FileIndex(String filePath,int count) throws FileNotFoundException{
		RandomAccessFile file=new RandomAccessFile(filePath,"r");
	}
	
	// ����ָ�������������
	public List<String> getBlock(int i){
		return null;
	}
}
