package br.com.lojasquare.qrcode.utils;

import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;

@UtilityClass
public class StringUtils {
    public String formatar(Double numero){
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        Double balance = numero;
        String formatted = formatter.format(balance);
        if (formatted.endsWith(".")) {
            formatted = formatted.substring(0, formatted.length() - 1);
        }
        return formatted;
    }
}
