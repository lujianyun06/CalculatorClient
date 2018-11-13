LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
APP_CPPFLAGS += -std=c++11
LOCAL_MODULE := filesys
LOCAL_ALLOW_UNDEFINED_SYMBOLS := true
LOCAL_SRC_FILES =: main.cpp \
                    sdcard.cpp
include $(BUILD_SHARED_LIBRARY)
LOCAL_LDLIBS    := -llog