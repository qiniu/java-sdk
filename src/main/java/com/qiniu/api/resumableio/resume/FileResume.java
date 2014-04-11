package com.qiniu.api.resumableio.resume;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class FileResume extends BaseResume {
	/** 
	 * 指定断点记录外部文件夹,默认在用户目录下
	 * */
	public static String RESUME_DIR = null;
	/**
	 * @see RESUME_FILENAME_TYPE
	 */
	public static final int SHA256_TYPE = 2;
	/**
	 * @see RESUME_FILENAME_TYPE
	 */
	public static final int MD5_TYPE = 1;
	/**
	 * @see RESUME_FILENAME_TYPE
	 */
	public static final int BASE64_TYPE = 0;
	/**
	 * 断点记录文件文件名生成规则，默认key的urlsafebase64编码
	 */
	public static int RESUME_FILENAME_TYPE = 0;
	
	
	private File file;

	public FileResume(int blockCount, String key) throws Exception {
		super(blockCount, key);
		initFile();
		this.load();
	}

	/**
	 * 通过配置文件传入外部指定文件夹
	 * @throws IOException
	 */
	private void initFile() throws IOException {
		if(file == null){
			String dir = getDir();
			file = new File(dir);
			file.mkdirs();
		}
		if(file.isDirectory()){
			
			file = new File(file.getCanonicalPath(), getTempFileName() + ".resume");
			file.createNewFile();
		}
	}
	
	private String getTempFileName(){
		if(RESUME_FILENAME_TYPE == MD5_TYPE){
			return DigestUtils.md5Hex(key);
		}else if(RESUME_FILENAME_TYPE == SHA256_TYPE){
			return DigestUtils.sha256Hex(key);
		}else{
			return Base64.encodeBase64URLSafeString(key.getBytes());
		}
		
	}

	@Override
	public void load() throws Exception {
		FileReader freader = null;
		BufferedReader reader = null;
		try {
			freader = new FileReader(file);
			reader = new BufferedReader(freader);
			String line = null;
			while ((line = reader.readLine()) != null) {
				Block b = analyLine(line);
				if(b != null){
					this.set(b);
				}
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (freader != null) {
				try {
					freader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected Block analyLine(String line) {
		String[] s = line.split(",");
		if(s.length > 1){
			int idx = Integer.parseInt(s[0]);
			String ctx = s[1];
			Block block = new Block(idx, ctx);
			return block;
		}else{
			return null;
		}
	}
	
	protected String buildLine(Block block){
		String line = block.getIdx() + "," + block.getCtx();
		return line;
	}

	@Override
	public void save() throws Exception {
		FileWriter fw = null;
		BufferedWriter bw = null;
		ArrayList<Block> bs = new ArrayList<Block>();
		try {
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			for(Block block : blocks){
				if(block != null && block.isNewAdd()){
					block.setNewAdd(false);
					bw.write(buildLine(block));
					bw.newLine();
					bs.add(block);
				}
			}
			bw.flush();
		}catch(Exception e){
			for(Block block : bs){
				block.setNewAdd(true);
			}
			throw e;
		}finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void clean() {
		file.delete();
	}

	public static String getDir() throws IOException{
		String dir = RESUME_DIR;
		if(dir == null){
			dir = System.getProperties().getProperty("user.home");
			File file = new File(dir, ".qiniu_client");
			file = new File(file, "upload_resume");
			dir = file.getCanonicalPath();		
		}
		return dir;
	}
	
	public static void cleanAll() throws IOException{
		cleanAll(null);
	}
	

	/**
	 * 会删除所传入的文件，谨慎使用此方法
	 * @param file
	 * @throws IOException
	 */
	protected static void cleanAll(File file) throws IOException{
		if(file == null){
			file = new File(getDir());
		}
		if(!file.exists()){
			return;
		}else if(file.isFile()){
			file.delete();
		}else{
			for(File f : file.listFiles()){
				cleanAll(f);
			}
		}
	}
}
