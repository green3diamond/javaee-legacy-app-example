package com.example.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.domain.UserAccount;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final Executor auditExecutor;

    public AuditService(Executor auditExecutor) {
        this.auditExecutor = auditExecutor;
    }

    public CompletableFuture<String> registrationCreated(UserAccount account) {
        return CompletableFuture.supplyAsync(() -> {
            String message = "AUDIT registration-created email=%s role=%s".formatted(
                    account.getEmail(),
                    account.getRole()
            );
            log.info(message);
            return message;
        }, auditExecutor);
    }
}
