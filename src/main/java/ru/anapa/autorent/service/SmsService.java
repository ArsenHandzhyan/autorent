package ru.anapa.autorent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SmsService {

    @Value("${sms.api.key}")
    private String apiKey;

    @Value("${sms.api.url}")
    private String apiUrl;

    @Value("${sms.api.call.url:https://sms.ru/code/call}")
    private String apiCallUrl;

    @Value("${sms.api.timeout:30}")
    private int timeout;

    @Value("${sms.api.test:true}")
    private boolean testMode;

    @Value("${sms.sender.name:SMS}")
    private String senderName;  // Добавляем стандартное имя отправителя с дефолтным значением "SMS"

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom secureRandom = new SecureRandom();

    // Кэш для хранения времени последней отправки SMS на номер
    private final ConcurrentHashMap<String, Long> lastSmsTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastCallTime = new ConcurrentHashMap<>();
    private static final long MIN_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    // Создаем конструктор без параметров
    public SmsService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        log.info("SmsService инициализирован с использованием SMS.ru API");
    }

    /**
     * Генерирует и отправляет OTP-код на указанный номер телефона через SMS
     *
     * @param phoneNumber номер телефона
     * @return сгенерированный OTP-код
     */
    public String generateAndSendOtp(String phoneNumber) {
        log.debug("Запрос на генерацию и отправку OTP для номера {}", phoneNumber);

        // Проверяем, не слишком ли часто отправляем SMS
        Long lastTime = lastSmsTime.get(phoneNumber);
        long currentTime = System.currentTimeMillis();

        if (lastTime != null && (currentTime - lastTime) < MIN_INTERVAL) {
            long secondsToWait = (MIN_INTERVAL - (currentTime - lastTime)) / 1000;
            log.warn("Слишком частый запрос SMS для номера {}. Нужно подождать {} секунд", phoneNumber, secondsToWait);
            throw new RuntimeException("Пожалуйста, подождите " + secondsToWait + " секунд перед повторной отправкой SMS");
        }

        // Генерируем OTP
        String otp = generateRandomOtp();

        // Отправляем SMS
        try {
            String message = "Ваш код подтверждения для регистрации в АвтоРент: " + otp;
            sendSms(phoneNumber, message);

            // Записываем время отправки
            lastSmsTime.put(phoneNumber, currentTime);
            log.info("OTP успешно отправлен на номер {}", phoneNumber);
            return otp;
        } catch (Exception e) {
            log.error("Ошибка при отправке OTP на номер {}: {}", phoneNumber, e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить SMS: " + e.getMessage());
        }
    }

    /**
     * Генерирует случайный OTP-код заданной длины
     *
     * @return сгенерированный OTP-код
     */
    private String generateRandomOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Отправляет SMS-сообщение через API SMS.ru
     *
     * @param phoneNumber номер телефона получателя
     * @param message     текст сообщения
     * @throws Exception если произошла ошибка при отправке
     */
    private void sendSms(String phoneNumber, String message) {
        try {
            log.debug("Отправка SMS на номер {}: {}", phoneNumber, message);

            // Создаем параметры запроса
            Map<String, String> parameters = new HashMap<>();
            parameters.put("api_id", apiKey);
            parameters.put("to", phoneNumber);
            parameters.put("msg", message);
            parameters.put("json", "1");  // Получаем ответ в формате JSON

            // Удаляем параметр from, чтобы использовалось имя отправителя по умолчанию
            // parameters.put("from", senderName);

            if (testMode) {
                parameters.put("test", "1");  // Включаем тестовый режим
                log.debug("Используется тестовый режим отправки SMS");
            }

            // Формируем строку запроса из параметров
            String requestBody = parameters.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            // Создаем POST запрос
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Отправляем запрос и получаем ответ
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Обрабатываем ответ
            int statusCode = response.statusCode();
            String responseBody = response.body();
            log.debug("Ответ от SMS.ru API (код {}): {}", statusCode, responseBody);

            if (statusCode != 200) {
                throw new RuntimeException("Ошибка API SMS.ru: HTTP " + statusCode);
            }

            // Парсим JSON ответ
            JsonNode rootNode = objectMapper.readTree(responseBody);

            if (rootNode.has("status_code")) {
                int apiStatusCode = rootNode.path("status_code").asInt();

                if (apiStatusCode != 100) {
                    String errorMessage = "Ошибка API SMS.ru: " + rootNode.path("status_text").asText();
                    log.error(errorMessage);
                    throw new RuntimeException(errorMessage);
                }

                // Проверяем статус отправки для указанного номера
                if (rootNode.has("sms") && rootNode.path("sms").has(phoneNumber)) {
                    JsonNode smsNode = rootNode.path("sms").path(phoneNumber);
                    if (smsNode.has("status_code") && smsNode.path("status_code").asInt() != 100) {
                        String errorMessage = "Ошибка отправки SMS на номер " + phoneNumber + ": " +
                                smsNode.path("status_text").asText();
                        log.error(errorMessage);
                        throw new RuntimeException(errorMessage);
                    }
                }
            }

            log.info("SMS успешно отправлено на номер {}", phoneNumber);

        } catch (Exception e) {
            log.error("Ошибка при отправке SMS: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить SMS: " + e.getMessage());
        }
    }

    /**
     * Отправляет запрос на звонок с кодом через API SMS.ru
     *
     * @param phoneNumber номер телефона получателя
     * @param code        код, который будет продиктован при звонке
     * @throws Exception если произошла ошибка при отправке
     */
    private void sendCallOtp(String phoneNumber, String code) {
        try {
            log.debug("Инициация звонка с кодом {} на номер {}", code, phoneNumber);

            // Создаем параметры запроса
            Map<String, String> parameters = new HashMap<>();
            parameters.put("api_id", apiKey);
            parameters.put("phone", phoneNumber);
            parameters.put("code", code);
            parameters.put("json", "1");  // Получаем ответ в формате JSON

            // Убираем тестовый режим, чтобы звонки отправлялись реально
            // if (testMode) {
            //     parameters.put("test", "1");
            //     log.debug("Используется тестовый режим для звонка");
            // }

            // Формируем строку запроса из параметров
            String requestBody = parameters.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            // Создаем POST запрос
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiCallUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Отправляем запрос и получаем ответ
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Обрабатываем ответ
            int statusCode = response.statusCode();
            String responseBody = response.body();
            log.debug("Ответ от SMS.ru API для звонка (код {}): {}", statusCode, responseBody);

            if (statusCode != 200) {
                throw new RuntimeException("Ошибка API SMS.ru для звонка: HTTP " + statusCode);
            }

            // Парсим JSON ответ
            JsonNode rootNode = objectMapper.readTree(responseBody);

            if (rootNode.has("status")) {
                String status = rootNode.path("status").asText();
                if (!"OK".equals(status)) {
                    String errorMessage = "Ошибка API SMS.ru для звонка: " + rootNode.path("status_text").asText();
                    log.error(errorMessage);
                    throw new RuntimeException(errorMessage);
                }
                log.info("Звонок с кодом {} успешно инициирован на номер {}", rootNode.path("code").asText(), phoneNumber);
            }

        } catch (Exception e) {
            log.error("Ошибка при инициации звонка с кодом: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось выполнить звонок с кодом: " + e.getMessage());
        }
    }

    /**
     * Генерирует случайный OTP-код заданной длины для звонка (4 цифры)
     *
     * @return сгенерированный OTP-код для звонка
     */
    private String generateRandomCallOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Генерирует и отправляет OTP-код на указанный номер телефона через звонок
     *
     * @param phoneNumber номер телефона
     * @return сгенерированный OTP-код
     */
    public String generateAndSendCallOtp(String phoneNumber) {
        log.debug("Запрос на генерацию и отправку OTP через звонок для номера {}", phoneNumber);

        // Проверяем, не слишком ли часто отправляем звонки
        Long lastTime = lastCallTime.get(phoneNumber);
        long currentTime = System.currentTimeMillis();

        if (lastTime != null && (currentTime - lastTime) < MIN_INTERVAL) {
            long secondsToWait = (MIN_INTERVAL - (currentTime - lastTime)) / 1000;
            log.warn("Слишком частый запрос звонка для номера {}. Нужно подождать {} секунд", phoneNumber, secondsToWait);
            throw new RuntimeException("Пожалуйста, подождите " + secondsToWait + " секунд перед повторным звонком");
        }

        // Генерируем OTP (4 цифры для звонка)
        String otp = generateRandomCallOtp();

        // Отправляем запрос на звонок с кодом
        try {
            sendCallOtp(phoneNumber, otp);

            // Записываем время звонка
            lastCallTime.put(phoneNumber, currentTime);
            log.info("Звонок с OTP успешно инициирован на номер {}", phoneNumber);
            return otp;
        } catch (Exception e) {
            log.error("Ошибка при инициации звонка с OTP на номер {}: {}", phoneNumber, e.getMessage(), e);
            throw new RuntimeException("Не удалось выполнить звонок с кодом: " + e.getMessage());
        }
    }
}