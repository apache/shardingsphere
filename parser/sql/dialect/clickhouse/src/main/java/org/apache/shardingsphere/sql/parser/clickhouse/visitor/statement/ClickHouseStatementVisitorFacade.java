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

package org.apache.shardingsphere.sql.parser.clickhouse.visitor.statement;

import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.*;
import org.apache.shardingsphere.sql.parser.clickhouse.visitor.statement.type.*;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;

/**
 * @author zzypersonally@gmail.com
 * @since 2024/5/7 16:04
 */
public class ClickHouseStatementVisitorFacade implements SQLStatementVisitorFacade {
    
    @Override
    public Class<? extends DMLStatementVisitor> getDMLVisitorClass() {
        return ClickHouseDMLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DDLStatementVisitor> getDDLVisitorClass() {
        return ClickHouseDDLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends TCLStatementVisitor> getTCLVisitorClass() {
        return ClickHouseTCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DCLStatementVisitor> getDCLVisitorClass() {
        return ClickHouseDCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DALStatementVisitor> getDALVisitorClass() {
        return ClickHouseDALStatementVisitor.class;
    }
    
    @Override
    public Class<? extends RLStatementVisitor> getRLVisitorClass() {
        return ClickHouseRLStatementVisitor.class;
    }
    
    @Override
    public String getDatabaseType() {
        return "ClickHouse";
    }
}
