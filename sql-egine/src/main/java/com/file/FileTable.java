package com.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import com.conf.SqlConf;

/**
 * ���ļ��м���Table
 * 
 * @author κ����
 *
 */
public class FileTable extends Table {

	
	public FileTable(String input) {
		this(null, input, null, null, null);
	}

	public FileTable(String name, String input) {
		this(name, input, null, null, null);
	}

	public FileTable(String name, String input, String split, String format) {
		this(name, input, split, format, null);
	}

	public FileTable(String name, String split, String format, List<String> rows) {
		this(name, null, split, format, rows);
	}

	public FileTable(String name, String input, String split, String format,
			List<String> rows) {

		if (name == null) {
			name = new File(input).getName();
			if (name.lastIndexOf(".") > -1) {
				name = name.substring(0, name.lastIndexOf("."));
			}
		}
		if(input.startsWith("classpath:")){
			input=getResource(input);
		}
		if (split == null)
			split = this.readSplit(input);
		if (format == null)
			format = this.readFormat(input);
		
		this.name = name;
		this.input = input;
		this.split = split;
		this.format = format;
		if (rows != null) {
			this.addRows(rows);
		}
		this.addRows(this.readRows(input));

	}

	/**
	 * ���ļ��л�ȡ��־��ʽ
	 * 
	 * @param path
	 * @return
	 */
	public String readFormat(String path) {
		String format = null;

		// ��ȡһ��
		List<String> rows = readFile(path, null, 1);

		if (rows != null) {
			format = rows.get(0);
			if (format.startsWith("#")) {
				format = format.substring("#".length());
			} else {
				String spl = this.readSplit(path);
				String splits[] = format.split(spl);
				String fmt = "";
				for (int i = 1; i <= splits.length; i++) {
					fmt += "col" + i + spl;
				}
				if (!fmt.equals("")) {
					format = fmt.substring(0, fmt.length() - 1);
				}
			}
		}
		return format;
	}

	/**
	 * ���ļ��л�ȡ�ָ���
	 * 
	 * @param path
	 * @return
	 */
	public String readSplit(String path) {

		String split = "|";

		// ���ļ��ж�ȡ3��
		List<String> rows = readFile(path, null, 2);

		String header = rows.get(0);
		String line = rows.get(1);

		// ����ʹ�ó��÷ָ����зּ�¼
		String[] splits = { "\\|", ",", "\\s+", "-", "_", ":" };

		for (String regex : splits) {
			int len1 = header.split(regex).length;
			int len2 = line.split(regex).length;

			if (len1 != 0 && len1 == len2) {
				if (regex.equals("\\s+")) {
					split = " ";
				}
				if (regex.equals("\\|")) {
					split = "|";
				} else {
					split = regex;
				}
				break;
			}
		}
		// ���طָ���
		return split;
	}

	/**
	 * ���ļ��л�ȡ��¼��
	 * 
	 * @param path
	 * @return
	 */
	public List<String> readRows(String path) {
		// ��ȡ���з�#�ſ�ͷ����
		return this.readFile(path, "[^#].*", -1);
	}

	/**
	 * ��ȡ�ļ�ָ������
	 * 
	 */
	private List<String> readFile(String path, String filter, int num) {

		// ��ȡ�ļ����ݵ�List��
		List<String> rows = new ArrayList<String>();
		// ��ȡ0��
		if (num == 0)
			return rows;
		try {
			// ��ȡ�ļ���������
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(URLDecoder.decode(path, "UTF-8")),
					"UTF-8"));
			// ��¼����
			int counter = 0;
			// ��ȡ�ļ�
			String line = null;
			while ((line = reader.readLine()) != null) {
				// ��ȡָ������
				if (num > 0 && counter++ > num) {
					break;
				} else {
					if (filter == null
							|| (filter != null && line.matches(filter))) {
						rows.add(line);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rows.size() == 0 ? null : rows;
	}
	private String getResource(String resource) {
		String path = SqlConf.class.getClassLoader().getResource(resource.substring("classpath:".length()))
				.getPath();
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return path;
	}
}
