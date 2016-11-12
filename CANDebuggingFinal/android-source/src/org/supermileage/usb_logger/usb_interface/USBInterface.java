package org.supermileage.usb_logger.usb_interface;

import android.util.Log;
import org.qtproject.qt5.android.bindings.QtApplication;
import org.qtproject.qt5.android.bindings.QtActivity;
import android.os.Bundle;
import android.os.IBinder;
import android.content.ServiceConnection;
import android.content.Intent;
import android.content.ComponentName;
import android.content.Context;
import android.app.PendingIntent;
import org.json.JSONObject;
import android.content.BroadcastReceiver;
import java.util.HashMap;
import java.util.Iterator;
import java.io.*;
import java.lang.Thread;
import java.util.concurrent.TimeUnit;
import android.os.Handler;
import android.content.IntentFilter;
import android.hardware.usb.*;

import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

public class USBInterface extends QtActivity
{
    private static USBInterface m_instance;
    private UsbAccessory accessory;
    private static final String TAG = "USB_FOR_QT_JAVA:";
    private static final String ACTION_USB_PERMISSION = "org.supermileage.usb_logger.usb_interface.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
    private UsbManager manager;
    private UsbInterface intf;
    private UsbDeviceConnection connection;
    private HashMap<Integer, Integer> connectedDevices;

    private UsbEndpoint epIN = null;
    private UsbEndpoint epOUT = null;
    private UsbSerialDevice serial;

