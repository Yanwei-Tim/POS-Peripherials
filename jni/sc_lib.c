/****************************************************************************
 *
 * Copyright (c) 2004 - 2006 Winbond Electronics Corp. All rights reserved.
 *
 ****************************************************************************/

/****************************************************************************
 *
 * FILENAME
 *     sc_lib.c
 *
 * VERSION
 *     1.1
 *
 * DESCRIPTION
 *     Library for Winbond Smart Card.
 *
 * DATA STRUCTURES
 *     None
 *
 * FUNCTIONS
 *     None
 *
 * HISTORY
 *	2005/04/28		Ver 1.0 Created by PC34 QFu
 *	2005/09/14		Modified for simple implementation
 *
 * REMARK
 *     Do not open /dev/smartcard directly, use this library.
 *
 *************************************************************************/
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>
#include "sc_lib.h"


//#define SC_DEBUG
static const char *TAG = "AclasArmPosDBG";

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

#ifdef SC_DEBUG

#include <time.h>

#define PDEBUG(fmt, arg...) 			LOGD(fmt, ##arg)
#define ENTER()	PDEBUG("%s Entering ... [%s]\t\t%d\n", __FILE__, __FUNCTION__, clock())
#define LEAVE()	PDEBUG("%s Leaving  ... [%s]\t\t%d\n", __FILE__, __FUNCTION__, clock())
#else
#define PDEBUG(fmt, arg...)
#define ENTER()
#define LEAVE()
#endif

/* 7816-3 1997 Table 7, 8 */
static int Ftab[] = { 372, 372, 558,  744, 1116, 1488, 1860, -1,
		  -1, 512, 768, 1024, 1536, 2048,   -1, -1 };
static int Dtab[] = { -1, 1, 2, 4, 8, 16, 32, -1, 12, 20, -1, -1, -1, -1, -1, -1 };


/* convet a byte from inverse to direct */
static unsigned char sc_ConvertByte(unsigned char byte)
{
	unsigned char value = 0;
	int i;

	for (i = 0; i < 8; i++)
		if ((byte >> i) & 0x01)
			value |= (1 << (7-i));
	return ~value;
}

static void sc_ConvertString(unsigned char *buf, int len)
{
	int i;

	ENTER();

	for(i=0; i<len;i++)
		buf[i] = sc_ConvertByte(buf[i]);

	LEAVE();

}

static void sc_uDelay(SmartCard_Dev *dev, int millisecond)
{
	struct timeval tv;

	tv.tv_sec = 0;
	tv.tv_usec = millisecond;

	select(dev->fd, 0, 0, 0, &tv);
}

/* clear FIFO buffer of reception */
static void sc_ClearFIFO(SmartCard_Dev *dev)
{
	ENTER();

	ioctl(dev->fd, SC_IOC_CLEARFIFO);

	LEAVE();
}

static void sc_PowerOff(SmartCard_Dev *dev)
{
	ENTER();

	ioctl(dev->fd, SC_IOC_POWEROFF);

	LEAVE();
}

static void sc_PowerOn(SmartCard_Dev *dev)
{
	ENTER();

	ioctl(dev->fd, SC_IOC_POWERON);

	LEAVE();
}

static int sc_RawWrite(SmartCard_Dev *dev, const unsigned char *buf, int *count)
{
	int retval;
	unsigned char *p = (unsigned char *)buf;

	ENTER();

	if(dev->conv )	/* if need, convert byte */
		sc_ConvertString(p, *count);

	retval = write(dev->fd, buf, *count);

	if(retval != *count)
		return retval;

	LEAVE();

	return 0;

}

static int sc_RawRead(SmartCard_Dev *dev, unsigned char *buf, int *count)
{
	int retval;

	ENTER();

	retval = read(dev->fd, buf, *count);

	if(dev->conv)
		sc_ConvertString(buf, *count);

	if(retval != *count)
		return retval;

	LEAVE();

	return 0;
}

static void sc_ActivateCard(SmartCard_Dev *dev)
{
    int retval;
	ENTER();

	retval = ioctl(dev->fd, SC_IOC_ACTIVATE);
    if(retval)
    {
        LOGD("SC_IOC_ACTIVATE card error!\n");
    }

	retval = ioctl(dev->fd, SC_IOC_GETATR, dev->atr);
    if(retval)
    {
        LOGD("SC_IOC_GETATR card error!\n");
    }

	dev->atr_len = (unsigned int)dev->atr[0];

	memcpy(dev->atr, dev->atr + 1, dev->atr_len);

	LEAVE();
}

