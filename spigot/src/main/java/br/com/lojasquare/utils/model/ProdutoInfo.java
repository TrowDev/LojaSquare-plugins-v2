package br.com.lojasquare.utils.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class ProdutoInfo {
	private String grupo;
	private String produto;
}
