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

package org.apache.shardingsphere.sql.parser.statement.doris.ddl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.view.MaterializedViewColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.LinkedList;
import java.util.List;

/**
 * Create materialized view statement for Doris.
 */
@Getter
@Setter
public final class DorisCreateMaterializedViewStatement extends DDLStatement {
    
    private SimpleTableSegment materializedView;
    
    private boolean ifNotExists;
    
    private final List<MaterializedViewColumnSegment> columns = new LinkedList<>();
    
    private String buildMode;
    
    private String refreshMethod;
    
    private String refreshTrigger;
    
    private Integer refreshInterval;
    
    private String refreshUnit;
    
    private String startTime;
    
    private boolean duplicateKey;
    
    private final List<ColumnSegment> keyColumns = new LinkedList<>();
    
    private CommentSegment comment;
    
    private ColumnSegment partitionColumn;
    
    private String partitionFunctionName;
    
    private ColumnSegment partitionFunctionColumn;
    
    private String partitionFunctionUnit;
    
    private String distributeType;
    
    private final List<IdentifierValue> distributeColumns = new LinkedList<>();
    
    private Integer bucketCount;
    
    private boolean autoBucket;
    
    private PropertiesSegment properties;
    
    private SelectStatement selectStatement;
    
    public DorisCreateMaterializedViewStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
}
