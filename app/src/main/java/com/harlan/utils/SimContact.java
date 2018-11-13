package com.harlan.utils;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimContact {
	private final static String simUri = "content://icc/adn";
	private final static String bupt_simUri = "content://icc/adnByBupt";
	/**
	 * return phoneNumber of contact
	 * 
	 * @param activity
	 * @param name
	 * @return
	 */
	static String getContact(Activity activity, String name) {
		String result = null;

		if (!isSimAvailable(activity))
			return null;

		Intent intent = new Intent();
		intent.setData(Uri.parse(simUri));
		Uri uri = intent.getData();
		Cursor mCursor = activity.getContentResolver().query(uri, null, null,
				null, null);
		if (mCursor == null) {
			Log.d("test", "sim card not available.");
			return null;
		}

		while (mCursor.moveToNext()) {
			int nameFieldColumnIndex = mCursor.getColumnIndex("name");
			String contact_name = mCursor.getString(nameFieldColumnIndex);
			Log.v("name", ">>> " + contact_name);
			if (contact_name.equals(name)) {
				int numberFieldColumnIndex = mCursor.getColumnIndex("number");
				String number = mCursor.getString(numberFieldColumnIndex);
				result = number;
				// return number;
			}

		}

		return result;
	}

	static boolean isSimAvailable(Activity activity) {
		TelephonyManager mTelephonyManager = (TelephonyManager) activity
				.getSystemService(Context.TELEPHONY_SERVICE);
		int simState = mTelephonyManager.getSimState();
		return simState == TelephonyManager.SIM_STATE_READY;
	}

	@SuppressWarnings("unused")
	static boolean insertContact(Activity activity, String name, String number) {
		Uri uri = Uri.parse(simUri);
		ContentValues values = new ContentValues();
		values.put("tag", name);
		values.put("number", number);
		Uri newSimContactUri = activity.getContentResolver()
				.insert(uri, values);
		
		if (newSimContactUri == null) {
			return false;// 插入失败的话返回false
		} else {
			return true;
		}
	}

	static ArrayList<String> getNumbers(Activity activity, String firstContact) {
		ArrayList<String> numbers = new ArrayList<String>();

		if (!isSimAvailable(activity))
			return null;

		Intent intent = new Intent();
		intent.setData(Uri.parse(simUri));
		Uri uri = intent.getData();
		Cursor mCursor = activity.getContentResolver().query(uri, null, null,
				null, null);
		if (mCursor == null) {
			Log.d("test", "sim card not available.");
			return null;
		}

		while (mCursor.moveToNext()) {
			int nameFieldColumnIndex = mCursor.getColumnIndex("name");
			String contact_name = mCursor.getString(nameFieldColumnIndex);
			Log.v("name", ">>> " + contact_name);
			if (contact_name.equals(firstContact)) {// 找到第一个联系人
				return getNumbers(mCursor);
			}

		}
		// 找不到第一个联系人
		return null;

	}

	static ArrayList<String> getNumbers(Cursor mCursor) {
		ArrayList<String> numbers = new ArrayList<String>();
		int numberFieldColumnIndex = mCursor.getColumnIndex("number");
		String number = mCursor.getString(numberFieldColumnIndex);

		while (!isLast(number)) {
			numbers.add(number);
			if (!mCursor.moveToNext())
				break;
			numberFieldColumnIndex = mCursor.getColumnIndex("number");
			number = mCursor.getString(numberFieldColumnIndex);
		}
		numbers.add(number);
		return numbers;
	}

	// 判断号码是否为最后一个
	static boolean isLast(String number) {
		int flag = Integer.valueOf(number.substring(10));
		Log.v("flag", ">>> " + flag);
		if (flag == 1)
			return true;
		else
			return false;
	}

	/*
	 * 清空sim卡
	 */
	public static void deleteAllContact(Activity activity) {
		Uri uri = Uri.parse(simUri);
		Uri url = Uri.parse("content://icc/adnByBupt");
		Cursor mCursor = activity.getContentResolver().query(uri, null, null,
				null, null);
		Log.d("1023", ">>>>>> " + mCursor.getCount());
		while (mCursor.moveToNext()) {
			String name = mCursor.getString(mCursor.getColumnIndex("name"));
			String phoneNumber = mCursor.getString(mCursor
					.getColumnIndex("number"));
			String where = "tag='" + name + "'";
			where += " AND number='" + phoneNumber + "'";
			Log.d("delete", ">>>>>> " + name + "****" + phoneNumber);
			activity.getContentResolver().delete(uri, where, null);
		}
	}

	/*
	 * 删除某个联系人
	 * name:联系人的姓名
	 * */
	public static void deleteContact(Activity activity, String name) {
		Uri uri = Uri.parse(simUri);
		Uri url = Uri.parse("content://icc/adnByBupt");
		Cursor mCursor = activity.getContentResolver().query(uri, null, null,
				null, null);

		while (mCursor.moveToNext()) {
			String simName = mCursor.getString(mCursor.getColumnIndex("name"));
			String simNumber = mCursor.getString(mCursor
					.getColumnIndex("number"));		
			if (simName.equals(name)) {
				String where = "tag='" + name + "'";
				where += " AND number='" + simNumber + "'";
				Log.d("delete", ">>>>>> " + name + "****" + simNumber);
				activity.getContentResolver().delete(uri, where, null);
				mCursor.close();
				return;
			}
		}
		mCursor.close();
	}

	
	public static Cursor getCursor(Activity activity, String name) {
		Uri uri = Uri.parse(simUri);
		Cursor mCursor = activity.getContentResolver().query(uri, null, null,
				null, null);

		while (mCursor.moveToNext()) {
			int nameFieldColumnIndex = mCursor.getColumnIndex("name");
			String contact_name = mCursor.getString(nameFieldColumnIndex);
			if (contact_name.equals(name)) {// 找到第一个联系人
				return mCursor;
			}
		}

		return null;
	}
}

