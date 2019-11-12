package es.upv.epsg.igmagi.raspberrymanagement;

import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;

import java.io.IOException;

public class ArduinoUart {
    private static final String TAG = "ArduinoUart" ;
    private UartDevice uart;

    public ArduinoUart(String nombre, int baudios) {
        try {
            uart = PeripheralManager.getInstance().openUartDevice(nombre);
            Log.d(TAG, PeripheralManager.getInstance().getUartDeviceList().toString());
            uart.setBaudrate(baudios);
            uart.setDataSize(8);
            uart.setParity(UartDevice.PARITY_NONE);
            uart.setStopBits(1);
        } catch (IOException e) {
            Log.w(TAG, "Error iniciando UART", e);
        }
    }

    public void escribir(String s) {
        try {
            int escritos = uart.write(s.getBytes(), s.length());
            Log.d(TAG, escritos + " bytes escritos en UART");
        } catch (IOException e) {
            Log.w(TAG, "Error al escribir en UART", e);
        }
    }

    public String leer() {
        String s = "";
        int len;
        final int maxCount = 8;
        byte[] buffer = new byte[maxCount];
        try {
            do {
                len = uart.read(buffer, buffer.length);
                for (int i = 0; i < len; i++) {
                    s += (char) buffer[i];
                }
            } while (len > 0);
        } catch (IOException e) {
            Log.w(TAG, "Error al leer de UART", e);
        }
        return s;
    }

    public void cerrar() {
        try {
            uart.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
