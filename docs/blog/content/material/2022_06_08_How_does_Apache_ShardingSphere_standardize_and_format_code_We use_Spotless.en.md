+++ 
title = "How does Apache ShardingSphere standardize and format code? We use Spotless"
weight = 59
chapter = true 
+++

Why do we need to format code? Simply put, it’s to make code easier to read, understand, and modify.

As a Top-Level Apache open source project, [ShardingSphere](https://shardingsphere.apache.org/) has 400 contributors as of today. Since most developers do not have the same coding style, it is not easy to standardize the project’s overall code format in a GitHub open collaboration model. To solve this issue, ShardingSphere uses [Spotless](https://github.com/diffplug/spotless/tree/main/plugin-maven) to unify code formatting.

## What is Spotless?
Spotless is a multi-lingual code formatting tool that supports [Maven](https://maven.apache.org/) and [Gradle](https://gradle.org/) building with plugin.

Devs can use Spotless in two ways: reviewing code for format-related issues, and formatting code.

> The ShardingSphere community uses Maven to build its projects — and Spotless uses Maven for its demos.

## How to use it?
Let’s check the official demo below:

```
user@machine repo % mvn spotless:check
[ERROR]  > The following files had format violations:
[ERROR]  src\main\java\com\diffplug\gradle\spotless\FormatExtension.java
[ERROR]    -\t\t····if·(targets.length·==·0)·{
[ERROR]    +\t\tif·(targets.length·==·0)·{
[ERROR]  Run 'mvn spotless:apply' to fix these violations.
user@machine repo % mvn spotless:apply
[INFO] BUILD SUCCESS
user@machine repo % mvn spotless:check
[INFO] BUILD SUCCESS
```
When you check the project code with `mvn spotless:check`，an error occurs, then you format the code with `mvn spotless:apply`. And once you check it again, the formatting error has magically disappeared.

**1. Preparing your environment**
ShardingSphere uses Spotless to adding Java file `licenseHeader` and formatting Java code.

Spotless has several Java code formatting methods, such as: `googleJavaFormat`, `eclipse`, `prettier` etc.

For customization reasons, we chose `eclipse` for Java code formatting.
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/95sueksml8zq351xeme4.png)
 

```
**a) Add `licenseHeader` according to project requirements**

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
```
Note: remember to include a space at the end of the `licenseHeader`. Otherwise there will be no space between the `licenseHeader` and the package.

**b) Add `shardingsphereeclipseformatter.xml`**

```xml
?xml version="1.0" encoding="UTF-8" standalone="no"?>
<profiles version="13">
    <profile kind="CodeFormatterProfile" name="'ShardingSphere Apache Current'" version="13">
        <setting id="org.eclipse.jdt.core.compiler.source" value="1.8"/>
        <setting id="org.eclipse.jdt.core.compiler.compliance" value="1.8"/>
        <setting id="org.eclipse.jdt.core.compiler.codegen.targetPlatform" value="1.8"/>
        <setting id="org.eclipse.jdt.core.formatter.indent_empty_lines" value="true"/>
        <setting id="org.eclipse.jdt.core.formatter.tabulation.size" value="4"/>
        <setting id="org.eclipse.jdt.core.formatter.lineSplit" value="200"/>
        <setting id="org.eclipse.jdt.core.formatter.comment.line_length" value="200"/>
        <setting id="org.eclipse.jdt.core.formatter.tabulation.char" value="space"/>
        <setting id="org.eclipse.jdt.core.formatter.indentation.size" value="1"/>
        <setting id="org.eclipse.jdt.core.formatter.comment.format_javadoc_comments" value="false"/>
        <setting id="org.eclipse.jdt.core.formatter.join_wrapped_lines" value="false"/>
        <setting id="org.eclipse.jdt.core.formatter.insert_space_before_colon_in_conditional" value="insert"/>
        <setting id="org.eclipse.jdt.core.formatter.insert_space_before_colon_in_default" value="do not insert"/>
        <setting id="org.eclipse.jdt.core.formatter.alignment_for_enum_constants" value="16"/>
        <setting id="org.eclipse.jdt.core.formatter.insert_space_before_colon_in_labeled_statement" value="do not insert"/>
        <setting id="org.eclipse.jdt.core.formatter.insert_space_before_colon_in_case" value="do not insert"/>
        <setting id="org.eclipse.jdt.core.formatter.alignment_for_conditional_expression" value="80"/>
        <setting id="org.eclipse.jdt.core.formatter.alignment_for_assignment" value="16"/>
        <setting id="org.eclipse.jdt.core.formatter.blank_lines_after_package" value="1"/>
        <setting id="org.eclipse.jdt.core.formatter.continuation_indentation_for_array_initializer" value="2"/>
        <setting id="org.eclipse.jdt.core.formatter.alignment_for_resources_in_try" value="160"/>
        <setting id="org.eclipse.jdt.core.formatter.alignment_for_throws_clause_in_method_declaration" value="10"/>
        <setting id="org.eclipse.jdt.core.formatter.alignment_for_parameters_in_method_declaration" value="106"/>
        <setting id="org.eclipse.jdt.core.formatter.alignment_for_parameters_in_constructor_declaration" value="106"/>
        <setting id="org.eclipse.jdt.core.formatter.alignment_for_throws_clause_in_constructor_declaration" value="106"/>
        <setting id="org.eclipse.jdt.core.formatter.alignment_for_arguments_in_explicit_constructor_call.count_dependent" value="16|5|80"/>
    </profile>
</profiles>
```
For latest rules of ShardingSphere, see `[shardingsphereeclipseformatter.xml](https://github.com/apache/shardingsphere/blob/master/src/resources/shardingsphere_eclipse_formatter.xml)`. For references, check the `[eclipse-java-google-style.xml](https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml)` file.

