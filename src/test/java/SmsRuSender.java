import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SmsRuSender {
    // API-ключ из вашего личного кабинета SMS.ru
    private static final String API_KEY = "A25F8010-8575-8B76-AC1C-78A8E528F108";
    private static final String API_URL = "https://sms.ru/sms/send";

    public static void main(String[] args) {
        try {
            // Номер получателя SMS
            String phoneNumber = "+79186814513";
            // Текст сообщения
            String message = "Это тестовое сообщение от SmsRuSender";

            // Создаем параметры запроса
            Map<String, String> parameters = new HashMap<>();
            parameters.put("api_id", API_KEY);
            parameters.put("to", phoneNumber);
            parameters.put("msg", message);
            parameters.put("json", "1");  // Получаем ответ в формате JSON
            parameters.put("test", "1");  // Включаем тестовый режим

            // Формируем строку запроса из параметров
            String requestBody = parameters.entrySet()
                    .stream()
                    .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            // Создаем HTTP клиент
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // Создаем POST запрос
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Отправляем запрос и получаем ответ
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Выводим результат
            System.out.println("Код статуса: " + response.statusCode());
            System.out.println("Ответ: " + response.body());

        } catch (Exception e) {
            System.err.println("Ошибка при отправке сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}