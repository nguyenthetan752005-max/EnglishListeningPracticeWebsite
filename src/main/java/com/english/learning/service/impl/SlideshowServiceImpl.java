package com.english.learning.service.impl;

import com.english.learning.entity.Slideshow;
import com.english.learning.enums.SlideshowPosition;
import com.english.learning.repository.SlideshowRepository;
import com.english.learning.service.SlideshowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SlideshowServiceImpl implements SlideshowService {

    private final SlideshowRepository slideshowRepository;

    @Override
    public List<Slideshow> getActiveSlideshowsByPosition(SlideshowPosition position) {
        return slideshowRepository.findByIsActiveTrueAndPositionOrderByDisplayOrderAsc(position);
    }
}
