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

package org.apache.shardingsphere.transaction.spring.boot.fixture;

import org.apache.shardingsphere.transaction.annotation.ShardingTransactionType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
@ShardingTransactionType(TransactionType.XA)
public class ShardingTransactionalTestService {
    
    @ShardingTransactionType
    public void testChangeTransactionTypeToLOCAL() {
    }
    
    @ShardingTransactionType(TransactionType.XA)
    public void testChangeTransactionTypeToXA() {
    }
    
    @ShardingTransactionType(TransactionType.BASE)
    public void testChangeTransactionTypeToBASE() {
    }
    
    public void testChangeTransactionTypeInClass() {
    }
}
