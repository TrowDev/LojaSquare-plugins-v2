package br.com.lojasquare.qrcode.utils.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CheckoutResponse {
    private String urlPayment;
    private String code;
    private String message;
    private String info;
    private String msg;
    private boolean img;
    private boolean error;
}
