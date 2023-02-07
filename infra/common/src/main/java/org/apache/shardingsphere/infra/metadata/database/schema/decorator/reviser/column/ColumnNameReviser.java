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

import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;

import java.util.Optional;

/**
 * Column name reviser.
 */
public abstract class ColumnNameReviser implements ColumnReviser {
    
    @Override
    public final Optional<ColumnMetaData> revise(final ColumnMetaData originalMetaData) {
        return getColumnName(originalMetaData.getName()).map(optional -> createColumnMetaData(optional, originalMetaData));
    }
    
    private ColumnMetaData createColumnMetaData(final String name, final ColumnMetaData metaData) {
        return new ColumnMetaData(name, metaData.getDataType(), metaData.isPrimaryKey(), metaData.isGenerated(), metaData.isCaseSensitive(), metaData.isVisible(), metaData.isUnsigned());
    }
    
    protected abstract Optional<String> getColumnName(String originalName);
}