    public USBInterface()
    {
        m_instance = this;

        connectedDevices = new HashMap<Integer, Integer>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        registerReceiver(usbManagerBroadcastReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
        registerReceiver(usbManagerBroadcastReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
        registerReceiver(usbManagerBroadcastReceiver, new IntentFilter(ACTION_USB_PERMISSION));

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        checkForDevices();

        //final Handler handler = new Handler();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static native void notifyDeviceAttached(int fd);
    private static native void notifyDeviceDetached(int fd);
    private static native void deviceConnected(char c);
    private static native void printToScreen(String s);
    private static native void printToLog(String s);


    private final BroadcastReceiver usbManagerBroadcastReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            try
            {
                String action = intent.getAction();

                Log.d(TAG, "INTENT ACTION: " + action);

                if (ACTION_USB_PERMISSION.equals(action))
                {
                    Log.d(TAG, "onUsbPermission");

                    synchronized (this)
                    {
                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                        {
                            if(device != null)
                            {
                                int fd = connectToDevice(device);
                                Log.d(TAG,"device file descriptor: " + fd);
                                printToScreen("Device file descriptior: " + Integer.toString(fd));
                                notifyDeviceAttached(fd);
                            }
                        }
                        else
                        {
                            Log.d(TAG, "permission denied for device " + device);
                            printToScreen("Device cannont be opended.");
                        }
                    }
                }

                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
                {
                    Log.d(TAG, "onDeviceConnected");

                    synchronized(this)
                    {
                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        if (device != null)
                        {
                            manager.requestPermission(device, mPermissionIntent);
                            printToScreen("Device Connected" + device);
                        }
                    }
                }

                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
                {
                    Log.d(TAG, "onDeviceDisconnected");

                    synchronized(this)
                    {
                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        int fd = connectedDevices.get(device.getDeviceId());

                        Log.d(TAG, "device: " + device.getDeviceId() + " disconnected. fd: " + fd);

                        notifyDeviceDetached(fd);

                        printToScreen("Device Detached! " + device);

                        connectedDevices.remove(device.getDeviceId());
                    }
                }
            }
            catch(Exception e)
            {
                Log.d(TAG, "Exception: " + e);
            }
        }
    };


    UsbSerialInterface.UsbReadCallback serialCallback = new UsbSerialInterface.UsbReadCallback() {
     //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0)
        {
            String data = null;
            try
            {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                printToScreen(data);
                printToLog(data);
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }
    };


    private int connectToDevice(UsbDevice device)
    {
        //Get the interface.
        /*intf = device.getInterface(0);
        printToScreen("This is a test.");
        //Check which endpoint we are using and assign it to the correct class member.
        for (int i = 0; i < intf.getEndpointCount(); i++)
        {
            UsbEndpoint ep = intf.getEndpoint(i);
            if (ep.getDirection() == UsbConstants.USB_DIR_IN)
                epIN = ep;
            else if (ep.getDirection() == UsbConstants.USB_DIR_OUT)
                epOUT = ep;
        }
        printToScreen("This is a second test.");
        connection = manager.openDevice(device);
        boolean connected = connection.claimInterface(intf, true);

        //Controll statements to set up the uart interface.
        //Send control statements for setting up speed.
        connection.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
        connection.controlTransfer(0x21, 32, 0, 0, new byte[] { (byte) 0x80,
                             0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);


        byte DATA[] = new byte [] {(byte)0x0A, (byte)0x53, (byte)0x36, (byte)0x5C, (byte)0x72, (byte)0x0D, (byte)0x0A, (byte)0x4F, (byte)0x5C, (byte)0x72, (byte)0x0D};
        printToScreen("Third test");
        int success = connection.bulkTransfer(epOUT, DATA, DATA.length, 0);
        printToScreen("Post send.");*/
        byte DATA[] = new byte [] {(byte)0x43, (byte)0x0D, (byte)0x53, (byte)0x36, (byte)0x0D, (byte)0x4F, (byte)0x0D};
        serial = UsbSerialDevice.createUsbSerialDevice(device, connection);
        if(serial != null)
        {
            printToScreen("Serial Library Has Been activated.");
            serial.open();
            serial.setBaudRate(3000000);
            serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
            serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
            serial.setParity(UsbSerialInterface.PARITY_NONE);
            serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_XON_XOFF);

            try
            {
               Thread.sleep(1000);
            }
            catch (Exception e)
            {
               printToScreen("Error Sleeping");
            }
            serial.write(DATA);
            serial.read(serialCallback);
            printToScreen("Post read handler.");

/*
            int beat_this = 3;
            while(beat_this-- > 0)
            {

            try
            {
               Thread.sleep(1000);
            }
            catch (Exception e)
            {
               printToScreen("Error Sleeping");
            }
            byte DATA2[] = new byte [] {0x73, 0x30,0x30, 0x44, 0x34, 0x31, 0x31, 0x32, 0x32, 0x33, 0x33, 0x34, 0x34, 0x0D};
            serial.write(DATA2);*/
    //printToScreen("Writing");
            //}
        /*serial.write("C\r\n".getBytes());
            printToScreen("Sent C");
            serial.write("S6\r\n".getBytes());
            printToScreen("Sent S6");
            serial.write("O\r\n".getBytes());
            printToScreen("Sent O");
            serial.write("t01D23331".getBytes());*/
            //serial.write(new byte[] {0x0D, 0x0A, 0x43, 0x5C, 0x72, 0x0D, 0x0A, 0x53, 0x36, 0x5C, 0x72, 0x0D, 0x0A, 0x4F, 0x5C, 0x72, 0x0D, 0x0A});
            //serial.write(new byte[] { 0x43, 0x5C, 0x72, 0x0D, 0x0A, 0x53, 0x36, 0x5C, 0x72, 0x0D, 0x0A, 0x4F, 0x5C, 0x72, 0x0D, 0x0A });
            //printToScreen("Post Write.");
        }

        return connection.getFileDescriptor();
    }

    private void checkForDevices()
    {
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        printToScreen("CheckForDevices was called");
        while(deviceIterator.hasNext())
        {
            UsbDevice device = deviceIterator.next();

            printToScreen("Found a device: " + device.getManufacturerName() + device.getDeviceName());

            manager.requestPermission(device, mPermissionIntent);
        }
    }
}
