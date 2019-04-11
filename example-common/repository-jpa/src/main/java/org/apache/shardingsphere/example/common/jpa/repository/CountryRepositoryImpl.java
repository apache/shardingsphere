/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.example.common.jpa.repository;

import org.apache.shardingsphere.example.common.entity.Country;
import org.apache.shardingsphere.example.common.repository.CountryRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class CountryRepositoryImpl implements CountryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void createTableIfNotExists() {
        throw new UnsupportedOperationException("createTableIfNotExists for JPA");
    }

    @Override
    public void dropTable() {
        throw new UnsupportedOperationException("truncateTable for JPA");
    }

    @Override
    public void truncateTable() {
        throw new UnsupportedOperationException("dropTable for JPA");
    }

    @Override
    public Long insert(final Country country) {
        entityManager.persist(country);
        return 1L;
    }

    @Override
    public void delete(final String code) {
        Query query = entityManager.createQuery("DELETE FROM CountryEntity o WHERE o.code = ?1");
        query.setParameter(1, code);
        query.executeUpdate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Country> selectAll() {
        return (List<Country>) entityManager.createQuery("SELECT o FROM CountryEntity o").getResultList();
    }
}
