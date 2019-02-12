package com.example.itrack.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringHelper {

    static private DateFormat dateFormater = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public static String dateToText(Date date) {
        return dateFormater.format(date);
    }
    public static String dateToText(Long value) {
        return dateFormater.format(new Date(value));
    }
}
