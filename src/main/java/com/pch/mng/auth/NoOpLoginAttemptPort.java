package com.pch.mng.auth;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"test", "dev"})
@Primary
public class NoOpLoginAttemptPort implements LoginAttemptPort {

    @Override
    public void checkLocked(String email) {
    }

    @Override
    public void onSuccess(String email) {
    }

    @Override
    public void onFailure(String email) {
    }
}
