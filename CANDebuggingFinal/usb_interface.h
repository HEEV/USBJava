#ifndef USB_INTERFACE_H
#define USB_INTERFACE_H

//QT lib.
#include <QObject>
#include <QtSerialPort/QSerialPort>
#include <QtSerialPort/QSerialPortInfo>
#include <QFile>
#include <QTextStream>
#include <QByteArray>
#include <QDebug>
#include <QDateTime>

//Standard C++ library
#include <string>
#include <iostream>
#include <sstream>
#include <fcntl.h>
#include <errno.h>
#include <fcntl.h>
#include <termios.h>
#include <unistd.h>
#include <termio.h>

/*
 * USB Interface that handles CAN information passed to it by any device adhearing to the SocketCAN protocall (Serial CAN.)
 * */
class usb_interface : public QObject
{
    Q_OBJECT
    Q_PROPERTY(QString messages READ getMessages WRITE setMessages NOTIFY messagesChanged)
public:
    explicit usb_interface(QObject *parent = 0);
    ~usb_interface();

    Q_INVOKABLE int openCloseLog(bool o_c);

    QString getMessages();

    void setMessages(QString string);
    void setMode(bool enableAndroid);
    void closecan();

    int printToNLog(QString info);

    static int fileDescriptor;
    static QString messages;
    static usb_interface *interfacePointer;
    static QFile *f;

signals:
    void messagesChanged();

public slots:
    void handleReadyRead();

private:
    QString parseCANMessage(QString str);



    bool isLogOpen = false;
    bool isSerialOpen = false;

    bool androidMode = true;
    QSerialPort port;
};

#endif // USB_INTERFACE_H
