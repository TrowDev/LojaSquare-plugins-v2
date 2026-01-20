package br.com.lojasquare.utils;

import com.google.gson.JsonElement;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Representa uma resposta HTTP da API LojaSquare.
 * Classe agnóstica de plataforma - reutilizada do projeto Spigot.
 */
@Data
@ToString
@Builder
public class HttpResponse {
    private int code;
    private JsonElement object;
    private long ms;
    private String message;
}
