package br.com.lojasquare.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
public enum LSEntregaStatus {

    PENDENTE(1, "PENDENTE"),
    ENTREGUE(2, "ENTREGUE");

    @Getter private int code;
    @Getter private String status;

    public static LSEntregaStatus findByCode(int code) {
        Optional<LSEntregaStatus> retOp = Arrays.stream(LSEntregaStatus.values()).filter(p -> p.code == code).findFirst();
        if(!retOp.isPresent()) {
            return PENDENTE;
        }
        return retOp.get();
    }

}
