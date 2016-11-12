TEMPLATE = app

QT += qml quick serialport
android: QT += androidextras
CONFIG += c++11

SOURCES += main.cpp \
    usb_interface.cpp

android {
    SOURCES += usb_android.cpp
}

RESOURCES += qml.qrc

# Additional import path used to resolve QML modules in Qt Creator's code model
QML_IMPORT_PATH =

# Default rules for deployment.
include(deployment.pri)

HEADERS += \
    usb_interface.h

DISTFILES += \
    android-source/src/org/supermileage/usb_logger/usb_interface/USBInterface.java \
    android-source/AndroidManifest.xml \
    android-source/res/drawable/cmd.png \
    android-source/libs/classes.jar \
    android-source/res/drawable-hdpi/icon.png \
    android-source/res/drawable-ldpi/icon.png \
    android-source/res/drawable-mdpi/icon.png

ANDROID_PACKAGE_SOURCE_DIR = $$PWD/android-source
