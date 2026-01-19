package br.com.lojasquare.qrcode.core.gui.acoes.impl;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.core.gui.acoes.AcaoStrategy;
import br.com.lojasquare.qrcode.core.mapcode.QrCodeMap;
import br.com.lojasquare.qrcode.utils.StringUtils;
import br.com.lojasquare.qrcode.utils.enums.LSGateway;
import br.com.lojasquare.qrcode.utils.model.*;
import com.google.zxing.WriterException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class GerarQrcodeStrategyImpl implements AcaoStrategy {

    private LojaSquare pl;

    @Override
    public void executar(Player p, Inventory inv) {
        try {
            p.closeInventory();
            ProdutoInfoGUI produto = validaPlayerEscolhendoProduto(p);
            if (Objects.isNull(produto)) return;
            p.sendMessage(pl.getMsg("Msg.Aguarde_Gerando_QrCode"));

            new BukkitRunnable() {
                @SneakyThrows
                @Override
                public void run() {
                    CheckoutResponse checkoutResponse = geraCodigoPagamento(p, produto);
                    if (validaCheckoutResponse(p, checkoutResponse)) return;

                    geraMapaQrCode(p, checkoutResponse);
                }
            }.runTaskAsynchronously(pl);
        } catch (Exception e) {
            p.sendMessage("§4[LSQrCode] §cHouve uma falha ao gerar o mapa com o QrCode. Erro: §a"+e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validaCheckoutResponse(Player p, CheckoutResponse checkoutResponse) {
        if(checkoutResponse.isError()
                || Objects.nonNull(checkoutResponse.getInfo())
                || Objects.nonNull(checkoutResponse.getMsg())
                || Objects.isNull(checkoutResponse.getUrlPayment())) {
            String msg = checkoutResponse.getMessage();
            if(Objects.nonNull(checkoutResponse.getInfo())) {
                msg = checkoutResponse.getInfo();
            } else if(Objects.nonNull(checkoutResponse.getMsg())) {
                msg = checkoutResponse.getMsg();
            } else if(StringUtils.isEmpty(msg)) {
                msg = "Falha ao gerar QrCode. Notifique o administrador.";
            }
            p.sendMessage(pl.getMsg("Msg.Falha_Gerar_Qrcode").replace("@erro", msg));
            return true;
        }
        return false;
    }

    private ProdutoInfoGUI validaPlayerEscolhendoProduto(Player p) {
        if(!pl.getOpenGuiConfirmar().getListaNickProduto().containsKey(p.getName())) {
            p.sendMessage(pl.getMsg("Msg.Nao_Foi_Possivel_Identificar_Produto_Selecionado"));
            return null;
        }
        ProdutoInfoGUI produto = pl.getOpenGuiConfirmar().getListaNickProduto().get(p.getName());
        if(Objects.isNull(produto)) {
            p.sendMessage(pl.getMsg("Msg.Nao_Foi_Possivel_Identificar_Produto_Selecionado"));
            return null;
        }
        return produto;
    }

    private CheckoutResponse geraCodigoPagamento(Player p, ProdutoInfoGUI produto) {
        CheckoutResponse checkoutResponse = pl.getLsProvider().getQrCodePayment(Checkout.builder()
                .carrinho(Collections.singletonList(ItemInfo.builder()
                        .produtoId(produto.getProdutoId()).quantidade(produto.getQuantidade())
                        .build()))
                .cliente(Cliente.builder().clienteID(0L).build())
                .gateway(LSGateway.PAGSQUARE.getGateway())
                .player(p.getName())
                .servidor(pl.getServidor())
                .build());
        return checkoutResponse;
    }

    private void geraMapaQrCode(Player p, CheckoutResponse checkoutResponse) throws UnsupportedEncodingException, WriterException {
        BufferedImage imgQrcode = QrCodeMap.generateQRCode(checkoutResponse.getUrlPayment());
        if(Objects.isNull(imgQrcode)) {
            p.sendMessage(pl.getMsg("Msg.Falha_Gerar_Qrcode").replace("@erro", "QrCode Image is Null"));
            return;
        }

        List<String> lore = new ArrayList<>();
        for(String linha : pl.getConfig().getStringList("Mapa.Item.Lore")) {
            lore.add(linha.replace("&", "§"));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                QrCodeMap.generateMap(imgQrcode, p, pl.getMsg("Mapa.Item.Nome"), lore);
                p.sendMessage(pl.getMsg("Mapa.Msg_Ao_Gerar"));
            }
        }.runTask(pl);
    }
}
