#ifndef _WINBOND_SMARTCARD_H
/****************************************************************************
 *
 * Copyright (c) 2004 - 2006 Winbond Electronics Corp. All rights reserved.
 *
 ****************************************************************************/

/****************************************************************************
 *
 * FILENAME
 *     sc_lib.h
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
 *    2005/09/14		Modified for simple implementation
 *
 * REMARK
 *     Do not open /dev/smartcard directly, use this library.
 *
 *************************************************************************/

#define _WINBOND_SMARTCARD_H

#include <sys/types.h>
#include <sys/ioctl.h>

typedef unsigned char UINT8;

#define MAX_ATR_LEN			(33)
#define MAX_CMD_LEN			(0x200)

struct sc_parameter{
	unsigned int n, t;		/* amount of etu in cycles, and others are in etus */
	unsigned int cwt, bwt;
	unsigned int fi, di;
};


/* define I/O control command */
#define SMARTCARD_IOC_MAGIC	's'

#define SC_IOC_MAXNR			19

#define SC_IOC_POWERON			_IOW(SMARTCARD_IOC_MAGIC, 0, int)		/* card power on */
#define SC_IOC_POWEROFF		_IO(SMARTCARD_IOC_MAGIC, 1)			/* card power off */

/* this two command is for power saving */
#define SC_IOC_POWERUP			_IO(SMARTCARD_IOC_MAGIC, 2)			/* card power up */
#define SC_IOC_POWERDOWN		_IO(SMARTCARD_IOC_MAGIC, 3)			/* card power down */

#define SC_IOC_ACTIVATE			_IO(SMARTCARD_IOC_MAGIC, 4)			/* activate card and save ATR to buffer,
																			you can use GETATR to retrieve */
#define SC_IOC_DEACTIVATE		_IO(SMARTCARD_IOC_MAGIC, 5)			/* deactivate */
#define SC_IOC_C4C8READ		_IOR(SMARTCARD_IOC_MAGIC, 6, int)		/* for general use */
#define SC_IOC_C4C8WRITE		_IOW(SMARTCARD_IOC_MAGIC, 7, int)
#define SC_IOC_SETPARAMETER	_IOW(SMARTCARD_IOC_MAGIC, 8, struct sc_parameter *)	/* set reader parameter to adjust card */
#define SC_IOC_CLEARFIFO		_IO(SMARTCARD_IOC_MAGIC, 9)			/* clear reception buffer */
#define SC_IOC_GETSTATUS		_IOR(SMARTCARD_IOC_MAGIC, 10, int)		/* check card present */
#define SC_IOC_SELECTCARD		_IO(SMARTCARD_IOC_MAGIC, 11)			/* for multi-card  use */
#define SC_IOC_GETATR			_IOR(SMARTCARD_IOC_MAGIC, 12, int)
#define SC_IOC_GETERRORNO		_IOR(SMARTCARD_IOC_MAGIC, 13, int)		/* get error number */
#define SC_IOC_MATCHREADER	_IOR(SMARTCARD_IOC_MAGIC, 14, int)		/* check whether the reader support the baudrate or not */

#define SC_IOC_SETPARITY		_IOW(SMARTCARD_IOC_MAGIC, 15, int)
#define SC_IOC_SETCLOCK			_IOW(SMARTCARD_IOC_MAGIC, 16, int)


#define SC_IOC_READ_DEVNUM      _IOW(SMARTCARD_IOC_MAGIC, 18, int)   /*read total card num*/
#define SC_IOC_SET_DEV          _IOW(SMARTCARD_IOC_MAGIC, 19, int)   /*set now used card num*/

/* iso7816 operation class */
#define SC_ISO_OPERATIONCLASS_A	(0x01)
#define SC_ISO_OPERATIONCLASS_B	(0x02)

/* only used by library */
#define SC_ERR_LIBRARY					(0x80)
#define SC_ERR_LIB_CARDREMOVED		(0x01 | SC_ERR_LIBRARY)
#define SC_ERR_LIB_CMD					(0x02 | SC_ERR_LIBRARY)
#define SC_ERR_LIB_KEY					(0x03 | SC_ERR_LIBRARY)
#define SC_ERR_LIB_UNSUPPORTEDCARD	(0x04 | SC_ERR_LIBRARY)
#define SC_ERR_LIB_DEVICEBUSY			(0x05 | SC_ERR_LIBRARY)
#define SC_ERR_LIB_READ					(0x06 | SC_ERR_LIBRARY)
#define SC_ERR_LIB_WRITE				(0x07 | SC_ERR_LIBRARY)
#define SC_ERR_LIB_CARDCHANGED		(0x08 | SC_ERR_LIBRARY)

typedef struct sc_SmartCard_Dev{
	int fd;
	unsigned char atr[33], *hbyte;
	unsigned int atr_len, hb_len;
	unsigned int vclass, conv;
	unsigned int bInitSuccess;

	struct sc_parameter para;
}SmartCard_Dev;

