package br.com.lojasquare.qrcode.utils;

import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;
import java.util.Objects;

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

    public boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public String removerCaracterCor(String value) {
        if(Objects.isNull(value)) return "";
        return value.replaceAll("&([0-9|a-f|r])", "").replaceAll("§([0-9|a-f|r])", "");
    }
}
