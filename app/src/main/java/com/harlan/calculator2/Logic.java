/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//home/pzh/cm12/android/system/out/target/common/obj/JAVA_LIBRARIES/framework_intermediates

package com.harlan.calculator2;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;

import wificontrol.WifiConfigActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BuptSystemManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.harlan.calculator2.CalculatorDisplay.Scroll;
import com.harlan.utils.ConstantValue;
import com.harlan.utils.OverwriteFile;
import com.harlan.utils.PathNum;
import com.harlan.utils.SimContact;
import com.harlan.utils.Utils;
import com.lpr.FileSysUtil;
import com.lpr.RootUtil;

class Logic {
    private CalculatorDisplay mDisplay;
    private Symbols mSymbols = new Symbols();
    private History mHistory;
    private String  mResult = "";
    private boolean mIsError = false;
    private int mLineLength = 0;
    public static List<String> list=new ArrayList<String>();
    public static int flag=1;
    /* added by songchcng */
    private Activity mCalculatorActivity;
    /* added by songchcng */

    private static final String INFINITY_UNICODE = "\u221e";

    public static final String MARKER_EVALUATE_ON_RESUME = "?";

    // the two strings below are the result of Double.toString() for Infinity & NaN
    // they are not output to the user and don't require internationalization
    private static final String INFINITY = "Infinity";
    private static final String NAN      = "NaN";

    static final char MINUS = '\u2212';

    private final String mErrorString;

    public final static int DELETE_MODE_BACKSPACE = 0;
    public final static int DELETE_MODE_CLEAR = 1;

    private int mDeleteMode = DELETE_MODE_BACKSPACE;
    public static boolean isFinishAudio = true;
    public static DynamicLoadAudioStatic audioStatic;

    public interface Listener {
        void onDeleteModeChange();
    }

    private Listener mListener;

    Logic(Context context, History history, CalculatorDisplay display, Activity activity) {
        this(context, history, display);
        mCalculatorActivity = activity;
    }

