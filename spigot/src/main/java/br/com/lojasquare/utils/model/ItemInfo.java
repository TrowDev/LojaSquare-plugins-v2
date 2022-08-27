package br.com.lojasquare.utils.model;

import lombok.Data;
import lombok.ToString;

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
	
	public ItemInfo(String p){
		player=p;
	}
	
}
