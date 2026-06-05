package com.attendance.service;

import com.attendance.dto.TechLoginRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FingerprintAuthService {

    public boolean verifyFingerprint(TechLoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getCredential())) {
            return false;
        }

        // TODO: Integrate with a real biometric verifier.
        return true;
    }
}
