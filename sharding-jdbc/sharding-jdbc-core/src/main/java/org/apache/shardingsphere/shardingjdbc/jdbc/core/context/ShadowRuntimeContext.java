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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.context;

import lombok.Getter;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.spi.database.DatabaseType;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Runtime context for shadow.
 *
 * @author zhyee
 */
@Getter
public class ShadowRuntimeContext extends AbstractRuntimeContext<ShadowRule> {
    
    private final TableMetas tableMetas;
    
    private final DataSource actualDataSource;
    
    private final DataSource shadowDataSource;
    
    public ShadowRuntimeContext(final DataSource actualDataSource, final DataSource shadowDataSource, final ShadowRule rule, final Properties props, final DatabaseType databaseType) {
        super(rule, props, databaseType);
        //TODO
        tableMetas = null;
        this.actualDataSource = actualDataSource;
        this.shadowDataSource = shadowDataSource;
    }
    
    private TableMetas createShadowTableMetas(final DataSource dataSource, final ShadowRule shadowRule) {
        Map<String, TableMetaData> tables = new LinkedHashMap<>();
        //TODO
        return new TableMetas(tables);
    }
}
