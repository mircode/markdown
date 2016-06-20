package com.hdfs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.IOUtils;

/**
 * Hadoop�ļ�ϵͳ-������
 * @author κ����
 */
public class HDFSHelper {

	/**
	 * �ж��ļ�����Ŀ¼�Ƿ����
	 * @param conf
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static boolean exits(Configuration conf, String path) throws IOException {
          FileSystem fs = FileSystem.get(conf);
          return fs.exists(new Path(path));
    }
	/**
	 * �ж��Ƿ���Ŀ¼
	 * @param conf
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static boolean isDirectory(Configuration conf, String path) throws IOException {
          FileSystem fs = FileSystem.get(conf);
         return fs.isDirectory(new Path(path));
    }
	/**
	 * �ж��Ƿ����ļ�
	 * @param conf
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static boolean isFile(Configuration conf, String path) throws IOException {
          FileSystem fs = FileSystem.get(conf);
         return fs.isFile(new Path(path));
    }
    /**
     * �����ļ�
     *
     * @param conf
     * @param filePath
     * @param contents
     * @throws IOException
     */
    public static void createFile(Configuration conf, String filePath, byte[] contents) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path path = new Path(filePath);
        FSDataOutputStream outputStream = fs.create(path);
        outputStream.write(contents);
        outputStream.close();
        fs.close();
    }

    /**
     * �����ļ�
     *
     * @param conf
     * @param filePath
     * @param fileContent
     * @throws IOException
     */
    public static void createFile(Configuration conf, String filePath, String fileContent) throws IOException {
        createFile(conf, filePath, fileContent.getBytes());
    }

    /**
     * �ϴ��ļ�
     * 
     * @param conf
     * @param localFilePath
     * @param remoteFilePath
     * @throws IOException
     */
    public static void uploadFile(Configuration conf, String localFilePath, String remoteFilePath) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path localPath = new Path(localFilePath);
        Path remotePath = new Path(remoteFilePath);
        fs.copyFromLocalFile(true, true, localPath, remotePath);
        fs.close();
    }
    /**
     * �����ļ�
     * 
     * @param conf
     * @param localFilePath
     * @param remoteFilePath
     * @throws IOException
     */
    public static void dowloadFile(Configuration conf, String remoteFilePath,String localFilePath) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path localPath = new Path(localFilePath);
        Path remotePath = new Path(remoteFilePath);
        fs.copyToLocalFile(false,remotePath,localPath,false);
        fs.close();
    }
    /**
     * ɾ��Ŀ¼���ļ�
     *
     * @param conf
     * @param remoteFilePath
     * @param recursive
     * @return
     * @throws IOException
     */
    public static boolean deleteFile(Configuration conf, String remoteFilePath, boolean recursive) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        boolean result = fs.delete(new Path(remoteFilePath), recursive);
        fs.close();
        return result;
    }
    /**
     * �������,��ɾ��
     *
     * @param conf
     * @param remoteFilePath
     * @param recursive
     * @return
     * @throws IOException
     */
    public static boolean deleteOnExit(Configuration conf, String remoteFilePath) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        boolean result = fs.deleteOnExit(new Path(remoteFilePath));
        fs.close();
        return result;
    }
    /**
     * ɾ��Ŀ¼���ļ�(�������Ŀ¼,����ɾ��)
     *
     * @param conf
     * @param remoteFilePath
     * @return
     * @throws IOException
     */
    public static boolean deleteFile(Configuration conf, String remoteFilePath) throws IOException {
        return deleteFile(conf, remoteFilePath, true);
    }

    /**
     * �ļ�������
     *
     * @param conf
     * @param oldFileName
     * @param newFileName
     * @return
     * @throws IOException
     */
    public static boolean renameFile(Configuration conf, String oldFileName, String newFileName) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path oldPath = new Path(oldFileName);
        Path newPath = new Path(newFileName);
        boolean result = fs.rename(oldPath, newPath);
        fs.close();
        return result;
    }

    /**
     * ����Ŀ¼
     *
     * @param conf
     * @param dirName
     * @return
     * @throws IOException
     */
    public static boolean createDirectory(Configuration conf, String dirName) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path dir = new Path(dirName);
        boolean result = fs.mkdirs(dir);
        fs.close();
        return result;
    }

    /**
     * �г�ָ��·���µ������ļ�(������Ŀ¼)
     *
     * @param conf
     * @param basePath
     * @param recursive
     */
    public static RemoteIterator<LocatedFileStatus> listFiles(FileSystem fs, String basePath, boolean recursive) throws IOException {
        RemoteIterator<LocatedFileStatus> fileStatusRemoteIterator = fs.listFiles(new Path(basePath), recursive);
        return fileStatusRemoteIterator;
    }

    /**
     * �г�ָ��·���µ��ļ����ǵݹ飩
     *
     * @param conf
     * @param basePath
     * @return
     * @throws IOException
     */
    public static RemoteIterator<LocatedFileStatus> listFiles(Configuration conf, String basePath) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        RemoteIterator<LocatedFileStatus> remoteIterator = fs.listFiles(new Path(basePath), false);
        fs.close();
        return remoteIterator;
    }

    /**
     * �г�ָ��Ŀ¼�µ��ļ�\��Ŀ¼��Ϣ���ǵݹ飩
     *
     * @param conf
     * @param dirPath
     * @return
     * @throws IOException
     */
    public static FileStatus[] listStatus(Configuration conf, String dirPath) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        FileStatus[] fileStatuses = fs.listStatus(new Path(dirPath));
        fs.close();
        return fileStatuses;
    }
    /**
     * �ϲ���
     * @param conf
     * @param src
     * @param output
     */
    public static void mergeFiles(Configuration conf, String src, String output) {
		try {
			FileSystem fs = FileSystem.get(conf);
			fs.deleteOnExit(new Path(output));
			
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(fs.create(new Path(output))));
			
			FileStatus[] outputFiles = fs.listStatus(new Path(src));
			for (FileStatus fileStatus : outputFiles) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(fileStatus.getPath())));
	    		String line;
	    		while((line = reader.readLine()) != null) {
	    			w.append(line);
	    			w.newLine();
	    		}
	    		reader.close();
			}
			w.close();
		} catch (Exception e) {
			throw new RuntimeException("Error merging files from dir: " + src + " into file: " + output, e);
		}
	}

    /**
     * ��ȡ�ļ�����
     *
     * @param conf
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String readFile(Configuration conf, String filePath) throws IOException {
        String fileContent = null;
        FileSystem fs = FileSystem.get(conf);
        Path path = new Path(filePath);
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            inputStream = fs.open(path);
            outputStream = new ByteArrayOutputStream(inputStream.available());
            IOUtils.copyBytes(inputStream, outputStream, conf);
            fileContent = outputStream.toString();
        } finally {
            IOUtils.closeStream(inputStream);
            IOUtils.closeStream(outputStream);
            fs.close();
        }
        return fileContent;
    }
    /**
     * ��ȡ�ļ�����
     *
     * @param conf
     * @param filePath
     * @return
     * @throws IOException
     */
    public static List<String> readLines(Configuration conf, String filePath) throws IOException {
    	
    	List<String> lines = new ArrayList<String>();
        FileSystem fs = FileSystem.get(conf);
        Path path = new Path(filePath);
        
        BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(fs.open(path)));
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			throw new RuntimeException("Error loading table in memory: " + path, e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
       return lines;
    }
    public static void main(String args[]) throws IOException {
    	
        Configuration conf = new Configuration();
        
        String newDir = "/test";
        //01.���·���Ƿ���� ����
        if (HDFSHelper.exits(conf, newDir)) {
            System.out.println(newDir + " �Ѵ���!");
        } else {
            //02.����Ŀ¼����
            boolean result = HDFSHelper.createDirectory(conf, newDir);
            if (result) {
                System.out.println(newDir + " �����ɹ�!");
            } else {
                System.out.println(newDir + " ����ʧ��!");
            }
        }
        String fileContent = "Hi,hadoop. I love you";
        String newFileName = newDir + "/myfile.txt";

        //03.�����ļ�����
        HDFSHelper.createFile(conf, newFileName, fileContent);
        System.out.println(newFileName + " �����ɹ�");

        //04.��ȡ�ļ����� ����
        System.out.println(newFileName + " ������Ϊ:\n" + HDFSHelper.readFile(conf, newFileName));

        //05. ���Ի�ȡ����Ŀ¼��Ϣ
        FileStatus[] dirs = HDFSHelper.listStatus(conf, "/");
        System.out.println("--��Ŀ¼�µ�������Ŀ¼---");
        for (FileStatus s : dirs) {
            System.out.println(s);
        }

        //06. ���Ի�ȡ�����ļ�
        FileSystem fs = FileSystem.get(conf);
        RemoteIterator<LocatedFileStatus> files = HDFSHelper.listFiles(fs, "/", true);
        System.out.println("--��Ŀ¼�µ������ļ�---");
        while (files.hasNext()) {
            System.out.println(files.next());
        }
        fs.close();

        //ɾ���ļ�����
        boolean isDeleted = HDFSHelper.deleteFile(conf, newDir);
        if(isDeleted){
        	System.out.println(newDir + " �ѱ�ɾ��");
        }

    }
}
