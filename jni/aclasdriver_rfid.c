#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <jni.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include "android/log.h"
#include "aclasdriver_rfid.h"
//#include "rfid_lib.h"

#define RFID_DEBUG

#ifdef RFID_DEBUG
#define rfiddbg(argc, argv...)  LOGD(argc, ##argv)
#else
#define rfiddbg(argc, argv...)
#endif


typedef enum
{
    BUZZER_BI_1=1,
    BUZZER_BI_2,
    BUZZER_BI_3,
    BUZZER_BI_LONG,
}BUZZER_BEEP;

static jint rfid_fd = -1;
const static unsigned char DEV_RFID_NAME[]   = "/dev/rfid0";

#define 	BUZZER_IOC_FREQ        _IOW('B', 1, int)

static const char *TAG = "AclasArmPosDBG";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

//-----------------------------------------------------------------------------------------------


static jint ReadCardNo(jint fd, jbyte *CardNo, jbyte *cardsize)
{
    jint RetVal;
    jbyte buf[4];

    RetVal = Mifs_Config(fd);
    if(RetVal != 0)
    {
        LOGD("Mifs_Config ERROR, RetVal = %d", RetVal);
        return RetVal;
    }

    RetVal = Mifs_Request(fd, 1, buf);
    if(RetVal != 0)
    {
        LOGD("Mifs_Request ERROR\n");
        return RetVal;
    }

    RetVal = Mifs_Anticoll2(fd, 0, 0, buf);
    if(RetVal != 0)
    {
        LOGD("Mifs_Anticoll2 ERROR\n");
        return RetVal;
    }

    sprintf(CardNo,"%2.2x%2.2x%2.2x%2.2x",buf[0]&0xFF,buf[1]&0xFF,buf[2]&0xFF,buf[3]&0xFF);
    RetVal = Mifs_Select(fd,buf,cardsize);
    LOGD("%s", CardNo);

    return RetVal;

}


/*
 * Class:     aclasdriver_rfid
 * Method:    ReadCardNo
 * Signature: ()[B
 */
JNIEXPORT jstring JNICALL Java_aclasdriver_rfid_ReadCardNo(JNIEnv *env, jobject thiz)
{
    int   i;
    int ret;
    jbyte CardNo[32],cardtype[2];

    memset(CardNo,0,32);

    if(rfid_fd < 0)
    {
        LOGD("open fail!\n");
        return NULL;
    }

    if((ret = ReadCardNo(rfid_fd, CardNo, cardtype)) == 0)
    {
        jstring result;

        result = (*env)->NewStringUTF(env, CardNo);
        return result;
    }
    else
    {
        LOGD("ReadCard Error [%d]\n",ret);
        return NULL;
    }
}


/*
 * Class:     aclasdriver_rfid
 * Method:    open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_rfid_open(JNIEnv *env, jobject thiz)
{
    if(rfid_fd < 0)
    {
        LOGD("really open ");
        rfid_fd = open(DEV_RFID_NAME, O_RDWR);
    }
    LOGD("open rfid %s, id = %d", (rfid_fd < 0)?"ERROR":"OK", rfid_fd);
    return rfid_fd;
}

/*
 * Class:     aclasdriver_rfid
 * Method:    close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_rfid_close(JNIEnv *env, jobject thiz)
{
    if(rfid_fd >= 0)
    {
        close(rfid_fd);
    }

    LOGD("close rfid");
    rfid_fd = -1;
    return 0;
}




JNIEXPORT jint JNICALL Java_aclasdriver_rfid_beep
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
		  int freq = 2600;
		  ioctl(bzfd,BUZZER_IOC_FREQ,&freq);
		  write(bzfd, &bz, 1);
	  }
	  close(bzfd);
	  return 0;
}

/*
 * Class:     aclasdriver_rfid
 * Method:    CheckCardPsw
 * Signature: (I[B)Ljava/lang/Boolean;
 */
