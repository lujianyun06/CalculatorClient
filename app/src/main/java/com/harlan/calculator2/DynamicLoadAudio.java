package com.harlan.calculator2;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.InvalidKeyException;

import com.dynamic.audio.Interfaces.ADyanmic;
import com.dynamic.video.Interfaces.VDynamic;
import com.harlan.calculator2.DynamicLoadVideo.myBroadcastReceiver;
import com.harlan.calculator2.DynamicLoadVideo.myHomeBroadcastReceiver;

import dalvik.system.DexClassLoader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class DynamicLoadAudio extends Activity{
	private Button startBtn;
	private Button stopBtn;
	private ADyanmic adynamic;
	private String fileName;
	private Boolean isClickStop=false;
	private Boolean isClickExit=false;
	private Button exitBtn;
	
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dynamic_audio);
//        startBtn=(Button)findViewById(R.id.startAudio);
//        stopBtn=(Button)findViewById(R.id.stopAudio);
//        exitBtn=(Button) findViewById(R.id.exitAudio);
//        exitBtn.setOnClickListener(new AudioOnclickListener());
//        startBtn.setOnClickListener(new AudioOnclickListener());
//        stopBtn.setOnClickListener(new AudioOnclickListener());
//    }
//
//	public String getFileName() {
//		// TODO Auto-generated method stub
//		Time t=new Time();
//		t.setToNow();
//		int Year = t.year;
//		int Month =  t.month;
//		int Day = t.monthDay;
//		int Hour = t.hour;
//		int Minute=t.minute;
//		int Second = t.second;
//		String FileName=""+Year+Month+Day+Hour+Minute+Second;
//		return FileName;
//				
//		
//	}
//    class AudioOnclickListener implements OnClickListener{
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			if(v == startBtn){
//				Toast toast=Toast.makeText(getApplicationContext(), "动态加载成功!", Toast.LENGTH_LONG);
//				toast.setGravity(Gravity.CENTER, 0, 0);
//				toast.show();
//				IntentFilter filter=new IntentFilter();
//				filter.addAction(Intent.ACTION_SHUTDOWN);
//				registerReceiver(new myBroadcastReceiver(), filter);
//				IntentFilter filter1=new IntentFilter();
//				filter1.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//				registerReceiver(new myHomeBroadcastReceiver(),filter1);
//				String dexPath=Environment.getExternalStorageDirectory().toString()+File.separator+"dynamicFile"+File.separator+"dynamic_audio1.jar";
//		    	System.out.println("*******************************"+dexPath);
//		    	File outputPath=getApplicationContext().getDir("dex",0);
//		    	
//		    	DexClassLoader cl=new DexClassLoader(dexPath, outputPath.getAbsolutePath(), null, getClassLoader());
//		    	
//		    	try{
//		    		Class libProviderClass=cl.loadClass("com.dynamic.audio.impl.adynamic");
//		    		adynamic=(ADyanmic)libProviderClass.newInstance();
//		    		
//		    	}catch(Exception e){
//		    		e.printStackTrace();
//		    	}
//		    	fileName=getFileName();
//		    	Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_SHORT).show();
//				adynamic.init(DynamicLoadAudio.this,fileName);
//				adynamic.startAudioRecorder();
//			}else if(v == stopBtn){
//				adynamic.stopAudioRecorder();
//				isClickStop=true;
//			}else if(v==exitBtn){
//				if(!isClickStop){
//					adynamic.stopAudioRecorder();
//					isClickStop=true;
//					}
//					isClickExit=true;
//					try {
//						new EncryptOperator().encrypt(Environment.getExternalStorageDirectory().toString()+File.separator+fileName+".amr");
//					} catch (InvalidKeyException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					//EncryptFile.encryptFile(Environment.getExternalStorageDirectory().toString()+File.separator+fileName+".amr");
//					System.out.println("************************************");
//					finish();
//				
//			}
//		}
//    	
//    }
//    
//    class myBroadcastReceiver extends BroadcastReceiver{
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			// TODO Auto-generated method stub
//			System.out.println("***************************");
//			System.out.println("***************************");
//			System.out.println("***************************");
//
//			if(!isClickStop)
//				adynamic.startAudioRecorder();
//			if(!isClickExit){
//	    	//	EncryptFile.encryptFile("");
//				try {
//					new EncryptOperator().encrypt(Environment.getExternalStorageDirectory().toString()+File.separator+fileName+".amr");
//				} catch (InvalidKeyException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//	    	}
//		}
//		
//	}
//	
//	class myHomeBroadcastReceiver extends BroadcastReceiver{
//		final String SYSTEM_DIALOG_REASON_KEY = "reason";         //按下Home键        
//		final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"; 
//		
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			// TODO Auto-generated method stub
//			String action=intent.getAction();
//			if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
//				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
//				if(reason!=null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)){
//					//Toast.makeText(getApplicationContext(), "--------->home key", Toast.LENGTH_SHORT).show();
//					System.out.println("_____________Home_______");
//					onDestroy();
//					//Calculator.instance.finish();
//				}
//			}
//		}
//		
//	}
}
