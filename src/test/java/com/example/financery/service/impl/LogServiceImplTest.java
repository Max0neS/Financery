package com.example.financery.service.impl;

import com.example.financery.exception.InvalidInputException;
import com.example.financery.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceImplTest {

    @InjectMocks
    private LogServiceImpl logService;

    private Path logFilePath;
    private Path tempDir;
    private LocalDate logDate;

    @BeforeEach
    void setUp() throws IOException {
        logFilePath = Paths.get("log/app.log");
        tempDir = Paths.get("D:/documents/JavaLabs/temp");
        logDate = LocalDate.of(2025, 4, 28);

        // Мокаем создание директории в статическом блоке
        try (MockedStatic<Paths> paths = mockStatic(Paths.class)) {
            try (MockedStatic<Files> files = mockStatic(Files.class)) {
                paths.when(() -> Paths.get("D:/documents/JavaLabs/temp")).thenReturn(tempDir);
                files.when(() -> Files.exists(tempDir)).thenReturn(true);
            }
        }
    }

    @Test
    void downloadLogs_success() throws IOException, MalformedURLException {
        String date = "28-04-2025";
        String formattedDate = "28-04-2025";

        // Создаем tempFilePath как мок
        Path tempFilePath = mock(Path.class);
        File tempFile = mock(File.class);
        URI tempFileUri = URI.create("file:///D:/documents/JavaLabs/temp/log-2025-04-28.log");

        try (MockedStatic<Paths> paths = mockStatic(Paths.class)) {
            try (MockedStatic<Files> files = mockStatic(Files.class)) {
                // Мокаем зависимости
                paths.when(() -> Paths.get("log/app.log")).thenReturn(logFilePath);
                files.when(() -> Files.exists(logFilePath)).thenReturn(true);

                // Мокаем создание временного файла
                files.when(() -> Files.createTempFile(tempDir, "log-" + logDate + "-", ".log"))
                        .thenReturn(tempFilePath);
                when(tempFilePath.toFile()).thenReturn(tempFile);
                when(tempFile.setReadable(true, true)).thenReturn(true);
                when(tempFile.setWritable(true, true)).thenReturn(true);
                when(tempFile.canExecute()).thenReturn(false);
                when(tempFilePath.toUri()).thenReturn(tempFileUri);
                when(tempFile.getAbsolutePath()).thenReturn("D:/documents/JavaLabs/temp/log-2025-04-28.log");

                // Мокаем чтение и запись логов
                BufferedReader reader = mock(BufferedReader.class);
                files.when(() -> Files.newBufferedReader(logFilePath)).thenReturn(reader);
                Stream<String> lines = Stream.of("Log entry on 28-04-2025");
                when(reader.lines()).thenReturn(lines);
                files.when(() -> Files.write(eq(tempFilePath), any(Iterable.class))).thenReturn(tempFilePath);

                // Мокаем Files.size
                files.when(() -> Files.size(tempFilePath)).thenReturn(100L); // Файл не пустой

                // Используем spy и doReturn для мока createUrlResource
                LogServiceImpl logServiceSpy = Mockito.spy(logService);
                Resource resource = mock(Resource.class);
                doReturn(resource).when(logServiceSpy).createUrlResource(tempFileUri);

                Resource result = logServiceSpy.downloadLogs(date);

                assertNotNull(result);
                assertEquals(resource, result);
                verify(tempFile).deleteOnExit();
            }
        }
    }

    @Test
    void downloadLogs_logFileNotFound_throwsNotFoundException() {
        String date = "28-04-2025";

        try (MockedStatic<Paths> paths = mockStatic(Paths.class)) {
            try (MockedStatic<Files> files = mockStatic(Files.class)) {
                paths.when(() -> Paths.get("log/app.log")).thenReturn(logFilePath);
                files.when(() -> Files.exists(logFilePath)).thenReturn(false);

                NotFoundException exception = assertThrows(NotFoundException.class,
                        () -> logService.downloadLogs(date));

                assertEquals("Файл не существует: log/app.log", exception.getMessage());
            }
        }
    }

    @Test
    void downloadLogs_noLogsForDate_throwsNotFoundException() throws IOException {
        String date = "28-04-2025";
        String formattedDate = "28-04-2025";

        // Создаем tempFilePath как мок
        Path tempFilePath = mock(Path.class);
        File tempFile = mock(File.class);

        try (MockedStatic<Paths> paths = mockStatic(Paths.class)) {
            try (MockedStatic<Files> files = mockStatic(Files.class)) {
                paths.when(() -> Paths.get("log/app.log")).thenReturn(logFilePath);
                files.when(() -> Files.exists(logFilePath)).thenReturn(true);

                files.when(() -> Files.createTempFile(tempDir, "log-" + logDate + "-", ".log"))
                        .thenReturn(tempFilePath);
                when(tempFilePath.toFile()).thenReturn(tempFile);
                when(tempFile.setReadable(true, true)).thenReturn(true);
                when(tempFile.setWritable(true, true)).thenReturn(true);
                when(tempFile.canExecute()).thenReturn(false);
                when(tempFile.getAbsolutePath()).thenReturn("D:/documents/JavaLabs/temp/log-2025-04-28.log");

                BufferedReader reader = mock(BufferedReader.class);
                files.when(() -> Files.newBufferedReader(logFilePath)).thenReturn(reader);
                files.when(() -> Files.size(tempFilePath)).thenReturn(0L); // Файл пустой
                Stream<String> lines = Stream.of("Log entry on 27-04-2025"); // Логи за другую дату
                when(reader.lines()).thenReturn(lines);
                files.when(() -> Files.write(eq(tempFilePath), any(Iterable.class))).thenReturn(tempFilePath);

                NotFoundException exception = assertThrows(NotFoundException.class,
                        () -> logService.downloadLogs(date));

                assertEquals("Нет логов за указанную дату: " + date, exception.getMessage());
            }
        }
    }

    @Test
    void parseDate_success() {
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
    void validateLogFileExists_success() {
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.exists(logFilePath)).thenReturn(true);

            logService.validateLogFileExists(logFilePath); // Не должно выбросить исключение
        }
    }

    @Test
    void validateLogFileExists_notFound_throwsNotFoundException() {
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.exists(logFilePath)).thenReturn(false);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> logService.validateLogFileExists(logFilePath));

            assertEquals("Файл не существует: log/app.log", exception.getMessage());
        }
    }

    @Test
    void createTempFile_success() throws IOException {
        // Создаем tempFilePath как мок
        Path tempFilePath = mock(Path.class);
        File tempFile = mock(File.class);
        when(tempFile.getAbsolutePath()).thenReturn("D:/documents/JavaLabs/temp/log-2025-04-28.log");

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.createTempFile(tempDir, "log-" + logDate + "-", ".log"))
                    .thenReturn(tempFilePath);
            when(tempFilePath.toFile()).thenReturn(tempFile);
            when(tempFile.setReadable(true, true)).thenReturn(true);
            when(tempFile.setWritable(true, true)).thenReturn(true);
            when(tempFile.canExecute()).thenReturn(false);

            Path result = logService.createTempFile(logDate);

            assertEquals(tempFilePath, result);
            verify(tempFile).setReadable(true, true);
            verify(tempFile).setWritable(true, true);
            verify(tempFile).canExecute();
        }
    }

    @Test
    void createTempFile_ioException_throwsIllegalStateException() throws IOException {
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.createTempFile(tempDir, "log-" + logDate + "-", ".log"))
                    .thenThrow(new IOException("IO Error"));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.createTempFile(logDate));

            assertEquals("Ошибка при создании временного файла: IO Error", exception.getMessage());
        }
    }

    @Test
    void createTempFile_setReadableFails_throwsIllegalStateException() throws IOException {
        // Создаем tempFilePath как мок
        Path tempFilePath = mock(Path.class);
        File tempFile = mock(File.class);
        when(tempFile.toString()).thenReturn("D:/documents/JavaLabs/temp/log-2025-04-28.log");

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.createTempFile(tempDir, "log-" + logDate + "-", ".log"))
                    .thenReturn(tempFilePath);
            when(tempFilePath.toFile()).thenReturn(tempFile);
            when(tempFile.setReadable(true, true)).thenReturn(false);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.createTempFile(logDate));

            assertEquals("Не удалось установить права на чтение для временного файла: " + tempFile,
                    exception.getMessage());
        }
    }

    @Test
    void createTempFile_setWritableFails_throwsIllegalStateException() throws IOException {
        // Создаем tempFilePath как мок
        Path tempFilePath = mock(Path.class);
        File tempFile = mock(File.class);
        when(tempFile.toString()).thenReturn("D:/documents/JavaLabs/temp/log-2025-04-28.log");

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.createTempFile(tempDir, "log-" + logDate + "-", ".log"))
                    .thenReturn(tempFilePath);
            when(tempFilePath.toFile()).thenReturn(tempFile);
            when(tempFile.setReadable(true, true)).thenReturn(true);
            when(tempFile.setWritable(true, true)).thenReturn(false);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.createTempFile(logDate));

            assertEquals("Не удалось установить права на запись для временного файла: " + tempFile,
                    exception.getMessage());
        }
    }

    @Test
    void createTempFile_setExecutableFails_logsWarning() throws IOException {
        // Создаем tempFilePath как мок
        Path tempFilePath = mock(Path.class);
        File tempFile = mock(File.class);
        when(tempFile.getAbsolutePath()).thenReturn("D:/documents/JavaLabs/temp/log-2025-04-28.log");

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.createTempFile(tempDir, "log-" + logDate + "-", ".log"))
                    .thenReturn(tempFilePath);
            when(tempFilePath.toFile()).thenReturn(tempFile);
            when(tempFile.setReadable(true, true)).thenReturn(true);
            when(tempFile.setWritable(true, true)).thenReturn(true);
            when(tempFile.canExecute()).thenReturn(true);
            when(tempFile.setExecutable(false, false)).thenReturn(false);

            Path result = logService.createTempFile(logDate);

            assertEquals(tempFilePath, result);
            verify(tempFile).setReadable(true, true);
            verify(tempFile).setWritable(true, true);
            verify(tempFile).canExecute();
            verify(tempFile).setExecutable(false, false);
        }
    }

    @Test
    void filterAndWriteLogsToTempFile_success() throws IOException {
        String formattedDate = "28-04-2025";
        Path tempFilePath = mock(Path.class);

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            BufferedReader reader = mock(BufferedReader.class);
            files.when(() -> Files.newBufferedReader(logFilePath)).thenReturn(reader);
            Stream<String> lines = Stream.of("Log entry on 28-04-2025");
            when(reader.lines()).thenReturn(lines);
            files.when(() -> Files.write(eq(tempFilePath), any(Iterable.class))).thenReturn(tempFilePath);

            logService.filterAndWriteLogsToTempFile(logFilePath, formattedDate, tempFilePath);

            verify(reader).lines();
            files.verify(() -> Files.write(eq(tempFilePath), any(Iterable.class)));
        }
    }

    @Test
    void filterAndWriteLogsToTempFile_ioException_throwsIllegalStateException() throws IOException {
        String formattedDate = "28-04-2025";
        Path tempFilePath = mock(Path.class);

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.newBufferedReader(logFilePath))
                    .thenThrow(new IOException("IO Error"));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.filterAndWriteLogsToTempFile(logFilePath, formattedDate, tempFilePath));

            assertEquals("Ошибка при обработке файла логов: IO Error", exception.getMessage());
        }
    }

    @Test
    void createResourceFromTempFile_success() throws IOException, MalformedURLException {
        String date = "28-04-2025";

        // Создаем tempFilePath как мок
        Path tempFilePath = mock(Path.class);
        File tempFile = mock(File.class);
        URI tempFileUri = URI.create("file:///D:/documents/JavaLabs/temp/log-2025-04-28.log");

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.size(tempFilePath)).thenReturn(100L); // Файл не пустой
            when(tempFilePath.toFile()).thenReturn(tempFile);
            when(tempFilePath.toUri()).thenReturn(tempFileUri);

            // Используем spy и doReturn для мока createUrlResource
            LogServiceImpl logServiceSpy = Mockito.spy(logService);
            Resource resource = mock(Resource.class);
            doReturn(resource).when(logServiceSpy).createUrlResource(tempFileUri);

            Resource result = logServiceSpy.createResourceFromTempFile(tempFilePath, date);

            assertNotNull(result);
            assertEquals(resource, result);
            verify(tempFile).deleteOnExit();
        }
    }

    @Test
    void createResourceFromTempFile_emptyFile_throwsNotFoundException() throws IOException {
        String date = "28-04-2025";
        Path tempFilePath = mock(Path.class);

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.size(tempFilePath)).thenReturn(0L); // Файл пустой

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> logService.createResourceFromTempFile(tempFilePath, date));

            assertEquals("Нет логов за указанную дату: " + date, exception.getMessage());
        }
    }

    @Test
    void createResourceFromTempFile_ioException_throwsIllegalStateException() throws IOException {
        String date = "28-04-2025";
        Path tempFilePath = mock(Path.class);

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.size(tempFilePath)).thenThrow(new IOException("IO Error"));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> logService.createResourceFromTempFile(tempFilePath, date));

            assertEquals("Ошибка при создании ресурса из временного файла: IO Error",
                    exception.getMessage());
        }
    }

    @Test
    void createUrlResource_malformedUri_throwsMalformedURLException() throws MalformedURLException {
        URI invalidUri = URI.create("invalid://uri"); // Некорректный URI

        LogServiceImpl logServiceSpy = Mockito.spy(logService);
        doCallRealMethod().when(logServiceSpy).createUrlResource(invalidUri);

        assertThrows(MalformedURLException.class,
                () -> logServiceSpy.createUrlResource(invalidUri));
    }

    @Test
    void createUrlResource_success() throws IOException {
        URI validUri = URI.create("file:///D:/documents/JavaLabs/temp/log-2025-04-28.log");

        LogServiceImpl logServiceSpy = Mockito.spy(logService);
        doCallRealMethod().when(logServiceSpy).createUrlResource(validUri);

        Resource result = logServiceSpy.createUrlResource(validUri);

        assertNotNull(result);
        assertTrue(result instanceof UrlResource);
        assertEquals(validUri, ((UrlResource) result).getURI());
    }
}