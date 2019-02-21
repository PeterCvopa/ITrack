package com.example.itrack.common;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringHelper {

    static private DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public static String dateToText(Date date) {
        return dateFormatter.format(date);
    }

    public static String dateToText(Long value) {
        return dateFormatter.format(new Date(value));
    }

    public static String toText(Float value) {
        return new DecimalFormat("#0.##").format(value);
    }

    public static String toText(Float value, SpeedUnits unit) {
        return new DecimalFormat("#0.##").format(value)+" "+unit.name;
    }

    public static String toText(Double value, DistanceUnit unit ) {
        return new DecimalFormat("#0.##").format(value)+" "+unit.name;
    }

    public static String toText(BigDecimal value,  DistanceUnit unit) {
        return new DecimalFormat("#0.##").format(value)+" "+unit.name;
    }

    public enum DistanceUnit{
        METES("m"),KILOMETER("km");
        String name ;
        DistanceUnit(String name){
            this.name = name;
        }
    }
    public enum SpeedUnits{
        METES_PER_SECOND("m/s"),KILOMETERPER_SECOND("km/s");
        String name ;
        SpeedUnits(String name){
            this.name = name;
        }
    }
}
