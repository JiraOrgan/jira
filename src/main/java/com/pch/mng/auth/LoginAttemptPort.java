package com.pch.mng.auth;

public interface LoginAttemptPort {

    void checkLocked(String email);

    void onSuccess(String email);

    void onFailure(String email);
}
