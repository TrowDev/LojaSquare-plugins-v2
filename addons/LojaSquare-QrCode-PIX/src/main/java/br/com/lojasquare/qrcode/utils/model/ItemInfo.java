package br.com.lojasquare.qrcode.utils.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ItemInfo {
    @SerializedName("id_produto")
    private Long produtoId;
    private Integer quantidade;
}
