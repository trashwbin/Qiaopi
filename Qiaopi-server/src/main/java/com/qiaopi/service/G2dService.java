package com.qiaopi.service;

import com.qiaopi.dto.LetterGenDTO;

public interface G2dService {

    String generateImage(LetterGenDTO letterGenDTO, Long currentUserId);
}