static void sc_DeactivateCard(SmartCard_Dev *dev)
{
	ENTER();

	ioctl(dev->fd, SC_IOC_DEACTIVATE);

	LEAVE();
}

static int sc_ATRCheck(SmartCard_Dev *dev)
{
	int i, tck, h;
	unsigned char *atr = dev->atr, td, ts;

	ENTER();
	PDEBUG("Atr length %d\n", dev->atr_len);
	LOGD("Atr length %d\n", dev->atr_len);
#if 0
	for(i=0;i<dev->atr_len;i++)
	{
		LOGD("%x ",atr[i]);
	}
	LOGD("\n");
#endif
	/* invalid length of atr */
	if(dev->atr_len < 0)
	{
        LOGD("1");
		return SC_ERR_LIB_UNSUPPORTEDCARD;
	}

	ts = *atr++;

	dev->conv = 0;

	if( ts == 0x03){
		dev->conv = 1;
		sc_ConvertString(dev->atr, dev->atr_len);
	}

	if( ts != 0x3b && ts != 0x3f )
	{
        LOGD("2");
		return SC_ERR_LIB_UNSUPPORTEDCARD;		/* unknow convention */
	}

	td = *atr++;
	tck = 0;
	h = td & 0x0f;

	while(1){
		if(td & 0x10)
			atr++;
		if(td & 0x20)
			atr++;
		if(td & 0x40)
			atr++;
		if(td & 0x80){
			td = *atr;
			atr++;
			tck = (td & 0x0f) != 0;
		}else
			break;

		if( (atr+h) > (dev->atr + dev->atr_len)){
			PDEBUG("lib : length check failed");
			LOGD("lib : length check failed");
			LOGD("3\n");
			return SC_ERR_LIB_UNSUPPORTEDCARD;
		}
	}

	if(tck != 0){
		td = dev->atr[0];
		for(i = 1; i < dev->atr_len; i++)
			td ^= dev->atr[i];
		if(td != 0)
		{
			LOGD("5\n");
			return SC_ERR_LIB_UNSUPPORTEDCARD;
		}
	}

	LEAVE();

	return 0;
}

static int sc_MatchReader(SmartCard_Dev *dev, int fi, int di)
{
	int retval, tmp;

	ENTER();

	tmp = ((fi << 16) & 0xffff0000) | (di & 0x0000ffff);

	retval = ioctl(dev->fd, SC_IOC_MATCHREADER, tmp);

	LEAVE();

	return retval;
}

static void sc_SetReaderPara(SmartCard_Dev *dev)
{
	ENTER();

	ioctl(dev->fd, SC_IOC_SETPARAMETER, &dev->para);

	LEAVE();
}

static int sc_DoPPS(SmartCard_Dev *dev, int f, int d, int t)
{
	unsigned char buf[4], rbuf[4];
	int len = 4, retval;

	ENTER();

	/* default envirment , pps not need */
	if((f == 0 || f == 1) && d == 1)
		return 0;

	buf[0] = 0xff;
	buf[1] = 0x10 |(t & 0x0f);
	buf[2] = ((f<<4)&0xf0) | (d & 0x0f);
	buf[3] = (buf[0] ^ buf[1] ^ buf[2]);

	sc_ClearFIFO(dev);

	PDEBUG("Sending PPS : %02x %02x %02x %02x\n",
				buf[0], buf[1], buf[2], buf[3]);

	if((retval = sc_RawWrite(dev, buf, &len)) != 0){
		PDEBUG("Write error %d\n", retval);
		return retval;
	}

	if((retval = sc_RawRead(dev, rbuf, &len)) !=0 ){
		PDEBUG("Read error %d\n", retval);
		return retval;
	}

	retval = SC_ERR_LIB_UNSUPPORTEDCARD;

	PDEBUG("Received PPS : %02x %02x %02x %02x\n",
				rbuf[0], rbuf[1], rbuf[2], rbuf[3]);

	if(rbuf[0] != buf[0])
		return retval;

	if( (rbuf[1]&0x0f) == (buf[1] &0x0f) &&		/* success */
		((rbuf[1] & 0xf0) == 0x10 ||(rbuf[1] & 0xf0) == 0x00)){
		PDEBUG("PPS Request Success, now baudrate is %d\n", 9600 * Dtab[d]);
		return 0;
	}

	LEAVE();

	return retval;

}

