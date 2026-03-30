package com.english.learning.repository;

import com.english.learning.entity.Slideshow;
import com.english.learning.enums.SlideshowPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SlideshowRepository extends JpaRepository<Slideshow, Long> {
    List<Slideshow> findByIsActiveTrueAndPositionOrderByDisplayOrderAsc(SlideshowPosition position);
}
