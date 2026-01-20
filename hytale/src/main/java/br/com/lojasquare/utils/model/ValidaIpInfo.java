package br.com.lojasquare.utils.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Representa a resposta de validação de IP da API LojaSquare.
 * Classe agnóstica de plataforma - reutilizada do projeto Spigot.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ValidaIpInfo {
    private boolean sucesso;
    private String ip;
}