extern int sc_TestCardReady(SmartCard_Dev *dev);
extern int sc_GetState(SmartCard_Dev *dev);
extern int sc_IsCardRemoved(SmartCard_Dev *dev);
extern int sc_Open(const char *devname, SmartCard_Dev *dev);
extern void sc_Close(SmartCard_Dev *dev);
extern int sc_SendCommand(SmartCard_Dev *dev,
					const unsigned char *send, int len,
						  unsigned char *recv, int *count);
extern int sc_ApduCommand(SmartCard_Dev *dev, int cla, int ins, int p1, int p2,
					int lc, char *data, int le, char *resp);

/* Standard Command */
extern int sc_SelectFile(SmartCard_Dev *dev, char *file_id, char *resp);
extern int sc_SelectApp(SmartCard_Dev *dev, UINT8 applen, char *app, char *resp);
extern int sc_GetResponse(SmartCard_Dev *dev, UINT8 le, char *resp);
extern int sc_GetChallenge4(SmartCard_Dev *dev, char *resp);
extern int sc_GetChallenge8(SmartCard_Dev *dev, char *resp);
extern int sc_ExternalAuthenticate(SmartCard_Dev *dev, UINT8 key_id, UINT8 lc, char *val, char *resp);
extern int sc_InternalAuthenticate(SmartCard_Dev *dev,  UINT8 key_id, UINT8 len, char *val, UINT8 le, char *resp);
extern int sc_ReadBinary_CUR(SmartCard_Dev *dev, UINT8 pos, UINT8 len, char *resp);
extern int sc_ReadBinary_SFI(SmartCard_Dev *dev, UINT8 SFI, UINT8 pos, UINT8 len, char *resp);
extern int sc_UpdateBinary_CUR(SmartCard_Dev *dev, UINT8 pos, UINT8 len, char *val, char *resp);
extern int sc_UpdateBinary_SFI(SmartCard_Dev *dev, UINT8 SFI, UINT8 pos, UINT8 len, char *val, char *resp);
extern int sc_UpdateBinary_CUR_MAC(SmartCard_Dev *dev, UINT8 pos, UINT8 len, char *val, char *resp);
extern int sc_UpdateBinary_SFI_MAC(SmartCard_Dev *dev, UINT8 SFI, UINT8 pos, UINT8 len, char *val, char *resp);
extern int sc_ReadRecord_CUR(SmartCard_Dev *dev, UINT8 rno, UINT8 len, char *resp);
extern int sc_ReadRecord_SFI(SmartCard_Dev *dev, UINT8 rno, UINT8 SFI, UINT8 len, char *resp);
extern int sc_AppendRecord_CUR(SmartCard_Dev *dev, UINT8 len, char *val, char *resp);
extern int sc_AppendRecord_SFI(SmartCard_Dev *dev, UINT8 SFI, UINT8 len, char *val, char *resp);
extern int sc_AppendRecord_CUR_MAC(SmartCard_Dev *dev, UINT8 len, char *val, char *resp);
extern int sc_AppendRecord_SFI_MAC(SmartCard_Dev *dev, UINT8 SFI, UINT8 len, char *val, char *resp);
extern int sc_UpdateRecord_CUR(SmartCard_Dev *dev, UINT8 rno, UINT8 len, char *val, char *resp);
extern int sc_UpdateRecord_SFI(SmartCard_Dev *dev, UINT8 rno, UINT8 SFI, UINT8 len, char *val, char *resp);
extern int sc_UpdateRecord_CUR_MAC(SmartCard_Dev *dev, UINT8 rno, UINT8 len, char *val, char *resp);
extern int sc_UpdateRecord_SFI_MAC(SmartCard_Dev *dev, UINT8 rno, UINT8 SFI, UINT8 len, char *val, char *resp);
extern int sc_VerifyPIN(SmartCard_Dev *dev, UINT8 PIN_id, UINT8 lc, char *PIN, char *resp);

/* Tax Card Command */
extern int sc_GetRegisterNB(SmartCard_Dev *dev, char *resp);
extern int sc_TerminalRegister(SmartCard_Dev *dev, char *MAC2, char *resp);
extern int sc_IssueInvoice(SmartCard_Dev *dev, char *data, char *resp);
extern int sc_DeclareDuty(SmartCard_Dev *dev, char *data, char *resp);
extern int sc_UpdateControls(SmartCard_Dev *dev, char *data, char *resp);
extern int sc_InputInvoiceNB(SmartCard_Dev *dev, char *data, char *resp);
extern int sc_VerifyFiscal_PIN(SmartCard_Dev *dev, char *PIN, char *resp);
extern int sc_DailyCollectSign(SmartCard_Dev *dev, char *data, char *resp);

/* User Card Command */
extern int sc_RegisterSign(SmartCard_Dev *dev, char *data, char *resp);
extern int sc_DataCollect(SmartCard_Dev *dev, char *data, char *resp);
extern int sc_EchoplexControls(SmartCard_Dev *dev, char *data, char *resp);
extern int sc_EchoplexControlsCipher(SmartCard_Dev *dev, char *data, char *resp);
extern int sc_DistributeInvoiceNB(SmartCard_Dev *dev, char *resp);

#endif
