#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlComponent>
#include <QQmlEngine>
#include <QtQuick>

//Local Includes
#include <usb_interface.h>

int main(int argc, char *argv[])
{
    QCoreApplication::setAttribute(Qt::AA_EnableHighDpiScaling);
    QGuiApplication app(argc, argv);
    QQmlEngine engine;

    usb_interface USB;
    engine.rootContext()->setContextProperty("USB", &USB);
    QQmlComponent component(&engine, QUrl(QLatin1String("qrc:/main.qml")));
    component.create();

    const int returnval = app.exec();

    qDebug() << "Clean up code now running";

    //close USB;
    USB.closecan();

    return returnval;
}
