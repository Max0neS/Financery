package com.example.financery.service;

import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.time.LocalDate;

public interface LogService {

    Resource downloadLogs(String date);

    LocalDate parseDate(String date);

    void validateLogFileExists(Path path);

    Path createTempFile(LocalDate logDate);

    void filterAndWriteLogsToTempFile(Path logFilePath,
                                      String formattedDate, Path tempFilePath);

    Resource createResourceFromTempFile(Path tempFilePath, String date);


}
