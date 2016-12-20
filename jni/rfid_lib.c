/****************************************************************************
 *
 *Copyright (c) 2010 Pinnacle Electronics Corp. All rights reserved.
 *
 ***************************************************************************/

/****************************************************************************
 *
 * FILENAME
 *     rfid_lib.c
 *
 * VERSION
 *     1.0
 *
 * DESCRIPTION
 *     DEMO PROGRAM OF READ-WRITE CONTROL OF Mifare CARD
 *
 * DATA STRUCTURES
 *     None
 *
 * FUNCTIONS
 *     None
 *
 * HISTORY
 *     2010/06/01		Ver 1.0 Created by Eric
 *
 * REMARK
 *     None
 **************************************************************************/

#include <sys/types.h>
#include <sys/stat.h>
#include <asm/fcntl.h>
#include <sys/ioctl.h>
#include <stdio.h>
#include <getopt.h>
#include <string.h>
#include <signal.h>

#include <asm/byteorder.h>
#include <linux/errno.h>
#include <linux/types.h>
#include <sys/time.h>
#include "rfid_lib.h"

#include "android/log.h"
static const char *TAG = "AclasArmPosDBG";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)


#define     Rfid_ReadCard			   _IOW('c', 1, unsigned int)


// void Putbuf(Uchar *p,Uint len)
//{
//    Uint i;
//    for(i=0; i<len; i ++)
//    {
//        printf("%02x ",p[i]);
//    }
//    printf("\n");
//}


Uchar mifs_request(int fd,Uchar mode,Uchar *TagType)
{
    Uchar     result;
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x41;
    pt.rxbuf.len = 0x01;
    pt.rxbuf.dat[0] = mode;

    result = ioctl(fd,Rfid_ReadCard,&pt);
    if( !result )
    {
        memcpy(TagType,&pt.txbuf.dat,pt.txbuf.len);
    }
    return result;
}
Uchar mifs_Anticoll(int fd,Uchar Bcnt,Uchar *SNR)
{
    Uchar     result;
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x42;
    pt.rxbuf.len = 0x01;
    pt.rxbuf.dat[0] = Bcnt;

    result = ioctl(fd,Rfid_ReadCard,&pt);
    memcpy(SNR,&pt.txbuf.dat,pt.txbuf.len);
    return result;
}

Uchar  mifs_select(int fd,const Uchar *Snr,char *cardlen)
{
    Uchar     result;
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x43;
    pt.rxbuf.len = 0x04;
    memcpy(pt.rxbuf.dat,Snr,4);
    result = ioctl(fd,Rfid_ReadCard,&pt);
    memcpy(cardlen,&pt.txbuf.dat,pt.txbuf.len);
    return result;
}

Uchar mifs_Authentication(int fd,Uchar Mode,Uchar SecNr)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x44;
    pt.rxbuf.len = 0x02;
    pt.rxbuf.dat[0] = Mode;
    pt.rxbuf.dat[1] = SecNr;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar  mifs_halt(int fd)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x45;
    pt.rxbuf.len = 0x00;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_Read(int fd,Uchar Adr,Uchar *Data)
{
    Uchar     result;
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x46;
    pt.rxbuf.len = 0x01;
    pt.rxbuf.dat[0] = Adr;

    result = ioctl(fd,Rfid_ReadCard,&pt);
    memcpy(Data,&pt.txbuf.dat,pt.txbuf.len);
    //LOGD("recv len = %u", pt.txbuf.len);
    return result;
}

