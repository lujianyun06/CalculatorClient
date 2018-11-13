#ifndef SDOP_H
#define SDOP_H

#include <string.h>
#include <limits.h>

//using namespace std;
/**
 * 璁惧鐨勫湴鍧�, mmcblk1涓烘墜鏈轰腑澶栫疆sdcard鐨勫瓨鍌ㄥ湴鍧�; sdb涓鸿鍗″櫒涓璼dcard鐨勫瓨鍌ㄥ湴鍧�; file涓烘祴璇曟枃浠跺湴鍧�
 */
//static const char* sdcard_path = "/home/qingfeng/develop/cworkspace/sdcard_api/filesys/data/test.file";
//static const char* sdcard_path = "/dev/sdb";
static const char* sdcard_path = "/dev/block/mmcblk1";
//android 7.0 replace the sdpath by /dev/block/vold/public:179_1;
//static const char* sdcard_path = "/dev/block/vold/public:179_1";
typedef char BYTE;
typedef long long LL;

static const LL SECTOR_SIZE = 512;              // 涓�涓墖鍖虹殑瀛楄妭鏁�, 鍚庨潰杩涜鍋忕Щ鏃堕渶瑕佺敤鍒版鍙傛暟, 浣跨敤SECTOR_SIZE瀹规槗閫犳垚int婧㈠嚭,鎵�浠ュ皢鍏惰缃负LL
static const int SDCARD_START_SECTOR = 8500000;  // SDCARD涓殣钘忔墖鍖虹殑璧峰鎵囧尯鏁�
static const int SDCARD_SECTOR_LEN = 7000000;    // SDCARD鐨勫彲鐢ㄦ墖鍖洪暱搴�, 榛樿涓�7000000涓囨墖鍖烘暟, 绾︿负3.5G宸﹀彸
static const int SDCARD_HEADER_LEN = 257 * 2;    // 澶存枃浠剁殑鍋忕Щ鎵囧尯鏁�, 鍏�514鎵囧尯. 鍗�8500514鎵囧尯鍗充负鏁版嵁娈佃捣濮嬪湴鍧�
static const int SDCARD_VOL_ADDR = SDCARD_START_SECTOR;      /* SDCARD鍗″閲忓瓧娈佃捣濮嬫墖鍖� */
static const int SDCARD_VOL_LEN = 1*2;    /* 瀹归噺瀛楁闀垮害: 2涓墖鍖� */
static const int SDCARD_FILE_ADDR = SDCARD_VOL_ADDR + SDCARD_VOL_LEN;    /* SDCARD鏂囦欢淇℃伅瀛楁璧峰鎵囧尯   */
static const int SDCARD_DATA_ADDR = SDCARD_START_SECTOR + SDCARD_HEADER_LEN;   //鏁版嵁鍖鸿捣濮嬫鍦板潃

static const int SDCARD_FILE_LEN = 256;   /* 姣忎釜鏂囦欢瀛楁闀垮害: 256瀛楄妭 */
static const int MAX_FILE_SIZE = 1024;    /* 鏈�澶ф枃浠舵暟, 鏂囦欢瀛楁榛樿瀛樺偍浜�1024涓枃浠� */ 


static const int MAX_OFFSET = INT_MAX;  //鏈�澶х殑lseek鍋忕Щ閲�  TODO 鏆傛椂涓嶇煡閬搊ff_t鐨勬渶澶у亸绉诲湴鍧�
static const BYTE MASK = 156;  // 鏈�澶х殑lseek鍋忕Щ閲�  TODO 鏆傛椂涓嶇煡閬搊ff_t鐨勬渶澶у亸绉诲湴鍧� 鐢ㄤ綔鍔犲瘑锛岄渶鍏堝垵濮嬪寲鎵嶈兘鐢�

/*  鍒濆鍖栨鍗�, 鍗抽噸缃畇dcard瀹归噺瀛楁 */
bool sdcard_reset();
int sdcard_getcapacity();

/* 鍒涘缓涓�涓枃浠�, 鍐欏叆鏁版嵁骞舵洿鏂版暟鎹� */
int sdcard_create(const BYTE *filepath, int len);
int sdcard_write(int sdfd, BYTE data[], int len);
int sdcard_write(int sdfd, const BYTE filename[]);
int sdcard_update(int sdfd);

/* 鎵撳紑鏂囦欢, 骞朵笖璇诲彇鏁版嵁 */
int sdcard_open(const BYTE *filepath, int len);
int sdcard_read(int sdfd, BYTE data[], int len);
int sdcard_read(int sdfd, const BYTE filename[]);

