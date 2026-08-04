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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser.table;

import org.apache.shardingsphere.infra.metadata.database.schema.reviser.column.ColumnGeneratedReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.constraint.ConstraintReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.index.IndexReviser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Optional;

/**
 * Table meta data revision context.
 *
 * @param <T> type of rule
 */
public interface TableMetaDataRevisionContext<T extends ShardingSphereRule> {
    
    /**
     * Revise table name.
     *
     * @param originalName original table name
     * @return revised table name
     */
    String reviseTableName(String originalName);
    
    /**
     * Get column generated reviser.
     *
     * @return column generated reviser
     */
    default Optional<? extends ColumnGeneratedReviser> getColumnGeneratedReviser() {
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
