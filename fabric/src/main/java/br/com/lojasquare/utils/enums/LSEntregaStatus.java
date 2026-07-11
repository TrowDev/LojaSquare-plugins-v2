package br.com.lojasquare.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum LSEntregaStatus {

    PENDENTE(1, "PENDENTE"),
    ENTREGUE(2, "ENTREGUE");

    @Getter private int code;
    @Getter private String status;
}
