package com.example.financery.controller;

import com.example.financery.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@Tag(name = "Логи", description = "API для работы с логами")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @Operation(
            summary = "Скачать лог-файл",
            description = "Возвращает .log файл с записями логов за указанную дату."
    )
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLogFile(
            @Parameter(description = "Дата логов в формате dd-mm-yyyy", required = true, example = "28-04-2025")
            @RequestParam String date) {
        Resource resource = logService.downloadLogs(date);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}