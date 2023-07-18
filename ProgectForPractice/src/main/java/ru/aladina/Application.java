package ru.aladina;

import ru.aladina.database.dsprovider.PostgreSQLProvider;
import ru.aladina.database.property.PropertyContainer;
import ru.aladina.database.repository.impl.PostgreSQLFileRepository;
import ru.aladina.database.service.FileService;


import java.io.File;
import java.io.IOException;


/**
 * Основной класс приложения.
 */
public class Application {

    public static void main(String[] args) throws IOException {
        PropertyContainer.loadProperties();
        File masterDirectory = new File(PropertyContainer.getProperty("filesystem.path"));

        var dataSourceProvider = new PostgreSQLProvider();

        var fileRepository = new PostgreSQLFileRepository(
                dataSourceProvider.getDataSource());
        var fileService = new FileService(fileRepository);


        fileService.findFilesSizeSum(masterDirectory);

        fileService.insertAllFiles(masterDirectory);
        fileService.findAllFilesMother(fileRepository.findAll());
        fileService.replaceDuplicateFilesWithLinks(fileRepository.findAll());

        fileService.findFilesSizeSum(masterDirectory);
        }
    }
