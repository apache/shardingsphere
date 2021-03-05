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

package org.apache.shardingsphere.infra.optimize.schema;

import lombok.Getter;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.commons.collections4.map.LinkedMap;

import java.util.Map;


/**
 * Calcite logic schema.
 *
 */
@Getter
public final class CalciteLogicSchema extends AbstractSchema {
    
    private final String name;
    
    private final Map<String, Table> tables = new LinkedMap<>();
    
    public CalciteLogicSchema(final String name, final Map<String, Table> tables) {
        this.name = name;
        this.tables.putAll(tables);
    }
    
    @Override
    protected Map<String, Table> getTableMap() {
        return tables;
    }
}
