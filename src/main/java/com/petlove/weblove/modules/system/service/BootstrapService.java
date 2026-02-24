package com.petlove.weblove.modules.system.service;

import com.petlove.weblove.config.AppBootstrapProperties;
import com.petlove.weblove.modules.admin.entity.AdminUser;
import com.petlove.weblove.modules.admin.enums.AdminRole;
import com.petlove.weblove.modules.admin.enums.AdminStatus;
import com.petlove.weblove.modules.admin.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapService.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppBootstrapProperties bootstrapProperties;

    public BootstrapService(AdminUserRepository adminUserRepository,
                            PasswordEncoder passwordEncoder,
                            AppBootstrapProperties bootstrapProperties) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapProperties = bootstrapProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        adminUserRepository.findByUsername(bootstrapProperties.getAdminUsername())
            .orElseGet(() -> {
                AdminUser admin = new AdminUser();
                admin.setUsername(bootstrapProperties.getAdminUsername());
                admin.setPasswordHash(passwordEncoder.encode(bootstrapProperties.getAdminPassword()));
                admin.setDisplayName(bootstrapProperties.getAdminDisplayName());
                admin.setRole(AdminRole.SUPER_ADMIN);
                admin.setStatus(AdminStatus.ACTIVE);
                AdminUser saved = adminUserRepository.save(admin);
                log.info("Bootstrap admin created: {}", saved.getUsername());
                return saved;
            });
    }
}
