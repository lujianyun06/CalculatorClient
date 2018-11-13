package com.harlan.calculator2;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.dynamic.audio.Interfaces.ADyanmic;
import com.harlan.utils.ConstantValue;
import com.harlan.utils.Utils;
import com.lpr.FileSysUtil;

import dalvik.system.DexClassLoader;

import static android.security.KeyStore.getApplicationContext;

//??????????
public class DynamicLoadAudioStatic{
	private static final String TAG ="DynamicLoadAudioStatic";
	private static final int TIME_UNIT=60*5;//视频分割时长60*5*1000ms，为5分钟
	private ADyanmic adynamic;
	private String fileName;
	private Boolean isClickStop=false;
	private Boolean isClickExit=false;
	private Context context;
	private Activity mActivity;
	private FileSysUtil fileUtil;
	private int fd;
	private EncryptOperator encryptOperator;
	private String fileNameForSave;
	private myBroadcastReceiver myBroad;
	private Timer timer;
	private int timeSize;
	private String filenameCut;
	private ExecutorService singleThreadExecutor;

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
					Toast toast=Toast.makeText(context, "写入数据成功！", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			} else if(msg.what == WRITE_ERROR){
				Utils.WRITE_SUCCESS = true;
				Log.e(TAG, "write file fail,filename:"+filenameCut);
				Toast toast=Toast.makeText(context, "写入数据失败！", Toast.LENGTH_LONG);
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
			} else {
				Log.e(TAG, "Can't reconize msg!");
			}
		}
	};

	public DynamicLoadAudioStatic(Context con,Activity activity){
		context=con;
		mActivity=activity;
		fileUtil=new FileSysUtil();
		encryptOperator=new EncryptOperator(context);
		singleThreadExecutor = Executors.newSingleThreadExecutor();
	}

	public String getFileName() {
		// TODO Auto-generated method stub
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String FileName=format.format(date);
		Log.e("audio filename", FileName);
		return FileName;
	}

	public void startAudio(){

        int remainCap = 3400-fileUtil.sdcard_getcapacity();//3400MB是默认的可用存储容量
		if(ToastButton.getToastStatus()) {
			Toast toast1=Toast.makeText(getApplicationContext(), "剩余的存储容量："+remainCap+"MB，请注意拍摄时长！", Toast.LENGTH_LONG);
			toast1.setGravity(Gravity.CENTER, 0, 0);
			toast1.show();
		}
		IntentFilter filter=new IntentFilter();
		filter.addAction(Intent.ACTION_SHUTDOWN);
		IntentFilter filter1=new IntentFilter();
		filter1.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		//String dexPath=Environment.getExternalStorageDirectory().toString()+File.separator+"dynamicFile"+File.separator+"dynamic_audio1.jar";
		System.out.println("*******************************"+ConstantValue.dexPath+"dynamic_audio1.jar");
		File outputPath=context.getDir("dex",0);

		DexClassLoader cl=new DexClassLoader(ConstantValue.dexPath+"dynamic_audio1.jar", outputPath.getAbsolutePath(), null, context.getClassLoader());

		try{
			Class libProviderClass=cl.loadClass("com.dynamic.audio.impl.adynamic");
			adynamic=(ADyanmic)libProviderClass.newInstance();

		}catch(Exception e){
			e.printStackTrace();
		}
		fileName=getFileName();
//		fileNameForSave=fileName+".amr";
		if(ToastButton.getToastStatus()) {
			Toast.makeText(context, fileName, Toast.LENGTH_SHORT).show();
		}
		timer = new Timer();
		timeSize = 0;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(timeSize == 0) {
					filenameCut = fileName+"_0";
					adynamic.init(mActivity,filenameCut);
					try {
						adynamic.startAudioRecorder();
					} catch (IllegalStateException e) {
						if(filenameCut != null) {
							File file = new File(Environment.getExternalStorageDirectory().toString()+File.separator,filenameCut+".amr");
							Log.e(TAG, "videoRecorderOnclickListener start IllegalStateException fileName:"+filenameCut+" fileLen:"+file.length());
							if(file.exists()) file.delete();
						}
						e.printStackTrace();
					}
				}  else if(timeSize > 0 && 3400-fileUtil.sdcard_getcapacity() < 1 && !isClickStop) {
                    try {
                        adynamic.stopAudioRecorder();
                    } catch (IllegalStateException e) {
                        if(filenameCut != null) {
                            File file = new File(Environment.getExternalStorageDirectory().toString()+File.separator,filenameCut+".amr");
                            Log.e(TAG, "videoRecorderOnclickListener start IllegalStateException fileName:"+filenameCut+" fileLen:"+file.length());
                            if(file.exists()) file.delete();
                        }
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(WRITE_OVER_BOARD);
                    isClickStop=true;
                    if (timer != null) {
                        timer.cancel();
                        timeSize = 0;
                    }
                    isClickExit=true;
                    writeFile2SDcard(filenameCut,fileUtil);
                    return;
                } else if(timeSize != 0 && timeSize%TIME_UNIT == 0) {
					try {
						adynamic.stopAudioRecorder();
					} catch (IllegalStateException e) {
						if(filenameCut != null) {
							File file = new File(Environment.getExternalStorageDirectory().toString()+File.separator,filenameCut+".amr");
							Log.e(TAG, "videoRecorderOnclickListener start IllegalStateException fileName:"+filenameCut+" fileLen:"+file.length());
							if(file.exists()) file.delete();
						}
						e.printStackTrace();
					}
					writeFile2SDcard(filenameCut,fileUtil);
					Log.e(TAG,"video name = "+fileName+"_"+(timeSize/TIME_UNIT-1));
					filenameCut=fileName+"_"+timeSize/TIME_UNIT;
					adynamic.init(mActivity,filenameCut);
					try {
						adynamic.startAudioRecorder();
					} catch (IllegalStateException e) {
						if(filenameCut != null) {
							File file = new File(Environment.getExternalStorageDirectory().toString()+File.separator,filenameCut+".amr");
							Log.e(TAG, "videoRecorderOnclickListener start IllegalStateException fileName:"+filenameCut+" fileLen:"+file.length());
							if(file.exists()) file.delete();
						}
						e.printStackTrace();
					}
				}
				timeSize++;
			}
		},0,1000);
		/*fd=fileUtil.sdcard_create(fileNameForSave.getBytes(), fileNameForSave.getBytes().length);//mActivity,
		if(fd>=0){
			adynamic.init(mActivity,fileName);

			adynamic.startAudioRecorder();
			if(ToastButton.getToastStatus()) {
				Toast toast=Toast.makeText(context, "动态加载成功，录音开始!", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}*/
	}

	public void stopAudio(){
		adynamic.stopAudioRecorder();
		isClickStop=true;
        if (timer != null) {
            timer.cancel();
            timeSize = 0;
        }
		writeFile2SDcard(filenameCut,fileUtil);

		/*try {
			encryptOperator.encrypt(Environment.getExternalStorageDirectory().toString()+File.separator,fileName+".amr",fd,fileUtil);//fd=0
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			File file = new File(Environment.getExternalStorageDirectory().toString()+File.separator,fileName+".amr");
			file.delete();
		}*/
		//EncryptFile.encryptFile(Environment.getExternalStorageDirectory().toString()+File.separator+fileName+".amr");
		/*System.out.println("************************************");
		if(ToastButton.getToastStatus()) {
			Toast toast=Toast.makeText(context, "结束录音!", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}*/
	}

	class myBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			System.out.println("***************************");
			System.out.println("***************************");
			System.out.println("***************************");

			if(!isClickStop){
				adynamic.startAudioRecorder();
                if (timer != null) {
                    timer.cancel();
                    timeSize = 0;
                }
				writeFile2SDcard(filenameCut,fileUtil);
			}
		}

	}

	class myHomeBroadcastReceiver extends BroadcastReceiver{
		final String SYSTEM_DIALOG_REASON_KEY = "reason";         //????Home??
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
					String fileNameForSave=filename+".amr";
					fd=fileUtil.sdcard_create(fileNameForSave.getBytes(), fileNameForSave.getBytes().length);
					Log.e(TAG,"filename is:"+filename+" fd is:"+fd);
					boolean isOk = encryptOperator.encrypt(Environment.getExternalStorageDirectory().toString()+File.separator,filename+".amr",fd,fileUtil);//fd=0
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
					File file = new File(Environment.getExternalStorageDirectory().toString()+File.separator,filename+".amr");
					Log.e(TAG, "writeFile2SDcard delete fileName:"+filename+" fileLen:"+file.length());
					if(file.exists()) file.delete();
				}
			}
		});
	}
}