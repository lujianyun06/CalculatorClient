package com.harlan.calculator2;

public class ToastButton {
	private static boolean isToastShow = true;
	
	public static void changeToastShow() {
		isToastShow = true;
	}
	
	public static void changeToastShowOff() {
		isToastShow = false;
	}
	
	public static boolean getToastStatus() {
		return isToastShow;
	}
}
