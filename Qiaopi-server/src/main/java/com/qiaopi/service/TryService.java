package com.qiaopi.service;

import com.qiaopi.dto.LetterGenDTO;

public interface TryService {

    String generateImage(LetterGenDTO letterGenDTO, Long currentUserId);
}