static int sc_WarmReset(SmartCard_Dev *dev)
{
	int retval;

	ENTER();

	sc_DeactivateCard(dev);

	sc_uDelay(dev, 10000);	/*delay 10ms */

	sc_ActivateCard(dev);

	retval = sc_ATRCheck(dev);

	LEAVE();

	return retval;

}


/* ATRIB = A(nswer) T(o) R(eset) I(nterface) B(yte) */
#define SC_ATRIB_TAI	0
#define SC_ATRIB_TBI	1
#define SC_ATRIB_TCI	2
#define SC_ATRIB_TDI	3

static int sc_ParseATR(SmartCard_Dev * dev)
{
	int i, retval, j;
	unsigned char td,*atr = dev->atr, tmp;
	unsigned char n, t, f, d, tck;
	struct
    {
		unsigned char data;
		unsigned char present;
	}ibyte[4][4];

	ENTER();
#if 1
	memset(&ibyte, 0, sizeof(ibyte));

	atr ++;
	td = *atr++;
	dev->hb_len = td & 0x0f;

	i = 0;

	while(i<8)
    {

		/* TAi present */
		if( td & 0x10){
			ibyte[i][SC_ATRIB_TAI].data = *atr++;
			ibyte[i][SC_ATRIB_TAI].present =1 ;
		}

		/* TBi present */
		if( td & 0x20){
			ibyte[i][SC_ATRIB_TBI].data = *atr++;
			ibyte[i][SC_ATRIB_TBI].present =1 ;
		}

		/* TCi present */
		if( td & 0x40){
			ibyte[i][SC_ATRIB_TCI].data = *atr++;
			ibyte[i][SC_ATRIB_TCI].present =1 ;
		}

		/* TDi present */
		if( td & 0x80){
			ibyte[i][SC_ATRIB_TDI].data = *atr++;
			ibyte[i][SC_ATRIB_TDI].present =1 ;
			td = ibyte[i][SC_ATRIB_TDI].data;
			i++;
		}
		else
			break;

	}

	dev->hbyte = atr;

	f = d = 1;

	/* set f and d */
	if(ibyte[0][SC_ATRIB_TAI].present)
    {
		f = (ibyte[0][SC_ATRIB_TAI].data >> 4) & 0x0f;
		d = ibyte[0][SC_ATRIB_TAI].data  & 0x0f;
	}

	n = 0;

	if(ibyte[0][SC_ATRIB_TCI].present)
		n = ibyte[0][SC_ATRIB_TCI].data;

	t = 0;

	if(ibyte[1][SC_ATRIB_TAI].present)
    {   /* in specific mode */
		t = ibyte[1][SC_ATRIB_TAI].data & 0x0f;

		if(ibyte[1][SC_ATRIB_TAI].data & 0x10){
			f = 0; d= 1;		/* default value */
		}

		if(sc_MatchReader(dev, Ftab[f], Dtab[d]) == 0){	/* reader supported mode? */
			/* do nothing, */
			;
		}
		else
        {
			if(!(ibyte[1][SC_ATRIB_TAI].data & 0x80))
            {   /* capable to change mode */
				retval = sc_WarmReset(dev);	/* change to negotiable mode */
				if(retval != 0)
					return retval;
				f = 0; d = 1; t = 0;
				retval = sc_DoPPS(dev, f, d, t);	/* parameter and protocol select */
				if(retval != 0)
					return retval;
			}
			else
				return SC_ERR_LIB_UNSUPPORTEDCARD;
		}

	}
	else
    {
		/* whatever reader support or not , keep in default mode */

		f = 0; d = 1; // t = 0;

	}

	dev->para.fi = Ftab[f];
	dev->para.di = Dtab[d];
	dev->para.t = t;
	dev->para.n = n;

	/* calculate cwt, bwt, etu, etc... */
	if( t == 1){
		/* t = 1, not implemented yet */
		LOGD("Not supported T=1 protocol\n");
		return SC_ERR_LIB_UNSUPPORTEDCARD;

	}
	else{	/* t = 0 , or others */
		int wi = 10;

		if(ibyte[1][SC_ATRIB_TCI].present)
			wi = ibyte[1][SC_ATRIB_TCI].data;

		dev->para.bwt = dev->para.cwt = 960 * wi * dev->para.fi;
	}

	if(sc_DoPPS(dev, f, d, t) != 0)
		return SC_ERR_LIB_READ;

	sc_SetReaderPara(dev);
#endif

	dev->bInitSuccess = 1;

	LEAVE();

	return 0;
}

