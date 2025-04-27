package com.example.financery.service;

import com.example.financery.dto.TagDtoRequest;
import com.example.financery.dto.TagDtoResponse;
import com.example.financery.dto.TransactionDtoResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TagService {
    List<TagDtoResponse> getAllTags();

    List<TagDtoResponse> getTagsByUserId(long userId);

    TagDtoResponse getTagById(long id);

    List<TagDtoResponse> getTagsByTransactionId(long transactionId);

    TagDtoResponse createTag(TagDtoRequest tagDto);

    void deleteTag(long id);
}