int sdcard_read_all(int sdfd,const BYTE filepathDir[]);


BYTE* sdcard_directory();   // TODO 鏈疄鐜�


/************ Helper Function: 鐢ㄤ簬璁剧疆鍜岃鍙栧悇涓瓧娈电殑淇℃伅 **************/

/* 瀹归噺瀛楁鎻忚堪: 浠yte涓哄熀鏈亸绉诲崟浣� */
struct VOL_OFFSET {
    static const int FILE_VOL_ST = 0;
    static const int FILE_VOL_LEN = 4;
    static const int SECTOR_ST = FILE_VOL_ST + FILE_VOL_LEN;
    static const int SECTOR_ST_LEN = 4;
    static const int SECTOR_LEN = SECTOR_ST + SECTOR_ST_LEN;
    static const int SECTOR_LEN_LEN = 4;
    static const int SECTOR_CNT = SECTOR_LEN + SECTOR_LEN_LEN;
    static const int SECTOR_CNT_LEN = 4;
    static const int FILE_CNT_ST = SECTOR_CNT + SECTOR_CNT_LEN;
    static const int FILE_CNT_LEN = 4;

    static const int RESERVE_VOL_BYTES = 1004;
};

/* 瀹归噺瀛楁绫� */
struct vol_info {
    unsigned max_fileno;
    unsigned sector_start;
    unsigned sector_len;

    /* 涓嶅悓鐨勫崱澶у皬涓嶅悓, 鏈�濂戒粠鎵囧尯涓幓璇诲彇, 姝ゅ璁剧疆涓�2G鍒嗗尯 */
    unsigned sector_cnt;
    unsigned file_cnt;
    BYTE reserve[VOL_OFFSET::RESERVE_VOL_BYTES];      // 淇濈暀瀛楄妭
    vol_info(int s_cnt=0, int f_cnt=0): sector_cnt(s_cnt), file_cnt(f_cnt) {
        max_fileno = MAX_FILE_SIZE;
        sector_start = SDCARD_START_SECTOR + SDCARD_HEADER_LEN;
        sector_len = SDCARD_SECTOR_LEN;

        for (int i=0; i<VOL_OFFSET::RESERVE_VOL_BYTES; i++)
            reserve[i] = 0;
    }

};



/* 鏂囦欢瀛楁鎻忚堪: 浠yte涓哄熀鏈亸绉诲崟浣� */
struct FILE_OFFSET {
    static const int FILENAME_ST = 0;
    static const int FILENAME_LEN = 128;        // 姝ゅ瓧娈典笉鍚敤
    static const int TIME_ST = FILENAME_ST + FILENAME_LEN;
    static const int TIME_LEN = 32;
    static const int SECTOR_ST = TIME_ST + TIME_LEN;
    static const int SECTOR_ST_LEN = 4;
    static const int SECTOR_ED = SECTOR_ST + SECTOR_ST_LEN;
    static const int SECTOR_ED_LEN = 4;
    static const int FILE_SIZE = SECTOR_ED + SECTOR_ED_LEN;
    static const int FILE_SIZE_LEN = 8;

    static const int RESERVE_FILE_BYTES = 80;
};

/* 閫氳繃褰撳墠鎵囧尯鏁版潵鍐欏叆鏁版嵁 */
struct file_info {
    BYTE filename[FILE_OFFSET::FILENAME_LEN];
    BYTE format_time[FILE_OFFSET::TIME_LEN];
    int sector_st;
    int sector_len;
    LL file_size;   /* count by byte */
    BYTE reserve[FILE_OFFSET::RESERVE_FILE_BYTES];   // 淇濈暀瀛楄妭

    file_info(BYTE filename_[], BYTE time_[], int st_): sector_st(st_), sector_len(0), file_size(0) {
        memcpy(filename, filename_, FILE_OFFSET::FILENAME_LEN);
        memcpy(format_time, time_, FILE_OFFSET::TIME_LEN);
        memset(reserve, 0, FILE_OFFSET::RESERVE_FILE_BYTES);
    }

    file_info() {
        memset(filename, 0, FILE_OFFSET::FILENAME_LEN);
        memset(format_time, 0, FILE_OFFSET::TIME_LEN);
        memset(reserve, 0, FILE_OFFSET::RESERVE_FILE_BYTES);

    }
};


/************************* test function ****************************/
bool test_get_vol_info(struct vol_info &vi);

#endif // SDOP_H