JNIEXPORT jboolean JNICALL Java_aclasdriver_rfid_AuthKey(JNIEnv *env , jobject thiz, jint Blockaddress, jbyteArray password)
{
    jbyte RetVal = 0;
    jbyte *psw;
    jint pswlen;
    jbyte tmpbuf[6];

    if(rfid_fd < 0)
    {
        LOGD("open fail!\n");
        return JNI_FALSE;
    }

    psw = (*env)->GetByteArrayElements(env, password, NULL);
    pswlen = (*env)->GetArrayLength(env, password);
    if(psw == NULL)
    {
        LOGD("get password error!");
        return JNI_FALSE;
    }
    memcpy(tmpbuf, psw, 6);
    (*env)->ReleaseByteArrayElements(env, password, psw, 0);

    LOGD("id = %d, Blockaddress=%d, psw[0] = %x", rfid_fd, Blockaddress, tmpbuf[0]);
    RetVal = Mifs_AuthKey(rfid_fd, 0, Blockaddress, tmpbuf);
    LOGD("authkey ret = %d", RetVal);

    return (jboolean)((RetVal != 0) ? JNI_FALSE : JNI_TRUE);
}


/*
 * Class:     aclasdriver_rfid
 * Method:    IsEmptyCard
 * Signature: ()Ljava/lang/Boolean;
 */
JNIEXPORT jboolean JNICALL Java_aclasdriver_rfid_IsEmptyCard(JNIEnv *env, jobject thiz)
{
    return JNI_TRUE;
}

/*
 * Class:     aclasdriver_rfid
 * Method:    WriteCardBlock
 * Signature: (I[B)Ljava/lang/Boolean;
 */
JNIEXPORT jboolean JNICALL Java_aclasdriver_rfid_WriteCardBlock(JNIEnv *env, jobject thiz, jint Blockaddress, jbyteArray BlockBuf)
{
    jbyte *pBuf;
    jint BufLen;

    if(rfid_fd < 0)
    {
        rfiddbg("open fail!\n");
        return JNI_FALSE;
    }

    pBuf = (*env)->GetByteArrayElements(env, BlockBuf, NULL);
    BufLen = (*env)->GetArrayLength(env, BlockBuf);
    rfiddbg("write buf len = %d", BufLen);
    if(pBuf == NULL)
    {
        rfiddbg("write buffer null!");
        return JNI_FALSE;
    }

    if(Mifs_Write(rfid_fd, Blockaddress, pBuf) == 0)
    {
        rfiddbg("write block success");
        return JNI_TRUE;
    }
    (*env)->ReleaseByteArrayElements(env, BlockBuf, pBuf, 0);

    rfiddbg("write block fail");
    return JNI_FALSE;
}

/*
 * Class:     aclasdriver_rfid
 * Method:    ReadCardBlock
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_aclasdriver_rfid_ReadCardBlock(JNIEnv *env, jobject thiz, jint Blockaddress)
{
    jbyte getdata[50];
    jint ret;
    jbyteArray result;
    #define BLOCK_SIZE  16

    if(rfid_fd < 0)
    {
        rfiddbg("open fail!\n");
        return NULL;
    }

    ret = Mifs_Read(rfid_fd, Blockaddress, getdata);
    if(ret < 0)
    {
        rfiddbg("mifs read err");
        return NULL;
    }

    result = (*env)->NewByteArray(env, BLOCK_SIZE);
    (*env)->SetByteArrayRegion(env, result, 0, BLOCK_SIZE, getdata);

    return result;
}

/*
 * Class:     aclasdriver_rfid
 * Method:    SetCardPsw
 * Signature: (I[B)Ljava/lang/Boolean;
 */
JNIEXPORT jboolean JNICALL Java_aclasdriver_rfid_SetCardPsw(JNIEnv *env, jobject thiz, jint Blockaddress, jbyteArray password)
{
    jboolean ret;

    return ret;
}


