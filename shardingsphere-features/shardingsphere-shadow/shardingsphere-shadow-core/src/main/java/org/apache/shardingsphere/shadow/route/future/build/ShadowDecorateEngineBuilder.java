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

package org.apache.shardingsphere.shadow.route.future.build;

import org.apache.shardingsphere.shadow.route.future.engine.decorate.ShadowDecorateEngine;
import org.apache.shardingsphere.shadow.route.future.engine.decorate.impl.ShadowDataSourceRouterDecorateEngine;
import org.apache.shardingsphere.shadow.route.future.engine.judge.impl.DeleteShadowDataSourceRouterJudgeEngine;
import org.apache.shardingsphere.shadow.route.future.engine.judge.impl.InsertShadowDataSourceRouterJudgeEngine;
import org.apache.shardingsphere.shadow.route.future.engine.judge.impl.NonMDLShadowDataSourceRouterJudgeEngine;
import org.apache.shardingsphere.shadow.route.future.engine.judge.impl.SelectShadowDataSourceRouterJudgeEngine;
import org.apache.shardingsphere.shadow.route.future.engine.judge.impl.UpdateShadowDataSourceRouterJudgeEngine;
import org.apache.shardingsphere.shadow.route.future.engine.rewrite.impl.DeleteShadowDataSourceRouterRewriteEngine;
import org.apache.shardingsphere.shadow.route.future.engine.rewrite.impl.InsertShadowDataSourceRouterRewriteEngine;
import org.apache.shardingsphere.shadow.route.future.engine.rewrite.impl.NonMDLShadowDataSourceRouterRewriteEngine;
import org.apache.shardingsphere.shadow.route.future.engine.rewrite.impl.SelectShadowDataSourceRouterRewriteEngine;
import org.apache.shardingsphere.shadow.route.future.engine.rewrite.impl.UpdateShadowDataSourceRouterRewriteEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

/**
 * Shadow decorate engine builder.
 */
public final class ShadowDecorateEngineBuilder {
    
    /**
     * Build shadow decorate engine.
     *
     * @param sqlStatement sql statement
     * @return shadow decorate engine
     */
    public static ShadowDecorateEngine buildShadowDecorateEngine(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            return createInsertShadowDataSourceRouterDecorateEngine();
        } else if (sqlStatement instanceof UpdateStatement) {
            return createUpdateShadowDataSourceRouterDecorateEngine();
        } else if (sqlStatement instanceof DeleteStatement) {
            return createDeleteShadowDataSourceRouterDecorateEngine();
        } else if (sqlStatement instanceof SelectStatement) {
            return createSelectShadowDataSourceRouterDecorateEngine();
        } else {
            return createNonMDLShadowDataSourceRouterDecorateEngine();
        }
    }
    
    private static ShadowDecorateEngine createNonMDLShadowDataSourceRouterDecorateEngine() {
        return new ShadowDataSourceRouterDecorateEngine(new NonMDLShadowDataSourceRouterJudgeEngine(), new NonMDLShadowDataSourceRouterRewriteEngine());
    }
    
    private static ShadowDecorateEngine createSelectShadowDataSourceRouterDecorateEngine() {
        return new ShadowDataSourceRouterDecorateEngine(new SelectShadowDataSourceRouterJudgeEngine(), new SelectShadowDataSourceRouterRewriteEngine());
    }
    
    private static ShadowDecorateEngine createDeleteShadowDataSourceRouterDecorateEngine() {
        return new ShadowDataSourceRouterDecorateEngine(new DeleteShadowDataSourceRouterJudgeEngine(), new DeleteShadowDataSourceRouterRewriteEngine());
    }
    
    private static ShadowDecorateEngine createUpdateShadowDataSourceRouterDecorateEngine() {
        return new ShadowDataSourceRouterDecorateEngine(new UpdateShadowDataSourceRouterJudgeEngine(), new UpdateShadowDataSourceRouterRewriteEngine());
    }
    
    private static ShadowDecorateEngine createInsertShadowDataSourceRouterDecorateEngine() {
        return new ShadowDataSourceRouterDecorateEngine(new InsertShadowDataSourceRouterJudgeEngine(), new InsertShadowDataSourceRouterRewriteEngine());
    }
}
