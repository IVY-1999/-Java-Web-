package com.enjoy.book.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

    /**
     * 获取上传图片名
     * @return
     */
    public static String getImageName(){
        SimpleDateFormat  sdf= new SimpleDateFormat("yyyyMMddHHmmssS");
        return sdf.format(new Date());
    }

    /**
     * 对date日期进行加减操作
     * @param date  ---- 一个日期
     * @param amount --- 加或者减多少天
     * @return --- 加减后的日期对象
     */
    public static java.sql.Date getNewDate(java.sql.Date date,long amount){
        long mills = date.getTime();
        mills += amount*24*60*60*1000;
        java.sql.Date backDate = new java.sql.Date(mills);
        return backDate;
    }

    /**
     * 计算两个具体日期之间的时间差
     * @param date01
     * @param date02
     * @return
     */
    public static int getSpan(Date date01, Date date02){
        long span = date01.getTime() - date02.getTime();
        int day = (int) (span/1000/60/60/24);
        return Math.abs(day);
    }

    public static void main(String[] args) {
        System.out.println(getImageName());

        java.sql.Date date1 = java.sql.Date.valueOf("2022-03-09");
        java.sql.Date date2 = getNewDate(date1,-3);
        System.out.println(date1);
        System.out.println(date2);
    }

}
