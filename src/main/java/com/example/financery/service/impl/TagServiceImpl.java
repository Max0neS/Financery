package com.example.financery.service.impl;

import com.example.financery.dto.TagDtoRequest;
import com.example.financery.dto.TagDtoResponse;
import com.example.financery.mapper.TagMapper;
import com.example.financery.mapper.TransactionMapper;
import com.example.financery.model.Tag;
import com.example.financery.model.User;
import com.example.financery.repository.TagRepository;
import com.example.financery.repository.UserRepository;
import com.example.financery.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class TagServiceImpl implements TagService {

    public static final String TAG_WITH_ID_NOT_FOUND = "Тег с id %d не найден";

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;

    @Override
    public List<TagDtoResponse> getAllTags() {
        List<TagDtoResponse> tagsResponse = new ArrayList<>();
        tagRepository.findAll().forEach(
                tag -> tagsResponse.add(tagMapper.toTagDto(tag)));
        return tagsResponse;
    }

    @Override
    public List<TagDtoResponse> getTagsByUserId(long userId) {
        List<Tag> tags = tagRepository.findByUser(userId);
        List<TagDtoResponse> tagsResponse = new ArrayList<>();
        tags.forEach(tag -> tagsResponse.add(tagMapper.toTagDto(tag)));
        return tagsResponse;
    }

    @Override
    public TagDtoResponse getTagById(long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        String.format(TAG_WITH_ID_NOT_FOUND, id)));
        return tagMapper.toTagDto(tag);
    }

    @Override
    public List<TagDtoResponse> getTagsByTransactionId(long transactionId) {
        List<Tag> tags = tagRepository.findByTransaction(transactionId);
        List<TagDtoResponse> tagsResponse = new ArrayList<>();
        tags.forEach(tag -> tagsResponse.add(tagMapper.toTagDto(tag)));
        return tagsResponse;
    }

    @Override
    public TagDtoResponse createTag(TagDtoRequest tagDto) {
        User user = userRepository.findById(tagDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        Tag tag = tagMapper.toTag(tagDto);
        tag.setUser(user);
        tagRepository.save(tag);

        return tagMapper.toTagDto(tag);
    }

    @Override
    public void deleteTag(long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        String.format(TAG_WITH_ID_NOT_FOUND, id)));
        tagRepository.delete(tag);
    }
}