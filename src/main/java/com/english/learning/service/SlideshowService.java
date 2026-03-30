package com.english.learning.service;

import com.english.learning.entity.Slideshow;
import com.english.learning.enums.SlideshowPosition;

import java.util.List;

public interface SlideshowService {
    List<Slideshow> getActiveSlideshowsByPosition(SlideshowPosition position);
}
