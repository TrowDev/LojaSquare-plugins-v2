package br.com.lojasquare.qrcode.providers.lojasquare.impl;

import br.com.lojasquare.qrcode.LojaSquare;
import br.com.lojasquare.qrcode.providers.lojasquare.ILSProvider;
import br.com.lojasquare.qrcode.providers.request.IRequestProvider;
import br.com.lojasquare.qrcode.utils.HttpResponse;
import br.com.lojasquare.qrcode.utils.model.Checkout;
import br.com.lojasquare.qrcode.utils.model.CheckoutResponse;
import br.com.lojasquare.qrcode.utils.model.ProdutoInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LSProviderImpl implements ILSProvider {

    @Getter private LojaSquare pl;
    private final IRequestProvider requestProvider;
    private final Gson gson;

    public LSProviderImpl(IRequestProvider requestProvider, LojaSquare pl, Gson gson) {
        this.requestProvider = requestProvider;
        this.pl     = pl;
        this.gson   = gson;
    }
    @Override
    public CheckoutResponse getQrCodePayment(Checkout checkout) {
        try {
            String bodyJson = gson.toJson(checkout);
            HttpResponse result = requestProvider.post("/v1/checkout", bodyJson);
            if (Objects.isNull(result) || Objects.isNull(result.getObject())){
                return CheckoutResponse.builder().error(true).build();
            }
            return gson.fromJson(result.getObject(), CheckoutResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CheckoutResponse.builder().error(true).build();
    }

    /**
     * Busca todos os produtos criados na loja e seus respectivos grupos
     * @return Lista de informações dos produtos criados na loja.
     */
    @Override
    public List<ProdutoInfo> getTodosProdutosDaLoja(String tokenServidor) {
        List<ProdutoInfo> prods = new ArrayList<>();
        try {
            HttpResponse result = requestProvider.get(String.format("/v1/produtos?tokenSubServidor=%s", tokenServidor));
            if (Objects.isNull(result) || Objects.isNull(result.getObject())){
                return prods;
            }
            if(result.getObject() instanceof  JsonArray) {
                JsonArray job   = result.getObject().getAsJsonArray();
                job.forEach(je -> {
                    try {
                        ProdutoInfo ii = gson.fromJson(je, ProdutoInfo.class);
                        prods.add(ii);
                    } catch (Exception e) {
                        pl.print("[LSQrCode] Nao foi possivel processar o produto " + je.toString()
                                + ". Erro: " + e.getMessage());
                    }
                });
            }
            return prods;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prods;
    }

}
