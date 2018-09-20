/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.revert;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.jdbc.core.ShardingContext;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.LexerEngineFactory;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.transaction.revert.RevertEngine;
import io.shardingsphere.transaction.revert.RevertResult;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * ${DESCRIPTION}.
 *
 * @author yangyi
 */
@RequiredArgsConstructor
public final class JDBCRevertEngine implements RevertEngine {
    
    private final ShardingContext shardingContext;
    
    @Override
    public RevertResult revert(final String datasource, final String sql, final List<List<Object>> params) throws SQLException {
        DataSource dataSource = shardingContext.getDataSourceMap().get(datasource);
        // TODO use new SnapShotEngine to get revert result.
        DatabaseType databaseType = shardingContext.getDatabaseType();
        String logicTable = getLogicTable(databaseType, sql);
        TableMetaData tableMetaData = shardingContext.getMetaData().getTable().getTableMetaDataMap().get(logicTable);
        RevertResult result = new RevertResult();
//        for (List<Object> each : params) {
//            RevertContext context = new SnapshotEngine(dataSource, sql, each.toArray(), 1, tableMetaData).snapshot();
//            if (context.getRevertParam().size() > 0) {
//                result.setRevertSQL(context.getRevertSQL());
//                result.getRevertSQLParams().addAll(context.getRevertParam());
//            }
//        }
        return result;
    }
    
    @Override
    public RevertResult revert(final String datasource, final String sql, final Object[] params) {
        return null;
    }
    
    private String getLogicTable(final DatabaseType dbType, final String sql) {
        LexerEngine lexerEngine = LexerEngineFactory.newInstance(dbType, sql);
        lexerEngine.nextToken();
        lexerEngine.skipAll(DefaultKeyword.values());
        String physicalTableName = lexerEngine.getCurrentToken().getLiterals();
        return physicalTableName.substring(0, physicalTableName.lastIndexOf('_'));
    }
}
