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

package org.apache.shardingsphere.example.db.discovery.spring.boot.starter.jpa;

import org.apache.shardingsphere.example.db.discovery.spring.boot.starter.jpa.entity.Order;
import org.apache.shardingsphere.example.db.discovery.spring.boot.starter.jpa.entity.OrderItem;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class MemoryLocalDbDiscoverySpringBootStarterJpaRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    public Long insertOrder(final Order order) {
        entityManager.persist(order);
        return order.getOrderId();
    }

    public Long insertOrderItem(final OrderItem orderItem) {
        entityManager.persist(orderItem);
        return orderItem.getOrderItemId();
    }
    
    public List<Order> selectAllOrder() {
        return (List<Order>) entityManager.createQuery("SELECT o FROM Order o").getResultList();
    }
    
    public List<OrderItem> selectAllOrderItem() {
        return (List<OrderItem>) entityManager.createQuery("SELECT o from OrderItem o").getResultList();
    }

    public void deleteOrder(final Long orderId) {
        Query query = entityManager.createQuery("DELETE FROM Order o WHERE o.orderId = ?1");
        query.setParameter(1, orderId);
        query.executeUpdate();
    }

    public void deleteOrderItem(final Long orderId) {
        Query query = entityManager.createQuery("DELETE FROM OrderItem i WHERE i.orderId = ?1");
        query.setParameter(1, orderId);
        query.executeUpdate();
    }
}
