

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <stdlib.h>
#include <sys/ioctl.h>

#include "aclas-lcd0.h"

#include "android/log.h"
static const char *TAG="AclasArmPosDBG";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)


#define LCD_IOC_MAGIC	                	'L'

#define LCD_IOC_GETDOTROW               _IOR(LCD_IOC_MAGIC, 0, int)
#define LCD_IOC_GETDOTCOL               _IOR(LCD_IOC_MAGIC, 1, int)
#define LCD_IOC_LIGHT_OP                _IOW(LCD_IOC_MAGIC, 2, int)
#define LCD_IOC_SET_CONTRAST            _IOW(LCD_IOC_MAGIC, 3, int)
#define LCD_IOC_DOT_MATRIX              _IOR(LCD_IOC_MAGIC, 6, int)

static int fd;

typedef struct{
    int grade;
}   CONTRAST_ST;


JNIEXPORT jint JNICALL  Java_aclasdriver_AclasLcd0_open(JNIEnv *env, jobject thiz)
{
		fd = open("/dev/lcd0", O_RDWR);
		if (fd == -1) 
		{
			/* Throw an exception */
			LOGE("Cannot open lcd0");
			return -1;
		}
		return 1;
}

JNIEXPORT void JNICALL  Java_aclasdriver_AclasLcd0_writeMsg(JNIEnv *env, jobject thiz, jbyteArray Msg)
{
	jsize length;
	jbyte *b;
	
	b = (*env)->GetByteArrayElements(env,Msg, NULL);
	if (b == NULL) 
        return;
    
	length = (*env)->GetArrayLength(env,Msg);
	if(length<0)
	{
		(*env)->ReleaseByteArrayElements(env,Msg, b, 0);
		return;
	}
	if (fd>0)	
		write(fd,b,length);
	(*env)->ReleaseByteArrayElements(env,Msg, b, 0);
}

JNIEXPORT void JNICALL Java_aclasdriver_AclasLcd0_SetContrast(JNIEnv *env, jobject thiz,jint val)
{
	CONTRAST_ST contrast;
	contrast.grade = val;
	if (fd>0)
		ioctl(fd,LCD_IOC_SET_CONTRAST,&contrast);  //SET CONTRAST  	
}

JNIEXPORT void JNICALL Java_aclasdriver_AclasLcd0_SetBacklight(JNIEnv *env, jobject thiz,jint light)
{
	if (fd>0)
		ioctl(fd,LCD_IOC_LIGHT_OP,&light);					 //OPEN THE BACKLIGHT
}

JNIEXPORT void JNICALL Java_aclasdriver_AclasLcd0_close(JNIEnv *env, jobject thiz)
{
	if (fd>0) 
	 close(fd);
	fd = 0;
}


/*
 * Class:     aclasdriver_AclasLcd0
 * Method:    WriteDotMatrix
 * Signature: ([B)V
 */
JNIEXPORT jint JNICALL Java_aclasdriver_AclasLcd0_WriteDotMatrix(JNIEnv *env, jobject thiz, jbyteArray buf)
{
    unsigned char *pbuf;
    int len;
    int ret;

    if(fd > 0)
    {
        len = (*env)->GetArrayLength(env, buf);
        pbuf = (*env)->GetByteArrayElements(env, buf, NULL);
        if(pbuf != NULL)
        {
        	ret = ioctl(fd, LCD_IOC_DOT_MATRIX, pbuf);                   //OPEN THE BACKLIGHT
        	(*env)->ReleaseByteArrayElements(env,buf, pbuf, 0);
            return ret;
        }
    }

    return -1;
}

/*
 * Class:     aclasdriver_AclasLcd0
 * Method:    GetWidth
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_AclasLcd0_GetWidth(JNIEnv *env, jobject thiz)
{
    jint width;

    if(fd < 0)
    {
        return -1;
    }

    ioctl(fd, LCD_IOC_GETDOTCOL, &width);

    return width;
}

/*
 * Class:     aclasdriver_AclasLcd0
 * Method:    GetHeight
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_AclasLcd0_GetHeight(JNIEnv *env, jobject thiz)
{
    jint height;

    if(fd < 0)
    {
        return -1;
    }

    ioctl(fd, LCD_IOC_GETDOTROW, &height);

    return height;
}
