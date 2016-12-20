#include     <stdio.h>
#include     <stdlib.h>
#include     <unistd.h>
#include     <sys/types.h>
#include     <sys/stat.h>
#include     <fcntl.h>
#include     <termios.h>
#include     <errno.h>
#include     <sys/ioctl.h>
#include     <sys/time.h>
#include     <string.h>
#include    "android/log.h"
#include    "aclasdriver_printer.h"

static int openflag = -1;
const static unsigned char DEV_PRINTER_NAME[]   = "/dev/ttyprinter0";

static const char *TAG = "AclasArmPosDBG";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)


#define PRN_IOC_MAGIC 'P'

#define PRN_IOC_SETCONTRAST         _IOW(PRN_IOC_MAGIC, 0, int)//SET THE HEATING DENSITY 1-8
#define PRN_IOC_FEED                _IOW(PRN_IOC_MAGIC, 1, int)//PAPER FEED COMMAND v < 8000
#define PRN_IOC_STOP                _IOW(PRN_IOC_MAGIC, 3, int)//STOP PRINTING
#define PRN_IOC_GETDOTWIDTH         _IOR(PRN_IOC_MAGIC, 4, int)//GET PRINTING WIDE DOTS
#define PRN_IOC_EPSON               _IOR(PRN_IOC_MAGIC, 13, int)
#define PRN_IOC_PRINTER_CONTINUE    _IOW(PRN_IOC_MAGIC, 17, int)    // 继续打印
#define PRN_IOC_PAPER_STATUS        _IOR(PRN_IOC_MAGIC, 20, int)    // 缺纸侦测


#define MIN_CONTRAST            1
#define MAX_CONTRAST            8
//-----------------------------------------------------------------------------------------------


/*
 * Class:     aclasdriver_printer
 * Method:    Open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_printer_Open(JNIEnv *env, jobject thiz)
{
    if(openflag < 0)
    {
        openflag = open(DEV_PRINTER_NAME, O_RDWR);
    }

    return openflag;
}

/*
 * Class:     aclasdriver_printer
 * Method:    Close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_aclasdriver_printer_Close(JNIEnv *env, jobject thiz)
{
    if(openflag >= 0)
    close(openflag);
    openflag = -1;
}

/*
 * Class:     aclasdriver_printer
 * Method:    Write
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_printer_Write(JNIEnv *env, jobject thiz, jbyteArray wrbuf)
{
    jbyte *pwrbuf;
    int ret;
    int wrlen;
    int i;

    if(openflag < 0)
    {
        return -1;
    }
    LOGD("Printer write");

    pwrbuf = (*env)->GetByteArrayElements(env, wrbuf, NULL);
    wrlen = (*env)->GetArrayLength(env, wrbuf);
    for(i =0 ;i< wrlen;i++)
    {
    	LOGD("%x ",pwrbuf[i]);
    }

    ret = write(openflag, pwrbuf, wrlen);
    (*env)->ReleaseByteArrayElements(env, wrbuf, pwrbuf, 0);

    return ret;
}

/*
 * Class:     aclasdriver_printer
 * Method:    Read
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_aclasdriver_printer_Read(JNIEnv *env, jobject thiz, jint getlen)
{
    jbyte *pgetbuf;
    int ret;
    jbyteArray retbuf;

    if(openflag < 0)
    {
        return NULL;
    }

    if(getlen <= 0)
    {
        return NULL;
    }

    pgetbuf = malloc(getlen);
    ret = read(openflag, pgetbuf, getlen);
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

/*
 * Class:     aclasdriver_printer
 * Method:    SetContrast
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_printer_SetContrast(JNIEnv *env, jobject thiz, jint contrast)
{
    if(openflag < 0)
    {
        return -1;
    }

    if((contrast < MIN_CONTRAST) || (contrast > MAX_CONTRAST))
    {
        return -1;
    }

    return ioctl(openflag, PRN_IOC_SETCONTRAST, &contrast);
}

/*
 * Class:     aclasdriver_printer
 * Method:    Feed
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_printer_Feed(JNIEnv *env, jobject thiz, jint setp)
{
    if(openflag < 0)
    {
        return -1;
    }

    if(setp <= 0)
    {
        LOGD("setp err, = %d", setp);
        return 0;
    }

    return ioctl(openflag, PRN_IOC_FEED, &setp);
}

/*
 * Class:     aclasdriver_printer
 * Method:    Stop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_printer_Stop(JNIEnv *env, jobject thiz)
{
    if(openflag < 0)
    {
        return -1;
    }

    return ioctl(openflag, PRN_IOC_STOP, 0);
}

/*
 * Class:     aclasdriver_printer
 * Method:    Conitnue
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_printer_Conitnue(JNIEnv *env, jobject thiz)
{
    if(openflag < 0)
    {
        return -1;
    }

    return ioctl(openflag, PRN_IOC_PRINTER_CONTINUE, 0);
}


/*
 * Class:     aclasdriver_printer
 * Method:    GetDotWidth
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_printer_GetDotWidth(JNIEnv *env, jobject thiz)
{
    jint width;

    if(openflag < 0)
    {
        return -1;
    }

    ioctl(openflag, PRN_IOC_GETDOTWIDTH, &width);

    return width;
}


/*
 * Class:     aclasdriver_printer
 * Method:    SetPrintMode
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_printer_SetPrintMode(JNIEnv *env, jobject thiz, jint Mode)
{
    if(openflag < 0)
    {
        return -1;
    }

    return ioctl(openflag, PRN_IOC_EPSON, &Mode);
}

/*
 * Class:     aclasdriver_printer
 * Method:    IsPaperExist
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_aclasdriver_printer_IsPaperExist(JNIEnv *env, jobject thiz)
{
    jbyte buff[8];
    jint paperstatus = 0;

    if(openflag < 0)
    {
        return -1;
    }

    //缺纸侦测
//    buff[0]= 0x10;
//    buff[1]= 0x04;
//    buff[2]= 0x02;                  //epson command
//    write(openflag, buff, 3);       //WRITE IN DATA
//    read(openflag, &buff[3], 1);    //GET THE PRINTING STATUS
//    LOGD("paper id = %d\n",buff[3]);

    if(ioctl(openflag, PRN_IOC_PAPER_STATUS, &paperstatus) < 0)
    {

    	LOGD("no such argument");
    	return -1;
    }
    LOGD("paperstatus = %d\n", paperstatus);
    if(paperstatus & 0x20)
    {
        LOGD("write No Paper\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

