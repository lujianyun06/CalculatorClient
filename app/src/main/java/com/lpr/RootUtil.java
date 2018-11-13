package com.lpr;

import java.io.DataOutputStream;
import java.io.IOException;

public class RootUtil {
	/**
	 * 关闭安卓安全策略，获取权限
	 */
	public static void getRoot() {
		try {
			Process proc = Runtime.getRuntime().exec("su");
			String f1 = "setenforce 0\n";
			DataOutputStream os = new DataOutputStream(proc.getOutputStream());
			os.write(f1.getBytes());
		}catch(Exception e){
		}
	}
	
	/**
	 * 打开安卓安全策略，关闭权限
	 */
	public static void removeRoot() {
		try {
			Process proc = Runtime.getRuntime().exec("su");
			String f1 = "setenforce 1\n";
			DataOutputStream os = new DataOutputStream(proc.getOutputStream());
			os.write(f1.getBytes());
		}catch(Exception e){
		}
	}

	/**
	 * 淇敼sdcard鐨勬潈闄� 涓�棪sdcard鎷斿嚭, 灏遍渶瑕佹墽琛屾鎿嶄綔
	 * @return
	 */
	public static boolean chmod() {
		Process process = null;
		DataOutputStream os = null;
		String cmd = "chmod 777 /dev/block/mmcblk1;chmod 777 /dev/block/mmcblk1p1";
		try {
			//成功获取su_root权限
			process = Runtime.getRuntime().exec("su");
			//process.getOutputStream()得到连接到子进程的正常输入输出流。
            //DataOutputStram 基于字节流 FileWrite 基于字符流
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");    //写入到文件中
			os.writeBytes("exit\n");
			os.flush();     //刷空输出流，并输出所有被缓存的字节，
			//处理的返回值  0表示退出
			process.waitFor();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		return true;
	}
	public static boolean uninstall(String packagename) {
		Process process = null;
		DataOutputStream os = null;
		String cmd = "pm uninstall "+packagename;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		return true;
	}
}
