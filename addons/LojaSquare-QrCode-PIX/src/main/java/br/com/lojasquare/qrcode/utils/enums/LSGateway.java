package br.com.lojasquare.qrcode.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LSGateway {
    PAGSQUARE("PagSquare");

    private String gateway;
}
