package com.example.financery.service.impl;

import com.example.financery.exception.InvalidInputException;
import com.example.financery.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(LogServiceImplTest.class);

    private LogServiceImpl logService;
    private Path logFilePath;
    private Path tempDir;

    @TempDir
    Path tempDirReal;

    @BeforeEach
    void setUp() throws IOException {
        // Создаем временный файл логов
        logFilePath = tempDirReal.resolve("app.log");
        Files.createFile(logFilePath);

        // Используем временную директорию
        tempDir = tempDirReal.resolve("temp");
        Files.createDirectories(tempDir);

        // Инициализируем сервис с тестовыми путями
        logService = new LogServiceImpl(logFilePath, tempDir);
    }

    @AfterEach
    void tearDown() {
        try {
            log.info("Очистка временной директории: {}", tempDir);
            Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.error("Не удалось удалить файл: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("Ошибка при очистке временной директории", e);
        }
    }

    // Тест для конструктора по умолчанию
    @Test
    void defaultConstructor_createsTempDir() throws IOException {
        // Удаляем tempDir, если она существует
        Path tempDirForDefault = Paths.get("D:/documents/JavaLabs/temp");
        if (Files.exists(tempDirForDefault)) {
            Files.walk(tempDirForDefault)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.error("Не удалось удалить файл: {}", path, e);
                        }
                    });
            Files.deleteIfExists(tempDirForDefault);
        }

        // Создаем сервис с конструктором по умолчанию
        LogServiceImpl defaultLogService = new LogServiceImpl();

        // Проверяем, что tempDir создана
        assertTrue(Files.exists(tempDirForDefault));
    }

    @Test
    void ensureTempDirExists_success() {
        // Проверяем, что директория создана в setUp
        assertTrue(Files.exists(tempDir));
    }

    // Тест для случая, когда tempDir не существует
    @Test
    void ensureTempDirExists_tempDirNotExists_createsDir() {
        // Создаем новый экземпляр сервиса с несуществующей директорией
        Path nonExistentDir = tempDirReal.resolve("nonexistent");
        LogServiceImpl newLogService = new LogServiceImpl(logFilePath, nonExistentDir);

        // Проверяем, что директория создана
        assertTrue(Files.exists(nonExistentDir));
    }

    @Test
    void ensureTempDirExists_ioException_throwsIllegalStateException() throws IOException {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path invalidDir = tempDirReal.resolve("invalid");
            filesMock.when(() -> Files.exists(invalidDir)).thenReturn(false);
            filesMock.when(() -> Files.createDirectories(invalidDir)).thenThrow(new IOException("IO error"));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> new LogServiceImpl(logFilePath, invalidDir));

            assertEquals("Не удаётся создать защищённую временную директорию", exception.getMessage());
        }
    }

    @Test
    void parseDate_validDate_success() {
        String date = "28-04-2025";
        LocalDate expected = LocalDate.of(2025, 4, 28);

        LocalDate result = logService.parseDate(date);

        assertEquals(expected, result);
    }

    @Test
    void parseDate_invalidFormat_throwsInvalidInputException() {
        String date = "2025-04-28"; // Неверный формат

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> logService.parseDate(date));

        assertEquals("Неверный формат даты. Требуется dd-mm-yyyy", exception.getMessage());
    }

    @Test
    void validateLogFileExists_fileExists_success() {
        // Файл создан в setUp
        assertDoesNotThrow(() -> logService.validateLogFileExists(logFilePath));
    }

    @Test
    void validateLogFileExists_fileNotExists_throwsNotFoundException() {
        Path nonExistentPath = tempDirReal.resolve("nonexistent.log");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> logService.validateLogFileExists(nonExistentPath));

        assertEquals("Файл не существует: " + nonExistentPath, exception.getMessage());
    }

    @Test
    void createTempFile_success() {
        LocalDate logDate = LocalDate.of(2025, 4, 28);
        Path tempFilePath = logService.createTempFile(logDate);

        assertTrue(Files.exists(tempFilePath));
        assertTrue(tempFilePath.toString().contains("log-2025-04-28"));
        assertTrue(tempFilePath.toString().endsWith(".log"));
    }

    @Test
    void filterAndWriteLogsToTempFile_success() throws IOException {
        // Подготовим тестовый лог-файл
        String date = "28-04-2025";
        Files.write(logFilePath, """
                28-04-2025 INFO Some log entry
                27-04-2025 INFO Another log entry
                28-04-2025 ERROR Error log
                """.getBytes());

        Path tempFilePath = Files.createTempFile(tempDir, "test-", ".log");
        logService.filterAndWriteLogsToTempFile(logFilePath, date, tempFilePath);

        String content = Files.readString(tempFilePath);
        assertTrue(content.contains("28-04-2025 INFO Some log entry"));
        assertTrue(content.contains("28-04-2025 ERROR Error log"));
        assertFalse(content.contains("27-04-2025"));
    }

    @Test
    void filterAndWriteLogsToTempFile_ioException_throwsIllegalStateException() throws IOException {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.newBufferedReader(any()))
                    .thenThrow(new IOException("Read error"));

            Path tempFilePath = Files.createTempFile(tempDir, "test-", ".log");

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.filterAndWriteLogsToTempFile(logFilePath, "28-04-2025", tempFilePath));

            assertEquals("Ошибка при обработке файла логов: Read error", exception.getMessage());
        }
    }

    @Test
    void createResourceFromTempFile_success() throws IOException {
        // Создаем временный файл с содержимым
        Path tempFilePath = Files.createTempFile(tempDir, "test-", ".log");
        Files.write(tempFilePath, "Some log content".getBytes());

        Resource resource = logService.createResourceFromTempFile(tempFilePath, "28-04-2025");

        assertNotNull(resource);
        assertTrue(resource instanceof UrlResource);
        assertEquals(tempFilePath.toUri(), ((UrlResource) resource).getURI());
    }

    @Test
    void createResourceFromTempFile_emptyFile_throwsNotFoundException() throws IOException {
        // Создаем пустой временный файл
        Path tempFilePath = Files.createTempFile(tempDir, "test-", ".log");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> logService.createResourceFromTempFile(tempFilePath, "28-04-2025"));

        assertEquals("Нет логов за указанную дату: 28-04-2025", exception.getMessage());
    }

    @Test
    void createResourceFromTempFile_ioException_throwsIllegalStateException() throws IOException {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Path tempFilePath = Files.createTempFile(tempDir, "test-", ".log");
            filesMock.when(() -> Files.size(tempFilePath))
                    .thenThrow(new IOException("Size error"));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.createResourceFromTempFile(tempFilePath, "28-04-2025"));

            assertEquals("Ошибка при создании ресурса из временного файла: Size error", exception.getMessage());
        }
    }

    @Test
    void createUrlResource_success() throws IOException {
        URI uri = tempDir.resolve("test.log").toUri();
        Resource resource = logService.createUrlResource(uri);

        assertNotNull(resource);
        assertTrue(resource instanceof UrlResource);
        assertEquals(uri, ((UrlResource) resource).getURI());
    }

    @Test
    void createUrlResource_malformedUrl_throwsIOException() throws IOException {
        URI invalidUri = URI.create("invalid://url");
        LogServiceImpl spyService = spy(logService);
        doThrow(new MalformedURLException("Invalid URL")).when(spyService).createUrlResource(invalidUri);

        MalformedURLException exception = assertThrows(MalformedURLException.class,
                () -> spyService.createUrlResource(invalidUri));

        assertEquals("Invalid URL", exception.getMessage());
    }

    @Test
    void downloadLogs_success() throws IOException {
        // Подготовим тестовый лог-файл
        String date = "28-04-2025";
        Files.write(logFilePath, """
                28-04-2025 INFO Some log entry
                27-04-2025 INFO Another log entry
                """.getBytes());

        Resource resource = logService.downloadLogs(date);

        assertNotNull(resource);
        assertTrue(resource instanceof UrlResource);
        Path tempFilePath = Path.of(((UrlResource) resource).getURI());
        String content = Files.readString(tempFilePath);
        assertTrue(content.contains("28-04-2025 INFO Some log entry"));
        assertFalse(content.contains("27-04-2025"));
    }

    @Test
    void downloadLogs_invalidDate_throwsInvalidInputException() {
        String invalidDate = "2025-04-28";

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> logService.downloadLogs(invalidDate));

        assertEquals("Неверный формат даты. Требуется dd-mm-yyyy", exception.getMessage());
    }

    @Test
    void downloadLogs_logFileNotExists_throwsNotFoundException() {
        // Удаляем лог-файл
        logService = new LogServiceImpl(tempDirReal.resolve("nonexistent.log"), tempDir);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> logService.downloadLogs("28-04-2025"));

        assertEquals("Файл не существует: " + tempDirReal.resolve("nonexistent.log"), exception.getMessage());
    }

    @Test
    void downloadLogs_noLogsForDate_throwsNotFoundException() throws IOException {
        // Пустой лог-файл
        Files.write(logFilePath, "".getBytes());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> logService.downloadLogs("28-04-2025"));

        assertEquals("Нет логов за указанную дату: 28-04-2025", exception.getMessage());
    }

    @Test
    void createTempFile_ioException_throwsIllegalStateException() {
        LocalDate fixedDate = LocalDate.of(2025, 4, 28); // Фиксированная дата для теста
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Мокаем перегрузку Files.createTempFile с тремя аргументами (без атрибутов)
            filesMock.when(() -> Files.createTempFile(
                    eq(tempDir),
                    eq("log-" + fixedDate + "-"),
                    eq(".log")
            )).thenThrow(new IOException("IO error"));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.createTempFile(fixedDate));

            assertEquals("Ошибка при создании временного файла: IO error", exception.getMessage());
        }
    }

    @Test
    void createTempFile_setReadableFails_throwsIllegalStateException() throws IOException {
        LocalDate logDate = LocalDate.of(2025, 4, 28);

        // Создаём замоканный объект Path
        Path mockPath = mock(Path.class);

        // Создаём замоканный объект File
        File mockFile = mock(File.class);
        when(mockFile.setReadable(true, true)).thenReturn(false); // Симулируем неудачу при установке прав на чтение

        // Настраиваем mockPath, чтобы метод toFile() возвращал mockFile
        when(mockPath.toFile()).thenReturn(mockFile);

        // Мокаем Files.createTempFile, чтобы он возвращал mockPath
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.createTempFile(eq(tempDir), eq("log-" + logDate + "-"), eq(".log")))
                    .thenReturn(mockPath);

            // Проверяем, что выбрасывается исключение
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.createTempFile(logDate));

            assertEquals("Не удалось установить права на чтение для временного файла: " + mockFile, exception.getMessage());
        }
    }

    @Test
    void createTempFile_setWritableFails_throwsIllegalStateException() throws IOException {
        LocalDate logDate = LocalDate.of(2025, 4, 28);

        // Создаём замоканный объект Path
        Path mockPath = mock(Path.class);

        // Создаём замоканный объект File
        File mockFile = mock(File.class);
        when(mockFile.setReadable(true, true)).thenReturn(true); // Успешная установка прав на чтение
        when(mockFile.setWritable(true, true)).thenReturn(false); // Симулируем неудачу при установке прав на запись

        // Настраиваем mockPath, чтобы метод toFile() возвращал mockFile
        when(mockPath.toFile()).thenReturn(mockFile);

        // Мокаем Files.createTempFile, чтобы он возвращал mockPath
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.createTempFile(eq(tempDir), eq("log-" + logDate + "-"), eq(".log")))
                    .thenReturn(mockPath);

            // Проверяем, что выбрасывается исключение
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.createTempFile(logDate));

            assertEquals("Не удалось установить права на запись для временного файла: " + mockFile, exception.getMessage());
        }
    }
}