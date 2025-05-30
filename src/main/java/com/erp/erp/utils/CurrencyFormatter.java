package com.erp.erp.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for formatting currency values in templates.
 * This class provides static methods that can be safely called from Thymeleaf templates.
 */
public class CurrencyFormatter {

    private static final NumberFormat CURRENCY_FORMAT;
    
    static {
        CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "RW"));
        CURRENCY_FORMAT.setMaximumFractionDigits(2);
        CURRENCY_FORMAT.setMinimumFractionDigits(2);
    }
    
    /**
     * Formats a BigDecimal value as currency.
     * 
     * @param value the value to format
     * @return the formatted currency string
     */
    public static String format(BigDecimal value) {
        if (value == null) {
            return "N/A";
        }
        return CURRENCY_FORMAT.format(value);
    }
    
    /**
     * Formats a double value as currency.
     * 
     * @param value the value to format
     * @return the formatted currency string
     */
    public static String format(Double value) {
        if (value == null) {
            return "N/A";
        }
        return CURRENCY_FORMAT.format(value);
    }
}