#undef SC_ATRIB_TAI
#undef SC_ATRIB_TBI
#undef SC_ATRIB_TCI
#undef SC_ATRIB_TDI

static int sc_ColdReset(SmartCard_Dev *dev)
{
	int retval;

	ENTER();

	sc_DeactivateCard(dev);
	sc_PowerOff(dev);

	/* delay 10 ms */
	sc_uDelay(dev, 10000);

	sc_PowerOn(dev);
	sc_uDelay(dev, 50000);
	sc_ActivateCard(dev);


	retval = sc_ATRCheck(dev);
	if(retval )
	{
		LOGD("retval = %d\n",retval);
		return retval;
	}

	retval = sc_ParseATR(dev);

	LEAVE();

	return retval;

}


/*��ÿһ������ֽ���,��������һ��ACK��NULL�ֽ��������������������ȥ,��
���ʵ��Ĳ�Ӧ���ʾ����ͬ,���ý�������SW1-SW2�����������*/
static int sc_T0_SendCommand(SmartCard_Dev *dev,
									const unsigned char *buf, int len,
									unsigned char * recv, int *count)
{
	char pbyte, INS = buf[1];
	int lc, le, local_count, pbl = 1;
	int retval = SC_ERR_LIB_CMD;

	int i;

	ENTER();

	/* check for incorrect length */
	if(len < 5 || len > MAX_CMD_LEN)
		return retval;

	if((len != 5) && (len != (buf[4] + 5)) && (len != (buf[4] + 6)))
		return retval;

	lc = 0; le = 0;			/* pick lc, le from command string */

	if(len == 5)
    {
		if( buf[4] != 0)
			le = buf[4];
	}
	else
    {
		if( len == (buf[4] + 5))
			lc = buf[4];
		else
        {
			lc = buf[4] + 1;
			le = buf[len -1];
			if(le == 0)			/* if le appears and it is equal to 0, it means
									max length(256) of data is needed */
				le = 256;
		}
	}

	local_count = 5;

	*count = 0;

	sc_ClearFIFO(dev);

	/* begin writting 5 bytes command header */
	if((retval = sc_RawWrite(dev, buf, &local_count)) != 0)
    {
		LOGD("raw write failed %d\n", retval);
		return SC_ERR_LIB_WRITE;
	}
    else
    {
        LOGD("SendCommand,rawWrite Success\n");
    }

	do
    {
		/* get procedure byte */
		do
        {
    		if((retval = sc_RawRead(dev, &pbyte, &pbl)) != 0)
            {
    			LOGD("raw read failed %d\n", retval);
    			return SC_ERR_LIB_READ;
    		}
            sc_uDelay(dev, 2);
		}
        while(pbyte == 0x60);//0X60 ��ʾNULL

        LOGD("get procedure byte,pbyte=%x INS=%x lc=%d le=%d\n", pbyte, INS, lc, le);

		if(pbyte == (INS ^ 0x01) || pbyte == INS)
        {//ACK ����ֽ�
			if(lc != 0)
				local_count = lc;
			else
				local_count = le;
		}
        else if((INS ^ 0xfe) == pbyte || (INS ^ 0xff) == pbyte)
        {//ACK ����ֽ�, �ӿ��豸���ֻ�����VPPΪ����״̬
        	local_count = 1;
        }
        else if( (pbyte&0xf0)==0x60 || (pbyte&0xf0)==0x90)
        {//�������� SW1 �� SW2
            LOGD("start raw read\n");
            sc_uDelay(dev, 2);

			recv[(*count) ++] = pbyte;		/* save SW1*/

			if((retval = sc_RawRead(dev, &pbyte, &pbl)) !=0)	/* read SW2 */
				return retval;

			recv[(*count)++] = pbyte;	/* save SW2 */

			/* wait for card turning into receiving mode */
			sc_uDelay(dev, 200);

			LEAVE();

			return 0;
		}
        else
		{
			LOGD("Unknown procedure byte %02x\n", pbyte);
			return SC_ERR_LIB_READ;
		}

		/* read/write card according lc, le and local_count */
		if(lc != 0)
        {
			/* if write, a delay is a must before write so that card can have time to
				enter receiving mode. At least 100us */
		    LOGD("sc raw write\n");
			sc_uDelay(dev, 200);

			if((retval = sc_RawWrite(dev, buf + len - lc, &local_count)) !=0 )
            {
				PDEBUG("Write Error (%d)\n", retval);
				return SC_ERR_LIB_WRITE;
			}
			lc -= local_count;
		}
		else
        {
            sc_uDelay(dev, 2);
            LOGD("sc raw read\n");
			if((retval = sc_RawRead(dev, recv + *count, &local_count)) != 0)
            {
				PDEBUG("Read Error(%d)\n", retval);
				return SC_ERR_LIB_READ;
			}
			le -= local_count;
			*count += local_count;
		}

		if(lc <0 || le < 0)
			return SC_ERR_LIB_READ;

	}while(1);
}

