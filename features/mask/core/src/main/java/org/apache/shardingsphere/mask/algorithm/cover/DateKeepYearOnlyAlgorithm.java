package org.apache.shardingsphere.mask.algorithm.replace;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateKeepYearOnlyAlgorithm implements MaskAlgorithm<Object, Date> {
    @Override
    public Date mask(Object plainValue) {
        java.sql.Date result = null == plainValue ? null : (java.sql.Date)plainValue;
        if(null == plainValue){
            return result;
        }
        Calendar cal = new GregorianCalendar();
        cal.setTime(result);
        cal.set(Calendar.DAY_OF_YEAR,1); // Remove Month and Day information from the Value.
        return new java.sql.Date(cal.getTime().getTime());
    }
    @Override
    public String getType() {
        return "DATE_KEEP_YEAR_ONLY";
    }
}
