package ru.aladina.database.repository.impl;

import ru.aladina.database.model.MyFile;
import ru.aladina.database.repository.FileRepository;

import javax.sql.DataSource;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Реализация репозитория задач для PostgreSQL.
 */
public class PostgreSQLFileRepository implements FileRepository {
    /** Источник данных. */
    private final DataSource dataSource;

    /**
     * Конструктор для {@link PostgreSQLFileRepository}.
     *
     * @param dataSource источник данных
     */
    public PostgreSQLFileRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        initTable();
    }

    /**
     * Инициализация таблицы.
     * Если таблица отсутствует, метод создаёт её. Иначе выводит сообщение,
     * что такая таблица уже существует в базе данных.
     */
    @Override
    public void initTable() {
        //System.out.println("Инициализация таблицы: " + MyFile.TABLE_NAME);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
                statement.executeUpdate(
                        "CREATE TABLE "
                                + MyFile.TABLE_NAME
                                + " ("
                                + "id VARCHAR(36) PRIMARY KEY, "
                                + "title VARCHAR(255),"
                                + "filePath VARCHAR(255),"
                                + "size VARCHAR(255),"
                                + "dateTime_of_creation VARCHAR(255),"
                                + "hashSum VARCHAR(255),"
                                + "motherID VARCHAR(36)"
                                + ")");
                //System.out.println("Таблица успешно создана");
        } catch (SQLException e) {
            if (e.getMessage().equals(String.format("ERROR: relation \"%s\" already exists", MyFile.TABLE_NAME))) {
                //System.out.println("Таблица уже существует");
            } else {
                System.out.println("Возникла ошибка при создании таблицы: " + e.getMessage());
            }
        } finally {
            //System.out.println("=========================");
        }
    }

    /**
     * Создаёт новую запись в указанной таблице и возвращает информацию об этой записи
     * в виде объекта {@link MyFile}.
     *
     * @param file файл, информацию о котором нужно внести в таблицу.
     *
     * @return файл, внесённый в таблицу
     */
    @Override
    public MyFile create(MyFile file) {
        var query = "INSERT INTO " + MyFile.TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        var entityId = UUID.randomUUID();

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {
            statement.setString(
                    1,
                    entityId.toString());
            statement.setString(
                    2,
                    file.getTitle());
            statement.setString(
                    3,
                    file.getFilePath());
            statement.setLong(
                    4,
                    file.getSize());
            statement.setString(
                    5,
                    file.getDateTime());
            statement.setString(
                    6,
                    file.getHashSum());
            statement.setString(
                    7,
                    file.getMotherID());
            statement.execute();
        } catch (SQLException e) {
            System.out.println("Возникла ошибка выполнения запроса (создание): " + e.getMessage());
            return null;
        }
        return findById(entityId.toString());
    }

    /**
     * Формирует список всех объектов {@link MyFile},
     * хранящихся в таблице базы данных на момент вызова метода.
     *
     * @return список всех объектов в таблице
     */
    @Override
    public List<MyFile> findAll() {
        List<MyFile> files = new ArrayList<>();
        MyFile newFileInList;
        var query = String.format(
                "SELECT * FROM %s",
                MyFile.TABLE_NAME);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                newFileInList = new MyFile(UUID.fromString(resultSet.getString("id")),
                        resultSet.getString("title"),
                        resultSet.getString("filePath"),
                        resultSet.getLong("size"),
                        resultSet.getString("dateTime_of_creation"),
                        resultSet.getString("hashSum"),
                        resultSet.getString("motherID"));
                files.add(newFileInList);
            }
        } catch (SQLException | IOException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        return files;
    }

    /**
     * Находит объект в базе данных по заданному id.
     *
     * @param id идентификатор файла
     *
     * @return запись из базы данных, найденная по заданному id
     */

    @Override
    public MyFile findById(String  id) {
        var file = (MyFile) null;
        var query = String.format(
                "SELECT id, title, filePath, size, dateTime_of_creation, hashSum, motherID FROM %s WHERE " +
                        "id = '%s'",
                MyFile.TABLE_NAME,
                id);

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                file = new MyFile(
                        UUID.fromString(resultSet.getString("id")),
                        resultSet.getString("title"),
                        resultSet.getString("filePath"),
                        resultSet.getLong("size"),
                        resultSet.getString("dateTime_of_creation"),
                        resultSet.getString("hashSum"),
                        resultSet.getString("motherID"));
            }
        } catch (SQLException | IOException | NoSuchAlgorithmException e) {
            System.out.println("Возникла ошибка выполнения запроса (поиск по id): " + e.getMessage());
        }
        return file;
    }

    /**
     * Формирует список всех объектов {@link MyFile}, которые были найдены
     * в базе данных по заданной хэш-сумме.
     *
     * @param hashSum хэш-сумма
     *
     * @return список записей в базе даных, найденных по заданной хэш-сумме
     */
    @Override
    public List<MyFile> findByHashSum(String hashSum) {
        List<MyFile> files = new ArrayList<>();
        MyFile newFileInList;
        var query = String.format(
                "SELECT id, title, filePath, size, dateTime_of_creation, hashSum, motherID FROM %s WHERE " +
                        "hashSum = '%s'",
                MyFile.TABLE_NAME,
                hashSum);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                newFileInList = new MyFile(UUID.fromString(resultSet.getString("id")),
                        resultSet.getString("title"),
                        resultSet.getString("filePath"),
                        resultSet.getLong("size"),
                        resultSet.getString("dateTime_of_creation"),
                        resultSet.getString("hashSum"),
                        resultSet.getString("motherID"));
                files.add(newFileInList);
            }
        } catch (SQLException | IOException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        return files;
    }

    /**
     * Обновляет весь столбец с идентификаторами исходных файлов,
     * заполняет его информацией.
     *
     * @param id идентификатор файла
     * @param motherID идентификатор исходного файла
     */
    @Override
    public void updateFilesMothersIdInfo(String id, String motherID) {
        var query = String.format(
                "UPDATE %s SET motherID = '%s' WHERE " +
                        "id = '%s'",
                MyFile.TABLE_NAME,
                motherID,
                id);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Очищает  всё содержимое указанной таблицы.
     *
     * @param tableName название таблицы
     */
    @Override
    public void  deleteTableInfo(String tableName){
        var query = "TRUNCATE " + tableName;
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(query)) {
            statement.execute();
            //System.out.println("Данные из таблицы очищены успешно");
        } catch (SQLException e) {
            System.out.println("Возникла ошибка выполнения запроса (удаление данных из таблицы): " + e.getMessage());
        }
        finally {
            //System.out.println("=========================");
        }
    }

}