int sc_TestCardReady(SmartCard_Dev *dev)
{
	int reg, retval, i;

	ENTER();

	ioctl(dev->fd, SC_IOC_GETSTATUS, &reg);

    LOGD("testcard, reg = %x\n", reg);

	if( (reg & 0x80000000) == 0)
		return SC_ERR_LIB_CARDREMOVED;
	else
		if((reg & 0x7fffffff) == 0)
			return 0;

	/* 3v start */
//	dev->vclass = SC_ISO_OPERATIONCLASS_B;

	retval = sc_ColdReset(dev);
#if 0
	for( i = 0; i < 2; i++){

		if(retval = sc_ColdReset(dev)){
			dev->vclass = SC_ISO_OPERATIONCLASS_A;
			continue;
		}

		break;
	}
#endif
	if(retval != 0)
		retval = SC_ERR_LIB_UNSUPPORTEDCARD;

	LEAVE();

	return retval;
}

int sc_GetState(SmartCard_Dev *dev)
{
	int reg, retval, i;

	if(!dev->bInitSuccess)
		return SC_ERR_LIB_UNSUPPORTEDCARD;

	ioctl(dev->fd, SC_IOC_GETSTATUS, &reg);

	if( (reg & 0x80000000) == 0)
		return SC_ERR_LIB_CARDREMOVED;
	else{
		if((reg & 0x7fffffff) == 0)
			return 0;
		else
			return SC_ERR_LIB_CARDCHANGED;
	}
}

int sc_IsCardRemoved(SmartCard_Dev *dev)
{
	int retval;

	ioctl(dev->fd, SC_IOC_GETSTATUS, &retval);

	if( (retval & 0x80000000) == 0)
		return SC_ERR_LIB_CARDREMOVED;

	return 0;
}

int sc_Open(const char *devname, SmartCard_Dev *dev)
{
	int retval, i;

	ENTER();

	dev->fd = open(devname, O_RDWR);

	if(dev->fd < 0)
		return SC_ERR_LIB_DEVICEBUSY;

	PDEBUG("Open Success\n");

	dev->bInitSuccess = 0;

    retval = ioctl(dev->fd, SC_IOC_READ_DEVNUM, &i);
    LOGD("-----------read total card num = %d, retval = %d\n", i, retval);

	LEAVE();

	return 0;

sc_open_err:

	close(dev->fd);
	return retval;
}

void sc_Close(SmartCard_Dev *dev)
{
	ENTER();

	close(dev->fd);

	LEAVE();
}

int sc_SendCommand(SmartCard_Dev *dev,
					const unsigned char *send, int len,
						  unsigned char *recv, int *count)
{
	int retval;

	ENTER();

	retval = sc_TestCardReady(dev);
	if(retval)
	{
        LOGD("send command, test card ready err\n");
		return retval;
    }
    else
    {
        LOGD("send command, test card ready Sucess\n");
    }

	retval = sc_T0_SendCommand(dev, send, len, recv, count);
    if(retval)
    {
        LOGD("send command, T0 SendCommand err\n");
    }else
    {
        LOGD("send command, T0 SendCommand Success\n");
    }

	LEAVE();

	return retval;
}

int sc_ApduCommand(SmartCard_Dev *dev, int cla, int ins, int p1, int p2,
					int lc, char *data, int le, char *resp)
{
	char buf[512];
	int i, retval;

	i = 0;

	buf[i++] = cla;
	buf[i++] = ins;

	if(p1 >= 0 && p1 <= 255)
		buf[i++] = (p1 & 0xff);

	if(p2 >= 0 && p2 <= 255)
		buf[i++] = (p2 & 0xff);

	if(lc >= 0 && lc <= 255){
		buf[i++] = (lc & 0xff);
		memcpy(buf + i, data, (lc & 0xff));
		i+= (lc & 0xff);
	}

	if(le >= 0 && le <= 255)
		buf[i++] = (le & 0xff);

	if(sc_SendCommand(dev, buf, i, resp, &retval))
		retval = 0;

	return retval;

}

