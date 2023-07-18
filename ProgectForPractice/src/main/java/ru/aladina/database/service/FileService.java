package ru.aladina.database.service;

import ru.aladina.database.model.MyFile;
import ru.aladina.database.property.PropertyContainer;
import ru.aladina.database.repository.FileRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Класс отвечает за манипуляции с данными в базе.
 */
public class FileService {
    /** Манипулятор. */
    private final FileRepository fileRepository;
    /** Имя файловой системы. */
    public static final String FILESYSTEM_NAME = PropertyContainer.getProperty("filesystem.path").
            split("/")[PropertyContainer.getProperty("filesystem.path").split("/").length - 1];
    /** Уровень расположения имени главной директории в абсолютных путях файлов системы. */
    public static final Integer FILESYSTEM_LEVEL = PropertyContainer.getProperty("filesystem.path").
            split("/").length-1;

    /**
     * Конструктор для {@link FileService}.
     *
     * @param fileRepository манипулятор
     */
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    /**
     * Вставка всех файлов системы в таблицу базы данных.
     *
     * @param directory главная директория файловой системы
     */
    public void insertAllFiles(File directory) {
        // На всякий случай очищаем таблицу, чтобы работать только с актуальной информацией
        fileRepository.deleteTableInfo(MyFile.TABLE_NAME);
        try {
            Path directoryPath = Paths.get(directory.toURI());
            Files.walk(directoryPath).forEach(path -> {
                File nextFile = path.toFile();
                if (!nextFile.isDirectory() && !Files.isSymbolicLink(nextFile.toPath())) {
                    var insertedFile = new MyFile(nextFile.getAbsolutePath());
                    try {
                        insertedFile.setHashSum(insertedFile.findHashSum());
                    } catch (IOException | NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                    fileRepository.create(insertedFile);
                    //System.out.println('\n'+"Успех "+nextFile.getName()+" "+nextFile.getPath());
                }
            });
        } catch (IOException e) {
            System.out.println("Ошибка при обработке файловой системы: " + e.getMessage());
        }
    }

    public AtomicLong myFindAllFilesSize(File directory) throws IOException {
        AtomicLong sum = new AtomicLong();
        Files.walk(directory.toPath())
                    .map(Path::toFile).forEach(path -> {
                        if (!path.isDirectory()) {
                            sum.addAndGet(path.length());
                        }
                    });
        return sum;
    }


    /**
     * Находит исходные файлы для всех файлов из файловой системы.
     * В базе данных файлы делятся на 2 типа:
     * 1) Исходный файл - первый найденный в системе файл с уникальным содержанием.
     * (В качестве motherID у таких файлов в базе записывается их же id)
     * 2) Дублирующийся файл - файл, который повторяет по содержанию уже найденный в системе файл.
     * (В качестве motherID у таких файлов в базе записывается id их исходного файла)
     */
    public void findAllFilesMother(List<MyFile> allFilesInTable) {
        for (MyFile myFile : allFilesInTable) {
            int countOfDuplicate = fileRepository.findByHashSum(myFile.getHashSum()).size();
            MyFile masterFile;
            try {
                masterFile = new MyFile(fileRepository.findByHashSum(myFile.getHashSum()).get(0).getId(),
                        fileRepository.findByHashSum(myFile.getHashSum()).get(0).getTitle(),
                        fileRepository.findByHashSum(myFile.getHashSum()).get(0).getFilePath(),
                        fileRepository.findByHashSum(myFile.getHashSum()).get(0).getSize(),
                        fileRepository.findByHashSum(myFile.getHashSum()).get(0).getDateTime(),
                        fileRepository.findByHashSum(myFile.getHashSum()).get(0).getHashSum(),
                        fileRepository.findByHashSum(myFile.getHashSum()).get(0).getMotherID());
                if (countOfDuplicate > 1) {
                    for (MyFile duplicate : fileRepository.findByHashSum(masterFile.getHashSum())) {
                        fileRepository.updateFilesMothersIdInfo(duplicate.getId().toString(),
                                masterFile.getId().toString());
                    }
                } else {
                    fileRepository.updateFilesMothersIdInfo(masterFile.getId().toString(),
                            masterFile.getId().toString());
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Иcходные файлы найдены");
        System.out.println("=========================");
    }

    /**
     * Выстраивает часть относительного пути от дублирующегося файла
     * к главной директории файловой системы.
     *
     * @param splitPath массив компонентов пути дублирующегося файла (без '/').
     *
     * @return часть относительного пути от дублирующегося файла
     * к главной директории файловой системы
     */
    private String cdStringForPath(String[] splitPath) {
        StringBuilder cdPath = new StringBuilder();
        boolean flag = true;
        for (int i = splitPath.length - 2; i > 0; i--) {
            if (flag && !Objects.equals(splitPath[i], FILESYSTEM_NAME)) {
                cdPath.append("../");
            }
            if (Objects.equals(splitPath[i], FILESYSTEM_NAME)) {
                flag = false;
            }
        }
        return cdPath.toString();
    }

    /**
     * Выстраивает часть относительного пути из
     * главной директории файловой системы до исходного файла.
     *
     * @param splitPath массив компонентов пути исходного файла (без '/').
     *
     * @return часть относительного пути из
     * главной директории файловой системы до исходного файла
     */
    private String motherPathStringForPath(String[] splitPath) {
        StringBuilder motherPath = new StringBuilder();
        int index = 0;
        for (int i = 0; i < splitPath.length - 1; i++) {
            if (splitPath[i].equals(FILESYSTEM_NAME) && i == FILESYSTEM_LEVEL) {
                index = i + 1;
            }
            if (index <= i && index != 0) {
                motherPath.append(splitPath[i]).append("/");
            }
        }
        return motherPath.toString();
    }

    /**
     * Выстраивает относительный путь от файла, который нужно заменить на ссылку, к исходному.
     *
     * @param link абсолютный путь к файлу, который необходимо заменить на ссылку
     * @param file абсолютный путь к исходному файлу
     *
     * @return относительный путь к исходному файлу
     */
    private String giveRelativePathToMotherFile(String link, String file) {
        String[] linkSplitPath = link.split("/");
        String[] fileSplitPath = file.split("/");
        StringBuilder newPath = new StringBuilder();

        newPath.append(cdStringForPath(linkSplitPath));
        newPath.append(motherPathStringForPath(fileSplitPath));

        newPath.append(fileSplitPath[fileSplitPath.length - 1]);
        return newPath.toString();
    }

    /**
     * Заменяет дублирующиеся файлы на ссылки.
     */
    public void replaceDuplicateFilesWithLinks(List<MyFile> allFilesInTable) {
        for (MyFile myFile : allFilesInTable) {
            String id = myFile.getId().toString();
            String motherID = myFile.getMotherID();
            String duplicateFilePath = myFile.getFilePath();

            if (!id.equals(motherID)) {
                String sourceFilePath = fileRepository.findById(motherID).getFilePath();
                Path symbolicLink = Paths.get(duplicateFilePath);
                Path sourcePath = Paths.get(giveRelativePathToMotherFile(duplicateFilePath, sourceFilePath));
                try {
                    Files.deleteIfExists(symbolicLink); // Удаление ссылки, если она уже существует
                    Files.createSymbolicLink(symbolicLink, sourcePath); // Создание символической ссылки
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //System.out.println("Файл успешно заменен на символическую ссылку.");
            }
        }
        System.out.println("Повторяющиеся файлы успешно заменены на ссылки");
        System.out.println("=========================");
    }
}


