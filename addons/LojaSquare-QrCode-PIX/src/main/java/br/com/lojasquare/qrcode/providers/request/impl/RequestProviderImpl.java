package br.com.lojasquare.qrcode.providers.request.impl;

import br.com.lojasquare.qrcode.providers.request.IRequestProvider;
import br.com.lojasquare.qrcode.utils.DateDuration;
import br.com.lojasquare.qrcode.utils.HttpResponse;
import br.com.lojasquare.qrcode.utils.SiteUtil;
import br.com.lojasquare.qrcode.utils.enums.LSResponseEnum;
import com.google.gson.JsonParser;
import lombok.Getter;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class RequestProviderImpl implements IRequestProvider {

    @Getter
    private SiteUtil siteUtil;

    public RequestProviderImpl(SiteUtil su) {
        this.siteUtil   = su;
    }

    @Override
    public HttpResponse get(String endpoint) throws IOException {
        DateDuration msCalc  = new DateDuration();
        JsonParser parser  = new JsonParser();
        URL urlObj  = new URL(siteUtil.getServerRequest()+endpoint);
        Long ms      = 0L;

        HttpsURLConnection c = buildDefaultConnection((HttpsURLConnection) urlObj.openConnection(), "GET", null, false);

        int statusCode = c.getResponseCode();

        if (statusCode == 200 || statusCode == 201 || statusCode == 204) {
            final BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            c.disconnect();
            br.close();

            ms  = msCalc.calculate();

            return HttpResponse.builder()
                    .code(statusCode)
                    .object(parser.parse(!sb.toString().equals("") ? sb.toString() : "{}"))
                    .ms(ms)
                    .message(null)
                    .build();
        }

        ms = msCalc.calculate();

        return HttpResponse.builder()
                .code(statusCode)
                .object(null)
                .ms(ms)
                .message(LSResponseEnum.findByCode(statusCode).getMessage())
                .build();
    }

    @Override
    public HttpResponse post(String endpoint, String body) throws IOException {
        DateDuration msCalc  = new DateDuration();
        JsonParser parser  = new JsonParser();
        URL urlObj  = new URL(siteUtil.getServerRequest()+endpoint);
        Long ms      = 0L;

        HttpsURLConnection c = buildDefaultConnection((HttpsURLConnection) urlObj.openConnection(), "POST", body, true);

        if (Objects.isNull(c)) {
            // Lidar com erro na criação da conexão
            return HttpResponse.builder()
                    .code(500).object(null)
                    .ms(ms).message("Erro ao construir a conexão").build();
        }


        int statusCode = c.getResponseCode();
        String response = getResponseBody(c, statusCode);

        ms  = msCalc.calculate();

        if (statusCode == 200 || statusCode == 201 || statusCode == 204 || statusCode == 404 || statusCode == 400) {

            return HttpResponse.builder()
                    .code(statusCode)
                    .object(parser.parse(response))
                    .ms(ms)
                    .message(null)
                    .build();
        }


        return HttpResponse.builder()
                .code(statusCode)
                .object(null)
                .ms(ms)
                .message(LSResponseEnum.findByCode(statusCode).getMessage())
                .build();
    }

    private HttpsURLConnection buildDefaultConnection(HttpsURLConnection c, String method, String body, boolean chavePublica) {
        try {
            c.setRequestMethod(method);
            if(getSiteUtil().getCredencial() != null) {
                String auth = chavePublica ? getSiteUtil().getChavePublica() : getSiteUtil().getCredencial();
                c.setRequestProperty("Authorization", auth);
            }

            c.setRequestProperty("Accept", "application/json");
            c.setRequestProperty("Content-Type", "application/json");
            c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(getSiteUtil().getConnectionTimeout());
            c.setReadTimeout(getSiteUtil().getReadTimeout());

            if(Objects.nonNull(body) && !body.isEmpty()) {
                c.setDoOutput(true);
                try (OutputStream os = c.getOutputStream()) {
                    byte[] input = body.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getResponseBody(HttpsURLConnection connection, int statusCode) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                statusCode >= 200 && statusCode < 300 ? connection.getInputStream() : connection.getErrorStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "{}";
        }
    }

}
