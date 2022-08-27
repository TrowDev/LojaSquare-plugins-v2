package br.com.lojasquare.utils;

import com.google.gson.JsonElement;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Builder
@Data
@ToString
public class HttpResponse {
    private final int code;
    private final JsonElement object;
    private final long ms;
    private final String message;
}
