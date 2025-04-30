package com.example.financery.service.impl;

import com.example.financery.exception.InvalidInputException;
import com.example.financery.exception.NotFoundException;
import com.example.financery.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
public class LogServiceImpl implements LogService {

    private final Path logFilePath;
    private final Path tempDir;

    // Конструктор по умолчанию
    public LogServiceImpl() {
        this(Paths.get("log/app.log"), Paths.get("D:/documents/JavaLabs/temp"));
    }

    // Конструктор с параметрами для тестов
    public LogServiceImpl(Path logFilePath, Path tempDir) {
        this.logFilePath = logFilePath;
        this.tempDir = tempDir;
        ensureTempDirExists();
    }

    // Метод для создания временной директории
    public void ensureTempDirExists() {
        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
                log.info("Создана защищённая временная директория: {}", tempDir);
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Не удаётся создать защищённую временную директорию", e);
        }
    }

    @Override
    public Resource downloadLogs(String date) {
        LocalDate logDate = parseDate(date);
        validateLogFileExists(logFilePath);
        String formattedDate = logDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        Path tempFilePath = createTempFile(logDate);
        filterAndWriteLogsToTempFile(logFilePath, formattedDate, tempFilePath);

        Resource resource = createResourceFromTempFile(tempFilePath, date);
        log.info("Файл логов с датой {} успешно загружен", date);
        return resource;
    }

    public LocalDate parseDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Неверный формат даты. Требуется dd-mm-yyyy");
        }
    }

    public void validateLogFileExists(Path path) {
        if (!Files.exists(path)) {
            throw new NotFoundException("Файл не существует: " + path);
        }
    }

    public Path createTempFile(LocalDate logDate) {
        try {
            Path tempFilePath = Files.createTempFile(tempDir, "log-" + logDate + "-", ".log");
            File tempFile = tempFilePath.toFile();
            if (!tempFile.setReadable(true, true)) {
                throw new IllegalStateException("Не удалось установить права на чтение "
                        + "для временного файла: " + tempFile);
            }
            if (!tempFile.setWritable(true, true)) {
                throw new IllegalStateException("Не удалось установить права на запись "
                        + "для временного файла: " + tempFile);
            }
            if (tempFile.canExecute() && !tempFile.setExecutable(false, false)) {
                log.warn("Не удалось удалить права на выполнение для временного файла: {}",
                        tempFile);
            }
            log.info("Создан защищённый временный файл: {}", tempFile.getAbsolutePath());
            return tempFilePath;
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка при создании временного файла: "
                    + e.getMessage());
        }
    }

    public void filterAndWriteLogsToTempFile(Path logFilePath,
                                             String formattedDate, Path tempFilePath) {
        try (BufferedReader reader = Files.newBufferedReader(logFilePath)) {
            Files.write(tempFilePath, reader.lines()
                    .filter(line -> line.contains(formattedDate))
                    .toList());
            log.info("Отфильтрованные логи за дату {} записаны во временный файл {}",
                    formattedDate, tempFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка при обработке файла логов: " + e.getMessage());
        }
    }

    protected Resource createUrlResource(URI uri) throws MalformedURLException {
        return new UrlResource(uri);
    }

    public Resource createResourceFromTempFile(Path tempFilePath, String date) {
        try {
            long size = Files.size(tempFilePath);
            log.debug("Размер временного файла {}: {} байт", tempFilePath, size);
            if (size == 0) {
                throw new NotFoundException("Нет логов за указанную дату: " + date);
            }
            Resource resource = createUrlResource(tempFilePath.toUri());
            tempFilePath.toFile().deleteOnExit();
            log.info("Создан загружаемый ресурс из временного файла: {}", tempFilePath);
            return resource;
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка при создании ресурса из временного файла: "
                    + e.getMessage());
        }
    }
}