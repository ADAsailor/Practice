package ru.aladina.database.repository;

import ru.aladina.database.model.MyFile;

import java.util.List;

/**
 * Интерфейс репозитория файлов.
 */
public interface FileRepository {
    /**
     * Метод инициализации таблицы.
     */
    void initTable();
    /**
     * Метод создания новой записи.
     */
    MyFile create(MyFile file);

    /**
     * Метод получения всех записей из таблицы.
     *
     * @return список записей
     */
    List<MyFile> findAll();

    /**
     * Метод поиска файла по его идентификатору.
     *
     * @param id идентификатор файла
     *
     * @return искомый файл
     */
    MyFile findById(String id);
    /**
     * Метод поиска файла по его хэш-сумме.
     *
     * @param hashSum идентификатор файла
     *
     * @return искомый файл
     */
    List<MyFile> findByHashSum(String hashSum);

    /**
     * Метод обновляет информацию об исходных файлах в базе данных.
     *
     * @param id идентификатор файла
     * @param motherID идентификатор исходного файла
     */
    void updateFilesMothersIdInfo(String id, String motherID);

    /**
     * Метод очищает таблицу от всей имеющейся в ней информации.
     *
     * @param tableName имя таблицы
     */
    void deleteTableInfo(String tableName);


}
