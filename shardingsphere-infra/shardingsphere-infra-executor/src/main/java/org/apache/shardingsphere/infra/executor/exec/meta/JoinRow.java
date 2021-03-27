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

package org.apache.shardingsphere.infra.executor.exec.meta;

import java.util.Objects;

public final class JoinRow extends Row {
    
    private Row left;
    
    private Row right;
    
    public JoinRow(final Row left, final Row right) {
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
    }
    
    @Override
    protected <T> T getValueByColumn(final int column) {
        if (column <= left.length()) {
            return left.getValueByColumn(column);
        }
        return right.getValueByColumn(column - left.length());
    }
    
    @Override
    public Object[] getColumnValues() {
        Object[] row = new Object[left.length() + right.length()];
        int idx = 0;
        for (Object val : left.getColumnValues()) {
            row[idx++] = val;
        }
        for (Object val : right.getColumnValues()) {
            row[idx++] = val;
        }
        return row;
    }
    
    @Override
    public int length() {
        return left.length() + right.length();
    }
}
