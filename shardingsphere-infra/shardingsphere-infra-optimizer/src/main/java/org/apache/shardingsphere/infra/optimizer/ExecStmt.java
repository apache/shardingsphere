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

package org.apache.shardingsphere.infra.optimizer;

import lombok.Getter;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;

import java.util.List;

@Getter
public class ExecStmt {
    
    private final boolean success;
    
    private final SqlNode sqlNode;
    
    private final RelNode physicalPlan;
    
    private final List<ColumnMetaData> resultColumns;
    
    public ExecStmt() {
        this.success = false;
        this.sqlNode = null;
        this.physicalPlan = null;
        this.resultColumns = null;
    }
    
    public ExecStmt(final SqlNode sqlNode, final RelNode physicalPlan, final List<ColumnMetaData> resultColumns) {
        this.success = true;
        this.sqlNode = sqlNode;
        this.physicalPlan = physicalPlan;
        this.resultColumns = resultColumns;
    }
}
