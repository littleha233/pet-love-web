package com.petlove.weblove.modules.system.service;

import com.petlove.weblove.modules.system.dto.CityDTO;
import com.petlove.weblove.modules.system.dto.HealthDTO;
import com.petlove.weblove.modules.system.repository.CityRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemService {

    private final CityRepository cityRepository;
    private final EntityManager entityManager;

    public SystemService(CityRepository cityRepository, EntityManager entityManager) {
        this.cityRepository = cityRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public HealthDTO health() {
        return new HealthDTO("pet-love-web", "UP", true, true);
    }

    @Transactional(readOnly = true)
    public HealthDTO ready() {
        boolean dbOk = isDatabaseReady();
        return new HealthDTO("pet-love-web", dbOk ? "READY" : "DOWN", dbOk, true);
    }

    @Transactional(readOnly = true)
    public List<CityDTO> cities() {
        return cityRepository.findByEnabledTrueOrderByCityNameAsc()
            .stream()
            .map(city -> new CityDTO(city.getCityCode(), city.getCityName()))
            .toList();
    }

    private boolean isDatabaseReady() {
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
