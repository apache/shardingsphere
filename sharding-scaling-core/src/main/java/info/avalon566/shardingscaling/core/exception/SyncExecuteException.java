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

package info.avalon566.shardingscaling.core.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Sync execute exception.
 *
 * @author avalon566
 */
public class SyncExecuteException extends Exception {

    private final List<Throwable> aggregatedExceptions = new ArrayList<>();

    /**
     * add exception to list.
     *
     * @param throwable exception
     */
    public void addException(final Throwable throwable) {
        aggregatedExceptions.add(throwable);
    }

    @Override
    public String toString() {
        StringBuilder exceptionString = new StringBuilder();
        for (Throwable exception : aggregatedExceptions) {
            exceptionString.append(exception.toString());
        }
        return exceptionString.toString();
    }
}
