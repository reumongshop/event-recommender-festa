package com.festa.common.commonService;

import org.springframework.stereotype.Service;

/*
 * 인터페이스를 선언하여 관심을 분리하고
 * 자유로운 확장성을 위해 생성함.
 */
@Service
public interface SessionLoginService {

    void setUserIdSession(Long userId);

    void removeUserIdSession();

    boolean isLoginUser();

}
