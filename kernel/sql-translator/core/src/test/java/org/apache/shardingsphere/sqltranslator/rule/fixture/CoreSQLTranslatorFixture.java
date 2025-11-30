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

package org.apache.shardingsphere.sqltranslator.rule.fixture;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sqltranslator.context.SQLTranslatorContext;
import org.apache.shardingsphere.sqltranslator.exception.UnsupportedTranslatedDatabaseException;
import org.apache.shardingsphere.sqltranslator.spi.SQLTranslator;

import java.util.List;
import java.util.Locale;

public final class CoreSQLTranslatorFixture implements SQLTranslator {
    
    @Override
    public SQLTranslatorContext translate(final String sql, final List<Object> parameters, final QueryContext queryContext, final DatabaseType storageType, final ShardingSphereDatabase database,
                                          final RuleMetaData globalRuleMetaData) {
        ShardingSpherePreconditions.checkState(!sql.startsWith("ERROR:"), () -> new UnsupportedTranslatedDatabaseException(storageType));
        return new SQLTranslatorContext(sql.toUpperCase(Locale.ROOT), parameters);
    }
    
    @Override
    public String getType() {
        return "CORE:FIXTURE";
    }
}
