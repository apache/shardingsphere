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

package org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.ShardingCTLHintParser;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.ShardingCTLHintStatement;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintAddDatabaseShardingValueCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintAddTableShardingValueCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintClearCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintSetDatabaseShardingValueCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintSetMasterOnlyCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintShowStatusCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintShowTableStatusCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintAddDatabaseShardingValueExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintAddTableShardingValueExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintClearExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintErrorFormatExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintErrorParameterExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintSetDatabaseShardingValueExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintSetMasterOnlyExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintShowStatusExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.executor.HintShowTableStatusExecutor;

/**
 * Hint command executor factory.
 *
 * @author liya
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HintCommandExecutorFactory {
    
    /**
     * Create hint command executor instance.
     *
     * @param shardingRule sharding rule
     * @param sql          SQL
     * @return hint command executor
     */
    public static HintCommandExecutor newInstance(final ShardingRule shardingRule, final String sql) {
        Optional<ShardingCTLHintStatement> shardingTCLStatement = new ShardingCTLHintParser(sql).doParse();
        if (!shardingTCLStatement.isPresent()) {
            return new HintErrorFormatExecutor(sql);
        }
        HintCommand hintCommand = shardingTCLStatement.get().getHintCommand();
        Optional<HintCommandExecutor> hintUpdateExecutor = getHintUpdateExecutor(hintCommand);
        if (hintUpdateExecutor.isPresent()) {
            return hintUpdateExecutor.get();
        }
        Optional<HintCommandExecutor> hintQueryExecutor = getHintQueryExecutor(hintCommand, shardingRule);
        if (hintQueryExecutor.isPresent()) {
            return hintQueryExecutor.get();
        }
        return new HintErrorParameterExecutor(sql);
    }
    
    private static Optional<HintCommandExecutor> getHintUpdateExecutor(final HintCommand hintCommand) {
        if (hintCommand instanceof HintSetMasterOnlyCommand) {
            return Optional.of((HintCommandExecutor) new HintSetMasterOnlyExecutor((HintSetMasterOnlyCommand) hintCommand));
        }
        if (hintCommand instanceof HintSetDatabaseShardingValueCommand) {
            return Optional.of((HintCommandExecutor) new HintSetDatabaseShardingValueExecutor((HintSetDatabaseShardingValueCommand) hintCommand));
        }
        if (hintCommand instanceof HintAddDatabaseShardingValueCommand) {
            return Optional.of((HintCommandExecutor) new HintAddDatabaseShardingValueExecutor((HintAddDatabaseShardingValueCommand) hintCommand));
        }
        if (hintCommand instanceof HintAddTableShardingValueCommand) {
            return Optional.of((HintCommandExecutor) new HintAddTableShardingValueExecutor((HintAddTableShardingValueCommand) hintCommand));
        }
        if (hintCommand instanceof HintClearCommand) {
            return Optional.of((HintCommandExecutor) new HintClearExecutor());
        }
        return Optional.absent();
    }
    
    private static Optional<HintCommandExecutor> getHintQueryExecutor(final HintCommand hintCommand, final ShardingRule shardingRule) {
        if (hintCommand instanceof HintShowStatusCommand) {
            return Optional.of((HintCommandExecutor) new HintShowStatusExecutor());
        }
        if (hintCommand instanceof HintShowTableStatusCommand) {
            return Optional.of((HintCommandExecutor) new HintShowTableStatusExecutor(shardingRule));
        }
        return Optional.absent();
    }
}
