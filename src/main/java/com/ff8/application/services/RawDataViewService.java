package com.ff8.application.services;

import com.ff8.application.dto.RawViewDTO;
import com.ff8.application.ports.primary.RawDataViewUseCase;
import com.ff8.application.ports.secondary.MagicRepository;
import com.ff8.domain.services.RawDataMappingService;

import java.util.Optional;

/**
 * Implementation of raw data view use case.
 * Coordinates between magic repository and raw data mapping service.
 */
public class RawDataViewService implements RawDataViewUseCase {
    
    private final MagicRepository magicRepository;
    private final RawDataMappingService rawDataMappingService;
    
    public RawDataViewService(MagicRepository magicRepository, RawDataMappingService rawDataMappingService) {
        this.magicRepository = magicRepository;
        this.rawDataMappingService = rawDataMappingService;
    }
    
    @Override
    public Optional<RawViewDTO> getRawView(int magicIndex) {
        return magicRepository.findByIndex(magicIndex)
                .map(rawDataMappingService::mapToRawView);
    }
    
    @Override
    public boolean magicExists(int magicIndex) {
        return magicRepository.findByIndex(magicIndex).isPresent();
    }
} 