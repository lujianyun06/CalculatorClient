package com.harlan.calculator2;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.lpr.FileSysUtil;

public class EncryptFile {
	private static String pasw="123456";
	private static String keyString="1234567887654321";
	private static Context con;
	public EncryptFile(Context context){
		con=context;
	}
	public static void encryptFile(String fileName){

		try {
			char[] encryptText = pasw.toCharArray();
			RandomAccessFile fileAccess = new RandomAccessFile(new File(fileName), "rw");
			if(fileAccess.length()>10){
				fileAccess.seek(0);
				for(int i=0;i<encryptText.length;i++){
					fileAccess.seek(i);
					char old = (char)fileAccess.read();
					fileAccess.seek(i);
					fileAccess.write(old^encryptText[i]);
				}
				fileAccess.seek(0);
				fileAccess.close();
			}else if(fileAccess.length()<=10){
				fileAccess.seek(0);
				for(int i=0;i<fileAccess.length();i++){
					fileAccess.seek(i);
					char old = (char)fileAccess.read();
					fileAccess.seek(i);
					fileAccess.write(old^encryptText[i]);
				}
				fileAccess.seek(0);
				fileAccess.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public  void aesncryptFile(String filePath,String fileName,String fileOutputName,FileSysUtil fileUtil,int fd){
		SecretKey key=new SecretKeySpec(keyString.getBytes(),"AES");
		try {
			File file=new File(filePath+fileOutputName);
			if(!file.exists()){
				file.createNewFile();
			}
			Cipher c=Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE,key);
			FileInputStream fin=new FileInputStream(filePath+fileName);
			FileOutputStream fout=new FileOutputStream(filePath+fileOutputName);
			CipherOutputStream cout=new CipherOutputStream(fout, c);
			byte[] b=new byte[4096];
			int d;
			while((d=fin.read(b))!=-1){
				cout.write(b, 0, d);
			}
			cout.flush();
			cout.close();
			fin.close();

			FileInputStream finForSave=new FileInputStream(filePath+fileOutputName);
			byte[] buffer=new byte[1024*1024];
			int len;
			int total=-2;
			while((len=finForSave.read(buffer))!=-1 && total!=-1){
				Log.d("test","fd:"+fd);
				total=fileUtil.sdcard_write(fd, buffer, len);//con,
				Log.d("test","total:"+total);
			}
			if(total == -1){
				Toast.makeText(con, "tf卡已存满！"+total, Toast.LENGTH_LONG).show();
				Log.d("EncryptFile","tf卡已存满:"+total);
			}else{
				if(ToastButton.getToastStatus()) {
					Toast.makeText(con, "总长度: "+total, Toast.LENGTH_LONG).show();
				}
			}
			int flag=fileUtil.sdcard_update(fd);//con,
			if(ToastButton.getToastStatus()) {
				Toast.makeText(con, "标志位: "+flag, Toast.LENGTH_LONG).show();
			}
			fin.close();

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			File orienFile=new File(filePath+fileName);
			File newFile=new File(filePath+fileOutputName);
			//删掉原来文件，并将加密文件重命名
			if(orienFile.exists()){
				orienFile.delete();

			}if(newFile.exists()){
				newFile.delete();
			}
		}

	}
}
