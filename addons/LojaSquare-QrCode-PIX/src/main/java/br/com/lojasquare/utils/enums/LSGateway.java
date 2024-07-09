package br.com.lojasquare.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LSGateway {
    PAGSQUARE("PagSquare");

    private String gateway;
}
