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

package org.apache.shardingsphere.infra.optimize.schema.table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.schema.row.CalciteRowExecutor;

/**
 * Abstract calcite table.
 */
@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
public abstract class AbstractCalciteTable extends AbstractTable {
    
    private final String name;
    
    private final TableMetaData tableMetaData;
    
    private final RelProtoDataType relProtoDataType;
    
    private final CalciteRowExecutor executor;
    
    @Override
    public final RelDataType getRowType(final RelDataTypeFactory typeFactory) {
        return relProtoDataType.apply(typeFactory);
    }
}
