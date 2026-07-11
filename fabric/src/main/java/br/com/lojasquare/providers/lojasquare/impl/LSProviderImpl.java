package br.com.lojasquare.providers.lojasquare.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import br.com.lojasquare.LojasquareMod;
import br.com.lojasquare.providers.lojasquare.ILSProvider;
import br.com.lojasquare.providers.request.IRequestProvider;
import br.com.lojasquare.utils.HttpResponse;
import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.model.ItemInfo;
import br.com.lojasquare.utils.model.ProdutoInfo;
import br.com.lojasquare.utils.model.ValidaIpInfo;
import lombok.Getter;

public class LSProviderImpl implements ILSProvider {

    @Getter
    private final LojasquareMod mod;
    private final IRequestProvider requestProvider;

    public LSProviderImpl(IRequestProvider requestProvider, LojasquareMod mod) {
        this.requestProvider = requestProvider;
        this.mod = mod;
    }

    @Override
    public List<ItemInfo> getTodasEntregas(LSEntregaStatus status) {
        List<ItemInfo> itens = new ArrayList<>();
        try {
            String endpoint = "/v1/entregas/@status?status=@status".replace("@status", status.getCode() + "");
            HttpResponse result = requestProvider.get(mod.getSiteUtil().getServerRequest() + endpoint);

            if (Objects.isNull(result) || Objects.isNull(result.getObject())) {
                return itens;
            }

            if (result.getObject() instanceof JsonArray) {
                JsonArray job = result.getObject().getAsJsonArray();
                job.forEach(je -> {
                    try {
                        ItemInfo ii = new Gson().fromJson(je, ItemInfo.class);
                        itens.add(ii);
                    } catch (Exception e) {
                        mod.log("[LojaSquare] Nao foi possivel processar o item " + je.toString()
                                + ". Erro: " + e.getMessage());
                    }
                });
            }

            return itens;
        } catch (Exception e) {
            e.printStackTrace();
            return itens;
        }
    }

    @Override
    public List<ProdutoInfo> getTodosProdutosDaLoja() {
        List<ProdutoInfo> prods = new ArrayList<>();
        try {
            HttpResponse result = requestProvider.get(mod.getSiteUtil().getServerRequest()
                    + String.format("/v1/produtos?tokenSubServidor=%s", mod.getSiteUtil().getTokenServidor()));

            if (Objects.isNull(result) || Objects.isNull(result.getObject())) {
                return prods;
            }

            if (result.getObject() instanceof JsonArray) {
                JsonArray job = result.getObject().getAsJsonArray();
                job.forEach(je -> {
                    try {
                        ProdutoInfo pi = new Gson().fromJson(je, ProdutoInfo.class);
                        prods.add(pi);
                    } catch (Exception e) {
                        mod.log("[LojaSquare] Nao foi possivel processar o produto " + je.toString()
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

    @Override
    public boolean updateDelivery(ItemInfo ii) {
        if (Objects.isNull(ii)) {
            return false;
        }
        try {
            HttpResponse httpResponse = requestProvider.put(mod.getSiteUtil().getServerRequest()
                    + String.format("/v1/entregas/%d/entregue", ii.getEntregaID()));
            return !Objects.isNull(httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean activateAccount(String codigo, String usuario) {
        if (Objects.isNull(codigo)) {
            return false;
        }
        try {
            HttpResponse httpResponse = requestProvider.put(mod.getSiteUtil().getServerRequest()
                    + String.format("/v1/clientes/activate?codigo=%s&usuario=%s", codigo, usuario));
            return !Objects.isNull(httpResponse) && !Objects.isNull(httpResponse.getObject());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public ValidaIpInfo getIpMaquina() {
        ValidaIpInfo validaIp = ValidaIpInfo.builder().ip("IP nao encontrado.").sucesso(false).build();
        try {
            String endpoint = "/v1/sites/extensoes";
            HttpResponse result = requestProvider.get(mod.getSiteUtil().getServerRequest() + endpoint);

            if (Objects.isNull(result) || Objects.isNull(result.getObject())) {
                return validaIp;
            }

            validaIp = new Gson().fromJson(result.getObject(), ValidaIpInfo.class);
            return validaIp;
        } catch (Exception e) {
            e.printStackTrace();
            return validaIp;
        }
    }
}
