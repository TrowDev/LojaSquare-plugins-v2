package br.com.lojasquare.providers.database.impl;

import br.com.lojasquare.providers.database.IDbProvider;
import br.com.lojasquare.utils.DB;
import br.com.lojasquare.utils.enums.LSEntregaStatus;
import br.com.lojasquare.utils.model.ItemInfo;
import br.com.lojasquare.utils.model.ProdutoInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static br.com.lojasquare.utils.Constants.PREFIXO_ERRO;

@AllArgsConstructor
public class IDbProviderImpl implements IDbProvider {

    @Getter private DB db;

    @Override
    public List<ProdutoInfo> getTodosGruposEntrega() {
        List<ProdutoInfo> lista = new ArrayList<>();
        PreparedStatement ps = null;
        try {
            ps = db.getConnection().prepareStatement("SELECT nmProduto, nmGrupo FROM pedido GROUP BY nmProduto, nmGrupo ;");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(ProdutoInfo.builder()
                                .grupo(rs.getString("nmGrupo"))
                                .produto(rs.getString("nmProduto"))
                        .build());
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(PREFIXO_ERRO+" §cErro ao listar grupos de entregas pendentes §c. Erro: §a"+e.getMessage());
        }
        finally {
            try {
                ps.close();
            } catch (Exception e2) {}
        }
        return lista;
    }

    @Override
    public List<ItemInfo> getTodasEntregas(LSEntregaStatus status) {
        List<ItemInfo> lista = new ArrayList<>();
        PreparedStatement ps = null;
        try {
            ps = db.getConnection().prepareStatement("SELECT * FROM pedido WHERE statusId=? ;");
            ps.setInt(1, status.getCode());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(ItemInfo.builder()
                        .id(rs.getLong("id"))
                        .entregaID(rs.getLong("entregaId"))
                        .produto(rs.getString("nmProduto"))
                        .servidor(rs.getString("servidor"))
                        .subServidor(rs.getString("subServidor"))
                        .player(rs.getString("nmPlayer"))
                        .codigo(rs.getString("cdTransacao"))
                        .quantidade(rs.getInt("quantidade"))
                        .grupo(rs.getString("nmGrupo"))
                        .dias(rs.getInt("dias"))
                        .status(LSEntregaStatus.findByCode(rs.getInt("statusId")))
                        .criadoEm(toDateTime(rs.getTimestamp("createdAt")))
                        .atualizadoEm(toDateTime(rs.getTimestamp("updatedAt")))
                        .build());
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(PREFIXO_ERRO+" §cErro ao listar entregas pendentes §c. Erro: §a"+e.getMessage());
        }
        finally {
            try {
                ps.close();
            } catch (Exception e2) {}
        }
        return lista;
    }

    private OffsetDateTime toDateTime(Timestamp ts) {
        if(Objects.isNull(ts)) return null;
        return ts.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    @Override
    public boolean updateDelivery(ItemInfo ii) {
        PreparedStatement ps = null;
        try {
            ps = db.getConnection().prepareStatement("UPDATE pedido SET statusId=? WHERE id=? ;");
            ps.setInt(1, LSEntregaStatus.ENTREGUE.getCode());
            ps.setLong(2, ii.getId());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(PREFIXO_ERRO+" §cErro ao executar UPDATE da entrega §a"+ii.toString()+" §c. Erro: §a"+e.getMessage());
        }
        finally {
            try {
                ps.close();
            } catch (Exception e2) {}
        }
        return false;
    }
}
