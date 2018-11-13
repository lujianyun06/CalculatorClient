package com.harlan.calculator2;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dynamic.audio.Interfaces.ADyanmic;
import com.dynamic.pic.interace.PDynamic;
import com.harlan.utils.Utils;
import com.lpr.FileSysUtil;
import com.lpr.RootUtil;

import dalvik.system.DexClassLoader;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.media.AudioManager;

public class DynamicLoadPicture extends Activity{

	private Button startCapture;
	private PDynamic pd;
	private String fileName;
	private String filePath;
	private String fileoutputName;
	private FileSysUtil fileUtil;
	private AudioManager audioManager;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dynamic_picture);
		init();
	}
	public void init(){
		Utils.WRITE_SUCCESS = false;
        fileUtil=new FileSysUtil();
		int remainCap = 3400-fileUtil.sdcard_getcapacity();//3400MB是默认的可用存储容量
        if(ToastButton.getToastStatus()) {
            Toast toast1=Toast.makeText(getApplicationContext(), "剩余的存储容量："+remainCap+"MB，请注意拍摄时长！", Toast.LENGTH_LONG);
            toast1.setGravity(Gravity.CENTER, 0, 0);
            toast1.show();
        }
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		fileName=getFileName()+".jpg";
		String dexPath=Environment.getExternalStorageDirectory().toString()+File.separator+"dynamicFile"+File.separator+"dynamic_picture1.jar";
		System.out.println("*******************************"+dexPath);
		File outputPath=getApplicationContext().getDir("dex",0);

		DexClassLoader cl=new DexClassLoader(dexPath, outputPath.getAbsolutePath(), null, getClassLoader());

		try{
			Class libProviderClass=cl.loadClass("com.dynamic.pic.impl.pdynamic");
			pd=(PDynamic)libProviderClass.newInstance();

		}catch(Exception e){
			e.printStackTrace();
		}
		pd.init(DynamicLoadPicture.this);
//		pd.capturePic();
		capturePic();
	}
	private void capturePic(){
		fileoutputName="temp.jpg";
		File file=new File(Environment.getExternalStorageDirectory()+File.separator+fileoutputName);
		Intent intent = new Intent( 
                MediaStore.ACTION_IMAGE_CAPTURE); 
        
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri 
                .fromFile(file)); 
        startActivityForResult(intent, 2); 
	}
	@SuppressLint("SimpleDateFormat")
	public String getFileName() {
		// TODO Auto-generated method stub
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String FileName=format.format(date);
		Log.e("picture filename", FileName);
		return FileName;
		
	}
	@SuppressLint("ShowToast")
	private void processFile(){
		filePath=Environment.getExternalStorageDirectory()+File.separator;
		
		int fd=fileUtil.sdcard_create(fileName.getBytes(), fileName.length());//DynamicLoadPicture.this,
		Log.d("test","fd:"+fd);
		if(fd<0){
			if(true) {
				Toast.makeText(getApplicationContext(), "创建文件失败!", 2000).show();
			}
		}else{
			EncryptFile encryptFile=new EncryptFile(DynamicLoadPicture.this);
			encryptFile.aesncryptFile(filePath,fileoutputName, fileName,fileUtil,fd);
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch(requestCode){
				case 2:
					if(Utils.checkSdcardValidate(this)){
						processFile();
						if(ToastButton.getToastStatus()) {
							Toast.makeText(getApplicationContext(), "开始加密！", 3000).show();
							Toast.makeText(getApplicationContext(), "拍摄并保存成功！", 4000).show();
						}
					}else{
						if(true) {
							Toast.makeText(getApplicationContext(), "保存失败，请检查sd卡是否正确插入", 3000).show();
						}
					}
					finish();
					break;
				case 1:
					Bundle bundle=data.getExtras();
					Bitmap bitmap=(Bitmap) bundle.get("data");

					if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
						if(true) {
							Toast.makeText(getApplicationContext(), "请确认SD卡是否存在", Toast.LENGTH_LONG).show();
						}
					}else{
						fileoutputName="temp.jpg";
						File file=new File(Environment.getExternalStorageDirectory()+File.separator+fileoutputName);
						if(!file.exists()){
							try {
								file.createNewFile();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						int options = 100;
						FileOutputStream fout = null;
						try {
							fout = new FileOutputStream(file);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						filePath=Environment.getExternalStorageDirectory()+File.separator;
						if(bitmap!=null){
							//Toast.makeText(getApplicationContext(), "bitmap不为空！", 500).show();
							bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
							try{
								fout.write(baos.toByteArray());
								fout.flush();
								fout.close();
							}catch(Exception e){
								
							}
							/*bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout);
							Toast.makeText(getApplicationContext(), "new空！", Toast.LENGTH_LONG).show();
							fileUtil=new FileSysUtil();

							Toast.makeText(getApplicationContext(), "创建文件失败!", Toast.LENGTH_LONG);

							int fd=fileUtil.sdcard_create(this,fileName.getBytes(), fileName.length());*/
							/*try{
								Process proc=Runtime.getRuntime().exec("su"); // –c /data/sdcard reset 0 0
								DataOutputStream os=new DataOutputStream(proc.getOutputStream());
								os.writeBytes("/data/sdcard create"+fileName.getBytes()+fileName.length()+"\n");
								EncryptFile encryptFile=new EncryptFile(getApplicationContext());
								Toast.makeText(getApplicationContext(), "开始加密！", Toast.LENGTH_LONG).show();
								encryptFile.aesncryptFile(filePath,fileoutputName, fileName,fileUtil,1);
								Toast.makeText(getApplicationContext(), "拍摄并保存成功！", Toast.LENGTH_LONG).show();
							}catch(Exception e){
								Toast.makeText(getApplicationContext(), "创建文件失败!", Toast.LENGTH_LONG);
							}*/
							/*if(fd<0){
								Toast.makeText(getApplicationContext(), "创建文件失败!", Toast.LENGTH_LONG);
							}else{
								EncryptFile encryptFile=new EncryptFile(getApplicationContext());
								Toast.makeText(getApplicationContext(), "开始加密！", Toast.LENGTH_LONG).show();
								encryptFile.aesncryptFile(filePath,fileoutputName, fileName,fileUtil,fd);
								Toast.makeText(getApplicationContext(), "拍摄并保存成功！", Toast.LENGTH_LONG).show();
							}*/
						}else{if(ToastButton.getToastStatus()) {Toast.makeText(getApplicationContext(), "bitmap为空！", Toast.LENGTH_LONG).show();}};
						//finish();
					}
			}
		}else if(resultCode == RESULT_CANCELED){
			finish();
		}
		audioManager.setStreamVolume(AudioManager.STREAM_RING, 50, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
	}

}
