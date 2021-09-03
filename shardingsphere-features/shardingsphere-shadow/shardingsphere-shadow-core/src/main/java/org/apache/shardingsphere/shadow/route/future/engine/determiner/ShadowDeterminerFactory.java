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

package org.apache.shardingsphere.shadow.route.future.engine.determiner;

import org.apache.shardingsphere.shadow.algorithm.shadow.ShadowAlgorithmException;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.algorithm.ColumnShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.algorithm.NoteShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.table.AnyAlgorithmApplicableShadowTableDeterminer;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shadow determiner factory.
 */
public final class ShadowDeterminerFactory {
    
    /**
     * Create new instance of shadow table determiner.
     *
     * @param tableName table name
     * @param shadowRule shadow rule
     * @return new instance of shadow table determiner
     */
    public static Optional<ShadowTableDeterminer> getShadowTableDeterminer(final String tableName, final ShadowRule shadowRule) {
        return shadowRule.getRelatedShadowAlgorithms(tableName).map(shadowAlgorithms -> new AnyAlgorithmApplicableShadowTableDeterminer(createShadowAlgorithmDeterminers(shadowAlgorithms)));
    }
    
    private static Collection<ShadowAlgorithmDeterminer> createShadowAlgorithmDeterminers(final Collection<ShadowAlgorithm> shadowAlgorithms) {
        return shadowAlgorithms.stream().map(ShadowDeterminerFactory::getShadowAlgorithmDeterminer).collect(Collectors.toCollection(LinkedList::new));
    }
    
    /**
     * Create new instance of Shadow algorithm determiner.
     *
     * @param shadowAlgorithm shadow algorithm.
     * @return new instance of Shadow algorithm determiner
     */
    @SuppressWarnings(value = "unchecked")
    public static ShadowAlgorithmDeterminer getShadowAlgorithmDeterminer(final ShadowAlgorithm shadowAlgorithm) {
        if (shadowAlgorithm instanceof NoteShadowAlgorithm) {
            return new NoteShadowAlgorithmDeterminer((NoteShadowAlgorithm<Comparable<?>>) shadowAlgorithm);
        } else if (shadowAlgorithm instanceof ColumnShadowAlgorithm) {
            return new ColumnShadowAlgorithmDeterminer((ColumnShadowAlgorithm<Comparable<?>>) shadowAlgorithm);
        } else {
            throw new ShadowAlgorithmException("Shadow algorithm determiner factory new instance failed shadow algorithm type is `%s`.", shadowAlgorithm.getType());
        }
    }
}
