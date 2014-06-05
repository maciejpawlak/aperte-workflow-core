package org.aperteworkflow.util.vaadin;

import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: POlszewski
 * Date: 2012-02-17
 * Time: 22:22
 */
public class LocalizedFormats {
	public static String getLongDateFormatStr(I18NSource i18NSource) {
		 String d= i18NSource.getMessage("format.long.date");
		return "dd-MM-yyyy HH:mm";
	}

	public static String getShortDateFormatStr(I18NSource i18NSource) {
		return i18NSource.getMessage("format.short.date");
	}

	public static String getDayMonthFormatStr(I18NSource i18NSource) {
		return i18NSource.getMessage("format.day.month.date");
	}
	
	public static String getCurrencyFormatStr(I18NSource i18NSource) {
		return i18NSource.getMessage("format.currency");
	}

	public static DecimalFormat createDecimalFormat(String format, I18NSource i18NSource) {
		return new DecimalFormat(format, new DecimalFormatSymbols(i18NSource.getLocale()));
	}

	public static DecimalFormat createCurrencyFormat(I18NSource i18NSource) {
		return createDecimalFormat(getCurrencyFormatStr(i18NSource), i18NSource);
	}

	public static SimpleDateFormat getLongDateFormat(I18NSource i18NSource) {
		return new SimpleDateFormat(getLongDateFormatStr(i18NSource));
	}

	public static SimpleDateFormat getShortDateFormat(I18NSource i18NSource) {
		return new SimpleDateFormat(getShortDateFormatStr(i18NSource));
	}

	public static SimpleDateFormat getDayMonthFormat(I18NSource i18NSource) {
		return new SimpleDateFormat(getDayMonthFormatStr(i18NSource));
	}

    public static String formatShortDate(Date date, I18NSource i18NSource) {
        return date != null ? getShortDateFormat(i18NSource).format(date) : "";
    }
}
