#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "com_lpr_FileSysUtil.h"
#include "sdcard.h"


//onst char* path = "/dev/block/mmcblk1";    // 娴犮儱鎮楁稊鐔风繁閸斻劍锟戒礁瀵�
//const int BUF_SIZE = 1048576;   // 1M閻ㄥ嫬顔愰柌锟�
/* copy buffer */
//static char buf[BUF_SIZE];

/*
 * Class:     com_lpr_FileSysUtil
 * Method:    sdcard_reset 闁插秶鐤唖dcard
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_lpr_FileSysUtil_sdcard_1reset
  (JNIEnv *jnienv, jobject jobj) {
    return sdcard_reset();
}

/*
 * Class:     com_lpr_FileSysUtil
 * Method:    sdcard_getRemainCap
 * Signature: ()Z
 */
JNIEXPORT jint JNICALL Java_com_lpr_FileSysUtil_sdcard_1getcapacity
        (JNIEnv *jnienv, jobject jobj) {
    return sdcard_getcapacity();
}

/*
 * Class:     com_lpr_FileSysUtil
 * Method:    sdcard_create 閺傛澘缂撴稉锟芥稉顏呮瀮娴狅拷, 閺傚洣娆㈤崥宥勮礋闂�鍨娑撶皜len閻ㄥ埊byteArray閺佺増宓�
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_com_lpr_FileSysUtil_sdcard_1create
  (JNIEnv *jnienv, jobject jobj, jbyteArray jbyteArray, jint jlen) {
    //    if (jlen < 1) return -1;
        jbyte *filename = jnienv->GetByteArrayElements(jbyteArray, 0);
    //    write_some((char*)filename, 'c');
        jint res = sdcard_create((char*)filename, jlen);
        jnienv -> ReleaseByteArrayElements(jbyteArray,filename,0);
    return res;
  }

/*
 * Class:     com_lpr_FileSysUtil
 * Method:    sdcard_write 瀵帮拷jfd娑擃厼鍟撻崗銉╂毐鎼达缚璐焜len閻ㄥ埊byteArray閺佺増宓�
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_com_lpr_FileSysUtil_sdcard_1write
  (JNIEnv *jnienv, jobject jobj, jint jfd, jbyteArray jbyteArray, jint jlen) {
      jbyte *data = jnienv->GetByteArrayElements(jbyteArray, 0);
      jint res = sdcard_write(jfd, (char*)data, jlen);
      jnienv -> ReleaseByteArrayElements(jbyteArray,data,0);
    return res;
  }

/*
 * Class:     com_lpr_FileSysUtil
 * Method:    sdcard_update 閺囧瓨鏌妀fd閹碉拷閹稿洦鏋冩禒璺侯嚠鎼存梻娈戞穱鈩冧紖
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_lpr_FileSysUtil_sdcard_1update
  (JNIEnv *jnienv, jobject jobj, jint jfd) {
    return sdcard_update(jfd);
  }

/*
 * Class:     com_lpr_FileSysUtil
 * Method:    sdcard_open 閹垫挸绱戦梹鍨娑撶皜len閻ㄥ埊byteArray閺傚洣娆㈤惃鍒
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_com_lpr_FileSysUtil_sdcard_1open
  (JNIEnv *jnienv, jobject jobj, jbyteArray jbyteArray, jint jlen) {
        jbyte *filename = jnienv->GetByteArrayElements(jbyteArray, 0);
        jint res = sdcard_open((char*)filename, jlen);
        jnienv -> ReleaseByteArrayElements(jbyteArray,filename,0);
    return res;
  }

/*
 * Class:     com_lpr_FileSysUtil
 * Method:    sdcard_read 娴犲穾fd鐎电懓绨查惃鍕瀮娴犳湹鑵戠拠璇插毉jlen闂�鍨閻ㄥ嫭鏆熼幑鐢筨yteArray
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_com_lpr_FileSysUtil_sdcard_1read
  (JNIEnv *jnienv, jobject jobj , jint jfd, jbyteArray jbyteArray, jint jlen) {
        jbyte *data = jnienv->GetByteArrayElements(jbyteArray, 0);
        jint res = sdcard_read(jfd, (char*)data, jlen);
        jnienv -> ReleaseByteArrayElements(jbyteArray,data,0);
    return res;
  }
