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

package org.apache.shardingsphere.mask.merge;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecoratorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mask.constant.MaskOrder;
import org.apache.shardingsphere.mask.merge.dql.MaskDQLResultDecorator;
import org.apache.shardingsphere.mask.rule.MaskRule;

import java.util.Optional;

/**
 * Result decorator engine for mask.
 */
public final class MaskResultDecoratorEngine implements ResultDecoratorEngine<MaskRule> {
    
    @Override
    public Optional<ResultDecorator> newInstance(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final ConfigurationProperties props,
                                                 final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext
                ? Optional.of(new MaskDQLResultDecorator(database, metaData, (SelectStatementContext) sqlStatementContext))
                : Optional.empty();
    }
    
    @Override
    public int getOrder() {
        return MaskOrder.ORDER;
    }
    
    @Override
    public Class<MaskRule> getTypeClass() {
        return MaskRule.class;
    }
}
