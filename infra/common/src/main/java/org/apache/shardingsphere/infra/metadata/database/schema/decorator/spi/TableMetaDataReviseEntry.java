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

import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.column.ColumnDataTypeReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.column.ColumnGeneratedReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.column.ColumnNameReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.constraint.ConstraintReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.index.IndexReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.table.TableNameReviser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import java.util.Optional;

/**
 * Table name revise entry.
 * 
 * @param <T> type of rule
 */
public interface TableMetaDataReviseEntry<T extends ShardingSphereRule> extends TypedSPI {
    
    /**
     * Get table name reviser.
     * 
     * @return table name reviser
     */
    default Optional<? extends TableNameReviser<T>> getTableNameReviser() {
        return Optional.empty();
    }
    
    /**
     * Get column name reviser.
     *
     * @return column name reviser
     */
    default Optional<? extends ColumnNameReviser<T>> getColumnNameReviser() {
        return Optional.empty();
    }
    
    /**
     * Get column data type reviser.
     *
     * @return column data type reviser
     */
    default Optional<? extends ColumnDataTypeReviser<T>> getColumnDataTypeReviser() {
        return Optional.empty();
    }
    
    /**
     * Get column generated reviser.
     *
     * @return column generated reviser
     */
    default Optional<? extends ColumnGeneratedReviser<T>> getColumnGeneratedReviser() {
        return Optional.empty();
    }
    
    /**
     * Get index reviser.
     *
     * @return index reviser
     */
    default Optional<? extends IndexReviser<T>> getIndexReviser() {
        return Optional.empty();
    }
    
    /**
     * Get constraint reviser.
     *
     * @return constraint reviser
     */
    default Optional<? extends ConstraintReviser<T>> getConstraintReviser() {
        return Optional.empty();
    }
}
