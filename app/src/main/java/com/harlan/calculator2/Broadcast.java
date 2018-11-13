package com.harlan.calculator2;

import java.io.DataOutputStream;
import java.util.Calendar;
import java.util.Iterator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class Broadcast extends BroadcastReceiver{
	final String ssid="WiFiAW";
  final String password="12121212";
  final String type="WIFICIPHER_WPA";
  private Context mcontext;
  public static boolean buptwifi=false;
	@Override
	public void onReceive(Context context, Intent intent) {
		this.mcontext=context;
		// TODO Auto-generated method stub
		final String msg=intent.getAction();
		/*if (msg.equals("com.bupt.adbshell.test")) {
			Toast toast = Toast.makeText(mcontext, "成功！", 2000);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			Log.v("1", "chenggong");
			Logic.flag=1;
			if(Logic.list.size()!=0){
				try{
					String filename=Logic.list.get(Logic.list.size()-1);
	    			Log.v(filename, filename);
	    			Process proc=Runtime.getRuntime().exec("su"); // –c /data/sdcard reset 0 0
	    			String f1="/data/sdcard write "+filename+" "+filename.length()+" "+"/storage/emulated/legacy/ftpServer/"+filename+"\n";
	    			DataOutputStream os=new DataOutputStream(proc.getOutputStream());
	    			os.write(f1.getBytes());
				Logic.flag=0;
				Logic.list.remove(Logic.list.size()-1);
    			}catch(Exception e){

    			}
			}
		}else if(msg.equals("com.bupt.ftpserver.file")){
			String fileName = (String)intent.getExtras().get("fileName");
			Logic.list.add(fileName);
			if(Logic.flag==1){
				try{
					String filename=Logic.list.get(Logic.list.size()-1);
	    			Log.v(filename, filename);
	    			Process proc=Runtime.getRuntime().exec("su"); // –c /data/sdcard reset 0 0
	    			String f1="/data/sdcard write "+filename+" "+filename.length()+" "+"/storage/emulated/legacy/ftpServer/"+filename+"\n";
	    			DataOutputStream os=new DataOutputStream(proc.getOutputStream());
	    			os.write(f1.getBytes());
					Logic.list.remove(Logic.list.size()-1);
	    			}catch(Exception e){
	    			}
			}
		}*/
		
	}
}
