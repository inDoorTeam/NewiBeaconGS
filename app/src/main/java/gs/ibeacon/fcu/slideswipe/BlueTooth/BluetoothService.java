package gs.ibeacon.fcu.slideswipe.BlueTooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import gs.ibeacon.fcu.slideswipe.Log.DLog;

/**
 * Created by bing on 2016/6/10.
 */
public class BluetoothService {
    private OutputStream outStream = null;
    public static BluetoothSocket btSocket = null;
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID2 = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter mBluetoothAdapter = null;
    public BluetoothService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connectDevice(Intent data) {
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            btSocket.connect();
            DLog.d("藍芽已連線");
        }
        catch (IOException e) {
            DLog.d("藍芽socket建立失敗");
        }

    }
    public void writeData(String data) {
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            DLog.d("OutStream 取得失敗");
        }

        byte[] msgBuffer = data.getBytes();

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            DLog.d("藍芽訊息傳送失敗");
        }
    }

}
