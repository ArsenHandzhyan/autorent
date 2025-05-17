
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.nio.charset.StandardCharsets;

public class SmsSender {
    private static final String API_URL = "https://lcab.smsprofi.ru/json/v1.0/sms/send/text";
    private static final String TOKEN = "t9sie32rkp9nhzw5xxp3m64s30pgr8pwyfoyp0bbearnxba2un4lyazmnek3mzko"; // токен AUTORENT_ANAPA

    public static void main(String[] args) {
        try {
            // Создаем JSON для запроса
            // Обратите внимание, мы убрали поле sender из запроса,
            // так как система будет использовать отправителя, настроенного в личном кабинете
            String jsonRequest = "{"
                    + "\"messages\": ["
                    + "  {"
                    + "    \"recipient\": \"+79186814513\","
                    + "    \"text\": \"Это тестовое сообщение от SmsSender\""
                    + "  }"
                    + "],"
                    + "\"validate\": false"
                    + "}";

            // Создаем HTTP клиент
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // Создаем запрос
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("X-Token", TOKEN)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest, StandardCharsets.UTF_8))
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