package br.com.lojasquare.utils.enums;

import java.util.Arrays;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Códigos de resposta da API LojaSquare.
 * Classe agnóstica de plataforma - reutilizada do projeto Spigot.
 */
@AllArgsConstructor
public enum LSResponseEnum {

    SEM_CONEXAO(0, "[LojaSquare-v2] Servidor sem conexao com a internet."),
    CONEXAO_NAO_AUTORIZADA(401,
            "[LojaSquare-v2] Conexao nao autorizada! Por favor, confira se a sua credencial esta correta."),
    IP_OU_KEY_BLOQUEADOS(403, "[LojaSquare-v2] O IP da maquina do seu servidor ou a sua key-api foram bloqueados."),
    NADA_ENCONTRADO(404, "[LojaSquare-v2] Nenhuma entrega pendente encontrada ou SECRET KEY invalida."),
    ASSINATURA_EXPIROU(405,
            "[LojaSquare-v2] Erro ao autenticar sua loja! Verifique se sua assinatura e credencial estao ativas!"),
    NADA_MUDOU(406, "[LojaSquare-v2] Nao foi executada nenhuma atualizacao referente ao requerimento efetuado."),
    IP_NAO_LIBERADO(409,
            "[LojaSquare-v2] O IP enviado e diferente do que temos em nosso Banco de Dados. IP da sua Maquina: @ipServidor");

    @Getter
    private final int code;

    @Getter
    private final String message;

    public static LSResponseEnum findByCode(int code) {
        Optional<LSResponseEnum> lsOpt = Arrays.stream(LSResponseEnum.values())
                .filter(p -> p.code == code)
                .findFirst();
        return lsOpt.orElse(SEM_CONEXAO);
    }
}
