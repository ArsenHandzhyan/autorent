package ru.anapa.autorent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.anapa.autorent.model.Account;
import ru.anapa.autorent.model.DailyPayment;
import ru.anapa.autorent.model.Rental;
import ru.anapa.autorent.model.User;
import ru.anapa.autorent.repository.DailyPaymentRepository;
import ru.anapa.autorent.repository.RentalRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyPaymentServiceTest {

    @Mock
    private DailyPaymentRepository dailyPaymentRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private PaymentNotificationService notificationService;

    @InjectMocks
    private DailyPaymentService dailyPaymentService;

    private User testUser;
    private Account testAccount;
    private Rental testRental;
    private DailyPayment testPayment;

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Тест");
        testUser.setLastName("Пользователь");

        // Создаем тестовый счет
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUser(testUser);
        testAccount.setBalance(new BigDecimal("5000.00"));
        testAccount.setAllowNegativeBalance(false);
        testAccount.setCreditLimit(new BigDecimal("10000.00"));

        // Создаем тестовую аренду
        testRental = new Rental();
        testRental.setId(1L);
        testRental.setUser(testUser);
        testRental.setStartDate(LocalDateTime.now());
        testRental.setEndDate(LocalDateTime.now().plusDays(7));

        // Создаем тестовый платеж
        testPayment = new DailyPayment();
        testPayment.setId(1L);
        testPayment.setRental(testRental);
        testPayment.setAccount(testAccount);
        testPayment.setPaymentDate(LocalDate.now());
        testPayment.setAmount(new BigDecimal("3000.00"));
        testPayment.setStatus(DailyPayment.PaymentStatus.PENDING);
    }

    @Test
    void testCreateDailyPayment_Success() {
        // Arrange
        when(dailyPaymentRepository.findByRentalAndPaymentDate(any(Rental.class), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(accountService.getAccountByUserId(anyLong())).thenReturn(testAccount);
        when(dailyPaymentRepository.save(any(DailyPayment.class))).thenReturn(testPayment);

        // Act
        DailyPayment result = dailyPaymentService.createDailyPayment(testRental, LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(testPayment.getId(), result.getId());
        verify(dailyPaymentRepository).save(any(DailyPayment.class));
    }

    @Test
    void testCreateDailyPayment_AlreadyExists() {
        // Arrange
        when(dailyPaymentRepository.findByRentalAndPaymentDate(any(Rental.class), any(LocalDate.class)))
                .thenReturn(Optional.of(testPayment));

        // Act
        DailyPayment result = dailyPaymentService.createDailyPayment(testRental, LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(testPayment.getId(), result.getId());
        verify(dailyPaymentRepository, never()).save(any(DailyPayment.class));
    }

    @Test
    void testProcessPayment_Success_EnoughBalance() {
        // Arrange
        testAccount.setBalance(new BigDecimal("5000.00"));
        testAccount.setAllowNegativeBalance(false);
        testPayment.setAmount(new BigDecimal("3000.00"));

        when(dailyPaymentRepository.save(any(DailyPayment.class))).thenReturn(testPayment);

        // Act
        dailyPaymentService.processPayment(testPayment);

        // Assert
        assertEquals(DailyPayment.PaymentStatus.PROCESSED, testPayment.getStatus());
        assertNotNull(testPayment.getProcessedAt());
        verify(accountService).updateBalance(testUser.getId(), new BigDecimal("-3000.00"));
        verify(notificationService).sendPaymentProcessedNotification(testPayment);
    }

    @Test
    void testProcessPayment_Failed_InsufficientBalance_NoNegativeAllowed() {
        // Arrange
        testAccount.setBalance(new BigDecimal("2000.00"));
        testAccount.setAllowNegativeBalance(false);
        testPayment.setAmount(new BigDecimal("3000.00"));

        when(dailyPaymentRepository.save(any(DailyPayment.class))).thenReturn(testPayment);

        // Act
        dailyPaymentService.processPayment(testPayment);

        // Assert
        assertEquals(DailyPayment.PaymentStatus.FAILED, testPayment.getStatus());
        assertNotNull(testPayment.getProcessedAt());
        assertTrue(testPayment.getNotes().contains("Недостаточно средств на счете"));
        verify(accountService, never()).updateBalance(anyLong(), any(BigDecimal.class));
        verify(notificationService).sendPaymentFailedNotification(eq(testPayment), anyString());
        verify(notificationService).sendAdminPaymentFailureNotification(eq(testPayment), anyString());
    }

    @Test
    void testProcessPayment_Success_NegativeBalanceAllowed() {
        // Arrange
        testAccount.setBalance(new BigDecimal("2000.00"));
        testAccount.setAllowNegativeBalance(true);
        testAccount.setCreditLimit(new BigDecimal("10000.00"));
        testPayment.setAmount(new BigDecimal("3000.00"));

        when(dailyPaymentRepository.save(any(DailyPayment.class))).thenReturn(testPayment);

        // Act
        dailyPaymentService.processPayment(testPayment);

        // Assert
        assertEquals(DailyPayment.PaymentStatus.PROCESSED, testPayment.getStatus());
        assertNotNull(testPayment.getProcessedAt());
        verify(accountService).updateBalance(testUser.getId(), new BigDecimal("-3000.00"));
        verify(notificationService).sendPaymentProcessedNotification(testPayment);
    }

    @Test
    void testProcessPayment_Failed_CreditLimitExceeded() {
        // Arrange
        testAccount.setBalance(new BigDecimal("-8000.00"));
        testAccount.setAllowNegativeBalance(true);
        testAccount.setCreditLimit(new BigDecimal("10000.00"));
        testPayment.setAmount(new BigDecimal("3000.00"));

        when(dailyPaymentRepository.save(any(DailyPayment.class))).thenReturn(testPayment);

        // Act
        dailyPaymentService.processPayment(testPayment);

        // Assert
        assertEquals(DailyPayment.PaymentStatus.FAILED, testPayment.getStatus());
        assertNotNull(testPayment.getProcessedAt());
        assertTrue(testPayment.getNotes().contains("Превышен кредитный лимит"));
        verify(accountService, never()).updateBalance(anyLong(), any(BigDecimal.class));
        verify(notificationService).sendPaymentFailedNotification(eq(testPayment), anyString());
        verify(notificationService).sendAdminPaymentFailureNotification(eq(testPayment), anyString());
    }

    @Test
    void testProcessPayment_Success_NegativeBalanceAllowed_NoCreditLimit() {
        // Arrange
        testAccount.setBalance(new BigDecimal("2000.00"));
        testAccount.setAllowNegativeBalance(true);
        testAccount.setCreditLimit(BigDecimal.ZERO);
        testPayment.setAmount(new BigDecimal("3000.00"));

        when(dailyPaymentRepository.save(any(DailyPayment.class))).thenReturn(testPayment);

        // Act
        dailyPaymentService.processPayment(testPayment);

        // Assert
        assertEquals(DailyPayment.PaymentStatus.PROCESSED, testPayment.getStatus());
        assertNotNull(testPayment.getProcessedAt());
        verify(accountService).updateBalance(testUser.getId(), new BigDecimal("-3000.00"));
        verify(notificationService).sendPaymentProcessedNotification(testPayment);
    }

    @Test
    void testProcessPayment_Exception_Handling() {
        // Arrange
        testAccount.setBalance(new BigDecimal("5000.00"));
        testAccount.setAllowNegativeBalance(false);
        testPayment.setAmount(new BigDecimal("3000.00"));

        when(accountService.updateBalance(anyLong(), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Техническая ошибка"));
        when(dailyPaymentRepository.save(any(DailyPayment.class))).thenReturn(testPayment);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            dailyPaymentService.processPayment(testPayment);
        });

        assertEquals(DailyPayment.PaymentStatus.FAILED, testPayment.getStatus());
        assertNotNull(testPayment.getProcessedAt());
        assertTrue(testPayment.getNotes().contains("Техническая ошибка"));
        verify(notificationService).sendPaymentFailedNotification(eq(testPayment), anyString());
        verify(notificationService).sendAdminPaymentFailureNotification(eq(testPayment), anyString());
    }

    @Test
    void testMarkPaymentAsFailed() {
        // Arrange
        String errorMessage = "Тестовая ошибка";
        when(dailyPaymentRepository.save(any(DailyPayment.class))).thenReturn(testPayment);

        // Act
        dailyPaymentService.markPaymentAsFailed(testPayment, errorMessage);

        // Assert
        assertEquals(DailyPayment.PaymentStatus.FAILED, testPayment.getStatus());
        assertNotNull(testPayment.getProcessedAt());
        assertEquals("Ошибка: " + errorMessage, testPayment.getNotes());
        verify(dailyPaymentRepository).save(testPayment);
    }
} 