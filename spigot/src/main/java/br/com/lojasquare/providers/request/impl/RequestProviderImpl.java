package br.com.lojasquare.providers.request.impl;

import br.com.lojasquare.providers.request.IRequestProvider;
import br.com.lojasquare.utils.DateDuration;
import br.com.lojasquare.utils.HttpResponse;
import br.com.lojasquare.utils.SiteUtil;
import br.com.lojasquare.utils.enums.LSResponseEnum;
import com.google.gson.JsonParser;
import lombok.Getter;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class RequestProviderImpl implements IRequestProvider {

    @Getter
    private SiteUtil siteUtil;

    public RequestProviderImpl(SiteUtil su) {
        this.siteUtil   = su;
    }

    @Override
    public HttpResponse get(String url) throws IOException {
        var msCalc  = new DateDuration();
        var parser  = new JsonParser();
        var urlObj  = new URL(url);
        var ms      = 0L;

        HttpsURLConnection c = buildDefaultConnection((HttpsURLConnection) urlObj.openConnection(), "GET");

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
    public HttpResponse put(String url) throws IOException {
        var msCalc  = new DateDuration();
        var parser  = new JsonParser();
        var urlObj  = new URL(url);
        var ms      = 0L;

        HttpsURLConnection c = buildDefaultConnection((HttpsURLConnection) urlObj.openConnection(), "PUT");

        int statusCode = c.getResponseCode();

        ms  = msCalc.calculate();

        if (statusCode == 200 || statusCode == 201 || statusCode == 204) {

            return HttpResponse.builder()
                    .code(statusCode)
                    .object(parser.parse("{}"))
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

    private HttpsURLConnection buildDefaultConnection(HttpsURLConnection c, String method) {
        try {
            if(getSiteUtil().getCredencial() != null) {
                c.setRequestProperty("Authorization", getSiteUtil().getCredencial());
            }

            c.setRequestProperty("Accept", "application/json");
            c.setRequestProperty("Content-Type", "application/json");
            c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            c.setRequestMethod(method);
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(getSiteUtil().getConnectionTimeout());
            c.setReadTimeout(getSiteUtil().getReadTimeout());

            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
