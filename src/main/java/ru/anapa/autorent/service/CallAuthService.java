package ru.anapa.autorent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CallAuthService {
    private static final Logger logger = LoggerFactory.getLogger(CallAuthService.class);
    private static final String API_URL = "https://sms.ru/callcheck";
    private final RestTemplate restTemplate;
    private final Map<String, String> phoneToCheckId = new ConcurrentHashMap<>();

    @Value("${sms.ru.api.id}")
    private String apiId;

    public CallAuthService() {
        this.restTemplate = new RestTemplate();
    }

    private String normalizePhone(String phone) {
        String digits = phone.replaceAll("\\D", "");
        if (digits.startsWith("8")) {
            digits = "7" + digits.substring(1);
        }
        if (!digits.startsWith("7")) {
            digits = "7" + digits;
        }
        return digits;
    }

    private String normalizePhoneForApi(String phone) {
        // Только цифры, начиная с 7, для передачи в API
        String digits = phone.replaceAll("\\D", "");
        if (digits.startsWith("8")) {
            digits = "7" + digits.substring(1);
        }
        if (!digits.startsWith("7")) {
            digits = "7" + digits;
        }
        return digits;
    }

    public Map<String, Object> initiateCallAuth(String phone) {
        try {
            String key = normalizePhoneForApi(phone); // только цифры для карты и API
            String url = UriComponentsBuilder.fromHttpUrl(API_URL + "/add")
                    .queryParam("api_id", apiId)
                    .queryParam("phone", key)
                    .queryParam("json", 1)
                    .build()
                    .toString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> result = response.getBody();

            if (result != null && "OK".equals(result.get("status"))) {
                String checkId = (String) result.get("check_id");
                phoneToCheckId.put(key, checkId);
                logger.info("Call auth initiated for phone: {}, checkId: {}", key, checkId);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error initiating call auth for phone {}: {}", phone, e.getMessage(), e);
            throw new RuntimeException("Ошибка при инициализации авторизации по звонку", e);
        }
    }

    public Map<String, Object> checkCallStatus(String phone) {
        try {
            String key = normalizePhoneForApi(phone); // только цифры для поиска
            String checkId = phoneToCheckId.get(key);
            if (checkId == null) {
                logger.warn("Не найдена активная проверка для номера: {}", phone);
                Map<String, Object> errorResult = new java.util.HashMap<>();
                errorResult.put("status", "ERROR");
                errorResult.put("error_code", "NO_ACTIVE_CHECK");
                errorResult.put("error_message", "Не найдена активная проверка для номера: " + phone);
                return errorResult;
            }

            String url = UriComponentsBuilder.fromHttpUrl(API_URL + "/status")
                    .queryParam("api_id", apiId)
                    .queryParam("check_id", checkId)
                    .queryParam("json", 1)
                    .build()
                    .toString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> result = response.getBody();

            if (result != null && "OK".equals(result.get("status"))) {
                String checkStatus = String.valueOf(result.get("check_status"));
                if ("401".equals(checkStatus)) {
                    phoneToCheckId.remove(key);
                }
            }

            return result;
        } catch (Exception e) {
            logger.error("Error checking call status for phone {}: {}", phone, e.getMessage(), e);
            throw new RuntimeException("Ошибка при проверке статуса звонка", e);
        }
    }

    public Map<String, Object> checkCallStatusByCheckId(String checkId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(API_URL + "/status")
                    .queryParam("api_id", apiId)
                    .queryParam("check_id", checkId)
                    .queryParam("json", 1)
                    .build()
                    .toString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> result = response.getBody();

            if (result != null && "OK".equals(result.get("status"))) {
                String checkStatus = String.valueOf(result.get("check_status"));
                if ("401".equals(checkStatus)) {
                    // Удаляем checkId из карты, если найдем соответствующий номер
                    phoneToCheckId.entrySet().removeIf(entry -> entry.getValue().equals(checkId));
                }
            }

            return result;
        } catch (Exception e) {
            logger.error("Error checking call status for check_id {}: {}", checkId, e.getMessage(), e);
            throw new RuntimeException("Ошибка при проверке статуса звонка", e);
        }
    }
} 