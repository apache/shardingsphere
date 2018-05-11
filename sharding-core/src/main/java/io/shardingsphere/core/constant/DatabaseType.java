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

package io.shardingsphere.core.constant;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import java.util.Arrays;

/**
 * Supported database.
 * 
 * @author zhangliang
 */
public enum DatabaseType {
    
    H2("H2"), MySQL("MySQL"), Oracle("Oracle"), SQLServer("Microsoft SQL Server"), PostgreSQL("PostgreSQL");
    
    private final String productName;
    
    DatabaseType(final String productName) {
        this.productName = productName;
    }
    
    /**
     * Get database type enum via database name string.
     * 
     * @param databaseProductName database name string
     * @return database enum
     */
    public static DatabaseType valueFrom(final String databaseProductName) {
        Optional<DatabaseType> databaseTypeOptional = Iterators.tryFind(Arrays.asList(DatabaseType.values()).iterator(), new Predicate<DatabaseType>() {
            
            @Override
            public boolean apply(final DatabaseType input) {
                return input.productName.equals(databaseProductName);
            }
        });
        if (databaseTypeOptional.isPresent()) {
            return databaseTypeOptional.get();
        }
        throw new UnsupportedOperationException(String.format("Can not support database type [%s].", databaseProductName)); 
    }
}
