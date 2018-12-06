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

package io.shardingsphere.transaction.fixture;

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.transaction.ShardingEnvironment;
import io.shardingsphere.transaction.annotation.ShardingTransactional;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
@ShardingTransactional(type = TransactionType.XA)
public class ShardingTransactionalTestService {
    
    @ShardingTransactional
    public void testChangeTransactionTypeToLOCAL() {
    }
    
    @ShardingTransactional(type = TransactionType.XA)
    public void testChangeTransactionTypeToXA() {
    }
    
    @ShardingTransactional(type = TransactionType.BASE)
    public void testChangeTransactionTypeToBASE() {
    }
    
    public void testChangeTransactionTypeInClass() {
    }
    
    @ShardingTransactional(type = TransactionType.BASE, environment = ShardingEnvironment.PROXY)
    public void testChangeTransactionTypeToBASEWithEnvironment() {
    }
    
    @ShardingTransactional(type = TransactionType.XA, environment = ShardingEnvironment.PROXY)
    public void testChangeTransactionTypeToXAWithEnvironment() {
    }
    
    @ShardingTransactional(environment = ShardingEnvironment.PROXY)
    public void testChangeTransactionTypeToLOCALWithEnvironment() {
    }
}
