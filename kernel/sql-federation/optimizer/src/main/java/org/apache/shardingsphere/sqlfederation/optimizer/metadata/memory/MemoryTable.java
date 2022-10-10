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

package org.apache.shardingsphere.sqlfederation.optimizer.metadata.memory;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.DataContext;
import org.apache.calcite.avatica.SqlType;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;

import java.util.List;

/**
 * Memory table.
 */
@RequiredArgsConstructor
public final class MemoryTable extends AbstractTable implements ScannableTable {
    
    private final ShardingSphereTable table;
    
    private final List<ShardingSphereRowData> rows;
    
    @Override
    public Enumerable<Object[]> scan(final DataContext dataContext) {
        return new AbstractEnumerable<Object[]>() {
            @Override
            public Enumerator<Object[]> enumerator() {
                return new MemoryEnumerator(rows);
            }
        };
    }
    
    @Override
    public RelDataType getRowType(final RelDataTypeFactory relDataTypeFactory) {
        return createRelDataType(table, relDataTypeFactory);
    }
    
    private RelDataType createRelDataType(final ShardingSphereTable table, final RelDataTypeFactory typeFactory) {
        RelDataTypeFactory.Builder fieldInfoBuilder = typeFactory.builder();
        for (ShardingSphereColumn each : table.getColumns().values()) {
            fieldInfoBuilder.add(each.getName(), getRelDataType(each, typeFactory));
        }
        return fieldInfoBuilder.build();
    }
    
    private RelDataType getRelDataType(final ShardingSphereColumn column, final RelDataTypeFactory typeFactory) {
        Class<?> sqlTypeClass = SqlType.valueOf(column.getDataType()).clazz;
        RelDataType javaType = typeFactory.createJavaType(sqlTypeClass);
        return typeFactory.createTypeWithNullability(javaType, true);
    }
}
