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

package org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.column.ColumnDataTypeReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.column.ColumnExistedReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.column.ColumnGeneratedReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.column.ColumnNameReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.constraint.ConstraintReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.index.IndexReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.schema.SchemaTableAggregationReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.table.TableNameReviser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import java.util.Optional;

/**
 * Meta data revise entry.
 * 
 * @param <T> type of rule
 */
public interface MetaDataReviseEntry<T extends ShardingSphereRule> extends TypedSPI {
    
    /**
     * Get schema table aggregation reviser.
     * 
     * @param rule rule
     * @param props configuration properties
     * @return schema table aggregation reviser
     */
    default Optional<? extends SchemaTableAggregationReviser<T>> getSchemaTableAggregationReviser(final T rule, final ConfigurationProperties props) {
        return Optional.empty();
    }
    
    /**
     * Get table name reviser.
     * 
     * @return table name reviser
     */
    default Optional<? extends TableNameReviser<T>> getTableNameReviser() {
        return Optional.empty();
    }
    
    /**
     * Get column existed reviser.
     *
     * @param rule rule
     * @param tableName table name
     * @return column existed reviser
     */
    default Optional<? extends ColumnExistedReviser<T>> getColumnExistedReviser(final T rule, final String tableName) {
        return Optional.empty();
    }
    
    /**
     * Get column name reviser.
     * 
     * @param rule rule
     * @param tableName table name
     * @return column name reviser
     */
    default Optional<? extends ColumnNameReviser<T>> getColumnNameReviser(final T rule, final String tableName) {
        return Optional.empty();
    }
    
    /**
     * Get column data type reviser.
     *
     * @param rule rule
     * @param tableName table name
     * @return column data type reviser
     */
    default Optional<? extends ColumnDataTypeReviser<T>> getColumnDataTypeReviser(final T rule, final String tableName) {
        return Optional.empty();
    }
    
    /**
     * Get column generated reviser.
     *
     * @param rule rule
     * @param tableName table name
     * @return column generated reviser
     */
    default Optional<? extends ColumnGeneratedReviser<T>> getColumnGeneratedReviser(final T rule, final String tableName) {
        return Optional.empty();
    }
    
    /**
     * Get index reviser.
     *
     * @param rule rule
     * @param tableName table name
     * @return index reviser
     */
    default Optional<? extends IndexReviser<T>> getIndexReviser(final T rule, final String tableName) {
        return Optional.empty();
    }
    
    /**
     * Get constraint reviser.
     *
     * @param rule rule
     * @param tableName table name
     * @return constraint reviser
     */
    default Optional<? extends ConstraintReviser<T>> getConstraintReviser(final T rule, final String tableName) {
        return Optional.empty();
    }
}
