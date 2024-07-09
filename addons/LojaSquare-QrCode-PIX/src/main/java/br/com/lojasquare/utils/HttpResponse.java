package br.com.lojasquare.utils;

import com.google.gson.JsonElement;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class HttpResponse {
    private int code;
    private JsonElement object;
    private long ms;
    private String message;
}
