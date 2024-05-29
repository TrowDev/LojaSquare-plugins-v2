package br.com.lojasquare.utils.model;

import br.com.lojasquare.utils.enums.LSEntregaStatus;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.OffsetDateTime;

@Data
@ToString
@Builder
public class ItemInfo {
	private Long id;
	private Long entregaID;
	private String player;
	private String produto;
	private String servidor;
	private String subServidor;
	private String grupo;
	private String codigo;
	private LSEntregaStatus status;
	private String cupom;
	private int statusID;
	private int dias;
	private int quantidade;
	private OffsetDateTime criadoEm;
	private OffsetDateTime atualizadoEm;
	
}
