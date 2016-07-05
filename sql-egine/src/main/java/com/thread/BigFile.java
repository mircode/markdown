package com.thread;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.CountDownLatch;

/**
 * 大文件Copy 
 * 
 * 
 * http://blog.csdn.net/zhuyijian135757/article/details/38471595
 * http://www.importnew.com/10712.html
 * 
 * @author 魏国兴
 *
 */
public class BigFile {

	// 128M的缓存区
	private static final int DATA_CHUNK = 128 * 1024 * 1024;

	/**
	 * FileChannel式拷贝
	 * 
	 * @param src
	 * @param dis
	 * @throws FileNotFoundException
	 */
	public static void CopyFileChannel(String src, String dis) throws Exception {

		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		RandomAccessFile rafi = new RandomAccessFile(src, "r");
		RandomAccessFile rafo = new RandomAccessFile(dis, "rw");

		FileChannel fco = rafo.getChannel();
		FileChannel fci = rafi.getChannel();

		
		//rafo.seek(rafo.length());
		ByteBuffer buf = ByteBuffer.allocate(DATA_CHUNK);// allocateDirect,通过系统创建缓冲区
		int count = 0;
		while ((count = fci.read(buf)) > 0) {
			buf.flip();
			fco.write(buf);
			buf.clear();
		}

		fci.close();
		rafi.close();

		fco.close();
		rafo.close();
	}

	

	/**
	 * 随机Transfer拷贝
	 * 
	 * @param src
	 * @param dis
	 * @throws Exception
	 */
	public static void CopyTransferFromRandom(String src, String dis)
			throws Exception {

		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		RandomAccessFile rafi = new RandomAccessFile(src, "r");
		RandomAccessFile rafo = new RandomAccessFile(dis, "rw");

		FileChannel fci = rafi.getChannel();
		FileChannel fco = rafo.getChannel();

		fco.transferFrom(fci, 0, fci.size());

		fci.close();
		rafi.close();

		fco.close();
		rafo.close();
	}

	/**
	 * 流式Transfer拷贝
	 * 
	 * @param src
	 * @param dis
	 * @throws Exception
	 */
	public static void CopyTransferStream(String src, String dis)
			throws Exception {

		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		FileInputStream inStream = new FileInputStream(src);
		FileOutputStream outStream = new FileOutputStream(dis);

		FileChannel in = inStream.getChannel();
		FileChannel out = outStream.getChannel();

		out.transferFrom(in, 0, in.size());

		inStream.close();
		in.close();
		outStream.close();
		out.close();
	}

	/**
	 * 使用MappedByteBuffer复制
	 * @param src
	 * @param dis
	 * @throws IOException
	 */
	public static void CopyMappedByteBuffer(String src, String dis)
			throws IOException {

		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		RandomAccessFile rafi = new RandomAccessFile(src, "r");
		RandomAccessFile rafo = new RandomAccessFile(dis, "rw");

		FileChannel fco = rafo.getChannel();
		FileChannel fci = rafi.getChannel();

		long count = fci.size();
		int pos = 0;

		// MappedByteBuffer的读性能远远大于FileChannel的性能，但是写性能相对较慢
		MappedByteBuffer mbbr = null;

		long len = count;
		while (len >= DATA_CHUNK) {
			mbbr = fci.map(MapMode.READ_ONLY, pos, DATA_CHUNK);
			fco.write(mbbr);
			mbbr.clear();
			len -= DATA_CHUNK;
			pos += DATA_CHUNK;
		}

		if (len > 0) {
			mbbr = fci.map(MapMode.READ_ONLY, pos, len);
			fco.write(mbbr);
			mbbr.clear();
		}

		unmap(mbbr); // release MappedByteBuffer

		fci.close();
		rafi.close();

		fco.close();
		rafo.close();
	}

