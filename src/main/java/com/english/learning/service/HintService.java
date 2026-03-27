package com.english.learning.service;

import java.util.List;
import java.util.Map;

public interface HintService {
    /**
     * Trích xuất danh từ riêng từ nội dung câu.
     */
    List<String> extractProperNouns(String content);

    /**
     * Tạo Map chứa hints cho danh sách các câu.
     */
    Map<Long, List<String>> getHintsMap(List<com.english.learning.entity.Sentence> sentences);
}
