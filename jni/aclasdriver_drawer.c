#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <jni.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <termios.h>
#include "android/log.h"
#include "aclasdriver_drawer.h"

static int fd = -1;
static const char *TAG = "AclasArmPosDBG";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

#define DRAWER_OPEN   _IOW('p', 1, unsigned int)


/*
 * Class:     aclasdriver_drawer
 * Method:    open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_drawer_open(JNIEnv *env, jobject thiz)
{
    int ret;

    if(fd > 0)
    {
        return -1;
    }

    fd = open("/dev/perdev0", O_RDWR);
    if(fd < 0)
    {
    	LOGD("Open The Perdev0 device Error");
        return fd;
    }
    else
    {

		if(ioctl(fd, DRAWER_OPEN, NULL) < 0)
		{
			LOGD("ioctl Drawer Open error");
		}
		else
		LOGD("Drawer Open ok");
    }
    close(fd);
    fd = -1;
    return 0;
}



