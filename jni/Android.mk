
LOCAL_PATH := $(call my-dir)
#获取当前目录


include $(CLEAR_VARS)
#清除一些变量
TARGET_PLATFORM := android-10
LOCAL_MODULE    := rfid 
#要生成的库名
LOCAL_SRC_FILES := rfid_lib.c
#库对应的源文件
LOCAL_LDLIBS    := -llog 
include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)
#清除一些变量
TARGET_PLATFORM := android-10
LOCAL_MODULE    := smartcard 
#要生成的库名
LOCAL_SRC_FILES := sc_lib.c
#库对应的源文件
LOCAL_LDLIBS    := -llog 
include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)
#清除一些变量
TARGET_PLATFORM := android-10
LOCAL_MODULE    := AclasArmPos
#要生成的库名
LOCAL_SRC_FILES := aclasdriver_drawer.c aclasdriver_UsbRfid.c aclasdriver_km3_printer.c aclasdriver_SerialPort.c aclasdriver_SmartCard.c aclasHdq.c aclasMagcard.c aclasdriver_rfid.c aclas-lcd0.c aclasdriver_printer.c 
#库对应的源文件
LOCAL_LDLIBS    := -llog 
LOCAL_STATIC_LIBRARIES := libsmartcard librfid
include $(BUILD_SHARED_LIBRARY)
