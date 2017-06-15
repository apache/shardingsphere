/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.example.id.repository;

import com.dangdang.ddframe.rdb.sharding.example.id.entity.Order;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class OrderRepositoryImpl implements OrderRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Order selectById(final long orderId) {
        return entityManager.find(Order.class, orderId);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Order> selectAll() {
        return (List<Order>) entityManager.createQuery("SELECT o FROM Order o").getResultList();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Order> selectOrderBy() {
        return (List<Order>) entityManager.createQuery("SELECT o FROM Order o order by o.orderId").getResultList();
    }
    
    @Override
    public void create(final Order order) {
        entityManager.persist(order);
    }
    
    @Override
    public void update(final Order order) {
        Query query = entityManager.createQuery("UPDATE Order o SET o.status = ?1 WHERE o.orderId = ?2 AND o.userId = ?3");
        query.setParameter(1, order.getStatus());
        query.setParameter(2, order.getOrderId());
        query.setParameter(3, order.getUserId());
        query.executeUpdate();
    }
    
    @Override
    public void delete(final long orderId) {
        Query query = entityManager.createQuery("DELETE FROM Order o WHERE o.orderId = ?1 AND o.userId = 51");
        query.setParameter(1, orderId);
        query.executeUpdate();
    }
}
