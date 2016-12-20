

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <stdlib.h>
#include <sys/ioctl.h>

#include "aclasHdqApi.h"

#define W1_READ_ROM		0x33


#include "android/log.h"
static const char *TAG="AclasArmPosDBG";

#define 	BUZZER_IOC_FREQ        _IOW('B', 1, int)

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

#define TRACK_SIZE  128

static int fd;

JNIEXPORT jint JNICALL Java_aclasdriver_aclasHdqApi_open
  (JNIEnv *env, jobject obj)
  {
	  fd = open("/dev/omap_hdq0",O_RDONLY);
	  if(fd < 0)
	  {
	  	LOGE("Cannot open omap hdq device");
	  	return -1;
	  }
  	 return 0;
  }

/*
 * Class:     aclasHdqApi_aclasHdqApi
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_aclasdriver_aclasHdqApi_close
  (JNIEnv *env, jobject obj)
  {
  	if (fd>0) 
	 close(fd);
  
  }

/*
 * Class:     aclasHdqApi_aclasHdqApi
 * Method:    read
 * Signature: ([B)V
 */
JNIEXPORT jbyteArray JNICALL Java_aclasdriver_aclasHdqApi_read
  (JNIEnv *env, jobject obj)
  { 
  		int len =0,i;
  		int connect;
  		char data[9];
  		jbyteArray tmp;

  		tmp =(*env)->NewByteArray(env,8);


  		LOGI("show the textView");	//DISPLAY THE SECOND TRACK DATA

  		if(fd > 0)
  		{
  			data[0] = W1_READ_ROM;
//  			    while(1)
			memset(&data[1],0,8);
			for(i=0;i<5;i++)
			{
				if((len=read(fd,data,9)) < 0)
				{
					connect = 0;
				}
				else
				{
					connect = 1;
					break;
				}
			}
			if(connect)
			{
				LOGD("IBUTTON CONNECT \n");
				for(i=8;i>0;i--)
					LOGD("%02x ",data[i]);
				LOGD("\n");


			}
			else
			{
				memset(data,0x00 ,9);
				LOGD("IBUTTON DISCONNECT\n");

			}



	     }
  		 else
  		 {
  			 	 LOGE("Hdq NO OPEN");
  			 	 memset(data,0x00 ,9);

  		 }
  		(*env)->SetByteArrayRegion(env,tmp,0,8,&data[1]);
  		return tmp;

	    }
  		

JNIEXPORT jint JNICALL Java_aclasdriver_aclasHdqApi_beep
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
		  int freq = 2000;
		  ioctl(bzfd,BUZZER_IOC_FREQ,&freq);
		  write(bzfd, &bz, 1);
	  }
	  close(bzfd);
	  return 0;


}