Uchar mifs_Write(int fd,Uchar Adr,Uchar *Data)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x47;
    pt.rxbuf.len = 0x11;
    pt.rxbuf.dat[0] = Adr;
    memcpy(&pt.rxbuf.dat[1],Data,16);

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_increment(int fd,Uchar Adr,long Value)
{
   MODULE_T  pt;
   LONGCHAR_T a;
   a.l = Value;
   pt.rxbuf.cmd = 0x48;
   pt.rxbuf.len = 0x05;
   pt.rxbuf.dat[0] = Adr;

   pt.rxbuf.dat[1] = a.c[0];    //HIGH
   pt.rxbuf.dat[2] = a.c[1];    //LOW

   return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_decrement(int fd,Uchar Adr,long Value)
{
    MODULE_T  pt;
    LONGCHAR_T a;
    a.l = Value;
    pt.rxbuf.cmd = 0x49;
    pt.rxbuf.len = 0x05;
    pt.rxbuf.dat[0] = Adr;
    pt.rxbuf.dat[1] = a.c[0];    //HIGH
    pt.rxbuf.dat[2] = a.c[1];  //LOW

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_restore(int fd,Uchar Adr)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x4A;
    pt.rxbuf.len = 0x01;
    pt.rxbuf.dat[0] = Adr;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_transfer(int fd,Uchar Adr)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x4B;
    pt.rxbuf.len = 0x01;
    pt.rxbuf.dat[0] = Adr;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_load_key(int fd,Uchar Mode,Uchar SecNr,Uchar *Nkey)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x4C;
    pt.rxbuf.len = 0x08;
    pt.rxbuf.dat[0] = Mode;
    pt.rxbuf.dat[1] = SecNr;
    memcpy(&pt.rxbuf.dat[2],Nkey,6);

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_reset(int fd,Uchar Msec)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x4E;
    pt.rxbuf.len = 0x01;
    pt.rxbuf.dat[0] = Msec;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_get_info(int fd,Uchar *Info)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x4F;
    pt.rxbuf.len = 0x00;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_set_control_bit(int fd)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x50;
    pt.rxbuf.len = 0x00;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_clr_control_bit(int fd)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x51;
    pt.rxbuf.len = 0x00;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_set_config(int fd)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x52;
    pt.rxbuf.len = 0x00;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_check_write(int fd,Uchar *Snr,Uchar Authenmode,Uchar Adr,Uchar *Data)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x53;
    pt.rxbuf.len = 22;
    memcpy(&pt.rxbuf.dat[0],Snr,4);
    pt.rxbuf.dat[4] = Authenmode;
    pt.rxbuf.dat[5] = Adr;
    memcpy(&pt.rxbuf.dat[6],Data,16);

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_read_E2(int fd,Uchar Adr,Uchar Lengthe,Uchar *Data)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x61;
    pt.rxbuf.len = 02;
    pt.rxbuf.dat[0] = Adr;
    pt.rxbuf.dat[1] = Lengthe;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_write_E2(int fd,Uchar Adr,Uchar Lengthe,Uchar *Data)
{
    Uchar     result;
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x62;
    pt.rxbuf.len = Lengthe + 2;
    pt.rxbuf.dat[0] = Adr;
    pt.rxbuf.dat[1] = Lengthe;
    memcpy(&pt.rxbuf.dat[2],Data,Lengthe);

    result = ioctl(fd,Rfid_ReadCard,&pt);
    memcpy(Data,&pt.txbuf.dat,pt.txbuf.len);
    return result;
}

Uchar mifs_close(int fd)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x3F;
    pt.rxbuf.len = 0x00;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_value(int fd,Uchar mode,Uchar Adr,long Value,Uchar Trans_Adr)
{
    MODULE_T  pt;
    LONGCHAR_T a;
    a.l = Value;
    pt.rxbuf.cmd = 0x70;
    pt.rxbuf.len = 0x07;
    pt.rxbuf.dat[0] = mode;
    pt.rxbuf.dat[1] = Adr;
    pt.rxbuf.dat[2] = a.c[3];
    pt.rxbuf.dat[3] = a.c[2];
    pt.rxbuf.dat[4] = a.c[1];
    pt.rxbuf.dat[5] = a.c[0];
    pt.rxbuf.dat[6] = Trans_Adr;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}
Uchar mifs_Anticoll2(int fd,Uchar Encoll,Uchar Bcnt,Uchar *SNR)
{
    Uchar     result;
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x71;
    pt.rxbuf.len = 0x02;
    pt.rxbuf.dat[0] = Encoll;
    pt.rxbuf.dat[1] = Bcnt;

    result = ioctl(fd,Rfid_ReadCard,&pt);
    memcpy(SNR,&pt.txbuf.dat,pt.txbuf.len);
    return result;
}

Uchar mifs_Authentication2(int fd,Uchar Mode,Uchar SecNr,Uchar KeyNr)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x72;
    pt.rxbuf.len = 0x03;
    pt.rxbuf.dat[0] = Mode;
    pt.rxbuf.dat[1] = SecNr;
    pt.rxbuf.dat[2] = KeyNr;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_Authentication3(int fd,Uchar Mode,Uchar SecNr,Uchar *Key)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x73;
    pt.rxbuf.len = 0x08;
    pt.rxbuf.dat[0] = Mode;
    pt.rxbuf.dat[1] = SecNr;
    memcpy(&pt.rxbuf.dat[2], Key, 6);
    LOGD("mifs_Authentication3, fd = %d, Mode=%u, SecNr = %u, size = %d", fd , Mode, SecNr, (int)sizeof(MODULE_T));
//    return 0;
    return(ioctl(fd, Rfid_ReadCard, &pt));
}
Uchar mifs_Cascanticoll(int fd,Uchar Select_Code,Uchar Bcnt,Uchar *SNR)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x74;
    pt.rxbuf.len = 0x02;
    pt.rxbuf.dat[0] = Select_Code;
    pt.rxbuf.dat[1] = Bcnt;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_Cascselect(int fd,Uchar Select_Code,Uchar *Snr)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x75;
    pt.rxbuf.len = 0x05;
    pt.rxbuf.dat[0] = Select_Code;
    memcpy(&pt.rxbuf.dat[1],Snr,4);

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_ULwrite(int fd,Uchar Adr, Uchar *Data,Uchar Sak)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x76;
    pt.rxbuf.len = 0x05;
    pt.rxbuf.dat[0] = Adr;
    memcpy(&pt.rxbuf.dat[1],Data,4);

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_valueDebit(int fd,Uchar mode,Uchar Adr,long Value)
{
    MODULE_T  pt;
    LONGCHAR_T a;
    a.l = Value;
    pt.rxbuf.cmd = 0x77;
    pt.rxbuf.len = 0x06;
    pt.rxbuf.dat[0] = mode;
    pt.rxbuf.dat[1] = Adr;
    pt.rxbuf.dat[2] = a.c[3];
    pt.rxbuf.dat[3] = a.c[2];
    pt.rxbuf.dat[4] = a.c[1];
    pt.rxbuf.dat[5] = a.c[0];

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_write_reg(int fd,Uchar Reg,Uchar Value)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x3D;
    pt.rxbuf.len = 0x02;
    pt.rxbuf.dat[0] = Reg;
    pt.rxbuf.dat[1] = Value;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

Uchar mifs_read_reg(int fd,Uchar Reg,Uchar *Value)
{
    MODULE_T  pt;
    pt.rxbuf.cmd = 0x3E;
    pt.rxbuf.len = 0x01;
    pt.rxbuf.dat[0] = Reg;

    return(ioctl(fd,Rfid_ReadCard,&pt));
}

//=============================================================================
//
//              调用Middle 层的函数
//
//=============================================================================
 Uchar  Mifs_Config(int fd)
{
	return mifs_set_config(fd);
}

 Uchar  Mifs_Request(int fd,Uchar mode,Uchar * TagType)
{
	return mifs_request(fd,mode,TagType);
}

  Uchar  Mifs_Anticoll(int fd,Uchar Bcnt,Uchar *SNR)           // ��ֹ��ײ
 {
     return mifs_Anticoll(fd,Bcnt,SNR);
 }


 Uchar  Mifs_Anticoll2(int fd,Uchar Encoll,Uchar Bcnt,Uchar *SNR)           // ��ֹ��ײ
{
	return mifs_Anticoll2(fd,Encoll,Bcnt,SNR);
}

 Uchar  Mifs_Close(int fd)
{
	return mifs_close(fd);
}

 Uchar  Mifs_Select(int fd,const char *Snt,char *cardsize)
{
	return mifs_select(fd,Snt,cardsize);
}

 Uchar  Mifs_AuthKey(int fd,Uchar Mode,Uchar SecorNum,char * Key)
{
     LOGD("Mifs_AuthKey");
	return mifs_Authentication3(fd, Mode, SecorNum, Key);
}

 Uchar  Mifs_Read(int fd,Uchar Adr,Uchar * Data)               // ��ȡ�û�����
{
	return mifs_Read(fd,Adr,Data);
}

Uchar  Mifs_Write(int fd,Uchar Adr,char * Data)
{
	return  mifs_Write(fd,Adr,Data);
}


