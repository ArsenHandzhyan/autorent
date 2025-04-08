package ru.anapa.autorent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.anapa.autorent.model.Role;
import ru.anapa.autorent.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    // Исправленный метод для поиска пользователей по роли
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRoles(@Param("role") Role role);

    // Если у вас есть поле registrationDate в User
    // List<User> findByRegistrationDateBetween(LocalDateTime start, LocalDateTime end);

    List<User> findByEnabledTrue();
    List<User> findByEnabledFalse();

    // Если у вас есть поле lastLoginDate в User
    // @Query("SELECT u FROM User u WHERE u.lastLoginDate < :date")
    // List<User> findInactiveUsers(@Param("date") LocalDateTime date);

    // Если у вас есть связь с Rentals
    // List<User> findByRentalsIdGreaterThan(int rentalId);
}