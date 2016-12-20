
#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <stdlib.h>
#include <sys/ioctl.h>

#include "aclasMagcardApi.h"

#include "android/log.h"

#define     MAG_RELEASE            _IOW('c', 5, unsigned int)     //释放block
#define 	BUZZER_IOC_FREQ        _IOW('B', 1, int)


static const char *TAG="AclasArmPosDBG";
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

typedef struct{
	int len1;                   //THE FIRST TRACK DATA LENGTH
	int len2;					//THE SECOND TRACK DATA LENGTH
	int len3;					//THE THIRD TRACK DATA LENGTH
	char buf1[TRACK_SIZE];      //THE FIRST TRACK DATA
	char buf2[TRACK_SIZE];      //THE SECOND TRACK DATA
	char buf3[TRACK_SIZE];      //THE THIRD TRACK DATA
}TARCK_DATA_ST;		

static int lcdfd;

JNIEXPORT jint JNICALL Java_aclasdriver_aclasMagcardApi_open
  (JNIEnv *env, jobject obj)
  {
	  lcdfd = open("/dev/magcard0",O_RDONLY);
	  if(lcdfd < 0)
	  {
	  	LOGE("Cannot open Magcard device");
	  	return -1;
	  }
  	 return 0;
  }

/*
 * Class:     aclasMagcardApi_aclasMagcardApi
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_aclasdriver_aclasMagcardApi_close
  (JNIEnv *env, jobject obj)
  {
  	if (lcdfd > 0)
  	{
  	  ioctl(lcdfd,MAG_RELEASE,NULL);
	  close(lcdfd);
	  lcdfd = -1;
	  LOGD("Close The DEVICE");
  	}
  	else
  	{
  		LOGE("Cannot Close The DEVICE");
  	}
  
  }

/*
 * Class:     aclasMagcardApi_aclasMagcardApi
 * Method:    read
 * Signature: ([B)V
 */
JNIEXPORT jint JNICALL Java_aclasdriver_aclasMagcardApi_read
  (JNIEnv *env, jobject obj, jobjectArray array)
  { 
  		TARCK_DATA_ST card;
  		int size = (*env)->GetArrayLength(env, array);
  		int i,j;
  		char data[3][128];

  		LOGI("show the textView");	//DISPLAY THE SECOND TRACK DATA

  		if(lcdfd > 0)
  		{
  		    memset(&card,0x00,sizeof(card));
  		   if( read(lcdfd, &card, sizeof(card)) > 0)
	      {
  			   if((card.len1 == 0) && (card.len2 == 0) && (card.len3 == 0))
  			   {
  				   LOGE("Magcard READ Nothing");
  				   return -1;
  			   }
			  sprintf((char *)&data[0], "%s", card.buf1);
			  LOGI("len1=%d, %s\n", card.len1, &data[0]);	//DISPLAY THE FIRST TRACK DATA
			  sprintf((char *)&data[1], "%s", card.buf2);
			  LOGI("len2=%d, %s\n", card.len2, &data[1]);	//DISPLAY THE SECOND TRACK DATA
			  sprintf((char *)&data[2], "%s", card.buf3);
			  LOGI("len3=%d, %s\n", card.len3, &data[2]);	//DISPLAY THE THIRD TRACK DATA
   			 for (i = 0; i < size; i++)
			 {
				 LOGI("show the textView 2");	//DISPLAY THE SECOND TRACK DATA
				 jstring CardNo = (*env)->NewStringUTF(env, (char *)&data[i]);
				 (*env)->SetObjectArrayElement(env,array,i,CardNo);

			 }

	     }
  		   else
  		   {

  			 LOGE("Magcard READ Error");
  		   }


	    }
  		 else
		 {
			 LOGE("Magcard NO OPEN");
			 return -1;

		 }
	     return (card.len1+card.len2+card.len3);
  }

JNIEXPORT jint JNICALL Java_aclasdriver_aclasMagcardApi_beep
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
		  int freq = 3000;
		  ioctl(bzfd,BUZZER_IOC_FREQ,&freq);
		  write(bzfd, &bz, 1);
	  }
	  close(bzfd);
	  return 0;
}
