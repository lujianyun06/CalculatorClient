package wificontrol;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

//import wificontrol.DirChooserDialog.PriorityListener;

import com.harlan.calculator2.R;
import com.harlan.calculator2.R.id;
import com.harlan.calculator2.R.layout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.os.BuptSystemManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class WifiConfigActivity extends Activity {
	private TimePicker tp;
	private EditText t1;
	private EditText t2;
	private EditText t3;
	private TextView t4;
	private Button b1;
	private Button b2;
	public int hour;
	public int minute;
	public int alarminterval;
	public String wifiname;
	public String wifipassword;
	public String fileuploadpath;
	public static final int FILE_RESULT_CODE = 1;
	
	/*private Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case 1:
				//do UI operation here
				dialog();
				break;
			default:
				break;
		  }
		}
	};*/


	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_config);
		this.tp=(TimePicker)findViewById(R.id.timePicker1);
		//this.tp.setIs24HourView(false);
		this.t1=(EditText)findViewById(R.id.editText1);
		this.t2=(EditText)findViewById(R.id.editText2);
		this.t3=(EditText)findViewById(R.id.editText3);
		/*this.t4=(TextView)findViewById(R.id.textView5);
		this.b1=(Button)findViewById(R.id.button1);*/
		this.b2=(Button)findViewById(R.id.button2);
		tp.setIs24HourView(true);
		/*b1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String path = null;
				String [] fileType = {"dst","rc","jpg"};//要找到的/过滤的文件类型列表     文件后缀
				DirChooserDialog dlg = new DirChooserDialog(WifiConfigActivity.this,2,fileType,path,new PriorityListener() {
					@Override
					public void refreshPriorityUI(String string) {
						// TODO Auto-generated method stub
						t4.setText(string);
					}
				});

				dlg.setTitle("请选择路径");
				dlg.show();
				Log.e("test",dlg.getPath()+"tet");


			}});*/

		b2.setOnClickListener(new OnClickListener(){

			@SuppressLint({ "ShowToast", "SimpleDateFormat" })
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method
				if(("".equals(t1.getText().toString().trim()))) {
					Toast.makeText(getApplicationContext(), "请完善所有信息！", 2000).show();
					return;
				}
				
				SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
				Date curDate = new Date(System.currentTimeMillis());
				String date=sdf.format(curDate);
				int currentHour = Integer.parseInt(date.substring(0,2));
				int currentMinute = Integer.parseInt(date.substring(3,5));
				

				hour = tp.getCurrentHour();
				minute=tp.getCurrentMinute();
				alarminterval=Integer.valueOf(t1.getText().toString().trim());
				wifiname=t2.getText().toString().trim();
				wifipassword=t3.getText().toString().trim();
				
				
				if(("".equals(wifiname)) || ("".equals(wifipassword))) {
					Toast.makeText(getApplicationContext(), "请完善所有信息！", 2000).show();
					return;
				} else if(wifipassword.length()<8) {
					Toast.makeText(getApplicationContext(), "WiFi密码长度不够，请重新填写", 2000).show();
					return;
				} else if(hour<currentHour || (hour==currentHour && minute<currentMinute)) {
					Toast.makeText(getApplicationContext(), "时间设置不正确，请重新设置", 2000).show();
					return;
				}
				
				//fileuploadpath=t4.getText().toString();
				/*
				MODE_WORLD_READABLE：表示当前文件可以被其他应用读取.
				“buptClientSetting”是名字，而context是方式
				* */
				/*
				* SharedPreferences preferences=getSharedPreferences("user", Context.MODE_PRIVATE);
                   String name=preferences.getString("name", "defaultname");
                   String age=preferences.getString("age", "0");
				* */
				Editor sharedata = getSharedPreferences("buptClientSetting", Context.MODE_WORLD_READABLE).edit();
				sharedata.putInt("hour",hour);
				sharedata.putInt("minute", minute);
				sharedata.putInt("alarmInterval",alarminterval);
				sharedata.putString("ssid",wifiname);
				sharedata.putString("password",wifipassword);
				//sharedata.putString("filedir",fileuploadpath);   //文件路径String
				sharedata.commit();   //应用更改
				Intent ii=new Intent("com.bupt.client.setting");   //发送广播，跳转到相应的应用  静态注册
				sendBroadcast(ii); //以上代码为客户端代码 也是启动客户端，以广播的形式发送出去，只不过可能启动1个、0个或者N个activity
				// BuptSystemManager om = (BuptSystemManager) WifiConfigActivity.this.getSystemService("");添加一个带Context参数的构造方法
				//将该activity作为对象传递到自己写的类中
				closeInputMethod();
				finish();
				//om.unistall("com.harlan.calculator2");

			}});

	}
	
	private void closeInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen = imm.isActive();
		if (isOpen) {
			//imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示
			imm.hideSoftInputFromWindow(this.t1.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			imm.hideSoftInputFromWindow(this.t2.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			imm.hideSoftInputFromWindow(this.t3.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			
		}
	}
	public void dialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(WifiConfigActivity.this);
	      //    设置Title的内容
	      builder.setTitle("警告");
	      //    设置Content来显示一个信息
	      builder.setMessage("请完善所有信息！");
	      //    设置一个PositiveButton
	      builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
	      {
	          @Override
	          public void onClick(DialogInterface dialog, int which)
	          {
	        	  dialog.dismiss();
	          }
	      });
	      //    设置一个NegativeButton
	      builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
	      {
	          @Override
	          public void onClick(DialogInterface dialog, int which)
	          {
	        	  dialog.dismiss();
	        	  WifiConfigActivity.this.finish();
	          }
	      });
	      AlertDialog alertDialog = builder.create();
	      alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	        // 显示对话框
	      alertDialog.show();
	}
}
