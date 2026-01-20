package br.com.lojasquare.utils.model;

import lombok.Data;
import lombok.ToString;

/**
 * Representa uma entrega de produto retornada pela API LojaSquare.
 * Classe agnóstica de plataforma - reutilizada do projeto Spigot.
 */
@Data
@ToString
public class ItemInfo {

    private Long entregaID;
    private String player;
    private String produto;
    private String servidor;
    private String subServidor;
    private String grupo;
    private String codigo;
    private String status;
    private String cupom;
    private int statusID;
    private int dias;
    private int quantidade;
    private long atualizadoEm;

    public ItemInfo() {
    }

    public ItemInfo(String player) {
        this.player = player;
    }
}