	/*
	 * 在MappedByteBuffer释放后再对它进行读操作的话就会引发jvm crash，在并发情况下很容易发生
	 * 正在释放时另一个线程正开始读取，于是crash就发生了。所以为了系统稳定性释放前一般需要检 查是否还有线程在读或写
	 * 
	 * @param mappedByteBuffer
	 */
	public static void unmap(final MappedByteBuffer mappedByteBuffer) {
		try {
			if (mappedByteBuffer == null) {
				return;
			}

			mappedByteBuffer.force();
			AccessController.doPrivileged(new PrivilegedAction<Object>() {
				@Override
				public Object run() {
					try {
						Method getCleanerMethod = mappedByteBuffer.getClass()
								.getMethod("cleaner", new Class[0]);
						getCleanerMethod.setAccessible(true);
						sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod
								.invoke(mappedByteBuffer, new Object[0]);
						cleaner.clean();

					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 通过系统提供的拷贝接口实现复制
	 */
	public static void CopyPath(String src, String dis) throws Exception {
		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		Files.copy(Paths.get(src), Paths.get(dis), LinkOption.NOFOLLOW_LINKS);
	}

	/**
	 * 改造RandomAccessFile进行复制
	 * @param src
	 * @param dis
	 * @throws Exception
	 */
	public static void CopyBufferedRandomAccessFile(String src, String dis)
			throws Exception {

		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		BufferedRandomAccessFile in = new BufferedRandomAccessFile(src,
				DATA_CHUNK);
		BufferedRandomAccessFile out = new BufferedRandomAccessFile(dis, "rw",
				DATA_CHUNK);

		byte buf[] = new byte[DATA_CHUNK];

		int count = 0;
		while ((count = in.read(buf)) != -1) {
			out.write(buf, 0, count);
		}

		in.close();
		out.close();
	}

	public static void CopyBufferedStream(String src, String dis)
			throws Exception {
		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				new File(src)), DATA_CHUNK);
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(new File(dis)), DATA_CHUNK);

		byte[] buf = new byte[DATA_CHUNK];
		int count;
		while ((count = in.read(buf)) != -1) {
			out.write(buf, 0, count);
		}
		in.close();
		out.close();
	}

	public static void CopyStream(String src, String dis) throws Exception {
		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		FileInputStream in = new FileInputStream(src);
		FileOutputStream out = new FileOutputStream(dis);

		byte[] buf = new byte[DATA_CHUNK];
		int count;
		while ((count = in.read(buf)) != -1) {
			out.write(buf, 0, count);
		}

		in.close();
		out.close();
	}

	public static void CopyBufferedReader(String src, String dis)
			throws Exception {
		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		BufferedReader in = new BufferedReader(new FileReader(new File(src)));
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(dis)));

		String line = null;
		while ((line = in.readLine()) != null) {
			out.write(line);
		}

		in.close();
		out.close();
	}

	public static void CopyReader(String src, String dis) throws Exception {
		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		Reader in = new FileReader(src);
		FileWriter out = new FileWriter(dis, true);

		char[] buf = new char[DATA_CHUNK];

		int length = 0;
		while ((length = in.read(buf)) != -1) {
			out.write(buf, 0, length);
		}

		in.close();
		out.close();

	}

	public static void CopyBufferedData(String src, String dis)
			throws Exception {
		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		BufferedInputStream inputStream = new BufferedInputStream(
				new FileInputStream(src), DATA_CHUNK);
		BufferedOutputStream outputStream = new BufferedOutputStream(
				new FileOutputStream(dis, true), DATA_CHUNK);

		DataInputStream in = new DataInputStream(inputStream);
		DataOutputStream out = new DataOutputStream(outputStream);

		byte[] buf = new byte[DATA_CHUNK];
		int length = 0;
		while ((length = in.read(buf)) != -1) {
			out.write(buf, 0, length);
		}

		in.close();
		out.close();
	}

	public static void CopyData(String src, String dis) throws Exception {
		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		FileInputStream inputStream = new FileInputStream(src);
		FileOutputStream outputStream = new FileOutputStream(dis, true);

		DataInputStream in = new DataInputStream(inputStream);
		DataOutputStream out = new DataOutputStream(outputStream);

		byte[] buf = new byte[DATA_CHUNK];
		int length = 0;
		while ((length = in.read(buf)) != -1) {
			out.write(buf, 0, length);
		}

		in.close();
		out.close();
	}

	public static void testCopy(String src, String dis) throws Exception {

		StopWatch sw = new StopWatch("NIO Channel & TransferFrom & MappedByteBuffer & Path2Path");

		sw.start("FileChannel");
		CopyFileChannel(src, dis);
		sw.stop();

		sw.start("Path2Path");
		CopyPath(src, dis);
		sw.stop();

		sw.start("MappedByteBuffer");
		CopyMappedByteBuffer(src, dis);
		sw.stop();

		sw.start("Path2Path");
		CopyPath(src, dis);
		sw.stop();

		System.out.println(sw.prettyPrint());

		sw = new StopWatch("BIO 字节流  & 字符流  & 二进制");

		sw.start("字节流：Stream");
		CopyStream(src, dis);
		sw.stop();

		sw.start("缓存字节流：BufferedStream");
		CopyBufferedStream(src, dis);
		sw.stop();

		sw.start("随机字缓存节流：BufferedRandomAccessFile");
		CopyBufferedRandomAccessFile(src, dis);
		sw.stop();

		sw.start("字符流：Reader");
		CopyReader(src, dis);
		sw.stop();

		sw.start("缓存字符流：BufferedReader");
		CopyBufferedReader(src, dis);
		sw.stop();

		sw.start("二进制流：Data");
		CopyData(src, dis);
		sw.stop();

		sw.start("缓存二进制流：BufferedData");
		CopyBufferedData(src, dis);
		sw.stop();

		System.out.println(sw.prettyPrint());
	}

	public static void main(String args[]) throws Exception {

		String src = "D:\\log2";
		String dis = "D:\\log2";

		// 测试文件复制
		//testCopy(src,dis);
		
		// 测试多线程文件复制
		testCopyMutil(src,dis);
	}

	public static void testCopyMutil(String src,String dis) throws Exception{
		/*long a=2013265920;
		long b=134217728;
		System.out.println(2013265920*2);*/
		ByteBuffer[] maps;
		int end_map;
		final FileChannel cnl = new FileInputStream(src).getChannel();
		
		
		int MAX_VALUE=Integer.MAX_VALUE;
        try {
            final int map_num = (int)(cnl.size()/MAX_VALUE)+1;
            maps = new ByteBuffer[map_num];
           
            for(int i=0; i < map_num; i++) {
                final int offset = MAX_VALUE*i;
                final int length = (int)Math.min(MAX_VALUE, cnl.size()-offset);
                maps[i] = cnl.map(FileChannel.MapMode.READ_ONLY, offset, length);
            }

            end_map = maps.length;
        } finally {
            cnl.close();
        }
		StopWatch sw = new StopWatch("NIO Channel & TransferFrom & MappedByteBuffer & Path2Path");

//		sw.start("readFileChannel");
//		readFileChannel(src);
//		sw.stop();
		
//		sw.start("Path2Path");
//		CopyPath(src, dis);
//		sw.stop();
		
//		sw.start("readMappedByteBuffer");
//		readMappedByteBuffer(src);
//		sw.stop();
		
//		sw.start("CopyMultiThreadFileChannel");
//		CopyMultiThreadFileChannel(src, dis);
//		sw.stop();
		
		
		System.out.println(sw.prettyPrint());
	}
	

	public static void readFileChannel(String src) throws Exception{
		
		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		RandomAccessFile rafi = new RandomAccessFile(src, "r");
		FileChannel fci = rafi.getChannel();
		ByteBuffer buf = ByteBuffer.allocate(DATA_CHUNK);// allocateDirect,通过系统创建缓冲区
		int count = 0;
		while ((count = fci.read(buf)) > 0) {
			buf.flip();
			buf.clear();
		}
		fci.close();
		rafi.close();
	}
	public static void readMappedByteBuffer(String src) throws Exception{
		
		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		RandomAccessFile rafi = new RandomAccessFile(src, "r");
		FileChannel fci = rafi.getChannel();
		
		long count = fci.size();
		int pos = 0;

		// MappedByteBuffer的读性能远远大于FileChannel的性能，但是写性能相对较慢
		MappedByteBuffer mbbr = null;

		long len = count;
		while (len >= DATA_CHUNK) {
			System.out.println(pos);
			System.out.println(DATA_CHUNK);
			mbbr = fci.map(MapMode.READ_ONLY, pos,DATA_CHUNK);
			//fco.write(mbbr);
			mbbr.clear();
			len -= DATA_CHUNK;
			pos += DATA_CHUNK;
		}

		if (len > 0) {
			mbbr = fci.map(MapMode.READ_ONLY, pos, len);
			//fco.write(mbbr);
			mbbr.clear();
		}
		fci.close();
		rafi.close();
	}
	public static void CopyMultiThread(String src, String dis) throws InterruptedException {
		
		
		class CopyFile extends Thread {
			private long from; // copy起始位置
			private long to; // copy结束位置

			private CountDownLatch latch;
			public CopyFile(long from, long to,CountDownLatch latch) {
				this.from = from;
				this.to = to;
				this.latch=latch;
			}

			public void run() {
				try {

					
					RandomAccessFile rafi = new RandomAccessFile(src, "r");
					RandomAccessFile rafo = new RandomAccessFile(dis, "rw");

					FileChannel out = rafo.getChannel();
					FileChannel in = rafi.getChannel();
					
					long len=to-from;
					long pos=from;
					MappedByteBuffer buf=null;
					while (len >= DATA_CHUNK) {
						buf = in.map(MapMode.READ_ONLY, pos, DATA_CHUNK);
						out.write(buf);
						buf.clear();
						len -= DATA_CHUNK;
						pos += DATA_CHUNK;
					}

					if (len > 0) {
						buf = in.map(MapMode.READ_ONLY, pos, len);
						out.write(buf);
						buf.clear();
					}
					
					unmap(buf);
					in.close();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				latch.countDown();
				System.out.println("执行完毕to:"+to+" from:"+from);
			}

		}
		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}

		CountDownLatch latch=new CountDownLatch(4);
		// 获取文件的长度 分成4块
		long length = new File(src).length();
		long size = length / 4;
		// 定义四个线程
		for (int i = 0; i < 4; i++) {
			new CopyFile(i * size, (i + 1) * size,latch).start();
		}
		latch.await();
	}
	public static void CopyMultiThreadFileChannel(String src, String dis) throws InterruptedException{
		class CopyFile extends Thread {
			private CountDownLatch latch;
			private long from; // copy起始位置
			private long to; // copy结束位置

			public CopyFile(long from, long to,CountDownLatch latch) {
				this.from = from;
				this.to = to;
				this.latch=latch;
			}

			public void run() {
				try {

					RandomAccessFile in = new RandomAccessFile(src,"r");
					RandomAccessFile out = new RandomAccessFile(dis, "rw");
					
					in.seek(from);
					out.seek(from);

					byte[] buff = new byte[DATA_CHUNK];
					
					long count = 0;
					int len = 0;

					// 读取的字节数必须有数量限制 限制小于 to 和from 的差
					while ((len = in.read(buff)) != -1
							&& count <= (to - from)) {
						out.write(buff, 0, len);
						count += len;
					}
					in.close();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				this.latch.countDown();
			}

		}
		File srcFile = new File(src);
		if (!srcFile.exists()) {
			System.out.println("can't find the file:" + src);
			return;
		}
		File disFile = new File(dis);
		if (disFile.exists()) {
			disFile.delete();
		}
		CountDownLatch latch=new CountDownLatch(4);
		// 获取文件的长度 分成4块
		long length = new File(src).length();
		long size = length / 4;
		// 定义四个线程
		for (int i = 0; i < 4; i++) {
			new CopyFile(i * size, (i + 1) * size,latch).start();
		}
		latch.await();
	}
	
}
