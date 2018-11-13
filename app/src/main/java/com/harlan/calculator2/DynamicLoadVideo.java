package com.harlan.calculator2;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dynamic.video.Interfaces.VDynamic;
import dalvik.system.DexClassLoader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.harlan.calculator2.EncryptFile;
import com.harlan.utils.ConstantValue;
import com.harlan.utils.Utils;
import com.lpr.FileSysUtil;

public class DynamicLoadVideo extends Activity implements Callback {
	/** Called when the activity is first created. */
	private static final String TAG ="DynamicLoadVideo";

	static final String STATE_VDYNAMIC = "vdynamic";
	private Button start;
	private Button stop;
	private Button exit;
	private Button hide;
	private Boolean isHide;
	private LinearLayout ly;
	private SurfaceView surfaceView;
	private VDynamic vd;
	SurfaceHolder surfaceHolder;
	private String filename;
	private myBroadcastReceiver myBroad;
	private myHomeBroadcastReceiver myHomeBroad;
	private Boolean isClickStop=false;
	private Boolean isClickExit=false;
	private Boolean isClickStart = false;
	private Boolean isUnregister=false;
	private int fd;
	private FileSysUtil fileUtil;
	private EncryptOperator encryptOperator;
	private AudioManager audioManager;
	private static final int TIME_UNIT=60;//视频分割时长120*1000ms，为两分钟
	private Timer timer;
	private int timeSize;
	private String filenameCut;
	private ExecutorService singleThreadExecutor;
	private int remainCap;

	private static final int WRITE_OK=0;
	private static final int WRITE_ERROR=1;
	private static final int WRITE_OVER_BOARD=2;

	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			if(msg.what == WRITE_OK){
				Utils.WRITE_SUCCESS = true;
				Log.e(TAG, "write file successful,filename:"+filenameCut);
				if(ToastButton.getToastStatus()) {
					Toast toast=Toast.makeText(getApplicationContext(), "写入数据成功！", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			} else if(msg.what == WRITE_ERROR){
				Utils.WRITE_SUCCESS = true;
				Log.e(TAG, "write file fail,filename:"+filenameCut);
				Toast toast=Toast.makeText(getApplicationContext(), "写入数据失败！", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			} else if (msg.what == WRITE_OVER_BOARD) {
				Utils.WRITE_SUCCESS = true;
				Log.e(TAG, "write file over board,filename:"+filenameCut);
				if(ToastButton.getToastStatus()) {
					Toast toast=Toast.makeText(getApplicationContext(), "存储空间不足，结束拍摄！", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER,0, 0);
					toast.show();
				}
				surfaceView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg3));
			} else {
				Log.e(TAG, "Can't reconize msg!");
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dynamic_main);
		Utils.WRITE_SUCCESS = false;
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		fileUtil=new FileSysUtil();
		encryptOperator=new EncryptOperator(getApplicationContext());
		singleThreadExecutor = Executors.newSingleThreadExecutor();

////启动密码检查
//        Intent intent=new Intent("buptsse.password.main");
//        Bundle bundle=new Bundle();
//        bundle.putString("sharedPreferenceName", "com.rbf.test");
//        bundle.putString("packageName", "com.rbf.test");
//        intent.putExtras(bundle);
//        startActivity(intent);
		isHide=false;
		ly=(LinearLayout)findViewById(R.id.ly);
		//ly.setVisibility(LinearLayout.INVISIBLE);
		start=(Button)findViewById(R.id.start);
		stop=(Button)findViewById(R.id.stop);
		exit=(Button)findViewById(R.id.exit);
		hide=(Button)findViewById(R.id.hide);
		surfaceView=(SurfaceView)findViewById(R.id.VideoPlay);
		surfaceHolder=surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		start.setOnClickListener(new videoRecorderOnclickListener());
		hide.setOnClickListener(new videoRecorderOnclickListener());
		stop.setOnClickListener(new videoRecorderOnclickListener());
		exit.setOnClickListener(new videoRecorderOnclickListener());
	}

