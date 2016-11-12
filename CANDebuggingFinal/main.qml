import QtQuick 2.5
import QtQuick.Window 2.2
import QtQuick.Controls 2.0
import QtQuick.Extras 1.4
import QtQuick.Layouts 1.3

Window {
    visible: true
    width: 640
    height: 480
    title: qsTr("CAN Logger")

    ColumnLayout {
        id: columnLayout1
        spacing: 5
        anchors.fill: parent

        Rectangle {
            id: mainElement
            color: "#000000"
            Layout.fillHeight: true
            anchors.fill: parent

            Flickable {
                id: flick
                contentHeight: canlog.paintedHeight
                contentWidth: canlog.paintedWidth
                flickableDirection: Flickable.VerticalFlick
                anchors.fill: parent
                clip: true

                function ensureVisible(r)
                {
                    if (contentY >= r.y)
                        contentY = r.y;
                    else if (contentY+height <= r.y+r.height)
                        contentY = r.y+r.height-height;
                }

                TextEdit {
                    id: canlog
                    x: 10
                    y: 10
                    width: mainElement.width;
                    color: "#ffffff"
                    text: USB.messages
                    anchors.horizontalCenter: mainElement.horizontalCenter
                    anchors.left: mainElement.left
                    anchors.bottom: mainElement.bottom
                    anchors.top: mainElement.top
                    cursorVisible: true
                    focus: true
                    readOnly: true
                    horizontalAlignment: Text.AlignHCenter
                    anchors.bottomMargin: 10
                    anchors.topMargin: 10
                    font.pixelSize: 12
                    onCursorRectangleChanged: moveDown()

                    function moveDown()
                    {
                        if(cursorPosition != length)
                        {
                            canlog.cursorPosition = canlog.length;
                            flick.ensureVisible(cursorRectangle);
                        }
                    }
                }
            }
        }

        RowLayout {
            id: rowLayout1
            width: 100
            height: 100
            Layout.fillWidth: true

            Button {
                id: loggerButton
                width: 99
                text: qsTr("Start Logging")
                onClicked:
                {
                    if(text == "Start Logging")
                    {
                        if(USB.openCloseLog(true) == 0)
                        {
                            text = "Stop Logging";
                        }
                        else
                        {
                            USB.messages += "ERROR: Problem Opening Log File\n";
                        }
                    }
                    else
                    {
                        if(USB.openCloseLog(false) == 0)
                        {
                            text = "Start Logging";
                        }
                        else
                        {
                            USB.messages += "ERROR: Problem Opening Log File\n";
                        }
                    }
                }
            }

            TextEdit {
                id: sendMessage
                width: 80
                height: 20
                color: "#ffffff"
                text: qsTr("CAN Message")
                horizontalAlignment: Text.AlignHCenter
                cursorVisible: true
                Layout.fillWidth: true
                font.pixelSize: 18
            }

            Button {
                id: sendButton
                text: qsTr("Send Message")
                Layout.fillHeight: false
                Layout.fillWidth: false
            }
        }
    }
}
