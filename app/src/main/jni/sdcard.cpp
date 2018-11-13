#include "sdcard.h"
#include <string.h>
#include <fcntl.h>
#include <stdio.h>
#include <limits.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include<errno.h>
//#include "log.h"
/**
 * log.h doesnot work
 * so,include and define android/log here.
 * add by liyang 2018.4.23
 */
#include<android/log.h>
#define  LOG_TAG    "HelloJni"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)   __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
/************************* helper function **************************/
static int sdcard_lseek(int fd, LL offset);   // 灏嗘枃浠秄d鍋忕Щ鍒皁ffset浣嶇疆, 鍙敮鎸丼EEK_SET鍋忕Щ
static void fill_buf(const void* src, void* dest, int offset, int len);  // 浠巇est鍋忕Щoffset澶勮鍙杔en涓瓧绗�
static unsigned cal_buf_val(BYTE buf[], int ed, int st);    // 璁＄畻buf浠巈d鍒皊t鐨勫瓧娈垫寚鍚戠殑鏁村舰鏁扮殑澶у皬: TODO 杩斿洖unsigned鏄惁灏忎簡
static bool isValidFilename(const BYTE timename[], int len);  // 鍒ゆ柇timename鏄惁鍚堟硶, 鍗冲瓨鍦ㄦ枃浠朵腑

static bool set_vol(int fd, const vol_info& vol_ini);  // 鏇存柊fd鐨勫閲忓瓧娈�
static bool sdcard_set_vol(vol_info &vi);   // 璋冪敤set_vol鏇存柊瀹归噺瀛楁

static bool get_vol(int fd, struct vol_info &vi);   // 鑾峰緱fd绯荤粺鐨勫閲忓瓧娈�
static bool sdcard_get_vol(struct vol_info &vi);   //鑾峰緱


static bool get_file(int fd, file_info &fi, int sdfd);  // 鑾峰緱fd绯荤粺鐨剆dfd鎸囧悜鐨勬枃浠跺瓧娈�
static bool sdcard_get_file(file_info &fi, int sdfd);  // 鑾峰彇sdfd鏂囦欢瀛楁

static bool set_file(int fd, file_info &fi, int file_cnt); // 璁剧疆fd绯荤粺鐨刦ile_cnt鏂囦欢瀛楁
static bool sdcard_set_file(file_info &fi, int file_cnt); // 璁剧疆file_cnt鏂囦欢瀛楁淇℃伅

/************************* 缁熻鏂囦欢褰撳墠鍋忕Щ *************************/
static LL BYTE_WRITE_CNT;     /* 鍐欏叆鐨勬暟鎹暟閲� */
static LL BYTE_READ_CNT; /* 璇诲嚭鐨勬暟鎹暟閲� */

bool sdcard_reset() {

    vol_info vol_ini(0, 0);     // 璁剧疆瀹归噺瀛楁鐨勫瓨鍌ㄦ枃浠朵釜鏁板拰宸茬敤鏂囦欢闀垮害涓�0
    LOGI("enter the function named sdcard_reset %d",8888);
    bool ret = sdcard_set_vol(vol_ini);
    LOGI("get the ret");
    return ret==0?false:true;
}

int sdcard_getcapacity() {
    LOGI("test JNI in sdcard_get_remain_cap");
    /* 鑾峰緱瀹归噺鐨勫瓧娈电殑鏂囦欢鏁伴噺鍜屽彲鐢ㄦ墖鍖鸿捣濮嬪湴鍧� */
    vol_info vi(0, 0);
    bool rc = sdcard_get_vol(vi);
    if (!rc) return -1;

    int sector_cnt = vi.sector_cnt;
    return sector_cnt/2048;
}


