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

package org.apache.shardingsphere.shadow.route.future.engine.determiner.algorithm;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.note.PreciseNoteShadowValue;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowAlgorithmDeterminer;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Note shadow algorithm determiner.
 */
@RequiredArgsConstructor
public final class NoteShadowAlgorithmDeterminer implements ShadowAlgorithmDeterminer {
    
    private final NoteShadowAlgorithm<Comparable<?>> noteShadowAlgorithm;
    
    @Override
    public boolean isShadow(final InsertStatementContext insertStatementContext, final Collection<String> relatedShadowTables, final String tableName) {
        Collection<PreciseNoteShadowValue<Comparable<?>>> noteShadowValues = createNoteShadowValues(insertStatementContext, tableName);
        for (PreciseNoteShadowValue<Comparable<?>> noteShadowValue : noteShadowValues) {
            if (noteShadowAlgorithm.isShadow(relatedShadowTables, noteShadowValue)) {
                return true;
            }
        }
        return false;
    }
    
    // FIXME refactor the method when sql parses the note and puts it in the statement context
    private Collection<PreciseNoteShadowValue<Comparable<?>>> createNoteShadowValues(final InsertStatementContext insertStatementContext, final String logicTableName) {
        Collection<PreciseNoteShadowValue<Comparable<?>>> result = new LinkedList<>();
        result.add(new PreciseNoteShadowValue<>(logicTableName, "/*table=t_user,shadow=true*/"));
        result.add(new PreciseNoteShadowValue<>(logicTableName, "/*aaa=bbb*/"));
        return result;
    }
}
