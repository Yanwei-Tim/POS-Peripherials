/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class aclasdriver_SerialPort */

#ifndef _Included_aclasdriver_SerialPort
#define _Included_aclasdriver_SerialPort
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     aclasdriver_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;II)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_aclasdriver_SerialPort_open
  (JNIEnv *, jclass, jstring, jint, jint);

/*
 * Class:     aclasdriver_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_aclasdriver_SerialPort_close
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
