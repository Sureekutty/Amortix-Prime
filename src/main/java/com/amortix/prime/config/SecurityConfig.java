package com.amortix.prime.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/images/**", "/favicon.ico", "/actuator/health").permitAll()
                .requestMatchers("/login", "/error", "/access-denied").permitAll()

                // ADMIN strictly required for System Governance, Rules, Rates, Audit, User Provisioning, Member Settlement, Day Book Closing
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/members/settle/**").hasRole("ADMIN")
                .requestMatchers("/accounting/daybook/close").hasRole("ADMIN")
                .requestMatchers("/thrift/annual-interest").hasRole("ADMIN")
                .requestMatchers("/loans/approve/**", "/loans/disburse/**", "/loans/surety-replace/approve/**").hasRole("ADMIN")

                // OPERATIONAL (ADMIN + STAFF)
                .requestMatchers("/members", "/members/new", "/members/save").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/loans", "/loans/repay/**", "/loans/surety-replace").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/deposits", "/deposits/open", "/deposits/save", "/deposits/premature-close/**", "/deposits/rcd-installment").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/thrift").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/accounting/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/recovery/**").hasAnyRole("ADMIN", "STAFF")

                // MEMBER + ALL AUTHENTICATED (Scoped by SecurityService Data Ownership)
                .requestMatchers("/dashboard", "/polls/**").authenticated()
                .requestMatchers("/members/view/**", "/members/my-profile").authenticated()
                .requestMatchers("/loans/apply", "/loans/save", "/loans/view/**", "/loans/pdf/**", "/loans/surety-replace/request").authenticated()
                .requestMatchers("/deposits/view/**").authenticated()
                .requestMatchers("/thrift/member/**", "/thrift/my-thrift").authenticated()

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
