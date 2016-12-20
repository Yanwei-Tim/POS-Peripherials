#ifndef _RFID_H
#define _RFID_H

typedef unsigned char         Uchar;
typedef unsigned short int    Uint;
typedef unsigned long         Ulong;
typedef unsigned char         u8;
typedef unsigned short int    u16;
typedef unsigned long         u32;


typedef struct
{
    unsigned char pkg_no;
    unsigned char cmd;
    unsigned char len;
    unsigned char dat[22];      // check write
    unsigned char bcc;
}__attribute__ ((packed))MODULE_PKG_T;

typedef struct
{
    MODULE_PKG_T rxbuf;        // rx buffer
    MODULE_PKG_T txbuf;        // tx buffer
}__attribute__ ((packed))MODULE_T;

typedef union
{
    Uchar c[4];
    Uint  i[2];
    Ulong l;
}__attribute__ ((packed))LONGCHAR_T;




Uchar  Mifs_Config(int fd);
Uchar  Mifs_Request(int fd,Uchar mode,Uchar * TagType);
Uchar  Mifs_Anticoll(int fd,Uchar Bcnt,Uchar * SNR);           // ·ÀÖ¹Åö×²
Uchar  Mifs_Anticoll2(int fd,Uchar Encoll,Uchar Bcnt,Uchar * SNR);           // ·ÀÖ¹Åö×²
Uchar  Mifs_Close(int fd);
Uchar  Mifs_Select(int fd,const char *Snt,char *cardsize);
Uchar  Mifs_AuthKey(int fd,Uchar Mode,Uchar SecNr,char * Key);
Uchar  Mifs_Read(int fd,Uchar Adr,Uchar * Data);
Uchar  Mifs_Write(int fd,Uchar Adr,char * Data);
void Putbuf(Uchar *p,Uint len);


#endif
