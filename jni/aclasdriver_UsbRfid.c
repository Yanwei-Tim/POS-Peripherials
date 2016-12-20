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
#include "aclasdriver_UsbRfid.h"

static int fd = -1;
static const char *TAG = "AclasArmPos:UsbRFID:DBG";
static unsigned long  Baud[5] = {B9600,B19200,B38400,B57600,B115200};


#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

JNIEXPORT jint JNICALL Java_aclasdriver_UsbRfid_open
(JNIEnv *env, jobject thiz)
{

	 struct termios init_settings,new_settings;
    int brat = 0;
	if(fd > 0)
	{

		return fd;
	}
	fd = open("/dev/ttyACM0", O_RDWR|O_NONBLOCK);
	if(fd < 0)
	{
		LOGD("Open the usb rfid Error");
	}
    tcgetattr(fd,&init_settings);
    new_settings = init_settings;
    new_settings.c_lflag &= ~(ICANON|ISIG);
    new_settings.c_lflag &= ~ECHO;
    new_settings.c_iflag &= ~ICRNL;
    new_settings.c_iflag &= ~IXON;
    new_settings.c_iflag &= ~IXANY;
    new_settings.c_iflag &= ~IXOFF;
    new_settings.c_iflag &= ~IGNBRK;

    new_settings.c_cc[VMIN] = 0;
    new_settings.c_cc[VTIME] = 10;
   cfsetispeed(&new_settings,Baud[brat]);
   cfsetospeed(&new_settings,Baud[brat]);
   tcsetattr(fd,TCSANOW,&new_settings);

	return fd;
}
/*
 * Class:     aclasdriver_UsbRfidModel
 * Method:    close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_UsbRfid_close
(JNIEnv *env, jobject thiz)
{

 close(fd);
 fd = -1;
 return 0;
}

/*
 * Class:     aclasdriver_UsbRfidModel
 * Method:    write
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_UsbRfid_write
(JNIEnv *env, jobject thiz, jbyteArray wrbuf)
{
	    int ret, len;
	    unsigned char *pbuf;

	    if(fd < 0)
	    {
	        return -1;
	    }

	    pbuf = (*env)->GetByteArrayElements(env, wrbuf, NULL);
	    len = (*env)->GetArrayLength(env, wrbuf);
	    ret = write(fd, pbuf, len);
	    (*env)->ReleaseByteArrayElements(env,wrbuf, pbuf, 0);
	    LOGD("usb rfid write %d %d %s",fd,ret,strerror(ret));
	    return ret;


}

/*
 * Class:     aclasdriver_UsbRfidModel
 * Method:    read
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_aclasdriver_UsbRfid_read
(JNIEnv *env, jobject thiz, jint rdlen)
{
	  jbyte *pgetbuf;
	    int ret;
	    int i;
	    jbyteArray retbuf;
	    if(fd < 0)
	    {
	        return NULL;
	    }
	    if(rdlen <= 0)
	    {
	           return NULL;
	    }

	    pgetbuf = malloc(rdlen);
	    memset(pgetbuf,0x00,rdlen);
	    ret = read(fd, pgetbuf, rdlen);
	    if(ret < 0)
	    {
	    	LOGD("Read Nothing\n");
	        free(pgetbuf);
	        return NULL;
	    }
	    for(i=0;i< ret;i++)
	    {

	    	LOGD("%02X ",pgetbuf[i]&0x000000FF);
	    }
	    retbuf = (*env)->NewByteArray(env, ret);
	    (*env)->SetByteArrayRegion(env, retbuf, 0, ret, pgetbuf);
	    free(pgetbuf);
	    return retbuf;

}
