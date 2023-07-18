package ru.aladina.database.service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;


public class DirectorySizeVisitor extends SimpleFileVisitor<Path> {
    private long size = 0;

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (Files.isRegularFile(file)) {
            size += Files.size(file);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        // Если возникла ошибка при доступе к файлу, игнорируем и продолжаем обход
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        // Включаем также размер директории
        size += Files.size(dir);
        return FileVisitResult.CONTINUE;
    }

    public long getSize() {
        return size;
    }
}
