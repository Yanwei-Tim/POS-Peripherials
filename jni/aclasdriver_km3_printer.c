
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
#include "aclasdriver_km3_printer.h"

static int fd = -1;
static int swfd = -1;
static const char *TAG = "AclasArmPos:km3:DBG";
const static unsigned char PRINTER_NAME[]= "/dev/lp0";
const static unsigned char SW_NAME[]= "/sys/class/cs3_devices/perdev0/prn_sw";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)


typedef enum
{
    BUZZER_BI_1=1,
    BUZZER_BI_2,
    BUZZER_BI_3,
    BUZZER_BI_LONG,
}BUZZER_BEEP;

#define 	BUZZER_IOC_FREQ        _IOW('B', 1, int)
#define		NO_PAPER	1

char statuscmd[]    = {0x10,0x04,0x02};

/*
 * Class:     aclasdriver_km3_printer
 * Method:    open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_km3_1printer_open
(JNIEnv *env, jobject thiz)
{

	if(fd > 0)
	{
		return fd;
	}

	fd =  open(PRINTER_NAME, O_RDWR);
	if(fd < 0)
	{
		LOGD("Open the KM3 Printer Error");
		return fd;
	}

	return fd;
}

/*
 * Class:     aclasdriver_km3_printer
 * Method:    close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_km3_1printer_close
  (JNIEnv *env, jobject thiz)
{
   int ret =0;

   close(fd);

   fd = -1;

   return ret;
}
/*
 * Class:     aclasdriver_km3_printer
 * Method:    write
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_km3_1printer_write
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
	    LOGD("km3 printer write %d %s",ret,strerror(ret));
	    return ret;


}

/*
 * Class:     aclasdriver_km3_printer
 * Method:    read
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_aclasdriver_km3_1printer_read
  (JNIEnv *env, jobject thiz, jint rdlen)
{
	  jbyte *pgetbuf;
	    int ret;
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
	    ret = read(fd, pgetbuf, rdlen);
	    if(ret < 0)
	    {
	        free(pgetbuf);
	        return NULL;
	    }

	    retbuf = (*env)->NewByteArray(env, ret);
	    (*env)->SetByteArrayRegion(env, retbuf, 0, ret, pgetbuf);
	    free(pgetbuf);

	    return retbuf;

}

JNIEXPORT jint JNICALL Java_aclasdriver_km3_1printer_readstatus
  (JNIEnv *env, jobject thiz)
{
	  jbyte *pgetbuf;
	  char buf[32];
	    int len,ret = 0;
	    int status = 0;
	    jbyte retbuf;

	    if(fd < 0)
	    {
	        return -1;
	    }

	    write(fd,statuscmd,3);
	    pgetbuf = malloc(1);
	    //ret = read(fd, pgetbuf, 1);
	    len = read(fd, buf, 1);
	    if(len < 0)
	    {
	        free(pgetbuf);
	        return -1;
	    }

	    //retbuf = pgetbuf[1];
	    retbuf = buf[0];
	    free(pgetbuf);
	    status = buf[0] & 0x20;

	    if(status != 0)
	    {
	    	LOGD("printer status %x\n",retbuf);
	    	ret = NO_PAPER;
	    }

	    return ret;

}

JNIEXPORT jint JNICALL Java_aclasdriver_km3_1printer_beep
  (JNIEnv *env, jobject obj)
{
	   BUZZER_BEEP bz;

	   int bzfd;
	  bzfd = open("/dev/buzzer",O_WRONLY);
	  if(bzfd < 0)
	  {
	  	LOGE("Cannot open BUZZER device");
	  	return -1;
	  }
	  else
	  {
		  bz = BUZZER_BI_2;
		  int freq = 3800;
		  ioctl(bzfd,BUZZER_IOC_FREQ,&freq);
		  write(bzfd, &bz, 1);
	  }
	  close(bzfd);
	  return 0;
}