int sdcard_create(const BYTE timename[], int len) {
    LOGI("test JNI in sdcard_create");

	//printf("sdcard_create start\n");

   if (!isValidFilename(timename, len)) return -1;   // 鍒ゆ柇timename鏄惁鍙敤,鍚嶅瓧鏈娇鐢�, 涓旈暱搴﹀悎娉�

   /* 鑾峰緱瀹归噺鐨勫瓧娈电殑鏂囦欢鏁伴噺鍜屽彲鐢ㄦ墖鍖鸿捣濮嬪湴鍧� */
   vol_info vi(0, 0);
   bool rc = sdcard_get_vol(vi);
   if (!rc) return -2;

   int file_cnt = vi.file_cnt;
   int sector_cnt = vi.sector_cnt;

   /* 鍒濆鍖杅ile_cnt鍐冲畾鐨剆dcard_file_header閮ㄥ垎, 骞跺垵濮嬪寲sector_cnt淇℃伅, 渚泂dcard璇诲啓鏃朵娇鐢� */
   BYTE filename[FILE_OFFSET::FILENAME_LEN];
   BYTE time[FILE_OFFSET::TIME_LEN];

   memset(filename, 0, FILE_OFFSET::FILENAME_LEN);
   memset(time, 0, FILE_OFFSET::TIME_LEN);
   memcpy(time, timename, len);     // 淇濊瘉瀛楃涓蹭互0缁撳熬

   file_info fi(filename, time, sector_cnt);
   rc = sdcard_set_file(fi, file_cnt);    // 鍒濆鍖杅ile info

   if (!rc) return -3;

   BYTE_WRITE_CNT= 0;

   //printf("sdcard_create end\n");
   return file_cnt;     // the (file_cnt + 1)th file is sdfd
}

int sdcard_write(int sdfd, BYTE data[], int len) {

	//printf("sdcard_write 3 para start\n");

    // 鑾峰彇瀹归噺瀛楁鍜屾枃浠跺瓧娈电殑鐩稿叧淇℃伅
    file_info fi;
    vol_info vi(0, 0);
    bool ret = sdcard_get_file(fi, sdfd);
    if (!ret) return -1;

    ret = sdcard_get_vol(vi);
    if (!ret) return -1;

    // 娉�: 鎵囧尯杞崲涓哄瓧鑺傛椂鍙兘鍙戠敓婧㈠嚭, 鎵�浠ラ渶瑕佸皢SECTOR_SIZE閮借缃负LL, 鍋忕Щ:(鏁版嵁瀛楁璧峰鎵囧尯 + SDCARD宸插啓瀛楁) * 512 + 宸插啓瀛楄妭鏁�
    LL offset = (SDCARD_DATA_ADDR + vi.sector_cnt) * SECTOR_SIZE + BYTE_WRITE_CNT;     //

    // 淇濊瘉鍓╀綑鎵囧尯鏁�(vi.sector_len - vi.sector_cnt) > 姝ゆ枃浠朵娇鐢ㄦ�绘墖鍖烘暟(BYTE_WRITE_CNT + len) / SECTOR_SIZE
    if ( (BYTE_WRITE_CNT + len) / SECTOR_SIZE > (vi.sector_len - vi.sector_cnt - 100)) return -1;    // 闃叉婧㈠嚭

    int fd = open(sdcard_path, O_RDWR);
    int rc = sdcard_lseek(fd, offset);
    if (rc == -1) return -1;

    // 2. 鍐欏叆data鏁扮粍浠ュ強鏇存柊len鍊�
    int n = write(fd, data, len);
    if (n != len) {
        close(fd);
        return -1;
    }

    BYTE_WRITE_CNT += len;
    close(fd);
	//printf("sdcard_write 3 para end\n");

    return len;
}
int sdcard_write(int sdfd, const BYTE filename[]) {//鍏ㄨ矾寰�

     
	//printf("sdcard_write 2 para start\n");

    char buffer[1024];
    int n = 0;
    int m = 0;
    int rfd = open(filename, O_RDONLY);


    // 2. 鍐欏叆data鏁扮粍浠ュ強鏇存柊len鍊�
    while(n = read(rfd, buffer, 1024))
    {
        m = sdcard_write(sdfd, buffer, n);
        if (m == -1) {
            close(rfd);
            return -1;
        }

    }
    close(rfd);
	 
	//printf("sdcard_write 2 para end\n");

    return 1;
}

