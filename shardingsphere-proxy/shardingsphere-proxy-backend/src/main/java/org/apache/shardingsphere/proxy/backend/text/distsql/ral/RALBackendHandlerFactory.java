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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.CheckScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.DropScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.ResetScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.ShowScalingJobListStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.ShowScalingJobStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.StartScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.StopScalingJobStatement;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.impl.CheckScalingJobBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.impl.DropScalingJobBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.impl.ResetScalingJobBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.impl.ShowScalingJobListBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.impl.ShowScalingJobStatusBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.impl.StartScalingJobBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.impl.StopScalingJobBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Optional;

/**
 * RAL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RALBackendHandlerFactory {
    
    /**
     * Create new instance of RAL backend handler.
     *
     * @param sqlStatement SQL statement
     * @return RAL backend handler
     */
    public static Optional<TextProtocolBackendHandler> newInstance(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof ShowScalingJobListStatement) {
            return Optional.of(new ShowScalingJobListBackendHandler());
        }
        if (sqlStatement instanceof ShowScalingJobStatusStatement) {
            return Optional.of(new ShowScalingJobStatusBackendHandler((ShowScalingJobStatusStatement) sqlStatement));
        }
        if (sqlStatement instanceof StartScalingJobStatement) {
            return Optional.of(new StartScalingJobBackendHandler((StartScalingJobStatement) sqlStatement));
        }
        if (sqlStatement instanceof StopScalingJobStatement) {
            return Optional.of(new StopScalingJobBackendHandler((StopScalingJobStatement) sqlStatement));
        }
        if (sqlStatement instanceof DropScalingJobStatement) {
            return Optional.of(new DropScalingJobBackendHandler((DropScalingJobStatement) sqlStatement));
        }
        if (sqlStatement instanceof ResetScalingJobStatement) {
            return Optional.of(new ResetScalingJobBackendHandler((ResetScalingJobStatement) sqlStatement));
        }
        if (sqlStatement instanceof CheckScalingJobStatement) {
            return Optional.of(new CheckScalingJobBackendHandler((CheckScalingJobStatement) sqlStatement));
        }
        return Optional.empty();
    }
}