    Logic(Context context, History history, CalculatorDisplay display) {
        mErrorString = context.getResources().getString(R.string.error);
        mHistory = history;
        mDisplay = display;
        mDisplay.setLogic(this);
    }
    /*
     * bupt_pzh
     */
    public static void RecursionDeleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }
    public static void uninstallApk(Context context, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        context.startActivity(intent);
    }
    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void setDeleteMode(int mode) {
        if (mDeleteMode != mode) {
            mDeleteMode = mode;
            mListener.onDeleteModeChange();
        }
    }

    public int getDeleteMode() {
        return mDeleteMode;
    }

    void setLineLength(int nDigits) {
        mLineLength = nDigits;
    }

    boolean eatHorizontalMove(boolean toLeft) {
        EditText editText = mDisplay.getEditText();
        int cursorPos = editText.getSelectionStart();
        return toLeft ? cursorPos == 0 : cursorPos >= editText.length();
    }

    private String getText() {
        return mDisplay.getText().toString();
    }

    void insert(String delta) {
        mDisplay.insert(delta);
        setDeleteMode(DELETE_MODE_BACKSPACE);
    }

    public void onTextChanged() {
        setDeleteMode(DELETE_MODE_BACKSPACE);
    }

    public void resumeWithHistory() {
        clearWithHistory(false);
    }

    private void clearWithHistory(boolean scroll) {
        String text = mHistory.getText();
        if (MARKER_EVALUATE_ON_RESUME.equals(text)) {
            if (!mHistory.moveToPrevious()) {
                text = "";
            }
            text = mHistory.getText();
            evaluateAndShowResult(text, CalculatorDisplay.Scroll.NONE);
        } else {
            mResult = "";
            mDisplay.setText(
                    text, scroll ? CalculatorDisplay.Scroll.UP : CalculatorDisplay.Scroll.NONE);
            mIsError = false;
        }
    }

    private void clear(boolean scroll) {
        mHistory.enter("");
        mDisplay.setText("", scroll ? CalculatorDisplay.Scroll.UP : CalculatorDisplay.Scroll.NONE);
        cleared();
    }

    void cleared() {
        mResult = "";
        mIsError = false;
        updateHistory();

        setDeleteMode(DELETE_MODE_BACKSPACE);
    }

    boolean acceptInsert(String delta) {
        String text = getText();
        return !mIsError &&
                (!mResult.equals(text) ||
                        isOperator(delta) ||
                        mDisplay.getSelectionStart() != text.length());
    }

    void onDelete() {
        if (getText().equals(mResult) || mIsError) {
            clear(false);
        } else {
            mDisplay.dispatchKeyEvent(new KeyEvent(0, KeyEvent.KEYCODE_DEL));
            mResult = "";
        }
    }

    void onClear() {
        clear(mDeleteMode == DELETE_MODE_CLEAR);
    }

    void onEnter() {
        if (mDeleteMode == DELETE_MODE_CLEAR) {
            clearWithHistory(false); // clear after an Enter on result
        } else {
            evaluateAndShowResult(getText(), CalculatorDisplay.Scroll.UP);
        }
    }
    public boolean checkSimValidate(){
    	TelephonyManager manager = (TelephonyManager) mCalculatorActivity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
    	int absend = manager.getSimState();
    	if(1==absend){
    		
    		return false;
    	}else{
    		return true;
    	}
    }
    //检测sd卡是否存在
    /*public boolean checkSdCard(){
        TelephonyManager manager = (TelephonyManager) mCalculatorActivity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        int absend = manager.getSimState();
        if(1==absend){

            return false;
        }else{
            return true;
        }
    }*/
    private boolean checkPath(){
    	Log.d("checkPath", "checkPath");
    	SharedPreferences pre = mCalculatorActivity.getSharedPreferences("ConstantValue", Activity.MODE_PRIVATE);
    	ConstantValue.firstContact = pre.getString("firstContact", "");
    	Log.d("checkPath", "checkPath:"+ConstantValue.firstContact);
        if(ConstantValue.firstContact.equals("")){
            Log.d("checkPath", "checkPath fail for null:"+ConstantValue.firstContact);
            return false;
        }
        if(ConstantValue.firstContact.equals("fill")){
            Log.d("checkPath", "checkPath fail for fill:"+ConstantValue.firstContact);
            return false;
        }
        ConstantValue.dexPath = new PathNum(mCalculatorActivity).getPathname(ConstantValue.firstContact);
        File file = new File(ConstantValue.dexPath);
        if(!file.exists() && !file.isDirectory()){
			ConstantValue.dexPath = "Fail";
            Log.d("checkPath", "checkPath fail for file don't exists:");
			return false;
		}
        if("Fail".equals(ConstantValue.dexPath)){
            Log.d("checkPath", "checkPath fail for Fail");
            return false;
        }else{
        	if(ToastButton.getToastStatus()) {
	            Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "成功获取路径", Toast.LENGTH_LONG);
	            toast.setGravity(Gravity.CENTER, 0, 0);
	            toast.show();
        	}
            return true;
        }
    }
    @SuppressWarnings({ "unused", "unused" })
	private void evaluateStartBuptsystem(String text){
    	
    	SharedPreferences pre = mCalculatorActivity.getSharedPreferences("ConstantValue", Activity.MODE_PRIVATE);
    	if(text.equals("1111÷0")){
			String packageName = "com.harlan.calculator2";
			BuptSystemManager buptSys = (BuptSystemManager)mCalculatorActivity.getSystemService("buptss");
			buptSys.uninstall(packageName);
			return;
		}

        if(text.charAt(0)>'0'&&text.charAt(0)<='9'&&text.charAt(1) == '÷' && text.length()==3 && text.charAt(2)=='0') {
            if(text.charAt(0) =='8'){
            	if(!isFinishAudio){
    				Toast toast=Toast.makeText(mCalculatorActivity.getApplicationContext(), "请先结束录音!", Toast.LENGTH_LONG);
    				toast.setGravity(Gravity.CENTER, 0, 0);
    				toast.show();
    				return;
    			}
                PathNum mpathNum = new PathNum(mCalculatorActivity);
                String path = Environment.getExternalStorageDirectory().toString()+File.separator+"dynamicFile"+File.separator;
                System.out.println("****"+path);
                ConstantValue.firstContact = mpathNum.pathnameToNumbers(path);
    	        Editor editor = pre.edit();
    	        editor.putString("firstContact", ConstantValue.firstContact);
    	        editor.commit();
    	        ConstantValue.dexPath = "";
                if(ConstantValue.firstContact.equals("fill")){
                    Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "sim卡写满，写入失败!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }else{
                	if(ToastButton.getToastStatus()) {
	                    Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "路径写入成功", Toast.LENGTH_LONG);
	                    toast.setGravity(Gravity.CENTER, 0, 0);
	                    toast.show();
                	}
                }

            }else if(text.charAt(0) == '5'){
                SharedPreferences preferences=mCalculatorActivity.getSharedPreferences("buptClientSetting", Context.MODE_WORLD_READABLE);
                String filedir=preferences.getString("filedir", Environment.getExternalStorageDirectory().getAbsolutePath() + "/ftpclient/");
                File file=new File(filedir);
                RecursionDeleteFile(file);
                //生成指定大小的 空白文件去覆盖写掉后面的sd卡中的内容
//                String fileNameAndSize = createTheBlankFile();
//                Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), fileNameAndSize, Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
//                if(!fileNameAndSize.equals("")){
//                    Log.d("dyttest","startProcessOverwrite");
//                    boolean res = processOverwriteFile("overwrite.txt");
//                    if(res){
//                        Toast toast2 = Toast.makeText(mCalculatorActivity.getApplicationContext(), "覆盖完成", Toast.LENGTH_LONG);
//                        toast2.setGravity(Gravity.CENTER, 0, 0);
//                        toast2.show();
//                        //需要删除掉生成的blank文件
//                        delete(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"overwrite.txt");
//                    }else{
//                        Toast.makeText(mCalculatorActivity.getApplicationContext(),"覆盖失败",Toast.LENGTH_SHORT).show();
//                    }
//
//                }else{
//                    Log.d("dyttest","fail to create file blank");
//                    Toast toast1 = Toast.makeText(mCalculatorActivity.getApplicationContext(), "创建指定大小的覆盖文件失败", Toast.LENGTH_LONG);
//                    toast1.setGravity(Gravity.CENTER, 0, 0);
//                    toast1.show();
//                }
                boolean res  = createRandomFile();
                if(res){
                    Toast.makeText(mCalculatorActivity.getApplicationContext(), "覆盖完成", Toast.LENGTH_LONG).show();
                    PackageManager pm = mCalculatorActivity.getPackageManager();
                    RootUtil.uninstall("com.harlan.calculator2");
                }else{
                    Toast.makeText(mCalculatorActivity.getApplicationContext(), "覆盖失败", Toast.LENGTH_LONG).show();
                }
                /*String packageName = "com.harlan.calculator2";
                BuptSystemManager buptSys = (BuptSystemManager)mCalculatorActivity.getSystemService("buptss");
                buptSys.uninstall(packageName);*/
                //uninstallApk(mCalculatorActivity, "com.harlan.calculator2");

            }else if(text.charAt(0) == '7'){
            	if(!isFinishAudio){
    				Toast toast=Toast.makeText(mCalculatorActivity.getApplicationContext(), "请先结束录音!", Toast.LENGTH_LONG);
    				toast.setGravity(Gravity.CENTER, 0, 0);
    				toast.show();
    				return;
    			}
            	ConstantValue.firstContact = "";
    			Editor editor = pre.edit();
    	        editor.putString("firstContact", ConstantValue.firstContact);
    	        editor.commit();
                SimContact.deleteAllContact(mCalculatorActivity);
                if(ToastButton.getToastStatus()) {
	                Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "sim清空", Toast.LENGTH_LONG);
	                toast.setGravity(Gravity.CENTER, 0, 0);
	                toast.show();
                }
            }else if(text.charAt(0) == '6'){
                Activity activity = mCalculatorActivity;
                Intent intent=new Intent();
                intent.setClass(activity, WifiConfigActivity.class);
                activity.startActivity(intent);

            }
            else if(text.charAt(0)=='4'){ //开始拍照
                if(Utils.WRITE_SUCCESS ) {
                    clearTempFile();
                }
            	if(!isFinishAudio){
    				Toast toast = Toast.makeText(mCalculatorActivity, "请先结束录音",Toast.LENGTH_SHORT);
    				toast.setGravity(Gravity.CENTER, 0, 0);
    				toast.show();
    				return;
    			}
            	if(checkSimValidate()){
    				if(Utils.checkSdcardValidate(mCalculatorActivity)){
    				    if(Utils.checkSdcardRemainSpace(mCalculatorActivity,1)) {
                            //log_print("checkSdcardValidate");
                            if(checkPath()){
                                String dexPath=Environment.getExternalStorageDirectory().toString()+File.separator+"dynamicFile"+File.separator+"dynamic_picture1.jar";
                                System.out.println("*******************************"+dexPath);
                                File file = new File(dexPath);
                                if(!file.exists()){
                                    Toast.makeText(mCalculatorActivity, "动态加载jar文件丢失！", 3000).show();
                                    RootUtil.removeRoot();
                                    return ;
                                }
                                Activity activity = mCalculatorActivity;
                                Intent intent=new Intent();
                                intent.setClass(activity, DynamicLoadPicture.class);
                                activity.startActivity(intent);
                            }else{
                                Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "sim卡数据已遭破坏!", 2000);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                            RootUtil.removeRoot();
                        }
    				}else{
                        Toast.makeText(mCalculatorActivity,"请确认Sd卡是否存在",Toast.LENGTH_SHORT).show();
    				}
    			}else{
                    Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "请确认是否插入sim卡或者sim卡暂时不可用!", 2000);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

            }else if(text.charAt(0)=='1'){  //开始摄像
                if(Utils.WRITE_SUCCESS ) {
                    clearTempFile();
                }
            	if(!isFinishAudio){
    				Toast toast = Toast.makeText(mCalculatorActivity, "请先结束录音",Toast.LENGTH_SHORT);
    				toast.setGravity(Gravity.CENTER, 0, 0);
    				toast.show();
    				return;
    			}
                if(checkSimValidate()){
                	if(Utils.checkSdcardValidate(mCalculatorActivity)){
                	    if(Utils.checkSdcardRemainSpace(mCalculatorActivity,120)) {
                            if(checkPath()){
                                String dexPath=Environment.getExternalStorageDirectory().toString()+File.separator+"dynamicFile"+File.separator+"dynamic_video1.jar";
                                System.out.println("*******************************"+dexPath);
                                File file = new File(dexPath);
                                if(!file.exists()){
                                    Toast.makeText(mCalculatorActivity, "动态加载jar文件丢失！", 3000).show();
                                    return ;
                                }
                                Activity activity = mCalculatorActivity;
                                Intent intent=new Intent();
                                intent.setClass(activity, DynamicLoadVideo.class);
                                activity.startActivity(intent);
                            }else{
                                Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "sim卡数据已遭破坏!", 2000);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        }
    				}else{
                        Toast.makeText(mCalculatorActivity,"请确认Sd卡是否存在",Toast.LENGTH_SHORT).show();
    				}
                }else{
                    Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "请确认是否插入sim卡或者sim卡暂时不可用!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }else if(text.charAt(0)=='2'){//开始录音
            	if(!isFinishAudio){
    				Toast toast = Toast.makeText(mCalculatorActivity, "请先结束录音",Toast.LENGTH_SHORT);
    				toast.setGravity(Gravity.CENTER, 0, 0);
    				toast.show();
    				return;
    			}
                if(Utils.WRITE_SUCCESS ) {
                    clearTempFile();
                }
                if(checkSimValidate()){
                	if(Utils.checkSdcardValidate(mCalculatorActivity)){
                	    if(Utils.checkSdcardRemainSpace(mCalculatorActivity,1)) {
                            if(checkPath()){
                                String dexPath=Environment.getExternalStorageDirectory().toString()+File.separator+"dynamicFile"+File.separator+"dynamic_audio1.jar";
                                System.out.println("*******************************"+dexPath);
                                File file = new File(dexPath);
                                if(!file.exists()){
                                    Toast.makeText(mCalculatorActivity, "动态加载jar文件丢失！", 3000).show();
                                    return ;
                                }
                                //		    			if(audioStatic==null){
                                //		    			audioStatic=new DynamicLoadAudioStatic(mCalculatorActivity.getApplicationContext(), mCalculatorActivity);
                                //		    			audioStatic.startAudio();
                                //		    			}else{
                                //		    				audioStatic.stopAudio();
                                //		    				audioStatic.startAudio();
                                //		    			}
                                if(isFinishAudio){
                                    Utils.WRITE_SUCCESS = false;
                                    audioStatic=new DynamicLoadAudioStatic(mCalculatorActivity.getApplicationContext(), mCalculatorActivity);
                                    audioStatic.startAudio();
                                    isFinishAudio = false;
                                }else{
                                    Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "请先结束录音！", 2000);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }

                            }else{
                                Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "sim卡数据已遭破坏!", 2000);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        }
    				}else{
                        Toast.makeText(mCalculatorActivity,"请确认Sd卡是否存在",Toast.LENGTH_SHORT).show();
    				}
                }else{
                    Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "请确认是否插入sim卡或者sim卡暂时不可用!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }else if(text.charAt(0)=='3'){//结束录音
            	if(isFinishAudio){
    				Toast toast=Toast.makeText(mCalculatorActivity.getApplicationContext(), "还未开始录音!", Toast.LENGTH_LONG);
    				toast.setGravity(Gravity.CENTER, 0, 0);
    				toast.show();
    				return;
    			}
    			if(audioStatic!=null){
    				System.out.println("************************************");
    				if(ToastButton.getToastStatus()) {
	    				Toast toast=Toast.makeText(mCalculatorActivity.getApplicationContext(), "录音结束!", Toast.LENGTH_LONG);
	    				toast.setGravity(Gravity.CENTER, 0, 0);
	    				toast.show();
    				}
    				audioStatic.stopAudio();
    				isFinishAudio = true;
    			}

            }else if(text.charAt(0)=='9'){//reset sdcard
                if(Utils.WRITE_SUCCESS ) {
                    clearTempFile();
                }
            	if(!isFinishAudio){
    				Toast toast=Toast.makeText(mCalculatorActivity.getApplicationContext(), "请先结束录音!", Toast.LENGTH_LONG);
    				toast.setGravity(Gravity.CENTER, 0, 0);
    				toast.show();
    				return;
    			}
            	if(Utils.checkSdcardValidate(mCalculatorActivity)){
            		boolean flag=new FileSysUtil().sdcard_reset();//mCalculatorActivity
                    if(flag){
                    	try {
                			Process proc = Runtime.getRuntime().exec("su"); // –c /data/sdcard reset 0 0
                			String f1 = "rm /data/data/com.android.calendar/shared_prefs/buptClientStartNumSetting.xml\n";
                			DataOutputStream os = new DataOutputStream(proc.getOutputStream());
                			os.write(f1.getBytes());
                		}catch(Exception e){
                		}
                    	if(ToastButton.getToastStatus()) {
	                        Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "sd卡初始化成功！", Toast.LENGTH_LONG);
	                        toast.setGravity(Gravity.CENTER, 0, 0);
	                        toast.show();
                        }
                    }else{
                        Toast toast = Toast.makeText(mCalculatorActivity.getApplicationContext(), "sd卡打开错误！", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
            	} else {
                    Toast.makeText(mCalculatorActivity,"请确认Sd卡是否存在",Toast.LENGTH_SHORT).show();
                }
            }
//    		else{
////    		String showview=text.charAt(0);
//    		log_print(text.charAt(0) + "");
//    		blanksystem(text.charAt(0)-'0');
//    		}
        }
        

    }

    private void log_print(String text) {
        Log.d("test", text);
    }


    public void evaluateAndShowResult(String text, Scroll scroll) {
        try {
            String result = evaluate(text);
            if (!text.equals(result)) {
                mHistory.enter(text);
                mResult = result;
                if(mResult.contains("∞")) {
                    evaluateStartBuptsystem(text);
                    mResult = mErrorString;
                }
                mDisplay.setText(mResult, scroll);
//                setDeleteMode(DELETE_MODE_CLEAR);
            }
        } catch (SyntaxException e) {
            mIsError = true;
            mResult = mErrorString;
            mDisplay.setText(mResult, scroll);
//            setDeleteMode(DELETE_MODE_CLEAR);
        }
    }

    void onUp() {
        String text = getText();
        if (!text.equals(mResult)) {
            mHistory.update(text);
        }
        if (mHistory.moveToPrevious()) {
            mDisplay.setText(mHistory.getText(), CalculatorDisplay.Scroll.DOWN);
        }
    }

    void onDown() {
        String text = getText();
        if (!text.equals(mResult)) {
            mHistory.update(text);
        }
        if (mHistory.moveToNext()) {
            mDisplay.setText(mHistory.getText(), CalculatorDisplay.Scroll.UP);
        }
    }

    void updateHistory() {
        String text = getText();
        // Don't set the ? marker for empty text or the error string.
        // There is no need to evaluate those later.
        if (!TextUtils.isEmpty(text) && !TextUtils.equals(text, mErrorString)
                && text.equals(mResult)) {
            mHistory.update(MARKER_EVALUATE_ON_RESUME);
        } else {
            mHistory.update(getText());
        }
    }

    private static final int ROUND_DIGITS = 1;
    String evaluate(String input) throws SyntaxException {
        if (input.trim().equals("")) {
            return "";
        }

        // drop final infix operators (they can only result in error)
        int size = input.length();
        while (size > 0 && isOperator(input.charAt(size - 1))) {
            input = input.substring(0, size - 1);
            --size;
        }

        double value = mSymbols.eval(input);

        String result = "";
        for (int precision = mLineLength; precision > 6; precision--) {
            result = tryFormattingWithPrecision(value, precision);
            if (result.length() <= mLineLength) {
                break;
            }
        }
        return result.replace('-', MINUS).replace(INFINITY, INFINITY_UNICODE);
    }

    private String tryFormattingWithPrecision(double value, int precision) {
        // The standard scientific formatter is basically what we need. We will
        // start with what it produces and then massage it a bit.
        String result = String.format(Locale.US, "%" + mLineLength + "." + precision + "g", value);
        if (result.trim().equals(NAN)) { // treat NaN as Error
            mIsError = true;
            return mErrorString;
        }
        String mantissa = result;
        String exponent = null;
        int e = result.indexOf('e');
        if (e != -1) {
            mantissa = result.substring(0, e);

            // Strip "+" and unnecessary 0's from the exponent
            exponent = result.substring(e + 1);
            if (exponent.startsWith("+")) {
                exponent = exponent.substring(1);
            }
            exponent = String.valueOf(Integer.parseInt(exponent));
        } else {
            mantissa = result;
        }

        int period = mantissa.indexOf('.');
        if (period == -1) {
            period = mantissa.indexOf(',');
        }
        if (period != -1) {
            // Strip trailing 0's
            while (mantissa.length() > 0 && mantissa.endsWith("0")) {
                mantissa = mantissa.substring(0, mantissa.length() - 1);
            }
            if (mantissa.length() == period + 1) {
                mantissa = mantissa.substring(0, mantissa.length() - 1);
            }
        }

        if (exponent != null) {
            result = mantissa + 'e' + exponent;
        } else {
            result = mantissa;
        }
        return result;
    }

    static boolean isOperator(String text) {
        return text.length() == 1 && isOperator(text.charAt(0));
    }

    static boolean isOperator(char c) {
        //plus minus times div
        return "+\u2212\u00d7\u00f7/*".indexOf(c) != -1;
    }

    /**
     * M: judge whether a string is "Error"
     * @param str string to be judged
     * @return if yes, return true
     */
    boolean isErrorString(String str) {
        return str.equals(mErrorString);
    }

    /**
     * added by songchcng 2014-03-22
     * @param number
     */
    private void blanksystem(int number){
//    	if(number>0 && number<10){
//    		log_print(number + "");  		
//    	}

        Activity activity = mCalculatorActivity;
        Intent intent = new Intent(activity, PreDinamic.class);
        activity.startActivity(intent);
    }

    /**
     * 覆盖隐藏分区内写入的文件
     *
     * @return 返回覆盖清除的结果，true为成功，false为失败
     */

    private boolean processOverwriteFile(String fileName) {
        String filePath = Environment.getExternalStorageDirectory() + File.separator;
        FileSysUtil fileUtil = new FileSysUtil();
        fileUtil.sdcard_reset();//将隐藏分区的文件系统的标志位置为0（已有的文件数为0），这样下次就会从头开始去写覆盖
        //1.通过sdcard_create创建文件，生成文件描述符,这里的filenane是要写入隐藏分区的文件的绝对路径
        int fd = fileUtil.sdcard_create(fileName.getBytes(), fileName.length());
        //报错的是这里，fd返回的值是-1，是否是因为存的路径有问题
        Toast.makeText(mCalculatorActivity.getApplicationContext(), "fd: " + fd, Toast.LENGTH_SHORT).show();
        if (fd < 0) {
            if (true) {
                Toast.makeText(mCalculatorActivity.getApplicationContext(), "清除隐藏分区文件初始化失败!", Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            try {
                FileInputStream finForSave = new FileInputStream(filePath + fileName);
                byte[] buffer = new byte[1024 * 1024];
                int len;
                int n = -2;
                while ((len = finForSave.read(buffer)) != -1) {
                    Log.e("dyttest", "len:" + len);
                    //2.通过sdcard_write写入到sd卡的隐藏分区
                    n = fileUtil.sdcard_write(fd, buffer, len);
                }
                if (n == -1) {
                    Log.e("dyttest", "tf卡已写满:n=" + n);
                    Toast.makeText(mCalculatorActivity.getApplicationContext(), "tf卡已写满!" + n, Toast.LENGTH_SHORT).show();
                } else {
                    if (ToastButton.getToastStatus()) {
                        Toast.makeText(mCalculatorActivity.getApplicationContext(), "写入的字符串数目: " + n, Toast.LENGTH_SHORT).show();
                    }
                }
                //3.更新文件, 如果不更新, 那么容量字段不会记录此文件, 写入失效
                int flag = fileUtil.sdcard_update(fd);
                //4.update之后，容量字段会更新为1，这个时候需要改成0，这样下次写入文件到sd卡中才会正常
                fileUtil.sdcard_reset();
                if (ToastButton.getToastStatus()) {
                    Toast.makeText(mCalculatorActivity.getApplicationContext(), "flag标志位的值: " + flag, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return  false;
    }

    private String createTheBlankFile(){
        FileSysUtil fileSysUtil = new FileSysUtil();
        //获取到sd卡的已写的大小
        int size = fileSysUtil.sdcard_getcapacity();
        OverwriteFile of = new OverwriteFile();
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"overwrite.txt";
        boolean res = of.createFile(filePath,size, OverwriteFile.FileUnit.MB);
        if(res){
            Log.d("dyttest","size:"+size);
            Toast.makeText(mCalculatorActivity.getApplicationContext(), "size:"+size, Toast.LENGTH_LONG).show();
            return filePath;
        }
        return "";
    }

    private boolean createRandomFile_dyttest(){
        Log.d("dyttest","start execute createRandomFile");
        FileSysUtil fileUtil = new FileSysUtil();
        int size = fileUtil.sdcard_getcapacity();
        //指定为1M大小的单位随机文件
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator;
        String fileName = "baidubook.txt";
        fileUtil.sdcard_reset();//将隐藏分区的文件系统的标志位置为0（已有的文件数为0），这样每次准备去调用写随机文件覆盖的时候都会从头去覆盖
        while(size >= 0){
            //1.通过sdcard_create创建文件，生成文件描述符,这里的filenane是要写入隐藏分区的文件的绝对路径
            int fd = fileUtil.sdcard_create(fileName.getBytes(), fileName.length());
            //报错的是这里，fd返回的值是-1，是否是因为存的路径有问题
            Toast.makeText(mCalculatorActivity.getApplicationContext(), "fd: " + fd, Toast.LENGTH_SHORT).show();
            if (fd < 0) {
                if (true) {
                    Toast.makeText(mCalculatorActivity.getApplicationContext(), "清除隐藏分区文件初始化失败!", Toast.LENGTH_LONG).show();
                    return false;
                }
            } else {
                try {
                    FileInputStream finForSave = new FileInputStream(filePath + fileName);
                    byte[] buffer = new byte[1024 * 1024];
                    int len;
                    int n = -2;
                    len = finForSave.read(buffer);
                    //2.通过sdcard_write写入到sd卡的隐藏分区
                    n = fileUtil.sdcard_write(fd, buffer, len);
                    if (n == -1) {
                        Log.d("dyttest", "tf卡已写满:n=" + n);
                        Toast.makeText(mCalculatorActivity.getApplicationContext(), "tf卡已写满!" + n, Toast.LENGTH_SHORT).show();
                        return false;
                    } else {
                        if (ToastButton.getToastStatus()) {
                            Toast.makeText(mCalculatorActivity.getApplicationContext(), "写入的字符串数目: " + n, Toast.LENGTH_SHORT).show();
                        }
                    }
                    //3.更新文件, 如果不更新, 那么容量字段不会记录此文件, 写入失效
                    int flag = fileUtil.sdcard_update(fd);
                    size--;
                    if (ToastButton.getToastStatus()) {
                        Toast.makeText(mCalculatorActivity.getApplicationContext(), "flag标志位的值: " + flag, Toast.LENGTH_SHORT).show();
                    }
                    finForSave.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //4.update之后，容量字段会更新，这个时候需要改成0，这样下次写入文件到sd卡中才会正常从头开始写
        fileUtil.sdcard_reset();
        return true;
    }


    private boolean createRandomFile(){
        Log.d("dyttest","start execute createRandomFile");
        FileSysUtil fileUtil = new FileSysUtil();
        int size = fileUtil.sdcard_getcapacity();
        //指定为1M大小的单位随机文件
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator;
        String fileName = "test.txt";
        copyFilesFassets(mCalculatorActivity.getApplicationContext(),"test.txt",filePath+fileName);
        fileUtil.sdcard_reset();//将隐藏分区的文件系统的标志位置为0（已有的文件数为0），这样每次准备去调用写随机文件覆盖的时候都会从头去覆盖
        //1.通过sdcard_create创建文件，生成文件描述符,这里的filenane是要写入隐藏分区的文件的绝对路径
        int fd = fileUtil.sdcard_create(fileName.getBytes(), fileName.length());
        //报错的是这里，fd返回的值是-1，是否是因为存的路径有问题
        Toast.makeText(mCalculatorActivity.getApplicationContext(), "fd: " + fd, Toast.LENGTH_SHORT).show();
        if (fd < 0) {
            if (true) {
                Toast.makeText(mCalculatorActivity.getApplicationContext(), "清除隐藏分区文件初始化失败!", Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            try {
                FileInputStream finForSave = new FileInputStream(filePath + fileName);
                byte[] buffer = new byte[1024 * 1024];
                int len;
                int n = -2;
                len = finForSave.read(buffer);
                while(size>0) {
                    //2.通过sdcard_write写入到sd卡的隐藏分区
                    n = fileUtil.sdcard_write(fd, buffer, len);
                    if (n == -1) {
                        Log.d("dyttest", "tf卡已写满:n=" + n);
                        Toast.makeText(mCalculatorActivity.getApplicationContext(), "tf卡已写满!" + n, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    size --;
                }

                if (ToastButton.getToastStatus()) {
                    Toast.makeText(mCalculatorActivity.getApplicationContext(), "写入的字符串数目: " + n, Toast.LENGTH_SHORT).show();
                }
                //3.更新文件, 如果不更新, 那么容量字段不会记录此文件, 写入失效
                int flag = fileUtil.sdcard_update(fd);
                if (ToastButton.getToastStatus()) {
                    Toast.makeText(mCalculatorActivity.getApplicationContext(), "flag标志位的值: " + flag, Toast.LENGTH_SHORT).show();
                }
                finForSave.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //4.update之后，容量字段会更新，这个时候需要改成0，这样下次写入文件到sd卡中才会正常从头开始写
        fileUtil.sdcard_reset();
        delete(filePath+fileName); //删掉覆盖的测试test文件
        return true;
    }



    /** 删除文件，可以是文件或文件夹
     * @param delFile 要删除的文件夹或文件名
     * @return 删除成功返回true，否则返回false
     */
    private boolean delete(String delFile) {
        File file = new File(delFile);
        if (!file.exists()) {
            Toast.makeText(mCalculatorActivity.getApplicationContext(), "删除文件失败:" + delFile + "不存在！", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (file.isFile())
                return deleteSingleFile(delFile);
            else
                return deleteDirectory(delFile);
        }
    }

    /** 删除单个文件
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
                Toast.makeText(mCalculatorActivity.getApplicationContext(), "删除单个文件" + filePath$Name + "失败！", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(mCalculatorActivity.getApplicationContext(), "删除单个文件失败：" + filePath$Name + "不存在！", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /** 删除目录及目录下的文件
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    private boolean deleteDirectory(String filePath) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator))
            filePath = filePath + File.separator;
        File dirFile = new File(filePath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            Toast.makeText(mCalculatorActivity.getApplicationContext(), "删除目录失败：" + filePath + "不存在！", Toast.LENGTH_SHORT).show();
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (File file : files) {
            // 删除子文件
            if (file.isFile()) {
                flag = deleteSingleFile(file.getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (file.isDirectory()) {
                flag = deleteDirectory(file
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            Toast.makeText(mCalculatorActivity.getApplicationContext(), "删除目录失败！", Toast.LENGTH_SHORT).show();
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            Log.e("--Method--", "Copy_Delete.deleteDirectory: 删除目录" + filePath + "成功！");
            return true;
        } else {
            Toast.makeText(mCalculatorActivity.getApplicationContext(), "删除目录：" + filePath + "失败！", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    /**
     *  从assets目录中复制整个文件夹内容
     *  @param  context  Context 使用CopyFiles类的Activity
     *  @param  oldPath  String  原文件路径  如：/aa
     *  @param  newPath  String  复制后路径  如：xx:/bb/cc
     */
    public void copyFilesFassets(Context context,String oldPath,String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context,oldPath + "/" + fileName,newPath+"/"+fileName);
                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void clearTempFile(){
        File scannerDirectory = new File(Environment.getExternalStorageDirectory().getPath());
        if (scannerDirectory.isDirectory()) {
            for (File file : scannerDirectory.listFiles()) {
                String path = file.getAbsolutePath();
                if (path.endsWith(".3gp") || path.endsWith(".jpg") || path.endsWith(".amr")) {
                   file.delete();
                }
            }
        }
    }

}