/*
 * 鏇存柊澶存枃浠朵腑鐩稿叧淇℃伅, 璇存槑鍐欏叆鎴愬姛; 濡傛灉涓嶈皟鐢ㄦ鏂规硶, 鍒欏啓鍏ュけ璐�, sector_cnt鍜宖ile_cnt閮戒笉浼氭洿鏂�
 */
int sdcard_update(int sdfd) {

	//printf("sdcard_update start\n");

   vol_info vi(0, 0);
   bool ret = sdcard_get_vol(vi);
   if (!ret) return -1;

   file_info fi;
   ret = sdcard_get_file(fi, sdfd);
   if (!ret) return -1;

   // 鏇存柊sdfd鏂囦欢鐨勮捣濮嬫墖鍖�, 宸插啓鎵囧尯闀垮害浠ュ強瀛楄妭鏁�
   fi.sector_st = vi.sector_cnt;
   fi.sector_len = BYTE_WRITE_CNT / SECTOR_SIZE + 1;
   fi.file_size = BYTE_WRITE_CNT;

   // 鏇存柊瀹归噺瀛楁鐨勬枃浠舵暟鍜屼娇鐢ㄦ墖鍖烘暟
   vi.sector_cnt += fi.sector_len;
   vi.file_cnt++;

   ret = sdcard_set_vol(vi);
   if (!ret) return -1;

   ret = sdcard_set_file(fi, sdfd);
   if (!ret) return -1;
   BYTE_WRITE_CNT = 0;    /* BYTE_WRITE_CNT鍋忕Щ缃�0  */

   //printf("sdcard_update end\n");
   return 0;
}


int sdcard_open(const BYTE *timename, int len) {

	//printf("sdcard_open start\n");

    BYTE_READ_CNT = 0;

    if (len > FILE_OFFSET::TIME_LEN) return -1;     // 鏂囦欢鍚嶈繃闀挎椂, 鐩存帴open澶辫触, 杩斿洖-1

    // 1. 璇诲彇vol_info涓殑鏂囦欢涓暟
    vol_info vi(0, 0);
    bool ret = sdcard_get_vol(vi);
    if (!ret) return -1;
    int file_cnt = vi.file_cnt;

    // 2. 閫愪釜閬嶅巻鏂囦欢鍚嶅苟涓旀瘮杈�, 閬囧埌0鍒欑粨鏉熻鍙�
    file_info fi;
    int sdfd = -1;
    for (int i=0; i<file_cnt; i++) {
        ret = sdcard_get_file(fi, i);
        if (!ret) return -1;
        if (strncmp(timename, fi.format_time, len) == 0) {
            sdfd = i;
            break;
        }
    }


    // 3. 杩斿洖鐩稿簲鏂囦欢鐨剆dfd(鍗砯d椤哄簭), 鍚﹀垯杩斿洖-1.
    if (sdfd == -1)
        return -1;
    else
        return sdfd;
}

