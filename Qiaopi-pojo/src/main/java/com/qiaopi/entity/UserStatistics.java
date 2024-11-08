package com.qiaopi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserStatistics {
    private int receiveLetterCount;
    private int friendCount;
//    private Long collectionCount;
}
