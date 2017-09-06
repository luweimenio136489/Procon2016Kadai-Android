package com.example.tukitan.SensorSaver;

/**
 * This class is Output Dataset Structure
 *
 */

public class DataSet {

    public long time;
    public String attitude_x;
    public String attitude_z;
    public String acceleration_y;

    public DataSet(long time,String x,String z,String y){

        this.time = time;
        attitude_x = x;
        attitude_z = z;
        acceleration_y = y;
    }



}
