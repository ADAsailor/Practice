package ru.aladina.database.model;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

/**
 * Файловая система.
 */
public class MyFile {
    /** Название таблицы для хранения объектов класса MyFile. */
    public static final String TABLE_NAME = "file_system";
    /** Уникальный идентификатор файла в базе данных. */
    private UUID id;
    /** Название файла. */
    private String title;
    /** Путь к файлу. */
    private String filePath;
    /**  Размер файла. */
    private Long size;
    /** Дата и время последнего изменения файла. */
    private String dateTime;
    /** Хэш-сумма файла. */
    private String hashSum;
    /** Идентификатор исходника для данного файла. */
    private String motherID;
    /**
     * Консторуктор для {@link MyFile}.
     *
     * @param filePath путь к файлу
     */
    public MyFile(String filePath) {
        this.filePath = filePath;
    }
    /**
     *  Консторуктор для {@link MyFile}.
     *
     * @param id идентификатор файла
     * @param title имя файла
     * @param filePath путь к файлу
     * @param size размер файла
     * @param dateTime дата и время последнего изменения
     * @param hashSum хэш-сумма
     * @param motherID идентификатор исходного файла
     *
     * @throws IOException ошибка при пвычислении хэш-суммы
     * @throws NoSuchAlgorithmException ошибка при пвычислении хэш-суммы
     */
    public MyFile(UUID id, String title, String filePath, Long size, String dateTime, String hashSum, String motherID) throws IOException, NoSuchAlgorithmException {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.size = size;
        this.dateTime = dateTime;
        this.hashSum = findHashSum();
        this.motherID = motherID;
    }

    /**
     * Уникальный идентификатор файла в базе данных.
     *
     * @return уникальный идентификатор файла в базе данных
     */
    public UUID getId() {
        return id;
    }

    /**
     * Имя файла.
     *
     * @return имя файла
     */
    public String getTitle() {
        File file = new File(filePath);
        return file.getName();
    }

    /**
     * Путь к файлу.
     *
     * @return путь к файлу
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Размер файла.
     *
     * @return размер файла
     */
    public Long getSize() {
        File file = new File(filePath);
        return file.length();
    }

    /**
     * Время и дата последнего изменения файла.
     *
     * @return время и дата последнего изменения файла
     */
    public String getDateTime() {
        File file = new File(filePath);
        long timestamp = file.lastModified();
        Date dateLastModified = new Date(timestamp);
        setDateTime(dateLastModified.toString());
        return dateTime;
    }

    /**
     * Хэш-сумма файла.
     *
     * @return хэш-сумма файла
     */
    public String getHashSum(){
        return hashSum;
    }

    /**
     * Метод для рассчёта хэш-суммы файла при помощи хэш-функции SHA-256.
     * (Алгоритм при рассчёте хэш-суммы не учитывает название файла, а только содержимое)
     *
     * @return хэш-сумма
     *
     * @throws IOException ошибка при пвычислении хэш-суммы
     * @throws NoSuchAlgorithmException ошибка при пвычислении хэш-суммы
     */
    public String findHashSum() throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(filePath); DigestInputStream dis = new DigestInputStream(fis, md)) {
            while (dis.read() != -1) ;
        }
        byte[] hashBytes = md.digest();
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : hashBytes) {
            stringBuilder.append(String.format("%02x", b));
        }

        hashSum = stringBuilder.toString();
        return hashSum;
    }

    /**
     * Идентификатор исходного файла.
     *
     * @return идентификатор исходного файла.
     */
    public String getMotherID() {
        return motherID;
    }

    /**
     * Устанавливает время и дату последнего изменения файла.
     *
     * @param dateTime время и дату последнего изменения файла
     */
    public void setDateTime(String dateTime) {
        this.dateTime= dateTime;
    }

    /**
     * Устанавливает хэш-сумму.
     *
     * @param hashSum хэш-сумма
     */
    public void setHashSum(String hashSum) {
        this.hashSum = hashSum;
    }
}