/* Standard Command */
int sc_SelectFile(SmartCard_Dev *dev, char *file_id, char *resp)
{

	return sc_ApduCommand(dev,
			0, 0xa4, 0, 0, 2, file_id, -1, resp);
}

int sc_SelectApp(SmartCard_Dev *dev, UINT8 applen, char *app, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0xa4, 4, 0, applen, app, -1, resp);

}

int sc_GetResponse(SmartCard_Dev *dev, UINT8 le, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0xc0, 0, 0, -1, NULL, le, resp);
}

int sc_GetChallenge4(SmartCard_Dev *dev, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0x84, 0, 0, -1, NULL, 4, resp);

}

int sc_GetChallenge8(SmartCard_Dev *dev, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0x84, 0, 0, -1, NULL, 8, resp);
}

int sc_ExternalAuthenticate(SmartCard_Dev *dev, UINT8 key_id, UINT8 lc, char *val, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0x82, 0, key_id, 8, val, -1, resp);

}

int sc_InternalAuthenticate(SmartCard_Dev *dev,  UINT8 key_id, UINT8 len, char *val, UINT8 le, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0x88, 0, key_id, 8, val, -1, resp);

}

int sc_ReadBinary_CUR(SmartCard_Dev *dev, UINT8 pos, UINT8 len, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0xb0, (pos>>8) & 0x7f, pos & 0xff, -1, NULL, len, resp);

}

int sc_ReadBinary_SFI(SmartCard_Dev *dev, UINT8 SFI, UINT8 pos, UINT8 len, char *resp)
{
	return sc_ApduCommand(dev,
		0, 0xb0, (SFI & 0x1f) | 0x80, pos, -1, NULL, len, resp);
}

int sc_UpdateBinary_CUR(SmartCard_Dev *dev, UINT8 pos, UINT8 len, char *val, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0xd6, (pos>>8) & 0x7f, pos & 0xff, len, val, -1, resp);


}
int sc_UpdateBinary_SFI(SmartCard_Dev *dev, UINT8 SFI, UINT8 pos, UINT8 len, char *val, char *resp)
{


	return sc_ApduCommand(dev,
		0, 0xd6,  (SFI & 0x1f) | 0x80, pos, len, val, -1, resp);


}
int sc_UpdateBinary_CUR_MAC(SmartCard_Dev *dev, UINT8 pos, UINT8 len, char *val, char *resp)
{

	return sc_ApduCommand(dev,
		4, 0xd6, (pos>>8) & 0x7f, pos & 0xff, len, val, -1, resp);

}
int sc_UpdateBinary_SFI_MAC(SmartCard_Dev *dev, UINT8 SFI, UINT8 pos, UINT8 len, char *val, char *resp)
{

	return sc_ApduCommand(dev,
		4, 0xd6,  (SFI & 0x1f) | 0x80, pos, len, val, -1, resp);

}

int sc_ReadRecord_CUR(SmartCard_Dev *dev, UINT8 rno, UINT8 len, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0xb2, rno, 0x04, -1, NULL, len, resp);

}

int sc_ReadRecord_SFI(SmartCard_Dev *dev, UINT8 rno, UINT8 SFI, UINT8 len, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0xb2, rno, ((SFI << 3) & 0xf8) | 0x04, -1, NULL, len, resp);

}

int sc_AppendRecord_CUR(SmartCard_Dev *dev, UINT8 len, char *val, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0xe2, 0, 0, len, val, -1, resp);

}
int sc_AppendRecord_SFI(SmartCard_Dev *dev, UINT8 SFI, UINT8 len, char *val, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0xe2, 0, (SFI<<3)&0xf8, len, val, -1, resp);

}
int sc_AppendRecord_CUR_MAC(SmartCard_Dev *dev, UINT8 len, char *val, char *resp)
{

	return sc_ApduCommand(dev,
		4, 0xe2, 0, 0, len, val, -1, resp);

}
int sc_AppendRecord_SFI_MAC(SmartCard_Dev *dev, UINT8 SFI, UINT8 len, char *val, char *resp)
{

	return sc_ApduCommand(dev,
		4, 0xe2, 0, (SFI<<3)&0xf8, len, val, -1, resp);

}

