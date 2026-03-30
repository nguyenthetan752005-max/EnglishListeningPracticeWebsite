package com.english.learning.service.impl;

import com.english.learning.entity.Slideshow;
import com.english.learning.enums.SlideshowPosition;
import com.english.learning.repository.SlideshowRepository;
import com.english.learning.service.SlideshowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SlideshowServiceImpl implements SlideshowService {

    @Autowired
    private SlideshowRepository slideshowRepository;

    @Override
    public List<Slideshow> getActiveSlideshowsByPosition(SlideshowPosition position) {
        return slideshowRepository.findByIsActiveTrueAndPositionOrderByDisplayOrderAsc(position);
    }
}
