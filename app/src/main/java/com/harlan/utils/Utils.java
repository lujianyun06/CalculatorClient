package com.harlan.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Locale;

import com.lpr.FileSysUtil;
import com.lpr.RootUtil;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import static android.content.Context.STORAGE_SERVICE;

public class Utils {
	private static final String TAG = "Utils";
	public static boolean WRITE_SUCCESS = true;
	private static FileSysUtil fileUtil;
	 public static boolean checkSdcardValidate(Context context){
//		File dir = new File(getExternalSdcardDirectory());//"/storage/3837-6138"
		 File dir = new File(getSecondaryStoragePath(context));
		  Log.d("checkSdcardValidate",dir.getAbsolutePath());
		if(dir.exists() && dir.isDirectory()){
			RootUtil.getRoot();
			RootUtil.chmod();
//	    		File file = new File("/storage/3837-6138/temp.txt");
//	    		if(!file.exists()){
//	    			try {
//	    				Log.d("test","createNewFile");
//						file.createNewFile();
//	    				//File.createTempFile("/storage/3837-6138", "temp.txt");
//						file.delete();
//						return true;
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						Log.d("test","IOException");
//						e.printStackTrace();
//						return false;
//					}
//	    		}
			return true;
		}
		return false;
	}

	public static boolean checkSdcardRemainSpace(Context context,int Threshold) {
		fileUtil=new FileSysUtil();
		int remainCap = 3400-fileUtil.sdcard_getcapacity();//3400MB是默认的可用存储容量
		if(remainCap<Threshold) {
			Toast toast=Toast.makeText(context, "剩余的存储容量不足!", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
		} else {
			return true;
		}
	}
	 public static String getExternalSdcardDirectory(){
		 try {  
	            Runtime runtime = Runtime.getRuntime();  
	            // 运行mount命令，获取命令的输出，得到系统中挂载的所有目录  
	            Process proc = runtime.exec("df");  
	            InputStream is = proc.getInputStream();  
	            InputStreamReader isr = new InputStreamReader(is);  
	            String line;  
	            BufferedReader br = new BufferedReader(isr);  
	            while ((line = br.readLine()) != null)  
	            {
					Log.e("line", line);
					// 将常见的linux分区过滤掉
	                if (line.contains("proc") || line.contains("tmpfs") || line.contains("media") || line.contains("asec") || line.contains("secure") || line.contains("system") || line.contains("cache")  
	                        || line.contains("sys") || line.contains("data") || line.contains("shell") || line.contains("root") || line.contains("acct") || line.contains("misc") || line.contains("obb"))  
	                {  
	                    continue;  
	                }  
	  
	                // 下面这些分区是我们需要的  
	                if (line.contains("fat") || line.contains("fuse") || (line.contains("ntfs")))  
	                {  
	                    // 将mount命令获取的列表分割，items[0]为设备名，items[1]为挂载路径  
	                    String items[] = line.split("\\s+");  
	                    //Log.e("SDcard path 1", line);
	                    if (items != null && items.length > 1)  
	                    {  
	                        String tempPath = items[5];//.toLowerCase(Locale.getDefault())
	                        //Log.e("SDcard path 2", tempPath); 
	                        
	                        // 添加一些判断，确保是sd卡，如果是otg等挂载方式，可以具体分析并添加判断条件  
	                        if (tempPath != null && tempPath.contains("storage")){
	                        	String subItems[] = tempPath.split("/");
	                        	Log.e("SDcard path 3", subItems[2]);
	                        	if(!"emulated".equals(subItems[2])) {
	                        		Log.e("SDcard path",tempPath);  
		                            return tempPath;
	                        	}
	                        }
	                    }  
	                }  
	            }  
	            return "/storage/sdcard1";
	        } catch (Exception e)  
	        {  
	            // TODO Auto-generated catch block  
	            e.printStackTrace();  
	            return "/storage/sdcard1";
	        }  
	 }

	public static String getSecondaryStoragePath(Context context) {
		try {
			StorageManager sm = (StorageManager) context.getSystemService(STORAGE_SERVICE);
			Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", null);
			String[] paths = (String[]) getVolumePathsMethod.invoke(sm, null);
			// second element in paths[] is secondary storage path
			return paths.length <= 1 ? null : paths[1];
		} catch (Exception e) {
			Log.e(TAG, "getSecondaryStoragePath() failed", e);
		}
		return null;
	}

	// 获取存储卡的挂载状态. path 参数传入上两个方法得到的路径
	public static String getStorageState(String path,Context context) {
		try {
			StorageManager sm = (StorageManager) context.getSystemService(STORAGE_SERVICE);
			Method getVolumeStateMethod = StorageManager.class.getMethod("getVolumeState", new Class[] {String.class});
			String state = (String) getVolumeStateMethod.invoke(sm, path);
			return state;
		} catch (Exception e) {
			Log.e(TAG, "getStorageState() failed", e);
		}
		return null;
	}

}
