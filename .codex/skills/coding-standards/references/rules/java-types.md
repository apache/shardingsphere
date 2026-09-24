<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Java Type Rules

Apply these rules when adding or changing a Java type declaration or its type-level annotations.

- Do not use `lombok.Data` or `lombok.Value`, whether referenced through an import or by a fully qualified annotation name.
- Do not report an annotation named `Value` from another package as a violation of the Lombok prohibition.
- Use only the narrow Lombok annotations whose generated members are required.
- Resolve the annotation type before deciding whether a rule applies; text searches and regular expressions are candidate discovery only.
- Apply the annotation rules whether the annotation is imported or fully qualified.
- A naming or compatibility contract does not permit `lombok.Data` or `lombok.Value`.
