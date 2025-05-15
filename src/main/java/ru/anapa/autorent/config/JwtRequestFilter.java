package ru.anapa.autorent.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.anapa.autorent.util.JwtTokenUtil;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final ApplicationContext applicationContext;
    
    // Используем для решения проблемы циклической зависимости
    private UserDetailsService userDetailsService;

    @Autowired
    public JwtRequestFilter(JwtTokenUtil jwtTokenUtil, ApplicationContext applicationContext) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.applicationContext = applicationContext;
    }
    
    // Ленивое получение UserDetailsService для избежания циклической зависимости
    private UserDetailsService getUserDetailsService() {
        if (userDetailsService == null) {
            userDetailsService = applicationContext.getBean(UserDetailsService.class);
        }
        return userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Проверяем, является ли запрос API запросом
        if (!request.getRequestURI().startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token находится в заголовке Authorization в формате "Bearer token".
        // Удаляем часть "Bearer " и получаем только токен
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                logger.warn("Unable to get JWT Token or Token has expired", e);
            }
        } else {
            logger.debug("JWT Token does not begin with Bearer String or is missing");
        }

        // После получения токена, проверяем его валидность
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = getUserDetailsService().loadUserByUsername(username);

            // Если токен валиден, настраиваем Spring Security для ручной установки аутентификации
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // После настройки аутентификации в контексте, указываем что текущий пользователь аутентифицирован
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
} 