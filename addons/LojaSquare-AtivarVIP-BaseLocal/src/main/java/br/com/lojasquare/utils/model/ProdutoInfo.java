package br.com.lojasquare.utils.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@Builder
public class ProdutoInfo {
	private String grupo;
	private String produto;
}
