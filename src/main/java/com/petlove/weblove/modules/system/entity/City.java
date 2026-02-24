package com.petlove.weblove.modules.system.entity;

import com.petlove.weblove.common.util.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "cities")
public class City extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_code", nullable = false, unique = true)
    private String cityCode;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    public Long getId() {
        return id;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
