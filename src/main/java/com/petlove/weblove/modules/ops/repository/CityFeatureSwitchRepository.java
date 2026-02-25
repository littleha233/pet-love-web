package com.petlove.weblove.modules.ops.repository;

import com.petlove.weblove.modules.ops.entity.CityFeatureSwitch;
import com.petlove.weblove.modules.ops.enums.CityFeatureKey;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CityFeatureSwitchRepository extends JpaRepository<CityFeatureSwitch, Long>, JpaSpecificationExecutor<CityFeatureSwitch> {

    Optional<CityFeatureSwitch> findByCityCodeAndFeatureKey(String cityCode, CityFeatureKey featureKey);

    List<CityFeatureSwitch> findByFeatureKey(CityFeatureKey featureKey);
}
