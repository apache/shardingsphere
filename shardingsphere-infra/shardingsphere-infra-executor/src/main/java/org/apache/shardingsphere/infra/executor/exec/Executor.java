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

package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

/**
 * An instance of <code>Executor</code> the implementation of physical rational 
 * operator {@link org.apache.shardingsphere.infra.optimize.rel.physical.SSRel}.
 */
public interface Executor extends Enumerator<Row>, Iterable<Row> {
    
    /**
     * get the meta data of this <code>Executor</code>.
     * @return meta data instance.
     */
    QueryResultMetaData getMetaData();
    
    /**
     * Initialize this Executor instance. This method should be invoked before {@link #moveNext()} method is invoked.
     */
    void init();
    
    /**
     * Whether this <code>Executor</code> instance has been initialized. In other word, 
     * if the {@link #init()} method has been invoked or not.
     * @return true, if this instance has been initialized, or false.
     */
    boolean isInited();
    
}
