package com.example.android.bluetoothchat;

import java.util.LinkedList;
import java.util.Queue;

public class MakeStream{
    static Queue<Character> queue = new LinkedList<Character>();
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
            }
            strAnswer+=queue.poll();
            if(count!=9) strAnswer = "";

        }

        return strAnswer;
    }
}