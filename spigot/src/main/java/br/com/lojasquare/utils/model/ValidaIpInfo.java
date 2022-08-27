package br.com.lojasquare.utils.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
@AllArgsConstructor
@ToString
public class ValidaIpInfo {

    private boolean sucesso;
    private String ip;

}
