#include "usb_interface.h"

//Static Definitions.
QString usb_interface::messages = "";
int usb_interface::fileDescriptor = -1;
QFile *usb_interface::f = nullptr;
usb_interface *usb_interface::interfacePointer = nullptr;



usb_interface::usb_interface(QObject *parent) : QObject(parent)
{
    bool oneConnection = false;
    interfacePointer = this;
    if(!androidMode)
    {
        //Init logging for Desktop
        messages += "Init C++ USB for Desktop.\n";
        f = new QFile("~/");
        if (openCloseLog(true) == 0)
        {
            printToNLog("Logger file set up.");
            messages += "Logger file set up and ready.\n";
            openCloseLog(false);
        }
        else
        {
            messages += "Logger file error.\n";
        }

        //Init Serial QSerialPort.
        //Serial Port is easy on non-mobile platforms.
        foreach (const QSerialPortInfo &info, QSerialPortInfo::availablePorts())
        {
                if(oneConnection == false)
                {
                    messages += "USB: Name : " + info.portName() + "\n";
                    messages += "USB: Description : " + info.description() + "\n";
                    messages += "USB: Manufacturer: " + info.manufacturer() + "\n";
                }

                if(info.description() == "CANtact dev" && oneConnection == false)
                {
                    messages += "Description matched.\n";

                port.setPort(info);
                messages += "Serial object created\n";
                if(port.open(QIODevice::ReadWrite))
                {
                    isSerialOpen = true;
                    messages += "We have opened the port on: " + info.portName() + "\n";
                    connect(&port, &QSerialPort::readyRead, this, &usb_interface::handleReadyRead);
                    oneConnection = true;
                    QByteArray open = QString("O\r").toUtf8();
                    qDebug() << "Sending: " << QString(open);
                    port.write(open);
                }
            }
        }
    }
    else
    {
        //messages += "Init C++ USB for Android.\n";
        usb_interface::f = new QFile("/sdcard/Download/log.txt");
        if (openCloseLog(true) == 0)
        {
            printToNLog("Logger file set up.");
            messages += "Logger file set up and ready.\n";
            openCloseLog(false);
        }
        else
        {
            messages += "Logger file error.\n";
        }
    }
}

usb_interface::~usb_interface()
{
    if(port.open(QIODevice::ReadWrite) && !androidMode)
    {
        QByteArray close = QByteArrayLiteral("\x0D\x0A\x43\x5C\x72");
        port.write(close);
        port.close();
    }
}

//******** Getters and Setters.

QString usb_interface::getMessages()
{
    return messages;
}

void usb_interface::setMessages(QString str)
{
    messages = str;
    messagesChanged();
}

//******** Serial CAN Functions

void usb_interface::handleReadyRead()
{
    messages += parseCANMessage(QString(port.readAll())) + "\n";
    //messages += "Got Somthing\n";
    messagesChanged();
}
void usb_interface::closecan()
{
    QByteArray close = QString("C\r").toUtf8();
    port.write(close);
}

QString usb_interface::parseCANMessage(QString str)
{
   return str;
}

//******** Function to set OS type.

void usb_interface::setMode(bool enable)
{
    androidMode = enable;
}

//******** Logging Functions.

int usb_interface::printToNLog(QString info)
{
    if(isLogOpen)
    {
        QTextStream stream(usb_interface::f);
        stream << info << endl;
        return 0;
    }
    else
    {
        return -1;
    }
}

int usb_interface::openCloseLog(bool o_c)
{
    if(o_c)
    {
        if(f->open(QIODevice::ReadWrite | QIODevice::Append))
        {
            isLogOpen = true;
            QDateTime dateTime = QDateTime::currentDateTime();
            QString dateTimeString = dateTime.toString();
            printToNLog("Log File Opended: " + dateTimeString + " ==========================>>");
            return 0;
        }
        else
        {
            isLogOpen = false;
            return -1;
        }
    }
    else
    {
        printToNLog("Log File Closed: <<=======================");
        usb_interface::f->close();
        isLogOpen = false;
        return 0;
    }
}
