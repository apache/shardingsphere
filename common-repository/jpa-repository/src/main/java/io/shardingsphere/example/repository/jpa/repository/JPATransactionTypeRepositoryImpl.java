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

import io.shardingsphere.example.repository.api.entity.TransactionType;
import io.shardingsphere.example.repository.api.repository.TransactionTypeRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class JPATransactionTypeRepositoryImpl implements TransactionTypeRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public TransactionType showTransactionType() {
        TransactionType result = new TransactionType();
        result.setTransactionType((String) entityManager.createNativeQuery("SCTL\\:SHOW TRANSACTION_TYPE").getSingleResult());
        return result;
    }
}
