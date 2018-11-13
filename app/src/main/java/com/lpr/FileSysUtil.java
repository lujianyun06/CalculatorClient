package com.lpr;

import java.io.DataOutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


public class FileSysUtil {
//	public FileSysUtil() {
//		RootUtil.getRoot();
//	}
	   /**
	    * 功能: 重置文件系统. 清空SD卡容量字段, 将已写文件数和已写扇区数清零.
	    * @return true : 重置成功; false : 重置失败
	    */
	   public native boolean sdcard_reset();

		/**
		 * 功能: 获取SD卡剩余容量，单位MB.
		 * @return  正整数: 获取成功; 负数 : 获取失败
		 */
		public native int sdcard_getcapacity();
	   
	   /**
	    * 功能: 创建文件, 并返回文件描述符	
	    * @param filepath: 文件名, 长度不大于32
	    * @param len: 文件名长度, 不包括'\0'
	    * @return >=0 : 打开文件成功;	-1:打开文件失败, 可能是系统出错或者文件已存在;
	    */
	   public native int sdcard_create(byte[] filepath, int len);
	   
	   /**
	    * 功能:	往fd中写入长度为len的data数据;
	    * @param fd: 合法的文件描述符(fd >= 0)
	    * @param data: 字节数据
	    * @param len: 数据长度, (0< len < 1M)
	    * @return -1: 写入失败, 可能fd不存在后者len超过1M上限; >0 表示写入成功
	    */
	   public native int sdcard_write(int fd, byte[] data, int len);
	   
	   /**
	    * 功能: 更新文件, 如果不更新, 那么容量字段不会记录此文件, 等价于写入失效.
	    * @param fd: 合法的文件描述符(fd >= 0)
	    * @return  0: 表示成功; -1: 关闭文件失败, fd不存在或者系统错误;
	    */
	   public native int sdcard_update(int fd);
	   
	   
	   /********************  后两个接口是读取文件接口, 移动端可能暂时使用不到   ********************/
	   
	   /**
	    * 功能: 打开文件, 获得相应文件句柄
	    * @param filepath: 文件名, 长度不大于32
	    * @param len: 文件名长度, 不包括'\0'
	    * @return >=0 : 打开文件成功;	-1:打开文件失败, 可能是系统出错或者文件已存在;
	    */
	   public native int sdcard_open(byte[] filepath, int len);
	   
	   /**
	    * 功能: 从fd中读出数据, 将数据放在长度为len的data数据中.
	    * @param fd: 合法的文件描述符(fd >= 0)
	    * @param data: buffer
	    * @param len: 数据长度 建议len长度设置为1M.
	    * @return >0: 表示读出成功, 返回读到的数据长度. 0: 表示读取文件结束; 
	    * -1: 表示系统出错, fd不存在或者系统错误
	    */
	   public native int sdcard_read(int fd, byte[] data, int len);
	   
	   static {
	        System.loadLibrary("filesys");
	    }
    
}