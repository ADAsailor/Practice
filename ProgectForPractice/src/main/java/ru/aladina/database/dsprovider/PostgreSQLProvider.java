package ru.aladina.database.dsprovider;

import org.postgresql.ds.PGPoolingDataSource;
import ru.aladina.database.property.PropertyContainer;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * Провайдер для работы с PostgreSQL (JavaDB).
 * (Данный класс предоставляет подключение к базе данных)
 */
public class PostgreSQLProvider implements DataSourceProvider{
    /** Источник данных для PostgreSQL. */
    private PGPoolingDataSource dataSource;
    /**
     * Метод получения источника данных для PostgreSQL.
     *
     * @return источник данных для PostgreSQL
     */
    @Override
    public DataSource getDataSource() {
        if (Objects.isNull(dataSource)) {
            dataSource = new PGPoolingDataSource();
            dataSource.setDatabaseName(PropertyContainer.getProperty("database.name"));

            // задаем пользователя для всех будущих соединений с БД
            var username = PropertyContainer.getProperty("database.username");
            var password = PropertyContainer.getProperty("database.password");
            if (!username.isEmpty() && !password.isEmpty()) {
                dataSource.setUser(username);
                dataSource.setPassword(password);
            }
        }
        return dataSource;
    }
}