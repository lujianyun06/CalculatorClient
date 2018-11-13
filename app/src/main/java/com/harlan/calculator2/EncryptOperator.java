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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.lpr.FileSysUtil;

public class EncryptOperator {
	private static final String TAG ="EncryptOperator";
	private String keyString="1234567887654321";
	public byte[] content;
	private Context con;
	public EncryptOperator(Context context){
		con=context;
	}
	public Boolean encrypt(String filePath,String fileName,int fd,FileSysUtil fileUtil) throws InvalidKeyException, IOException{
		File finForRead=new File(filePath+fileName);
		Log.e(TAG,"fileName1:"+filePath+fileName+" fileLen1:"+finForRead.length());
		SecretKey myKey=new SecretKeySpec(keyString.getBytes(),"AES");
		RandomAccessFile fin=new RandomAccessFile(new File(filePath+fileName),"rw");
		try {
			Cipher c=Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, myKey);
			byte[] data=new byte[8];	//用来加密

			byte[] saveData=new byte[8];
			fin.seek(0);
			fin.read(data);
			System.out.println(new String(data));
			fin.seek(8);
			fin.read(saveData);
			fin.seek(fin.length());
			fin.write(saveData);
			content=c.doFinal(data);
			fin.seek(0);
			fin.write(content);
			fin.seek(0);
			Log.e(TAG,"fileLen:"+fin.length());
			fin.close();
			FileInputStream finForSave=new FileInputStream(filePath+fileName);
			byte[] buffer=new byte[1024*1024];
			int len;
			int n = -2;
			while((len=finForSave.read(buffer))!=-1){
				Log.e(TAG,"len:"+len);
				n =fileUtil.sdcard_write(fd, buffer, len);//con,
			}
			if(n==-1){
				Log.e(TAG,"tf卡已写满:n="+n);
				return false;
			}
			int flag=fileUtil.sdcard_update(fd);//con,
			finForSave.close();
		}catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

/*	public Boolean decrypt() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException{
		SecretKey myKey=new SecretKeySpec(ConstantValues.keyString.getBytes(),"AES");
		RandomAccessFile fin=new RandomAccessFile(new File(ConstantValues.filePath+ConstantValues.fileName),"rw");

		try {
			Cipher c=Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, myKey);
			byte[] data=new byte[16];//用来加密
			byte[] saveData=new byte[8];
			fin.seek(0);
			fin.read(data);
			fin.seek(fin.length()-8);//定位到文件最后，读出保存字符
			fin.read(saveData);

			content=c.doFinal(data);
			fin.seek(0);
			fin.write(content);
			fin.seek(8);
			fin.write(saveData);
			fin.seek(fin.length()-8);
			byte[] delete=new byte[8];
			fin.write(delete);
			fin.seek(0);
			System.out.println("倡导: "+fin.length())	;
			fin.close();
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}*/
}