The content of `shardingsphereeclipseformatter.xml` is tailor-made in accordance with the ShardingSphere code specification and can be changed flexibly.

**c) Add Maven plugin**

```xml
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>2.22.1</version>
    <configuration>
        <java>
            <eclipse>
                <file>${maven.multiModuleProjectDirectory}/src/resources/shardingsphere_eclipse_formatter.xml</file>
            </eclipse>
            <licenseHeader>
                <file>${maven.multiModuleProjectDirectory}/src/resources/license-header</file>
            </licenseHeader>
        </java>
    </configuration>
</plugin>
```

Spotless supports specified formatting directories and the exclusion of specified directories. For further information, see `plugin-maven#java`. If not specified, when check or apply is executed, all of the project code will be affected by default.

**d) Execute code formatting**

After performing the above three steps, you can execute commands in your project to check the Java code for compliance with the specification, as well as the code formatting features.

```
user@machine repo % mvn spotless:apply
[INFO] BUILD SUCCESS
user@machine repo % mvn spotless:check
[INFO] BUILD SUCCESS
```

## 2. Binding the Maven Life Cycle
In the actual ShardingSphere application, you can opt to bind Spotless apply to the compile phase so that it is automatically formatted when mvn install is executed locally.

```xml
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>2.22.1</version>
    <configuration>
        <java>
            <eclipse>
                <file>${maven.multiModuleProjectDirectory}/src/resources/shardingsphere_eclipse_formatter.xml</file>
            </eclipse>
            <licenseHeader>
                <file>${maven.multiModuleProjectDirectory}/src/resources/license-header</file>
            </licenseHeader>
        </java>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>apply</goal>
            </goals>
            <phase>compile</phase>
        </execution>
    </executions>
</plugin>

```

## 3. IDEA formatting
If you want to check a single file for compliance when writing code, executing `mvn spotless:check` or `mvn spotless:apply` will be a bit unwieldy, as by default the formatting scope is the entire project.

IntelliJ IDEA’s native formatting functionality can be replaced by shardingsphereeclipseformatter.xml.

This way, you can format your code at any time during the writing process, improving efficiency significantly.

IDEA Version: 2019.3.4

**a) Install the plugin Eclipse Code Formatter
**
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/r8rn6kd5w19bqjeugu3n.png)
 
**b) Select shardingsphereeclipseformatter.xml as the default formatting template**
![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/e92s42k83nff4q06wavo.png)
 

Spotless code formatting can be done using IDEA code formatting shortcuts.

## FAQ
**1. Conflicts between Spotless and Checkstyle**
[Checkstyle](https://github.com/checkstyle/checkstyle) is a tool for checking Java source code for compliance with code standards or a set of validation rules (best practices).

In extreme circumstances, Spotless formatted code cannot pass Checkstyle checking.

The underlying cause is a conflict between the checking mechanism and formatting configurations set by both. For example, Spotless formats a newline with a 16-space indent, while Checkstyle checks for a 12-space newline.

```java
private static Collection<PreciseHintShadowValue<Comparable<?>>> createNoteShadowValues(final ShadowDetermineCondition shadowDetermineCondition) {
    // format that can pass Checkstyle
    return shadowDetermineCondition.getSqlComments().stream().<PreciseHintShadowValue<Comparable<?>>>map(
        each -> new PreciseHintShadowValue<>(tableName, shadowOperationType, each)).collect(Collectors.toList());
    // After being formatted by Spotless
    return shadowDetermineCondition.getSqlComments().stream().<PreciseHintShadowValue<Comparable<?>>>map(
           each -> new PreciseHintShadowValue<>(tableName, shadowOperationType, each)).collect(Collectors.toList());
}
```
This case requires devs to weigh the trade-offs. There are two solutions: modify Spotless’ formatting rules, or modify Checkstyle’s checking rules.

**2. Formatting conflict between CRLF & LF**

See [https://github.com/diffplug/spotless/issues/1171](https://github.com/diffplug/spotless/issues/1171)

## Summary
Apache ShardingSphere uses Spotless to format legacy code, and the subsequent standardization of code formatting, which helps keeping the project’s code tidy.

Of course, Spotless is not limited to Java code formatting, but also includes the formatting of file types such as `Pom` and `Markdown`, which will soon be applied in ShardingSphere.

## Author
**Longtai**

[Github ID](https://github.com/longtai-cn): longtai-cn

[Apache ShardingSphere](https://shardingsphere.apache.org/) Contributor; hippo4j author (2.2K GitHub stars).
