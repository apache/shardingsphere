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

import io.shardingsphere.example.repository.api.entity.Order;
import io.shardingsphere.example.repository.api.repository.OrderRepository;
import io.shardingsphere.example.repository.jpa.entity.JPAOrder;
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
    public void createTableIfNotExists() {}
    
    @Override
    public void truncateTable() {}
    
    @Override
    public void dropTable() {}
    
    @Override
    public Long insert(final Order entity) {
        entityManager.persist(entity);
        return entity.getOrderId();
    }
    
    @Override
    public void delete(final Long orderId) {
        Query query = entityManager.createQuery("DELETE FROM JPAOrder o WHERE o.orderId = ?1 AND o.userId = 51");
        query.setParameter(1, orderId);
        query.executeUpdate();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Order> selectAll() {
        return (List<Order>) entityManager.createQuery("SELECT o FROM JPAOrder o, JPAOrderItem i WHERE o.orderId = i.orderId").getResultList();
    }
}
