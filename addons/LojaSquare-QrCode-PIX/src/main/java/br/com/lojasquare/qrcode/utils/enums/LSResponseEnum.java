package br.com.lojasquare.qrcode.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
public enum LSResponseEnum {

    SEM_CONEXAO(0, "[LojaSquare-v2] §cServidor sem conexao com a internet."),

    CONEXAO_NAO_AUTORIZADA(401, "[LojaSquare-v2] §cConexao nao autorizada! Por favor, confira se a sua credencial esta correta."),

    IP_OU_KEY_BLOQUEADOS(403, "[LojaSquare-v2] §cO IP da maquina do seu servidor ou a sua key-api foram bloqueados."),

    NADA_ENCONTRADO(404, "[LojaSquare-v2] §cNenhuma entrega pendente encontrada ou SECRET KEY invalida."),

    ASSINATURA_EXPIROU(405, "[LojaSquare-v2] §cErro ao autenticar sua loja! Verifique se sua assinatura e credencial estao ativas!"),

    NADA_MUDOU(406, "[LojaSquare-v2] §cNao foi executada nenhuma atualizacao referente ao requerimento efetuado."),

    IP_NAO_LIBERADO(409, "[LojaSquare-v2] §cO IP enviado e diferente do que temos em nosso Banco de Dados. IP da sua Maquina: §a@ipServidor");

    @Getter
    private int code;

    @Getter
    private String message;

    public static LSResponseEnum findByCode(int code) {
        Optional<LSResponseEnum> lsOpt = Arrays.stream(LSResponseEnum.values()).filter(p -> p.code == code).findFirst();
        if(!lsOpt.isPresent()){
            return SEM_CONEXAO;
        }
        return lsOpt.get();
    }

}
