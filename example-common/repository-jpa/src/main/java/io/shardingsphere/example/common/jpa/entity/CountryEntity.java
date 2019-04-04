package io.shardingsphere.example.common.jpa.entity;

import io.shardingsphere.example.common.entity.Country;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_country")
public class CountryEntity extends Country {

    @Id
    @Column(name = "id")
    @Override
    public long getId() {
        return super.getId();
    }

    @Column(name = "name")
    @Override
    public String getName() {
        return super.getName();
    }

    @Column(name = "code")
    @Override
    public String getCode() {
        return super.getCode();
    }

    @Column(name = "language")
    @Override
    public String getLanguage() {
        return super.getLanguage();
    }
}
