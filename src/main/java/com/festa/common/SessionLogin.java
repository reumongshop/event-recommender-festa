package com.festa.common;

import com.festa.common.commonService.SessionLoginService;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;

/* 로그인/로그아웃 이라는 특정 도메인에 대한 공통 로직으로
 * 다른 클래스에 종속적으로 동작하지 않게 하면서 이후 확장성도 고려하여
 * 따로 인터페이스와 클래스로 분리시켜 둠.
 */
@RequiredArgsConstructor
public class SessionLogin implements SessionLoginService {

    public static final String USER_ID = "userId";

    public final HttpSession httpSession;

    /**
     * 세션에 userId 저장하는 메서드
     * @param userId
     */
    @Override
    public void setUserIdSession(Long userId) {
        httpSession.setAttribute(USER_ID, userId);
    }

    /**
     * 세션에 userId를 제거하는 메서드
     * No Param
     * No return
     */
    @Override
    public void removeUserIdSession() {
        httpSession.removeAttribute(USER_ID);
    }

    /**
     * 로그인 확인 여부
     * @return boolean
     */
    @Override
    public boolean isLoginUser() {
        Long userLogin = (Long) httpSession.getAttribute(USER_ID);

        if(userLogin != null) {
            return true;
        }
        return false;
    }
}
