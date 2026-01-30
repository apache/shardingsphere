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

package org.apache.shardingsphere.infra.merge.fixture.decorator;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecoratorEngine;
import org.apache.shardingsphere.infra.merge.fixture.rule.DecoratorRuleFixture;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.util.Optional;

public final class ResultDecoratorEngineFixture implements ResultDecoratorEngine<DecoratorRuleFixture> {
    
    @Override
    public Optional<ResultDecorator> newInstance(final ShardingSphereMetaData metaData,
                                                 final ShardingSphereDatabase database, final ConfigurationProperties props, final SQLStatementContext sqlStatementContext) {
        return Optional.of(new ResultDecoratorFixture());
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
    
    @Override
    public Class<DecoratorRuleFixture> getTypeClass() {
        return DecoratorRuleFixture.class;
    }
}
