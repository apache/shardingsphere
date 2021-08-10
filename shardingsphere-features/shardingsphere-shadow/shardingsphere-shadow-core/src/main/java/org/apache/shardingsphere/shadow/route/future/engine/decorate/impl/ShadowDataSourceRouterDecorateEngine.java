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

package org.apache.shardingsphere.shadow.route.future.engine.decorate.impl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.shadow.route.future.engine.decorate.ShadowDecorateEngine;
import org.apache.shardingsphere.shadow.route.future.engine.judge.ShadowJudgeEngine;
import org.apache.shardingsphere.shadow.route.future.engine.rewrite.ShadowRewriteEngine;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

/**
 * Shadow data source router decorate engine.
 */
@RequiredArgsConstructor
public final class ShadowDataSourceRouterDecorateEngine implements ShadowDecorateEngine {
    
    private final ShadowJudgeEngine shadowJudgeEngine;
    
    private final ShadowRewriteEngine shadowRewriteEngine;
    
    @Override
    public void doShadowDecorate(final RouteContext routeContext, final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final ShadowRule shadowRule) {
        if (shadowJudgeEngine.isShadow(routeContext, logicSQL, metaData, shadowRule)) {
            shadowRewriteEngine.rewrite(routeContext, logicSQL, metaData, shadowRule);
        }
    }
}