	/*@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the user's current game state
		savedInstanceState.putParcelable(STATE_VDYNAMIC, vd);

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}*/

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		//vd.stopVideoRecorder();
		if(isClickStart){
			if(!isClickStop)
				vd.stopVideoRecorder();
			isClickStop=true;
			if(!isClickExit){
				isClickExit=true;
				//EncryptFile.encryptFile(Environment.getExternalStorageDirectory().toString()+File.separator+filename+".3gp");
				writeFile2SDcard(filenameCut,fileUtil);
			}
		}
		audioManager.setStreamVolume(AudioManager.STREAM_RING, 50, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		if (timer != null) {
			timer.cancel();
			timeSize = 0;
		}
		finish();
		super.onDestroy();
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(isClickStart){
				if(!isClickStop){
					vd.stopVideoRecorder();
					isClickStop=true;
					if (timer != null) {
						timer.cancel();
						timeSize = 0;
					}
				}
				onDestroy();
			}

			System.out.println("---------------------->onStop()");
			System.out.println("---------------------->onStop()");
			System.out.println("---------------------->onStop()");
			System.out.println("---------------------->onStop()");
			System.out.println("---------------------->onStop()");

		}else if(keyCode==KeyEvent.KEYCODE_HOME){
			System.out.println("---------------------->onStop()");
			System.out.println("---------------------->onStop()");
			System.out.println("---------------------->onStop()");
			System.out.println("---------------------->onStop()");
			System.out.println("---------------------->onStop()");
		}
		return super.onKeyDown(keyCode, event);
	}



	class videoRecorderOnclickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v == start){
				Utils.WRITE_SUCCESS = false;
				isClickStart = true;
				if(ToastButton.getToastStatus()) {
					Toast toast=Toast.makeText(getApplicationContext(), "动态加载成功!", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
                remainCap = 3400-fileUtil.sdcard_getcapacity();//3400MB是默认的可用存储容量
                if(ToastButton.getToastStatus()) {
                    Toast toast1=Toast.makeText(getApplicationContext(), "剩余的存储容量："+remainCap+"MB，请注意拍摄时长！", Toast.LENGTH_LONG);
                    toast1.setGravity(Gravity.CENTER, 0, 0);
                    toast1.show();
                }
				IntentFilter filter=new IntentFilter();
				filter.addAction(Intent.ACTION_SHUTDOWN);
				myBroad=new myBroadcastReceiver();
				registerReceiver(myBroad, filter);
				IntentFilter filter1=new IntentFilter();
				filter1.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
				myHomeBroad=new myHomeBroadcastReceiver();
				registerReceiver(myHomeBroad,filter1);
				ConstantValue.dexPath=Environment.getExternalStorageDirectory().toString()+File.separator+"dynamicFile"+File.separator+"dynamic_video1.jar";
				System.out.println("*******************************"+ConstantValue.dexPath+"dynamic_video1.jar");
				File outputPath=getApplicationContext().getDir("dex",0);

				DexClassLoader cl=new DexClassLoader(ConstantValue.dexPath+"dynamic_video1.jar", outputPath.getAbsolutePath(), null, getClassLoader());

				try{
					Class libProviderClass=cl.loadClass("com.dynamic.video.impl.vdynamic");
					vd=(VDynamic)libProviderClass.newInstance();

				} catch(Exception e){
					e.printStackTrace();
				}
				surfaceView.setBackgroundDrawable(null);
				filename=getFileName();
				timer = new Timer();
				timeSize = 0;
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if(timeSize == 0) {
							filenameCut = filename+"_0";
							vd.init(DynamicLoadVideo.this, surfaceView, surfaceView.getHolder(),filenameCut);
							try {
								vd.startVideoRecorder();
							} catch (IllegalStateException e) {
								exceptionHandler();
								e.printStackTrace();
							} catch (IOException e){
								exceptionHandler();
								e.printStackTrace();
							}
							start.setClickable(false);
						} else if(timeSize > 0 && 3400-fileUtil.sdcard_getcapacity() < 120 && !isClickStop) {
							vd.stopVideoRecorder();
							mHandler.sendEmptyMessage(WRITE_OVER_BOARD);
							isClickStop=true;
							if (timer != null) {
								timer.cancel();
								timeSize = 0;
							}
							isClickExit=true;
							writeFile2SDcard(filenameCut,fileUtil);
							finish();
						} else if(timeSize != 0 && timeSize%TIME_UNIT == 0) {
							vd.stopVideoRecorder();
							writeFile2SDcard(filenameCut,fileUtil);
							Log.d(TAG,"video name = "+filename+"_"+(timeSize/TIME_UNIT-1));
							filenameCut=filename+"_"+timeSize/TIME_UNIT;
							vd.init(DynamicLoadVideo.this, surfaceView, surfaceView.getHolder(),filenameCut);
							/*remainCap = 3400-fileUtil.sdcard_getcapacity()-100;
							Log.d(TAG,"remainCap = "+ remainCap);*/
							try {
								vd.startVideoRecorder();
							} catch (IllegalStateException e) {
								exceptionHandler();
								e.printStackTrace();
							} catch (IOException e){
								exceptionHandler();
								e.printStackTrace();
							}
						}
						timeSize++;
					}
				},0,1000);



			}else if(v == stop){
				if(isClickStart){
					if(!isClickStop){
						vd.stopVideoRecorder();
						if(ToastButton.getToastStatus()) {
							Toast toast=Toast.makeText(getApplicationContext(), "加密成功", Toast.LENGTH_LONG);
							toast.setGravity(Gravity.CENTER,0, 0);
							toast.show();
						}
						isClickStop=true;
						timer.cancel();
						timeSize = 0;
					}

				}
				surfaceView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg3));

			}else if(v==exit){
				if(isClickStart){
					if(!isClickStop){
						vd.stopVideoRecorder();
						isClickStop=true;
						if (timer != null) {
							timer.cancel();
							timeSize = 0;
						}
					}
					isClickExit=true;
					writeFile2SDcard(filenameCut,fileUtil);
					//EncryptFile.encryptFile(Environment.getExternalStorageDirectory().toString()+File.separator+filename+".3gp");
					System.out.println("************************************");
					unregisterReceiver(myBroad);
					unregisterReceiver(myHomeBroad);
					isUnregister=true;
				}
				audioManager.setStreamVolume(AudioManager.STREAM_RING, 50, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				finish();

			}else if(v==hide){
				if(!isHide){
					ly.setVisibility(LinearLayout.INVISIBLE);
					isHide=true;
					hide.setText("disp");
				}else{
					ly.setVisibility(LinearLayout.VISIBLE);
					isHide=false;
					hide.setText("hide");
				}

			}

		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		// TODO Auto-generated method stub
		surfaceHolder = holder;
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		surfaceHolder = holder;
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		surfaceView = null;
		surfaceHolder = null;
		vd=null;
	}
	public String getFileName() {
		// TODO Auto-generated method stub
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String filename=format.format(date);
        Log.e(TAG,"video filename:"+ filename);
        return filename;
	}

	class myBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			System.out.println("***************************");
			System.out.println("***************************");
			System.out.println("***************************");
			System.out.println("***************************");
			System.out.println("***************************");
			if(!isClickStop)
				vd.stopVideoRecorder();
			if(!isClickExit){
				//EncryptFile.encryptFile(Environment.getExternalStorageDirectory().toString()+File.separator+filename+".3gp");
				writeFile2SDcard(filenameCut,fileUtil);
			}
		}

	}

	class myHomeBroadcastReceiver extends BroadcastReceiver{
		final String SYSTEM_DIALOG_REASON_KEY = "reason";         //按下Home键
		final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action=intent.getAction();
			if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if(reason!=null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)){
					//Toast.makeText(getApplicationContext(), "--------->home key", Toast.LENGTH_SHORT).show();
					System.out.println("_____________Home_______");
					onDestroy();
					//Calculator.instance.finish();
				}
			}
		}

	}

	private void writeFile2SDcard(final String filename,final FileSysUtil fileUtil){

		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if(filename == null) {
						Log.e(TAG,"filename is null!");
						Thread.sleep(50);
					}
					String fileNameForSave=filename+".3gp";
					fd=fileUtil.sdcard_create(fileNameForSave.getBytes(), fileNameForSave.getBytes().length);
					Log.e(TAG,"filename is:"+filename+" fd is:"+fd);
					boolean isOk = encryptOperator.encrypt(Environment.getExternalStorageDirectory().toString()+File.separator,filename+".3gp",fd,fileUtil);
					if(isOk) {
						mHandler.sendEmptyMessage(WRITE_OK);
					} else {
						mHandler.sendEmptyMessage(WRITE_ERROR);
					}
				} catch (InvalidKeyException e) {
					mHandler.sendEmptyMessage(WRITE_ERROR);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  catch (IOException e) {
					// TODO Auto-generated catch block
					mHandler.sendEmptyMessage(WRITE_ERROR);
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					mHandler.sendEmptyMessage(WRITE_ERROR);
					e.printStackTrace();
				} finally {
					File file = new File(Environment.getExternalStorageDirectory().toString()+File.separator,filename+".3gp");
					Log.e(TAG, "writeFile2SDcard delete fileName:"+filename+" fileLen:"+file.length());
					if(file.exists()) file.delete();
				}
			}
		});
	}

	public void exceptionHandler(){
		if(filenameCut != null) {
			File file = new File(Environment.getExternalStorageDirectory().toString()+File.separator,filenameCut+".3gp");
			Log.e(TAG, "videoRecorder#OnclickListener start IllegalStateException fileName:"+filenameCut+" fileLen:"+file.length());
			if(file.exists()) file.delete();
		}
		vd.stopVideoRecorder();
		isClickStop=true;
		timer.cancel();
		timeSize = 0;
		isClickExit=true;
		writeFile2SDcard(filenameCut,fileUtil);
		unregisterReceiver(myBroad);
		unregisterReceiver(myHomeBroad);
		isUnregister=true;
		audioManager.setStreamVolume(AudioManager.STREAM_RING, 50, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		finish();
	}
}