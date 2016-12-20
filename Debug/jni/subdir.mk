################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../jni/aclas-lcd0.c \
../jni/aclasHdq.c \
../jni/aclasMagcard.c \
../jni/aclasdriver_SerialPort.c \
../jni/aclasdriver_SmartCard.c \
../jni/aclasdriver_UsbRfid.c \
../jni/aclasdriver_drawer.c \
../jni/aclasdriver_km3_printer.c \
../jni/aclasdriver_printer.c \
../jni/aclasdriver_rfid.c \
../jni/rfid_lib.c \
../jni/sc_lib.c 

OBJS += \
./jni/aclas-lcd0.o \
./jni/aclasHdq.o \
./jni/aclasMagcard.o \
./jni/aclasdriver_SerialPort.o \
./jni/aclasdriver_SmartCard.o \
./jni/aclasdriver_UsbRfid.o \
./jni/aclasdriver_drawer.o \
./jni/aclasdriver_km3_printer.o \
./jni/aclasdriver_printer.o \
./jni/aclasdriver_rfid.o \
./jni/rfid_lib.o \
./jni/sc_lib.o 

C_DEPS += \
./jni/aclas-lcd0.d \
./jni/aclasHdq.d \
./jni/aclasMagcard.d \
./jni/aclasdriver_SerialPort.d \
./jni/aclasdriver_SmartCard.d \
./jni/aclasdriver_UsbRfid.d \
./jni/aclasdriver_drawer.d \
./jni/aclasdriver_km3_printer.d \
./jni/aclasdriver_printer.d \
./jni/aclasdriver_rfid.d \
./jni/rfid_lib.d \
./jni/sc_lib.d 


# Each subdirectory must supply rules for building sources it contributes
jni/%.o: ../jni/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: Cross GCC Compiler'
	gcc -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


