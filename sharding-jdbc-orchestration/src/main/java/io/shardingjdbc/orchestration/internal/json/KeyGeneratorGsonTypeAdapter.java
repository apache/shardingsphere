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
import io.shardingjdbc.core.keygen.KeyGenerator;
import io.shardingjdbc.core.keygen.KeyGeneratorFactory;

import java.io.IOException;

/**
 * Key generator gson type adapter.
 *
 * @author zhangliang
 */
public final class KeyGeneratorGsonTypeAdapter extends TypeAdapter<KeyGenerator> {
    
    @Override
    public KeyGenerator read(final JsonReader in) throws IOException {
        String keyGeneratorClassName = null;
        in.beginObject();
        while (in.hasNext()) {
            Preconditions.checkArgument("keyGenerator".equals(in.nextName()));
            keyGeneratorClassName = in.nextString();
        }
        in.endObject();
        return Strings.isNullOrEmpty(keyGeneratorClassName) ? null : KeyGeneratorFactory.newInstance(keyGeneratorClassName);
    }
    
    @Override
    public void write(final JsonWriter out, final KeyGenerator value) throws IOException {
        out.beginObject();
        if (null != value) {
            out.name("keyGenerator").value(value.getClass().getName());
        }
        out.endObject();
    }
}
