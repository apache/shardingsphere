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

package org.apache.shardingsphere.proxy.backend.text.sctl.hint;

import org.apache.shardingsphere.proxy.backend.text.sctl.ShardingCTLParser;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintAddDatabaseShardingValueCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintAddTableShardingValueCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintClearCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintErrorParameterCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintSetDatabaseShardingValueCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintSetMasterOnlyCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintShowStatusCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintShowTableStatusCommand;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sharding CTL hint parser.
 */
public final class ShardingCTLHintParser implements ShardingCTLParser<ShardingCTLHintStatement> {
    
    private final String setMasterOnlyRegex = "sctl:hint\\s+set\\s+MASTER_ONLY=(true|false)\\s*$";
    
    private final String setDatabaseShardingValueRegex = "sctl:hint\\s+set\\s+DatabaseShardingValue=(\\S*)";
    
    private final String addDatabaseShardingValueRegex = "sctl:hint\\s+addDatabaseShardingValue\\s+(\\S*)=(\\S*)";
    
    private final String addTableShardingValueRegex = "sctl:hint\\s+addTableShardingValue\\s+(\\S*)=(\\S*)";
    
    private final String clearRegex = "sctl:hint\\s+clear\\s*$";
    
    private final String showStatusRegex = "sctl:hint\\s+show\\s+status\\s*$";
    
    private final String showTableStatusRegex = "sctl:hint\\s+show\\s+table\\s+status\\s*$";
    
    private final String errorParameterRegex = "sctl:hint\\s+.*";
    
    private final Matcher setMasterOnlyMatcher;
    
    private final Matcher setDatabaseShardingValueMatcher;
    
    private final Matcher addDatabaseShardingValueMatcher;
    
    private final Matcher addTableShardingValueMatcher;
    
    private final Matcher clearMatcher;
    
    private final Matcher showStatusMatcher;
    
    private final Matcher showTableStatusMatcher;
    
    private final Matcher errorParameterMatcher;
    
    public ShardingCTLHintParser(final String sql) {
        setMasterOnlyMatcher = Pattern.compile(setMasterOnlyRegex, Pattern.CASE_INSENSITIVE).matcher(sql);
        setDatabaseShardingValueMatcher = Pattern.compile(setDatabaseShardingValueRegex, Pattern.CASE_INSENSITIVE).matcher(sql);
        addDatabaseShardingValueMatcher = Pattern.compile(addDatabaseShardingValueRegex, Pattern.CASE_INSENSITIVE).matcher(sql);
        addTableShardingValueMatcher = Pattern.compile(addTableShardingValueRegex, Pattern.CASE_INSENSITIVE).matcher(sql);
        clearMatcher = Pattern.compile(clearRegex, Pattern.CASE_INSENSITIVE).matcher(sql);
        showStatusMatcher = Pattern.compile(showStatusRegex, Pattern.CASE_INSENSITIVE).matcher(sql);
        showTableStatusMatcher = Pattern.compile(showTableStatusRegex, Pattern.CASE_INSENSITIVE).matcher(sql);
        errorParameterMatcher = Pattern.compile(errorParameterRegex, Pattern.CASE_INSENSITIVE).matcher(sql);
    }
    
    @Override
    public Optional<ShardingCTLHintStatement> doParse() {
        Optional<ShardingCTLHintStatement> updateShardingCTLHintStatement = parseUpdateShardingCTLHintStatement();
        if (updateShardingCTLHintStatement.isPresent()) {
            return updateShardingCTLHintStatement;
        }
        Optional<ShardingCTLHintStatement> queryShardingCTLHintStatement = parseQueryShardingCTLHintStatement();
        if (queryShardingCTLHintStatement.isPresent()) {
            return queryShardingCTLHintStatement;
        }
        if (errorParameterMatcher.find()) {
            return Optional.of(new ShardingCTLHintStatement(new HintErrorParameterCommand()));
        }
        return Optional.empty();
    }
    
    private Optional<ShardingCTLHintStatement> parseUpdateShardingCTLHintStatement() {
        if (setMasterOnlyMatcher.find()) {
            boolean masterOnly = Boolean.parseBoolean(setMasterOnlyMatcher.group(1).toUpperCase());
            return Optional.of(new ShardingCTLHintStatement(new HintSetMasterOnlyCommand(masterOnly)));
        }
        if (setDatabaseShardingValueMatcher.find()) {
            String shardingValue = setDatabaseShardingValueMatcher.group(1);
            return Optional.of(new ShardingCTLHintStatement(new HintSetDatabaseShardingValueCommand(shardingValue)));
        }
        if (addDatabaseShardingValueMatcher.find()) {
            String logicTable = addDatabaseShardingValueMatcher.group(1);
            String shardingValue = addDatabaseShardingValueMatcher.group(2);
            return Optional.of(new ShardingCTLHintStatement(new HintAddDatabaseShardingValueCommand(logicTable, shardingValue)));
        }
        if (addTableShardingValueMatcher.find()) {
            String logicTable = addTableShardingValueMatcher.group(1);
            String shardingValue = addTableShardingValueMatcher.group(2);
            return Optional.of(new ShardingCTLHintStatement(new HintAddTableShardingValueCommand(logicTable, shardingValue)));
        }
        if (clearMatcher.find()) {
            return Optional.of(new ShardingCTLHintStatement(new HintClearCommand()));
        }
        return Optional.empty();
    }
    
    private Optional<ShardingCTLHintStatement> parseQueryShardingCTLHintStatement() {
        if (showStatusMatcher.find()) {
            return Optional.of(new ShardingCTLHintStatement(new HintShowStatusCommand()));
        }
        if (showTableStatusMatcher.find()) {
            return Optional.of(new ShardingCTLHintStatement(new HintShowTableStatusCommand()));
        }
        return Optional.empty();
    }
}
