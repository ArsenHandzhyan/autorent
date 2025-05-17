package ru.anapa.autorent;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SmsRuCallTest {
    public static void main(String[] args) throws Exception {
        String apiId = "A25F8010-8575-8B76-AC1C-78A8E528F108"; // ваш ключ
        String phone = "79186814513"; // без +
        String urlStr = "https://sms.ru/code/call";
        String params = "api_id=" + apiId + "&phone=" + phone;

        URL url = new URL(urlStr + "?" + params);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println("Ответ SMS.ru: " + response);
    }
} 