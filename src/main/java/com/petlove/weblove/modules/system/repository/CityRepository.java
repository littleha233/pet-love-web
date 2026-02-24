package com.petlove.weblove.modules.system.repository;

import com.petlove.weblove.modules.system.entity.City;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByCityCode(String cityCode);

    List<City> findByEnabledTrueOrderByCityNameAsc();
}
