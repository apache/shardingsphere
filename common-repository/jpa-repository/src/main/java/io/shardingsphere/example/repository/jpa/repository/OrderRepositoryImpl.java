/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.repository.jpa.repository;

import io.shardingsphere.example.repository.jpa.entity.JPAOrder;
import io.shardingsphere.example.repository.jpa.entity.JPAOrderItem;
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
    public void createIfNotExistsTable() {}
    
    @Override
    public void truncateTable() {}
    
    @Override
    public void dropTable() {}
    
    @Override
    public Long insert(final JPAOrder JPAOrder) {
        entityManager.persist(JPAOrder);
        return JPAOrder.getOrderId();
    }
    
    @Override
    public void delete(final Long orderId) {
        Query query = entityManager.createQuery("DELETE FROM Order o WHERE o.orderId = ?1 AND o.userId = 51");
        query.setParameter(1, orderId);
        query.executeUpdate();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<JPAOrder> selectAll() {
        return (List<JPAOrder>) entityManager.createQuery("SELECT o FROM Order o, OrderItem i WHERE o.orderId = i.orderId").getResultList();
    }
}