int sdcard_read(int sdfd, BYTE data[], int len) {

	//printf("sdcard_read 3 para start\n");
    // 浠巗dfd鎸囧悜鐨勭浉搴斾綅缃笂璇诲彇鏂囦欢淇℃伅骞惰繑鍥�
    vol_info vi(0, 0);
    bool rc = sdcard_get_vol(vi);
    if (!rc) return -1;
    if (unsigned(sdfd) >= vi.file_cnt) return -1;     // 鍒ゆ柇sdfd鏄惁鏈夋晥

    // 鑾峰彇sdfd鏂囦欢瀛楁
    file_info fi;
    rc = sdcard_get_file(fi,sdfd);
    if (!rc) return -1;

    // 鍒ゆ柇鏂囦欢鏄惁璇诲畬, 璇诲畬杩斿洖0, 鍚﹀垯缁х画璇�
    LL file_len = fi.file_size;
    if (BYTE_READ_CNT == file_len)   return 0;

    // 鍋忕Щ鍒扮浉搴斾綅缃�
    int fd = open(sdcard_path, O_RDWR);
    LL offset = (SDCARD_DATA_ADDR + fi.sector_st) * SECTOR_SIZE + BYTE_READ_CNT;
    int lrc = sdcard_lseek(fd, offset);
    if (lrc == -1) return -1;

    // 璇诲彇<=len鐨勯暱搴︽暟鎹�
    int ret = -1;
    if (BYTE_READ_CNT + len <= file_len) {
        int n = read(fd, data, len);
        if (n != len) {
            ret = -1;
        } else {
            ret = n;
        }
    } else {
        int n = read(fd, data, file_len - BYTE_READ_CNT);
        if (n != file_len - BYTE_READ_CNT) {
           ret = -1;
        } else {
           ret = n;
        }
    }

    close(fd);

    // 鏇存柊璇诲彇鐨勬暟鎹�
    if (ret != -1) {
        BYTE_READ_CNT += ret;
    }
    return ret;
}

/**********************Added by hsr 2017.4.10**************************/
int sdcard_read(int sdfd, const BYTE filename[]) {//鍏ㄨ矾寰�

    //printf("sdcard_read 2 para start\n");

    char buffer[1024];
    int n = 0;
    int m = 0;
    int rfd = open(filename, O_WRONLY | O_CREAT);

    // 
    while(n == sdcard_read(sdfd, buffer, 1024))
    {
        m = write(rfd, buffer, n);
        if (m == -1) {
            close(rfd);
            return -1;
        }

    }
    close(rfd);

    return 1;
}

/**********************Added by hsr 2017.4.17**************************/
int sdcard_read_all(int sdfd,const BYTE filepathDir[]){
	// 浠巗dfd鎸囧悜鐨勭浉搴斾綅缃笂璇诲彇澶氫釜鏂囦欢淇℃伅骞惰繑鍥�
    vol_info vi(0, 0);
    bool rc = sdcard_get_vol(vi);
    if (!rc) return -1;
    if (unsigned(sdfd) >= vi.file_cnt) return -1;     // 鍒ゆ柇sdfd鏄惁鏈夋晥

	int val=0;
	const BYTE* filename;
    BYTE* fileName;
	BYTE* filePath = (BYTE*)filepathDir;
	const BYTE* comp_filename;

	for(int i=sdfd; i<vi.file_cnt; i++){
		file_info fi;
	    bool ret = sdcard_get_file(fi, i);
	    if (!ret) return -1;

		filename = fi.format_time; 
		fileName = (BYTE*)filename;
		BYTE filepath[9999];
	    strcpy(filepath,filePath);
		comp_filename = strcat(filepath,fileName);

		int fd = sdcard_open((char*)fileName,strlen((char*)fileName));
		int k=sdcard_read(fd,comp_filename);
		if (k > 0)
		{
			val = i+1;
		}
	}
	return val;
}


 

/****************** test function **************************/
bool test_get_vol_info(struct vol_info &vi) {
    int ret = sdcard_get_vol(vi);
    return ret;
}

