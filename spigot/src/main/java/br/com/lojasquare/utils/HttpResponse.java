package br.com.lojasquare.utils;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class HttpResponse {
    private final int code;
    private final JsonElement object;
    private final long ms;
    private final String message;
}
