package br.com.lojasquare.utils.enums;

import java.util.Arrays;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Status de uma entrega na API LojaSquare.
 * Classe agnóstica de plataforma - reutilizada do projeto Spigot.
 */
@AllArgsConstructor
public enum LSEntregaStatus {

    PENDENTE(1, "PENDENTE"),
    ENTREGUE(2, "ENTREGUE");

    @Getter
    private final int code;

    @Getter
    private final String status;

    public static LSEntregaStatus findByCode(int code) {
        Optional<LSEntregaStatus> retOp = Arrays.stream(LSEntregaStatus.values())
                .filter(p -> p.code == code)
                .findFirst();
        return retOp.orElse(PENDENTE);
    }
}