int sc_UpdateRecord_CUR(SmartCard_Dev *dev, UINT8 rno, UINT8 len, char *val, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0xdc, rno, 0x04, len, val, -1, resp);
}

int sc_UpdateRecord_SFI(SmartCard_Dev *dev, UINT8 rno, UINT8 SFI, UINT8 len, char *val, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0xdc, rno, ((SFI<<3)&0xf8)|0x04, len, val, -1, resp);


}

int sc_UpdateRecord_CUR_MAC(SmartCard_Dev *dev, UINT8 rno, UINT8 len, char *val, char *resp)
{

	return sc_ApduCommand(dev,
		4, 0xdc, rno, 0x04, len, val, -1, resp);

}

int sc_UpdateRecord_SFI_MAC(SmartCard_Dev *dev, UINT8 rno, UINT8 SFI, UINT8 len, char *val, char *resp)
{

	return sc_ApduCommand(dev,
		4, 0xdc, rno, ((SFI<<3)&0xf8)|0x04, len, val, -1, resp);


}

int sc_VerifyPIN(SmartCard_Dev *dev, UINT8 PIN_id, UINT8 lc, char *PIN, char *resp)
{

	return sc_ApduCommand(dev,
		0, 0x20, 0, PIN_id, lc, PIN, -1, resp);

}

/* Tax card Command */
int sc_GetRegisterNB(SmartCard_Dev *dev, char *resp)
{

	return sc_ApduCommand(dev,
		0xc0, 0xf0, 0, 0, -1, NULL, 0x10, resp);

}

int sc_TerminalRegister(SmartCard_Dev *dev, char *MAC2, char *resp)
{

	return sc_ApduCommand(dev,
		0xc0, 0xf1, 0, 0, 4, MAC2, -1, resp);
}

int sc_IssueInvoice(SmartCard_Dev *dev, char *data, char *resp)
{

	return sc_ApduCommand(dev,
		0xc0, 0xf2, 0, 0, 0x2c, data, -1, resp);
}

int sc_DeclareDuty(SmartCard_Dev *dev, char *data, char *resp)
{


	return sc_ApduCommand(dev,
		0xc0, 0xf4, 0, 0, 0x47, data, -1, resp);

}

int sc_UpdateControls(SmartCard_Dev *dev, char *data, char *resp)
{


	return sc_ApduCommand(dev,
		0xc0, 0xf6, 0, 0, 0x1d, data, -1, resp);

}

int sc_InputInvoiceNB(SmartCard_Dev *dev, char *data, char *resp)
{


	return sc_ApduCommand(dev,
		0xc0, 0xf7, 0, 0, 0x16, data, -1, resp);

}

int sc_VerifyFiscal_PIN(SmartCard_Dev *dev, char *PIN, char *resp)
{


	return sc_ApduCommand(dev,
		0xc0, 0xf9, 0, 0, 0x08, PIN, -1, resp);

}

int sc_DailyCollectSign(SmartCard_Dev *dev, char *data, char *resp)
{
	return sc_ApduCommand(dev,
		0xc0, 0xfb, 0, 0, 0x41, data, -1, resp);
}

/* User Card Command */
int sc_RegisterSign(SmartCard_Dev *dev, char *data, char *resp)
{
	return sc_ApduCommand(dev,
		0xc0, 0xe4, 0, 0, 0x10, data, -1, resp);
}

int sc_DataCollect(SmartCard_Dev *dev, char *data, char *resp)
{
	return sc_ApduCommand(dev,
		0xc0, 0xe6, 0, 0, 0xdc, data, -1, resp);
}

int sc_EchoplexControls(SmartCard_Dev *dev, char *data, char *resp)
{
	return sc_ApduCommand(dev,
		0xc0, 0xe8, 0, 0, 0x1f, data, -1, resp);
}

int sc_EchoplexControlsCipher(SmartCard_Dev *dev, char *data, char *resp)
{
	return sc_ApduCommand(dev,
		0xc0, 0xe8, 0, 0, 0x25, data, -1, resp);
}

int sc_DistributeInvoiceNB(SmartCard_Dev *dev, char *resp)
{
	return sc_ApduCommand(dev,
		0xc0, 0xe9, 0, 0, -1, NULL, 0x16, resp);
}

