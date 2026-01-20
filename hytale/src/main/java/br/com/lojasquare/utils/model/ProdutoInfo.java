package br.com.lojasquare.utils.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Representa um produto cadastrado na loja LojaSquare.
 * Classe agnóstica de plataforma - reutilizada do projeto Spigot.
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoInfo {
    private String grupo;
    private String produto;
}
