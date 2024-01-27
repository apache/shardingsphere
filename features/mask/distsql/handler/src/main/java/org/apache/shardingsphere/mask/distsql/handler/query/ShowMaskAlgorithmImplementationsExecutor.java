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

package org.apache.shardingsphere.mask.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.type.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mask.distsql.statement.ShowMaskAlgorithmImplementationsStatement;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Show mask algorithm implementations executor.
 */
public final class ShowMaskAlgorithmImplementationsExecutor implements QueryableRALExecutor<ShowMaskAlgorithmImplementationsStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "class_path");
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowMaskAlgorithmImplementationsStatement sqlStatement, final ContextManager contextManager) {
        Collection<MaskAlgorithm> maskAlgorithms = ShardingSphereServiceLoader.getServiceInstances(MaskAlgorithm.class);
        return maskAlgorithms.stream().map(each -> new LocalDataQueryResultRow(each.getClass().getSimpleName(), each.getType(), each.getClass().getName())).collect(Collectors.toList());
    }
    
    @Override
    public Class<ShowMaskAlgorithmImplementationsStatement> getType() {
        return ShowMaskAlgorithmImplementationsStatement.class;
    }
}
