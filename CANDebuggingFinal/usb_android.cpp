#include "usb_interface.h"
#include <QtAndroidExtras/QAndroidJniObject>
using namespace std;

static void deviceConnected(JNIEnv *, jclass /*clazz*/, jchar c)
{

    return;
}
static void printToScreenN(string s)
{
    usb_interface::messages += QString::fromStdString(s) + "\n";
    if(usb_interface::interfacePointer != nullptr)
    {
       usb_interface::interfacePointer->messagesChanged();
    }
}

static void notifyDeviceAttached(JNIEnv *, jclass /*clazz*/, jint i)
{
   printToScreenN("Device attached!");
}

static void notifyDeviceDetached(JNIEnv *, jclass /*clazz*/, jint i)
{
    printToScreenN("Device detached!");
}



static void printToScreen(JNIEnv * env, jclass /*clazz*/, jstring s)
{
    string intermediate = env->GetStringUTFChars(s,NULL);
    usb_interface::messages += QString::fromStdString(intermediate) + "\n";
    if(usb_interface::interfacePointer != nullptr)
    {
       usb_interface::interfacePointer->messagesChanged();
    }
}

static int printToLog(JNIEnv * env, jclass /*clazz*/, jstring s)
{
    string intermediate = env->GetStringUTFChars(s,NULL);
    if(usb_interface::f != nullptr && usb_interface::f->isOpen())
    {
        QTextStream stream(usb_interface::f);
        stream << QString::fromStdString(intermediate) << endl;
        return 0;
    }
    else
    {
        return -1;
    }
}


static JNINativeMethod methods[] = {
    {"deviceConnected", "(C)V", (void *)deviceConnected},
    {"notifyDeviceAttached", "(I)V", (void *)notifyDeviceAttached},
    {"notifyDeviceDetached", "(I)V", (void *)notifyDeviceDetached},
    {"printToScreen", "(Ljava/lang/String;)V", (void *)printToScreen},
    {"printToLog", "(Ljava/lang/String;)V", (void *)printToLog}
};

jint JNICALL JNI_OnLoad(JavaVM *vm, void *) //Registers the USBInterface class native function callbacks.
{
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_4) != JNI_OK)
        return JNI_FALSE;

    jclass clazz = env->FindClass("org/supermileage/usb_logger/usb_interface/USBInterface");
    if (env->RegisterNatives(clazz, methods, sizeof(methods) / sizeof(methods[0])) < 0)
    {
        qDebug() << "Natives Init Failed";
        return JNI_FALSE;
    }

    qDebug() << "Natives Init sucessful";

    return JNI_VERSION_1_4;
}

