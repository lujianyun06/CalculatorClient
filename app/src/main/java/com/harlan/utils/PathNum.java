package com.harlan.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.util.Log;

public class PathNum {
	private Activity activity = null;
	private CCgenerator mgenerator = null;
	private String firstContact = null;
	/*
	 * 记录已经写入的联系人姓名
	 * 当号码写入失败后，用于回滚
	 * */
	private ArrayList<String> storedName = null;
	
	public PathNum(Activity activity) {
		this.activity = activity;
		this.mgenerator = new CCgenerator();
	}
	
	/**
	 * convert pathname to contact,then store in sim card
	 * @param pathname
	 * @return name of first contact
	 */
	public String pathnameToNumbers(String pathname) {
		String prefix = "135";
		String firstContact = null;
		String phoneNumber = prefix + "";
		String contactName = null;
		storedName = new ArrayList<String>();
		Set<String> nameSet = new HashSet<String>();
		//System.out.println("*******************************");
		int length = pathname.length();
		
		firstContact = mgenerator.getName();
		contactName = firstContact;
		nameSet.add(contactName);
		
		for (int i = 0; i < length; i++) {
			
			int tmp = pathname.charAt(i);
			String num = "";
			if (tmp < 100)
				num = "0" + tmp;
			else 
				num = "" + tmp;	
			
			phoneNumber += num;
			
			if (i % 2 == 1) {//每两个字符，产生一个电话号码				
				if (i != length - 1) {
					phoneNumber += "00";//还将产生其他号码	
				}else {
					phoneNumber += "01";//最后一个号码
				}
				
				if (!storePhoneNumber(contactName, phoneNumber)){
					Log.v("fail", ">>>> " + "插入失败");	
					return "fill";//返回fill
				}
				
				contactName = mgenerator.getName();	//产生一个新的联系人姓名
				while(nameSet.contains(contactName)) {
					contactName = mgenerator.getName();
				}
				nameSet.add(contactName);
				System.out.println(contactName);
				phoneNumber = prefix + "";
			}else {
				if (i == length - 1) {//产生最后一个号码，剩余位用000填充
					phoneNumber += "00001";
					if (!storePhoneNumber(contactName, phoneNumber)){
						Log.v("fail", ">>>> " + "插入失败");	
						return "fill";//返回fill

					}
					contactName = mgenerator.getName();	//产生一个新的联系人姓名
					while(nameSet.contains(contactName)) {
						contactName = mgenerator.getName();
					}
					nameSet.add(contactName);
					System.out.println(contactName);
					phoneNumber = prefix + "";
				}
			}
		}
Log.v("firstContact", ">>>> " + firstContact);			
		return firstContact;
	}
	
	public String getPathname(String firstContact) {
		ArrayList<String> numbers = SimContact.getNumbers(this.activity, firstContact);
		if(numbers == null){
			return "Fail";
		}
		return numbersToPathname(numbers); 
	}
	
	
	String numbersToPathname(ArrayList<String> numbers) {
		String pathname = "";
		String number;
		int digit;
		char letter;
		
		for (int i = 0; i < numbers.size(); i++) {
			number = numbers.get(i);
			digit = Integer.valueOf(number.substring(3, 6));
			letter = (char)digit;
			pathname += letter;
			digit = Integer.valueOf(number.substring(6, 9));
			if (digit == 0)//填充位000
				return pathname;
			letter = (char)digit;
			pathname += letter;
			if (bedestroyed()) {
				return "Fail";
			}
		}
		return pathname;
	}
	
	
	boolean storePhoneNumber(String name, String number) {
Log.v("insert", ">>>> " + name + " " + number);	
		if (SimContact.insertContact(this.activity, name, number)) {
			storedName.add(name);
			return true;//插入成功
		}else {
			dealWithStoreFail();//删除已经写入的号码
			return false;//插入失败
		}
			
	}
	
	
	/*
	 * 检查数据是否被破坏
	 * 内部逻辑还未写
	 * */
	boolean bedestroyed() {
		return false;
	}
	
	/*
	 * 写入SIM卡失败后，进行处理
	 * 
	 * */
	private void dealWithStoreFail() {
		for (int i = 0; i < storedName.size(); i++) {
			String name = storedName.get(i);
			SimContact.deleteContact(activity, name);
		}
	}

}
