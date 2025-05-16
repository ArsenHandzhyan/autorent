package ru.anapa.autorent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SmsService {

    @Value("${sms.api.login}")
    private String smsLogin;

    @Value("${sms.api.password}")
    private String smsPassword;

    @Value("${sms.api.url}")
    private String smsApiUrl;

    @Value("${sms.api.sender}")
    private String smsSender;

    @Value("${sms.api.charset}")
    private String charset;

    @Value("${sms.api.timeout:30}")
    private int timeout;

    @Value("${sms.api.retry:3}")
    private int retryCount;

    @Value("${sms.api.retry.delay:60}")
    private int retryDelay;

    @Value("${sms.api.proxy.enabled:false}")
    private boolean proxyEnabled;

    @Value("${sms.api.proxy.host:}")
    private String proxyHost;

    @Value("${sms.api.proxy.port:0}")
    private int proxyPort;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    // Кэш для хранения времени последней отправки SMS на номер
    private final ConcurrentHashMap<String, Long> lastSmsTime = new ConcurrentHashMap<>();
    private static final long MIN_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    public SmsService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout * 1000);
        factory.setReadTimeout(timeout * 1000);
        
        // Настраиваем прокси если он включен
        if (proxyEnabled && proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
            factory.setProxy(new java.net.Proxy(
                java.net.Proxy.Type.HTTP,
                new java.net.InetSocketAddress(proxyHost, proxyPort)
            ));
            log.info("Proxy enabled: {}:{}", proxyHost, proxyPort);
        }
        
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    public String generateAndSendOtp(String phoneNumber) {
        String otp = generateRandomOtp();
        
        try {
            // Форматируем номер телефона для SMSC.ru (убираем все кроме цифр)
            String formattedPhone = phoneNumber.replaceAll("[^0-9]", "");
            if (!formattedPhone.startsWith("7")) {
                formattedPhone = "7" + formattedPhone;
            }
            
            // Проверяем, не было ли отправки SMS на этот номер в последнюю минуту
            Long lastTime = lastSmsTime.get(formattedPhone);
            if (lastTime != null) {
                long timeSinceLastSms = System.currentTimeMillis() - lastTime;
                if (timeSinceLastSms < MIN_INTERVAL) {
                    long waitTime = MIN_INTERVAL - timeSinceLastSms;
                    throw new RuntimeException(String.format(
                        "Пожалуйста, подождите %d секунд перед повторной отправкой кода",
                        TimeUnit.MILLISECONDS.toSeconds(waitTime)
                    ));
                }
            }
            
            sendSms(formattedPhone, "Ваш код подтверждения: " + otp);
            // Сохраняем время отправки
            lastSmsTime.put(formattedPhone, System.currentTimeMillis());
            log.info("OTP sent to phone number: {}", phoneNumber);
            return otp;
        } catch (Exception e) {
            log.error("Failed to send OTP to phone number: {}", phoneNumber, e);
            throw new RuntimeException("Не удалось отправить код подтверждения: " + e.getMessage());
        }
    }

    private String generateRandomOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    private void sendSms(String phoneNumber, String message) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < retryCount) {
            try {
                // Проверяем имя отправителя
                if (smsSender == null || smsSender.trim().isEmpty()) {
                    throw new RuntimeException("Имя отправителя не настроено");
                }
                
                log.info("Using sender name: {}", smsSender);
                
                // Кодируем пароль в MD5
                String md5Password = getMD5Hash(smsPassword);
                
                // Кодируем сообщение в URL-формат
                String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
                
                // Формируем URL с параметрами для SMSC.ru
                String url = UriComponentsBuilder.fromHttpUrl(smsApiUrl)
                        .queryParam("login", smsLogin)
                        .queryParam("psw", md5Password)
                        .queryParam("phones", phoneNumber)
                        .queryParam("mes", encodedMessage)
                        .queryParam("sender", smsSender)
                        .queryParam("charset", charset)
                        .queryParam("cost", 3) // Получить стоимость и баланс
                        .queryParam("fmt", 3) // JSON формат ответа
                        .queryParam("valid", 1) // Срок жизни сообщения 1 час
                        .queryParam("maxsms", 1) // Максимум 1 SMS
                        .build()
                        .toUriString();

                log.info("Sending SMS request to SMSC.ru for phone: {}", phoneNumber);
                log.debug("Request URL: {}", url);
                
                // Отправляем GET запрос
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("SMSC.ru вернул ошибку: " + response.getStatusCode());
                }

                String responseBody = response.getBody();
                log.debug("SMSC.ru response: {}", responseBody);

                // Проверяем, является ли ответ JSON
                if (responseBody.startsWith("{")) {
                    // Парсим JSON ответ
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    
                    // Проверяем наличие ошибки
                    if (jsonResponse.has("error")) {
                        String errorCode = jsonResponse.get("error").asText();
                        String errorMessage = getErrorMessage(errorCode);
                        throw new RuntimeException("Ошибка SMSC.ru: " + errorMessage);
                    }

                    // Проверяем статус отправки
                    if (jsonResponse.has("cnt")) {
                        int count = jsonResponse.get("cnt").asInt();
                        if (count > 0) {
                            log.info("SMS sent successfully to {}, cost: {}, balance: {}", 
                                    phoneNumber, 
                                    jsonResponse.has("cost") ? jsonResponse.get("cost").asText() : "unknown",
                                    jsonResponse.has("balance") ? jsonResponse.get("balance").asText() : "unknown");
                            return; // Успешная отправка
                        } else {
                            throw new RuntimeException("SMS не был отправлен");
                        }
                    }
                } else {
                    // Обработка текстового ответа
                    if (responseBody.startsWith("ERROR")) {
                        String errorCode = responseBody.substring(7, 8); // Получаем код ошибки
                        String errorMessage = getErrorMessage(errorCode);
                        throw new RuntimeException("Ошибка SMSC.ru: " + errorMessage);
                    } else if (responseBody.startsWith("OK")) {
                        log.info("SMS sent successfully to {}", phoneNumber);
                        return; // Успешная отправка
                    } else {
                        throw new RuntimeException("Неизвестный формат ответа от SMSC.ru: " + responseBody);
                    }
                }
            } catch (Exception e) {
                lastException = e;
                attempts++;
                if (attempts < retryCount) {
                    log.warn("Attempt {} failed, retrying... Error: {}", attempts, e.getMessage());
                    try {
                        // Используем настраиваемую задержку между попытками
                        Thread.sleep(retryDelay * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("SMS sending interrupted", ie);
                    }
                }
            }
        }

        // Если все попытки не удались
        log.error("All {} attempts to send SMS failed", retryCount);
        throw new RuntimeException("Ошибка при отправке SMS после " + retryCount + " попыток: " + 
                (lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    private String getMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании MD5 хеша: " + e.getMessage());
        }
    }

    private String getErrorMessage(String errorCode) {
        return switch (errorCode) {
            case "1" -> "Ошибка в параметрах";
            case "2" -> "Неверный логин или пароль";
            case "3" -> "Недостаточно средств на счете";
            case "4", "ip is blocked" -> "IP-адрес временно заблокирован. Пожалуйста, обратитесь в службу поддержки SMSC.ru";
            case "5" -> "Неверный формат даты";
            case "6" -> "Сообщение запрещено";
            case "7" -> "Неверный формат номера телефона";
            case "8" -> "Сообщение на указанный номер не может быть доставлено";
            case "9" -> "Отправка более одного одинакового запроса на отправку SMS-сообщения в течение минуты";
            case "authorise error" -> "Ошибка авторизации. Проверьте логин и пароль";
            case "duplicate request, wait a minute" -> "Пожалуйста, подождите минуту перед повторной отправкой кода";
            default -> "Неизвестная ошибка: " + errorCode;
        };
    }
} 