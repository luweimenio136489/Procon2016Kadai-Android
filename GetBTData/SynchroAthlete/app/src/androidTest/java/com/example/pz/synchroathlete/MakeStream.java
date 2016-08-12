package com.example.android.bluetoothchat;

import java.util.LinkedList;
import java.util.Queue;

public class MakeStream{
    static Queue<Character> queue = new LinkedList<Character>();
    /*public static void main(String args[]){
        byte[] testStream="ThisIs\r\nTest".getBytes();
        byte[] testStream2="GreatestJava".getBytes();
        byte[] testStream3="HOGEHOGE\r\n".getBytes();
        System.out.println(tranceString(testStream));
        System.out.println(tranceString(testStream2));
        System.out.println(tranceString(testStream3));
    }*/
    synchronized public static String tranceString(byte[] buff,int size){
        String  strAnswer = new String();

        int i,count=0;
        for (i=0;i<size;i++) {
            queue.offer((char)buff[i]);/*
            System.out.print("\""+(char)buff[i]+"\" ");
            if (((char)buff[i]=='\n')||((char)buff[i]=='\r')) System.out.println("改行コード:"+buff[i]);*/
        }

        if (queue.contains((char)10)){
            while(queue.element() != (char)10){
                if(queue.element()==',') count++;
                strAnswer+=queue.poll();
                //System.out.println(strAnswer);
            }
            strAnswer+=queue.poll();
            if(count!=9) strAnswer = "";
            //System.out.println(strAnswer);
            /*while(!queue.isEmpty()){
                strAnswer+= queue.poll();
                if(strAnswer.charAt(strAnswer.length() - 1) == (char)10)break;
            }*/

        }
        //String tmp = strAnswer;

        return strAnswer;
    }
    /*
    public static boolean isMaked(){
        if(state){
            state = false;
            return true;
        } else {
            return false;
        }
    }*/
}