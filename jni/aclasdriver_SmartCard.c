/*!
 * aclasdriver_SmartCard.c
 *
 *  Created on: 2012-5-10
 *      Author: zhangbin
 */

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
#include "sc_lib.h"
#include "aclasdriver_SmartCard.h"


static const char *TAG = "AclasArmPosDBG";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)


//#define SC_DEBUG

#ifdef SC_DEBUG
#define PDEBUG(fmt, arg...)             LOGD(fmt, ##arg)
#define ENTER() PDEBUG("%s Entering ... [%s]\t\t%d\n", __FILE__, __FUNCTION__, clock())
#define LEAVE() PDEBUG("%s Leaving  ... [%s]\t\t%d\n", __FILE__, __FUNCTION__, clock())
#else
#define PDEBUG(fmt, arg...)
#define ENTER()
#define LEAVE()
#endif



#define SMART_CARD_DEVICE_NUM       2
static SmartCard_Dev smartcard[SMART_CARD_DEVICE_NUM];

static char const * const SMART_CARD_NAME = "/dev/smartcard";


static int inline check_dev_num(int dev)
{
    if((dev >= 0) && (dev < SMART_CARD_DEVICE_NUM))
    {
        return 0;
    }

    return -1;
}

static int inline check_dev_open(int dev)
{
    if(smartcard[dev].fd < 0)
    {
        return -1;
    }

    return 0;
}

/*
 * Class:     aclasdriver_SmartCard
 * Method:    sc_Open
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_SmartCard_Open(JNIEnv *env, jobject thiz, jint dev)
{
    jint retval, i;
    jbyte cardname[40];

    if(check_dev_num(dev) < 0)
    {
        PDEBUG("dev num error\n");
        return -1;
    }

    sprintf(cardname, "%s%1d", SMART_CARD_NAME, dev);
    LOGD("cardname = %s\n", cardname);
    return sc_Open(cardname, &smartcard[dev]);
}


/*
 * Class:     aclasdriver_SmartCard
 * Method:    sc_Close
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_aclasdriver_SmartCard_Close(JNIEnv *env, jobject thiz, jint dev)
{
    if(check_dev_num(dev) < 0)
    {
        PDEBUG("dev num error\n");
        return;
    }

    sc_Close(&smartcard[dev]);
    smartcard[dev].fd = -1;
}


/*
 * Class:     aclasdriver_SmartCard
 * Method:    sc_TestCardReady
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_SmartCard_TestCardReady(JNIEnv *env, jobject thiz, jint dev)
{
    if(check_dev_num(dev) < 0)
    {
        PDEBUG("dev num error\n");
        return -1;
    }

    if(check_dev_open(dev) < 0)
    {
        PDEBUG("dev not open");
        return -1;
    }

    return sc_TestCardReady(&smartcard[dev]);
}


/*
 * Class:     aclasdriver_SmartCard
 * Method:    GetATR
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_aclasdriver_SmartCard_GetATR(JNIEnv *env, jobject thiz, jint dev)
{
    jbyteArray atr;

    if(check_dev_num(dev) < 0)
    {
        PDEBUG("dev num error\n");
        return NULL;
    }

    if(check_dev_open(dev) < 0)
    {
        PDEBUG("dev not open");
        return NULL;
    }

    if(smartcard[dev].atr_len <= 0)
    {
        return NULL;
    }

    atr = (*env)->NewByteArray(env, smartcard[dev].atr_len);
    (*env)->SetByteArrayRegion(env, atr, 0, smartcard[dev].atr_len, smartcard[dev].atr);

    return atr;
}

/*
 * Class:     aclasdriver_SmartCard
 * Method:    sc_GetState
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_SmartCard_GetState(JNIEnv *env, jobject thiz, jint dev)
{
    if(check_dev_num(dev) < 0)
    {
        PDEBUG("dev num error\n");
        return -1;
    }

    if(check_dev_open(dev) < 0)
    {
        PDEBUG("dev not open");
        return -1;
    }

    return sc_GetState(&smartcard[dev]);
}


/*
 * Class:     aclasdriver_SmartCard
 * Method:    sc_IsCardRemoved
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_SmartCard_IsCardRemoved(JNIEnv *env, jobject thiz, jint dev)
{
    if(check_dev_num(dev) < 0)
    {
        PDEBUG("dev num error\n");
        return -1;
    }

    if(check_dev_open(dev) < 0)
    {
        PDEBUG("dev not open");
        return -1;
    }

    return sc_IsCardRemoved(&smartcard[dev]);
}

/*
 * Class:     aclasdriver_SmartCard
 * Method:    sc_SendCommand
 * Signature: (I[B[B)I
 */
JNIEXPORT jint JNICALL Java_aclasdriver_SmartCard_SendCommand(JNIEnv *env, jobject thiz, jint dev, jbyteArray send, jbyteArray recv)
{
    jbyte *psend, *precv;
    jint ret;
    jint send_len, recv_len;

    if(check_dev_num(dev) < 0)
    {
        PDEBUG("dev num error\n");
        return -1;
    }

    if(check_dev_open(dev) < 0)
    {
        PDEBUG("dev not open");
        return -1;
    }

    psend = (*env)->GetByteArrayElements(env, send, NULL);
    precv = malloc(512);
    ret = sc_SendCommand(&smartcard[dev], psend, send_len, precv, &recv_len);
    (*env)->SetByteArrayRegion(env, recv, 0, recv_len, precv);
    (*env)->ReleaseByteArrayElements(env, send, psend, 0);
    free(precv);

    return ret;
}

/*
 * Class:     aclasdriver_SmartCard
 * Method:    sc_ApduCommand
 * Signature: (IIIII[B[B)I
 */
JNIEXPORT jbyteArray JNICALL Java_aclasdriver_SmartCard_ApduCommand(JNIEnv *env,
                                                                    jobject thiz,
                                                                    jint dev,
                                                                    jint cla,
                                                                    jint ins,
                                                                    jint p1,
                                                                    jint p2,
                                                                    jbyteArray data,
                                                                    jint le)
{
    jint lc,resp_len;
    jbyte *pdata, *presp;
    jbyteArray resp;

    PDEBUG("apdu command-----------------");
    if(check_dev_num(dev) < 0)
    {
        PDEBUG("dev num error\n");
        return NULL;
    }

    if(check_dev_open(dev) < 0)
    {
        PDEBUG("dev not open");
        return NULL;
    }

    PDEBUG("apdu command,start get len");
    lc = (*env)->GetArrayLength(env, data);
    PDEBUG("apdu command,end get len, lc = %d\n", lc);
    if(lc == 0)
    {
        lc = -1;
        pdata = NULL;
    }
    else
    {
        pdata = (*env)->GetByteArrayElements(env, data, NULL);
    }

    LOGD("lc = %d\n", lc);
    presp = malloc(500);
    resp_len = sc_ApduCommand(&smartcard[dev], cla, ins, p1, p2, lc, pdata, le, presp);

    if(resp_len != 0)
    {
        resp = (*env)->NewByteArray(env, resp_len);
        (*env)->SetByteArrayRegion(env, resp, 0, resp_len, presp);
        if(lc > 0)
        	(*env)->ReleaseByteArrayElements(env, data, pdata, 0);
        free(presp);
        return resp;
    }
    else
    {
    	 if(lc > 0)
    		 (*env)->ReleaseByteArrayElements(env, data, pdata, 0);
        free(presp);
        return NULL;
    }
}