/***************** helper function *************************/
// 浠嶴EEK_SET寮�濮嬪亸绉�
// 娉�: 涓嶈兘閫氳繃鍒ゆ柇lseek == -1鏉ュ垽鏂亸绉讳笌鍚�, 鐢变簬閮ㄥ垎绯荤粺鏄痮ff_t鏄�32浣嶅疄鐜�, 鎵�鏈変繚瀛樿繑鍥炵殑LL鍋忕Щ. if ((n=lseek()) == -1) return -1鏄粷瀵圭姝㈢殑
static int sdcard_lseek(int fd, LL offset) {
    if (offset < 0) return false;
    lseek(fd, 0, SEEK_SET);

    while (offset > 0) {
//    printf("offset=%llu\n", offset);
        if (offset > MAX_OFFSET) {
            lseek(fd, MAX_OFFSET, SEEK_CUR);
            offset -= MAX_OFFSET;
        } else {
            lseek(fd, (int)offset, SEEK_CUR);
            break;
        }
    }

    return 0;
}

static void fill_buf(const void* src, void* dest, int offset, int len) {
    BYTE* buf = (BYTE*)dest;
    const BYTE* raw = (const BYTE*)src;

    for (int i=0; i<len; i++) {
        buf[i] = raw[i+offset] ^ MASK;
    }
}


/**
 * 鍒濆鍖杝dcard瀹归噺瀛楁
 * 鍓�12涓瓧鑺備负鏂囦欢鎬绘暟(), 璧峰鎵囧尯, 鎵囧尯鎬绘暟, 鍚�1012涓瓧鑺備负0
 */
static bool set_vol(int fd, const vol_info& vol_ini) {
    BYTE buf[SDCARD_VOL_LEN * SECTOR_SIZE];

    // 鍐欏叆max_fileno
    int offset = 0;
    fill_buf((const void*)(&vol_ini), buf, offset, SDCARD_VOL_LEN * SECTOR_SIZE);

  //  printf("sector_cnt=%d, file_cnt=%d\n", cal_buf_val(buf, VOL_OFFSET::SECTOR_CNT, VOL_OFFSET::SECTOR_CNT + VOL_OFFSET::SECTOR_CNT_LEN -1), cal_buf_val(buf, VOL_OFFSET::FILE_CNT_ST, VOL_OFFSET::FILE_CNT_ST+VOL_OFFSET::FILE_CNT_LEN - 1));

    LL vol_offset = SDCARD_VOL_ADDR * SECTOR_SIZE;

//    printf("vol_addr = %d\n vol_offset=%llu\n", SDCARD_VOL_ADDR, vol_offset);
    int rc = sdcard_lseek(fd, vol_offset);
    if (rc == -1) return false;

//    printf("start write\n");

    rc = write(fd, buf, SDCARD_VOL_LEN * SECTOR_SIZE);
    if (rc == -1) return false;

//    printf("start read\n");
//   /* start test */
//    sdcard_lseek(fd, vol_offset);
//    rc = read(fd, buf, SDCARD_VOL_LEN * SECTOR_SIZE);
//    int cnt = 0;
//    for (int i=0; i<SDCARD_VOL_LEN * SECTOR_SIZE; i++) {
//        if (cnt % 8 == 0)
//            printf("\n");
//        printf("%d ", buf[i]);
//        cnt++;
//    }

   /* finish test */

    return true;
}

static bool sdcard_set_vol(vol_info &vi) {
    int fd = open(sdcard_path, O_RDWR); // do not truncate data
    LOGD("The fd is %d", fd);
    LOGD("The error is %d", errno);
    if (fd == -1) return false;

    bool ret = set_vol(fd, vi);
    close(fd);

    return ret;
}

