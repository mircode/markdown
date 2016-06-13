package com.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * �ļ�������ӳ�����
 * 
 * @author κ����
 *
 */
public class FileTable {

	// �������
	private String name;
	// ����·��
	private String input;
	// �ļ�ϵͳ����
	private String type;
	// ��־��ʽ
	private TableFormat format;
	// �������
	private List<String> rows;

	public FileTable() {
	}

	public FileTable(String input) {
		this(null, null, null, input);
	}

	public FileTable(String name, String input) {
		this(name, null, null, input);
	}

	public FileTable(String name, String format, String input) {
		this(name, null, format, input);
	}

	public FileTable(String name, String split, String format, String input) {
		this(name, split, format, input, false);
	}

	public FileTable(String name, String split, String format, String input,
			boolean lazyLoad) {

		// input: file://xxxx.txt
		// input: classpath://xxxx.txt
		// input: hdfs://xxxx.txt

		this.type = "file";

		if (input != null) {

			if (!input.matches("^(file|classpath|hdfs).*")) {
				input = "file://" + input;
			}

			boolean isFile = true;
			String sp = File.separator;
			if (input.endsWith(sp)) {
				isFile = false;
				input = input.substring(0, input.length() - 1);
			}
			// ����input
			Pattern p = Pattern.compile("^(file|classpath|hdfs):(.*)");
			Matcher m = p.matcher(input);

			// ������ƥ��Ľ����ŵ�Map��
			while (m.find()) {
				type = m.group(1);

				String path = m.group(2);
				// ����·��
				if (path.startsWith(sp)) {
					this.input = path + (isFile ? "" : sp);

					// ���·��
				} else if (!path.startsWith(".")&&!path.matches("\\w:.*")) {
					path = "." + sp + path;
					this.input = path + (isFile ? "" : sp);
				} else {
					this.input = path + (isFile ? "" : sp);
				}

				Matcher pm = Pattern.compile(".*/([^/]*)$").matcher(path);
				if (pm.find()) {
					name = pm.group(1);
				}

				this.name = name;
			}
		}

		if (type.equals("hdfs")) {
			String path = this.input;
			this.rows=this.loadHdfs(path);
		} else {

			String path = this.input;
			if (type.equals("classpath")) {
				path = FileTable.class.getClassLoader().getResource(this.input)
						.getPath();
			}
			if (lazyLoad == false) {
				this.rows = loadFile(path, "^#format.*", -1);
			}
			// ���Դ��ļ��н�����־��ʽ
			if (format == null) {
				format = readFormat(path);
			}
			// ���Դ��ļ��н�����־�ķָ���
			if (split == null) {
				split = readSplit(path);
			}

			this.format = new TableFormat(split, format);

		}

	}

	public List<String> loadHdfs(String path) {
		return null;
	}

	public FileTable(String name, String split, String format, List<String> rows) {
		this(name, null, split, format, rows);
	}

	public FileTable(String name, String input, String split, String format,
			List<String> rows) {
		this.name = name;
		this.input = input;
		this.format = new TableFormat(split, format);
		this.rows = rows;
	}

	
	public String serialize(){
		return this.name+"$"+this.input+"$"+this.format.getSplit()+"$"+this.format.getFormat();
	}
	public void diserialize(String line){
		String[] splits=line.split("$");
		this.name=splits[0];
		this.input=splits[1];
		this.format = new TableFormat(splits[2], splits[3]);
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
		List<String> rows = loadFile(path, null, 2);

		String format = rows.get(0);
		String line = rows.get(1);

		String[] splits = { "\\|", ",", "\\s+", "-", "_", ":" };

		for (String regex : splits) {

			int len1 = format.split(regex).length;
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
		return split;
	}

	/**
	 * ���ļ��л�ȡ��־��ʽ
	 * 
	 * @param path
	 * @return
	 */
	public String readFormat(String path) {
		String format = null;
		List<String> rows = loadFile(path, null, 1);
		if (rows != null) {
			format = rows.get(0);
			if (format.startsWith("#format:")) {
				format = format.substring("#format:".length());
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
	 * ��ȡ�ļ�ָ������
	 * 
	 * @param path
	 */
	public List<String> loadFile(String path, String filter, int lineNums) {

		List<String> rows = new ArrayList<String>();

		try {
			path = URLDecoder.decode(path, "UTF-8");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(path), "UTF-8"));
			String str = null;

			int counter = 0;
			while ((str = reader.readLine()) != null) {
				if (filter != null && str.matches(filter)) {
					continue;
				}
				if (lineNums == 0) {
					break;
				} else if (lineNums > 0) {
					// ���ļ����ص��ڴ���
					rows.add(str);
					counter++;
					if (counter >= lineNums) {
						break;
					}
				} else {
					// ���ļ����ص��ڴ���
					rows.add(str);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rows.size() == 0 ? null : rows;
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

	public String toString() {
		String res = this.format.getFormat() + "\n";
		if(rows!=null){
			for (String row : rows) {
				res += row + "\n";
			}
		}
		return res;
	}

	public class TableFormat {
		// �зַ���
		private String split;
		// ��־��ʽ
		private String format;

		public TableFormat(String split, String format) {
			this.split = split;
			this.format = format;
		}

		/**
		 * ����index����line�ж�Ӧ��ֵ
		 * 
		 * @param line
		 * @param index
		 * @return
		 */
		public String getColumn(String line, int index) {
			String[] splits = line.split(getRegexSplit());
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
			String[] splits = format.split(getRegexSplit());
			int index = Arrays.asList(splits).indexOf(col);
			return this.getColumn(line, index);
		}

		/**
		 * ���طָ�����������ʽ��ʽ
		 * 
		 * @return
		 */
		private String getRegexSplit() {
			String regex = this.split;
			if (regex.equals("|")) {
				regex = "\\|";
			}
			if (regex.equals(" ")) {
				regex = "\\s+";
			}
			return regex;
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

	}

}
