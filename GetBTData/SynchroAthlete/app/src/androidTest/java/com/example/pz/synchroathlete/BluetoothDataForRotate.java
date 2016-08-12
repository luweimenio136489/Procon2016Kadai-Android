package com.example.android.bluetoothchat;

import android.hardware.SensorManager;

/**
 * Created by tukitan on 16/07/17.
 */

public class BluetoothDataForRotate {
    private static float[] accelerate = new float[3];
    private static float[] magnet = new float[3];
    private static float[] matrix=new float[9];
    volatile public static float[] attitude=new float[3];
    public static String[] data;

    synchronized public static void stringToData(String stream){
        data=stream.split(",");
        insertData(accelerate,data,0);
        insertData(magnet,data,6);
        SensorManager.getRotationMatrix(matrix,null,accelerate,magnet);
        SensorManager.getOrientation(matrix,attitude);
        System.out.println("attitudes:"+attitude[0]+","+attitude[1]+","+attitude[2]);
    }

    private static void insertData(float[] input, String[] output, int locate){
        float[] tmp = new float[10];
        for(int i=0;i<3;i++){
            try {
                tmp[i] = Float.parseFloat(output[i + locate]);
                input[i] = tmp[i];
            } catch (NumberFormatException e) {
                input[0] = 0;
                input[1] = 0;
                input[2] = 0;
                break;
            }
        }
    }

    synchronized public static float getEuler( String str) {
        switch (str) {
            case "roll":
                System.out.println("roll:"+attitude[0]);
                return attitude[0];
            case "pitch":
                System.out.println("pitch: "+attitude[2]);
                return attitude[2];
            case "gravity":
                System.out.println("g ravity:"+data[1]);
                return Float.parseFloat(data[1]);
            default:
                return -1;

        }
    }
    public static boolean isMakeRotate( ){
        if(attitude[0]==0.0&&attitude[2]==0.0) {
            return false;
        }
        return true;
    }
}