// 璇诲彇瀹归噺瀛楁鏁版嵁, 浣跨敤buffer to type鐨勫舰寮�
static bool get_vol(int fd, struct vol_info &vi) {
    LL vol_offset = SDCARD_VOL_ADDR * SECTOR_SIZE;
    int rc = sdcard_lseek(fd, vol_offset);
    if (rc == -1) return false;

    BYTE buf[SDCARD_VOL_LEN * SECTOR_SIZE];
    rc = read(fd, buf, SDCARD_VOL_LEN * SECTOR_SIZE);
    if (rc != SDCARD_VOL_LEN * SECTOR_SIZE) return false;

    for (int i = 0; i < sizeof(buf) / sizeof(BYTE); ++i) {
        buf[i] = buf[i] ^ MASK;
    }

    // 鍒濆鍖杤ol_info涓殑鍚庝袱涓瓧娈�
    vi.sector_cnt = cal_buf_val(buf, VOL_OFFSET::SECTOR_CNT + VOL_OFFSET::SECTOR_CNT_LEN - 1, VOL_OFFSET::SECTOR_CNT);
    vi.file_cnt = cal_buf_val(buf, VOL_OFFSET::FILE_CNT_ST + VOL_OFFSET::FILE_CNT_LEN - 1, VOL_OFFSET::FILE_CNT_ST);

    return true;
}

static bool sdcard_get_vol(struct vol_info &vi) {

    int fd = open(sdcard_path, O_RDWR); // do not truncate data

    if (fd == -1) return false;

    bool ret = get_vol(fd, vi);
    close(fd);
    return ret;
}

static unsigned cal_buf_val(BYTE buf[], int ed, int st) {
    unsigned sum = 0;
    for (int i=ed; i>=st; i--) {
        sum = sum * 256 + buf[i];
    }

    return sum;
}

static bool set_file(int fd, file_info &fi, int file_cnt) {

    const int buf_size = SDCARD_FILE_LEN;
    BYTE buf[buf_size];
    fill_buf((const void*)(&fi), buf, 0, buf_size);


    LL file_offset = SDCARD_FILE_ADDR * SECTOR_SIZE + file_cnt * SDCARD_FILE_LEN;
    int rc = sdcard_lseek(fd, file_offset);
    if (rc == -1) return false;

    rc = write(fd, buf, buf_size);
    if (rc == -1) return false;

    return true;
}

static bool sdcard_set_file(file_info &fi, int file_cnt) {

    int fd = open(sdcard_path, O_RDWR); // do not truncate data
    if (fd == -1) return false;

    bool rc = set_file(fd, fi, file_cnt);
    if (!rc) {
        close(fd);
        return false;
    }

    return true;
}

static bool get_file(int fd, file_info &fi, int sdfd) {
	//printf("get_file start\n");
    LL file_offset = SDCARD_FILE_ADDR * SECTOR_SIZE + sdfd * SDCARD_FILE_LEN;
    int rc = sdcard_lseek(fd, file_offset);
    if (rc == -1) return false;

	int n = read(fd, (void*)(&fi), SDCARD_FILE_LEN);

    if (n != SDCARD_FILE_LEN) return false;
    BYTE *p = (BYTE*)&fi;
    for (int i = 0; i < sizeof(fi); ++i) {
        p[i] = p[i] ^ MASK;
    }

	return true;
}

static bool sdcard_get_file(file_info &fi, int sdfd) {
    int fd = open(sdcard_path, O_RDWR); // do not truncate data

    if (fd == -1) return false;
	
    bool ret = get_file(fd, fi, sdfd);
    close(fd);
    return ret;

}

static bool isValidFilename(const BYTE timename[], int len) {

    if (len > FILE_OFFSET::TIME_LEN - 1)  return false;     // 鏂囦欢鍚嶈繃闀挎椂, 鐩存帴open澶辫触, 杩斿洖-1

	// 1. 璇诲彇vol_info涓殑鏂囦欢涓暟
    vol_info vi(0, 0);
    bool ret = sdcard_get_vol(vi);
    if (!ret) return false;

    int file_cnt = vi.file_cnt;


    // 2. 閫愪釜閬嶅巻鏂囦欢鍚嶅苟涓旀瘮杈�, 閬囧埌0鍒欑粨鏉熻鍙�
    file_info fi;
    for (int i=0; i<file_cnt; i++) {
        ret = sdcard_get_file(fi, i);
        if (strncmp(timename, fi.format_time, len) == 0) {
            return false;
        }
    }

    return true;
}
