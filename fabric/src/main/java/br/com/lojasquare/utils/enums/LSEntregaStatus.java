package br.com.lojasquare.utils.enums;

import java.util.Arrays;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum LSEntregaStatus {

    PENDENTE(1, "PENDENTE"),
    ENTREGUE(2, "ENTREGUE");

    @Getter private int code;
    @Getter private String status;

    public static LSEntregaStatus findByCode(int code) {
        Optional<LSEntregaStatus> retOp = Arrays.stream(values()).filter(p -> p.code == code).findFirst();
        return retOp.orElse(PENDENTE);
    }
}
