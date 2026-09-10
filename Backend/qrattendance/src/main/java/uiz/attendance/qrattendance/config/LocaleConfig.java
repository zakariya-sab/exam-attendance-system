package uiz.attendance.qrattendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.util.Locale;

/**
 * Pins the request locale to English regardless of the server's OS/JVM
 * default locale, so Bean Validation messages (e.g. from
 * MethodArgumentNotValidException) are consistent across environments
 * instead of depending on where the app happens to be deployed.
 */
@Configuration
public class LocaleConfig {

    @Bean
    public LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.ENGLISH);
    }
}
