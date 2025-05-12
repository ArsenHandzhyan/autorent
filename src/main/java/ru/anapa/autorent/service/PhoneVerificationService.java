package ru.anapa.autorent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Сервис для верификации телефонных номеров через отправку и проверку OTP-кодов
 */
@Service
@Slf4j
public class PhoneVerificationService {

    // Временное хранилище для OTP-кодов (телефон -> код и время создания)
    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();
    private static final long EXPIRY_TIME_MINUTES = 5; // Время действия кода в минутах
    private static final int OTP_LENGTH = 6; // Длина OTP-кода

    /**
     * Генерирует и "отправляет" OTP-код на указанный номер телефона
     * @param phoneNumber Номер телефона в формате +7(XXX)XXX-XX-XX
     * @return Сгенерированный код (в реальном приложении код не возвращается, а отправляется через SMS)
     */
    public String generateAndSendOTP(String phoneNumber) {
        // Генерируем случайный код
        String otp = generateRandomOTP();

        // Сохраняем код с меткой времени
        otpStorage.put(phoneNumber, new OtpEntry(otp, System.currentTimeMillis()));
        log.info("OTP generated for phone {}: {}", phoneNumber, otp);

        // В реальном приложении здесь должна быть интеграция с SMS-шлюзом
        // Например: smsGateway.sendSMS(phoneNumber, "Ваш код подтверждения: " + otp);
        // Для примера просто возвращаем код (в целях тестирования)
        return otp;
    }

    /**
     * Проверяет введенный пользователем код для указанного номера телефона
     * @param phoneNumber Номер телефона
     * @param inputOtp Код, введенный пользователем
     * @return true, если код верный и не истек срок действия
     */
    public boolean verifyOTP(String phoneNumber, String inputOtp) {
        OtpEntry entry = otpStorage.get(phoneNumber);
        if (entry == null) {
            log.warn("No OTP found for phone: {}", phoneNumber);
            return false;
        }

        // Проверяем, не истек ли срок действия кода
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - entry.getCreationTime();
        if (elapsedTime > TimeUnit.MINUTES.toMillis(EXPIRY_TIME_MINUTES)) {
            log.warn("OTP expired for phone: {}", phoneNumber);
            otpStorage.remove(phoneNumber);
            return false;
        }

        // Проверяем, совпадает ли код
        boolean isValid = entry.getOtp().equals(inputOtp);
        if (isValid) {
            log.info("OTP verified successfully for phone: {}", phoneNumber);
            otpStorage.remove(phoneNumber); // Удаляем после успешной проверки
        } else {
            log.warn("Invalid OTP for phone: {}", phoneNumber);
        }
        return isValid;
    }

    /**
     * Генерирует случайный OTP-код заданной длины
     */
    private String generateRandomOTP() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Внутренний класс для хранения OTP-кода и времени его создания
     */
    private static class OtpEntry {
        private final String otp;
        private final long creationTime;

        public OtpEntry(String otp, long creationTime) {
            this.otp = otp;
            this.creationTime = creationTime;
        }

        public String getOtp() {
            return otp;
        }

        public long getCreationTime() {
            return creationTime;
        }
    }
}
