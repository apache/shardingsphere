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

package org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.column;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Column data type reviser.
 * 
 * @param <T> type of rule
 */
public interface ColumnDataTypeReviser<T extends ShardingSphereRule> extends TypedSPI {
    
    /**
     * Revise column data type.
     * 
     * @param originalName original name
     * @param rule rule
     * @param databaseType database type
     * @param dataSource data source
     * @return revised data type
     */
    Optional<Integer> revise(String originalName, T rule, DatabaseType databaseType, DataSource dataSource);
}
