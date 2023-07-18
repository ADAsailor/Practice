package ru.aladina.database.property;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Контейнер с настройками программы.
 */
public class PropertyContainer {
    /** Настройки. */
    private final static Map<String, String> properties = new HashMap<>();

    /**
     * Загрузчик настроек. Формирует словарь с настройками, где ключом является
     * имя настройки, а значением - содержание настройки.
     *
     * @throws IOException ошибка при загрузке настроек
     */
    public static void loadProperties() throws IOException {
        var appProperties = new Properties();
        try {
            appProperties.load(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
            for (var entry : appProperties.entrySet()) {
                properties.put((String) entry.getKey(), (String) entry.getValue());
            }
        } catch (Exception e) {
            System.out.println("Возникла ошибка при загрузке настроек");
            throw e;
        }
    }

    /**
     * Возвращает значение, расположенное по заданному ключу.
     *
     * @param propertyKey имя настройки (ключ словаря настроек)
     *
     * @return значение из словаря настроек, расположенное по заданному ключу
     */
    public static String getProperty(String propertyKey) {
        return properties.getOrDefault(propertyKey, "");
    }
}
