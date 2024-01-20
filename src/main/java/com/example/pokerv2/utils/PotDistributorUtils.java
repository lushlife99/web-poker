package com.example.pokerv2.utils;

import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PotDistributorUtils {

    /**
     * 24/01/21 chan
     *
     * 쇼다운 시 승자에 따라 팟을 분배하는 유틸 클래스
     *
     */

    private PotDistributorUtils() {
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

}
