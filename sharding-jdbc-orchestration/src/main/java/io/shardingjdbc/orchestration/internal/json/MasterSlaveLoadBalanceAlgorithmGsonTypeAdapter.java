/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.internal.json;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.shardingjdbc.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithm;
import io.shardingjdbc.core.exception.ShardingJdbcException;

import java.io.IOException;

/**
 * Master-slave load balance algorithm gson type adapter.
 *
 * @author zhangliang
 */
public final class MasterSlaveLoadBalanceAlgorithmGsonTypeAdapter extends TypeAdapter<MasterSlaveLoadBalanceAlgorithm> {
    
    @Override
    public MasterSlaveLoadBalanceAlgorithm read(final JsonReader in) throws IOException {
        String masterSlaveLoadBalanceAlgorithmClassName = null;
        in.beginObject();
        while (in.hasNext()) {
            Preconditions.checkArgument("masterSlaveLoadBalanceAlgorithm".equals(in.nextName()));
            masterSlaveLoadBalanceAlgorithmClassName = in.nextString();
        }
        in.endObject();
        try {
            return Strings.isNullOrEmpty(masterSlaveLoadBalanceAlgorithmClassName) ? null : (MasterSlaveLoadBalanceAlgorithm) Class.forName(masterSlaveLoadBalanceAlgorithmClassName).newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
    
    @Override
    public void write(final JsonWriter out, final MasterSlaveLoadBalanceAlgorithm value) throws IOException {
        out.beginObject();
        if (null != value) {
            out.name("masterSlaveLoadBalanceAlgorithm").value(value.getClass().getName());
        }
        out.endObject();
    }
}
