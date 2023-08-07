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

package org.apache.shardingsphere.infra.connection.refresher.type.schema;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Schema refresher for drop schema statement.
 */
public final class DropSchemaStatementSchemaRefresher implements MetaDataRefresher<DropSchemaStatement> {
    
    @Override
    public void refresh(final ModeContextManager modeContextManager, final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                        final String schemaName, final DropSchemaStatement sqlStatement, final ConfigurationProperties props) {
        modeContextManager.dropSchema(database.getName(), getSchemaNames(sqlStatement));
    }
    
    private Collection<String> getSchemaNames(final DropSchemaStatement sqlStatement) {
        Collection<String> result = new LinkedList<>();
        for (IdentifierValue each : sqlStatement.getSchemaNames()) {
            result.add(each.getValue().toLowerCase());
        }
        return result;
    }
    
    @Override
    public Class<DropSchemaStatement> getType() {
        return DropSchemaStatement.class;
    }
}
