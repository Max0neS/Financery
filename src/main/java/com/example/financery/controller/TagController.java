package com.example.financery.controller;

import com.example.financery.dto.TagDtoRequest;
import com.example.financery.dto.TagDtoResponse;
import com.example.financery.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@AllArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/get-all-tags")
    public List<TagDtoResponse> getAllTags() {
        return tagService.getAllTags();
    }

    @GetMapping("/get-all-user-tags/{userId}")
    public List<TagDtoResponse> getAllUserTags(@PathVariable long userId) {
        return tagService.getTagsByUserId(userId);
    }

    @GetMapping("/get-tag-by-id/{tagId}")
    public ResponseEntity<TagDtoResponse> getTagById(@PathVariable long tagId) {
        return ResponseEntity.ok(tagService.getTagById(tagId));
    }

    @GetMapping("/get-tags-by-transaction/{transactionId}")
    public List<TagDtoResponse> getTagsByTransactionId(@PathVariable long transactionId) {
        return tagService.getTagsByTransactionId(transactionId);
    }

    @PostMapping("/create")
    public TagDtoResponse createTag(@RequestBody TagDtoRequest tagDto) {
        return tagService.createTag(tagDto);
    }

    @PutMapping("/update-by-id/{tagId}")
    public TagDtoResponse updateTag(@PathVariable long tagId, @RequestBody TagDtoRequest tagDto) {
        return tagService.updateTag(tagId, tagDto);
    }

    @DeleteMapping("/delete-by-id/{tagId}")
    public void deleteTagById(@PathVariable long tagId) {
        tagService.deleteTag(tagId);
    }
}