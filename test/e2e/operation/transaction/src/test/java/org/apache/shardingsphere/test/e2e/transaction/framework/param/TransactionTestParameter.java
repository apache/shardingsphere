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

package org.apache.shardingsphere.test.e2e.transaction.framework.param;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.test.e2e.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.transaction.api.TransactionType;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Getter
public final class TransactionTestParameter {
    
    private final DatabaseType databaseType;
    
    private final String adapter;
    
    private final List<TransactionType> transactionTypes;
    
    private final List<String> providers;
    
    private final String storageContainerImage;
    
    private final String scenario;
    
    private final Collection<Class<? extends BaseTransactionTestCase>> transactionTestCaseClasses;
    
    @Override
    public String toString() {
        return String.format("%s -> %s -> %s -> %s -> %s -> %s", databaseType.getType(), adapter, transactionTypes, providers, storageContainerImage, scenario);
    }
}
