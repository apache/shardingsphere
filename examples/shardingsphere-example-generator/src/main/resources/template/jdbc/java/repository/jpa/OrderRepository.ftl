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
<#assign package = feature?replace('-', '')?replace(',', '.') />

package org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.repository;

import org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.entity.Order;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class OrderRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public void createTableIfNotExists() {
    }
    
    public void dropTable() {
    }
    
    public void truncateTable() {
    }
    
    public Long insert(final Order order) {
        entityManager.persist(order);
        return order.getOrderId();
    }
    
    public void delete(final Long orderId) {
        Query query = entityManager.createQuery("DELETE FROM Order o WHERE o.orderId = ?1");
        query.setParameter(1, orderId);
        query.executeUpdate();
    }
    
    <#if feature?contains("shadow")>
    public void deleteShadow(final Long orderId) {
        Query query = entityManager.createQuery("DELETE FROM Order o WHERE o.orderId = ?1 AND order_type=1");
        query.setParameter(1, orderId);
        query.executeUpdate();
    }
    </#if>
    
    public List<Order> selectAll() {
        return (List<Order>) entityManager.createQuery("SELECT o FROM Order o").getResultList();
    }
}
