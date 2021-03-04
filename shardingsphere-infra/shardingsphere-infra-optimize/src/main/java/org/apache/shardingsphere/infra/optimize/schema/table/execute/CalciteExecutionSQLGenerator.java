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

package org.apache.shardingsphere.infra.optimize.schema.table.execute;

import org.apache.calcite.DataContext;
import org.apache.calcite.rex.RexNode;

import java.util.List;

/**
 * Calcite execution sql generator.
 */
public final class CalciteExecutionSQLGenerator {
    
    public CalciteExecutionSQLGenerator(final DataContext root, final List<RexNode> filters, final int[] projects) {
        // TODO
    }
    
    /**
     * Create sql.
     *
     * @param table table
     * @return sql
     */
    public String generate(final String table) {
        // TODO
        return String.format("SELECT * FROM %s", table);
    }
}
