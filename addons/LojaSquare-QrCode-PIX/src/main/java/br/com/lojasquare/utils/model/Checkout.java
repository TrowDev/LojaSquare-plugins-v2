package br.com.lojasquare.utils.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Checkout {
    private List<ItemInfo> carrinho;
    private Cliente cliente;
    private String servidor;
    private String player;
    private String gateway;
}
