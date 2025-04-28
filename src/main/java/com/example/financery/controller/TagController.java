package com.example.financery.controller;

import com.example.financery.dto.TagDtoRequest;
import com.example.financery.dto.TagDtoResponse;
import com.example.financery.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@AllArgsConstructor
@Tag(name = "Теги", description = "Управление тегами пользователя")
public class TagController {

    private final TagService tagService;

    @Operation(
            summary = "Получение всех существующих тегов",
            description = "Возвращает список всех тегов, доступных в системе."
    )
    @GetMapping("/get-all-tags")
    public List<TagDtoResponse> getAllTags() {
        return tagService.getAllTags();
    }

    @Operation(
            summary = "Получение всех тегов пользователя",
            description = "Возвращает список всех тегов, принадлежащих пользователю с указанным ID."
    )
    @GetMapping("/get-all-user-tags/{userId}")
    public List<TagDtoResponse> getAllUserTags(
            @Parameter(description = "ID пользователя, чьи теги необходимо получить", required = true, example = "1")
            @PathVariable long userId) {
        return tagService.getTagsByUserId(userId);
    }

    @Operation(
            summary = "Получение тега по ID",
            description = "Возвращает информацию о теге с указанным ID."
    )
    @GetMapping("/get-tag-by-id/{tagId}")
    public ResponseEntity<TagDtoResponse> getTagById(
            @Parameter(description = "ID тега, который необходимо получить", required = true, example = "1")
            @PathVariable long tagId) {
        return ResponseEntity.ok(tagService.getTagById(tagId));
    }

    @Operation(
            summary = "Получение тегов по транзакции",
            description = "Возвращает список тегов, связанных с транзакцией по указанному ID транзакции."
    )
    @GetMapping("/get-tags-by-transaction/{transactionId}")
    public List<TagDtoResponse> getTagsByTransactionId(
            @Parameter(description = "ID транзакции, теги которой необходимо получить", required = true, example = "1")
            @PathVariable long transactionId) {
        return tagService.getTagsByTransactionId(transactionId);
    }

    @Operation(
            summary = "Создание нового тега",
            description = "Создает новый тег на основе переданных данных."
    )
    @PostMapping("/create")
    public TagDtoResponse createTag(
            @Parameter(description = "Данные для создания нового тега", required = true)
            @Valid @RequestBody TagDtoRequest tagDto) {
        return tagService.createTag(tagDto);
    }

    @Operation(
            summary = "Обновление тега по ID",
            description = "Обновляет данные тега с указанным ID на основе переданных данных."
    )
    @PutMapping("/update-by-id/{tagId}")
    public TagDtoResponse updateTag(
            @Parameter(description = "ID тега, который необходимо обновить", required = true, example = "1")
            @PathVariable long tagId,
            @Parameter(description = "Обновленные данные тега", required = true)
            @Valid @RequestBody TagDtoRequest tagDto) {
        return tagService.updateTag(tagId, tagDto);
    }

    @Operation(
            summary = "Удаление тега по ID",
            description = "Удаляет тег с указанным ID."
    )
    @DeleteMapping("/delete-by-id/{tagId}")
    public void deleteTagById(
            @Parameter(description = "ID тега, который необходимо удалить", required = true, example = "1")
            @PathVariable long tagId) {
        tagService.deleteTag(tagId);
    }
}