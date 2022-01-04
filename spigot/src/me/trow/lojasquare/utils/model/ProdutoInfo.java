package me.trow.lojasquare.utils.model;

public class ProdutoInfo {
	
	private String grupo;
	private String produto;
	
	public ProdutoInfo(String grupo, String produto) {
		super();
		this.grupo = grupo;
		this.produto = produto;
	}
	

	@Override
	public String toString() {
		return "ProdutoInfo [grupo=" + grupo + ", produto=" + produto + "]";
	}

	public String getGrupo() {
		return grupo;
	}

	public void setGrupo(String grupo) {
		this.grupo = grupo;
	}

	public String getProduto() {
		return produto;
	}

	public void setProduto(String produto) {
		this.produto = produto;
	}
	
}
