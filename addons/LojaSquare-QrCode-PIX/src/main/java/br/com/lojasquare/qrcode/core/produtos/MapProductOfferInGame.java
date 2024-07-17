package br.com.lojasquare.qrcode.core.produtos;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.core.CheckService;
import br.com.lojasquare.qrcode.providers.lojasquare.ILSProvider;
import br.com.lojasquare.qrcode.utils.bukkit.ConfigManager;
import br.com.lojasquare.qrcode.utils.bukkit.Item;
import br.com.lojasquare.qrcode.utils.model.ProdutoInfoGUI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class MapProductOfferInGame implements CheckService {
	
	private LojaSquare pl;
	
	public void execute(ConsoleCommandSender b) {
		b.sendMessage("§3[LSQrCode] §bIniciando mapeamento dos produtos para itens GUI...");
		List<String> gruposConfigPlugin 			= pl.getProdutosConfigurados();
		ConfigManager cm 							= pl.getConfGrupos();
		if(!Objects.isNull(gruposConfigPlugin)) {
			for(String g : gruposConfigPlugin) {
				buscaProdutoConfigMontaObjetoItem(g, cm, b);
			}
		}
	}
	
	private void buscaProdutoConfigMontaObjetoItem(String grupo, ConfigManager cm,ConsoleCommandSender b) {
		if(!Objects.isNull(cm) && !Objects.isNull(cm.getString("Grupos"))) {
			String prefixo = "Grupos."+grupo;
			if(!cm.getBoolean(prefixo+".Ativado", false)) {
				b.sendMessage("§6[LSQrCode] §eGrupo: §a"+grupo+"§e nao esta ativado para venda in-game.");
				return;
			}
			double valorSemFormatacao = cm.getDouble(prefixo+".Valor_Sem_Formatacao");
			String valor = cm.getString(prefixo+".Valor");
			Long produtoId = cm.getLong(prefixo+".ProdutoID");
			int slot = cm.getInt(prefixo+".Slot");
			int linha = cm.getInt(prefixo+".Linha");
			String nomeProduto = cm.getString(prefixo+".Item.Nome");
			String itemId = cm.getString(prefixo+".Item.ID");
			List<String> lore = new ArrayList<>();
			for(String linhaLore : cm.getStringList(prefixo+".Item.Lore")) {
				lore.add(linhaLore.replace("&", "§"));
			}
			ItemStack item = Item.getItemStack(itemId, nomeProduto, lore);
			pl.getListaProdutos().add(ProdutoInfoGUI.builder()
							.produto(cm.getString(prefixo+".Nome"))
							.item(item).produtoId(produtoId).valor(valor)
							.slot(slot).linha(linha).itemId(itemId).quantidade(1)
							.itemNome(nomeProduto).lore(lore).valorSemFormatacao(valorSemFormatacao)
					.build());
			b.sendMessage("§3[LSQrCode] §bProduto: §a"+grupo+"§b mapeado para venda in-game.");
		}
	}
	
}
