+++
title = "Code of Conduct" 
weight = 2
chapter = true
+++

The following code of conduct is based on full compliance with [ASF CODE OF CONDUCT](https://www.apache.org/foundation/policies/conduct.html).

## Development Guidelines

 - Write codes with heart. Pursue clean, simplified and extremely elegant codes. Agree with concepts in &lt;Refactoring: Improving the Design of Existing Code&gt; and &lt;Clean Code: A Handbook of Agile Software Craftsmanship&gt;.
 - Be familiar with codes already had, to keep consistent with the style and use.
 - Highly reusable, no duplicated codes or configurations.
 - Delete codes out of use in time.

## Contributor Covenant Submitting of Conduct

 - Conform to `Contributor Covenant Code of Conduct` below.
 - Make sure Maven build process success. Run `./mvnw clean install -B -T1C -Dmaven.javadoc.skip -Dmaven.jacoco.skip -e` command in shell to start Maven build process.
 - Make sure the test coverage rate is not lower than the master branch.
 - Careful consideration for each `pull request`; Small and frequent `pull request` with complete unit function is welcomed.
 - Through the uniform code style of spotless, execute the `mvn spotless:apply` formatted code.
 - If using IDEA, you can import the recommended `src/resources/code-style-idea.xml`.

## Contributor Covenant Code of Conduct

 - Use linux line separators.
 - No meaningless blank lines. Please extract private methods to instead of blank lines if too long method body or different logic code fragments.
 - Use meaningful class, method and variable names, class and method name avoid to use abbreviation. Some variables could use abbreviation.
   - Variable `arguments` could abbreviate to `args`;
   - Variable `parameters` could abbreviate to `params`;
   - Variable `environment` could abbreviate to `env`;
   - Variable `properties` could abbreviate to `props`;
   - Variable `configuration` could abbreviate to `config`.
 - Abbreviation composed less than 3 characters should be uppercase, more than 3 characters must use camel case naming rule.
   - Example for abbreviation composed less than 3 characters: SQL92Lexer, XMLTransfer, MySQLAdminExecutorCreator;
   - Example for abbreviation composed more than 3 characters: JdbcUrlAppender, YamlAgentConfigurationSwapper;
   - A variable composed of abbreviation should use lower camel case: mysqlAuthenticationMethod, sqlStatement, mysqlConfig.
 - Except return an input parameter as result, returning variable should be named with `result`; Variables in the loop structure are named with `each`; Replace `each` with `entry` in map.
 - Exceptions when catch are named with `ex`; Exceptions when catch but do nothing are named with `ignored`.
 - Name property files with `Spinal Case`(a variant of `Snake Case` which uses hyphens `-` to separate words). 
 - Split codes that need to add notes with it into small methods, which are explained with method names.
 - Have constants on the left and variable on the right in `=` and `equals` conditional expressions; Have variable on the left and constants on the right in `greater than` and `less than` conditional expressions.
 - Beside using same names as input parameters and global fields in assign statement, avoid using `this` modifier.
 - Design class as `final` class except abstract class for extend.
 - Make nested loop structures a new method.
 - Order of members definition and parameters should be consistent during classes and methods.
 - Use guard clauses in priority.
 - Minimize the access permission for classes and methods.
 - Private method should be just next to the method in which it is used; Multiple private methods should be in the same as the appearance order of original methods.
 - No `null` parameters or return values.
 - Replace constructors, getters, setter methods and log variable with lombok in priority.
 - Use `LinkedList`  in priority. Use `ArrayList` for use index to get element only.
 - Use capacity based `Collection` such as `ArrayList`, `HashMap` must indicate initial capacity to avoid recalculate capacity.
 - Use English in all the logs and javadoc.
 - Include Javadoc, todo and fixme only in the comments.
 - Only `public` classes and methods need javadoc, the Javadoc for the user API and SPI needs to be written clearly and comprehensively, other methods, classes and override methods do not need javadoc.
 - Replace if else return and assign statement with ternary operator in priority.
 - Nested using ternary operator is forbidden.
 - Use forward semantics in priority for better understanding code logic in conditional expressions. For example: `if (null == param) {} else {}`.
 - Use concentrate `@SuppressWarnings("xxx")` instead of `@SuppressWarnings("all")`.
 - Avoid using Java Stream in hot methods, unless the performance of using Stream is better than using loop in that situation.
 - Utility class should be named in the form of `xxUtils`.

## Contributor Covenant Unit Test of Conduct

 - Test codes and production codes should follow the same kind of code of conduct.
 - Unit test should follow AIR (Automatic, Independent, Repeatable) principle.
   - Automatic: Unit test should run automatically, not interactively. Check test result manually and `System.out`, `log` are prohibited, use assert to check test results.
   - Independent: Call each other and sequence dependency during unit test cases are prohibited. Every test case should run independent.
   - Repeatable: Unit test case should not dependency external environment, they can run repeatable.
 - Unit test should follow BCDE (Border, Correct, Design, Error) design principle.
   - Border: Border value test, test for loop border, special value and value sequence to get expect result.
   - Correct: Correct value test, test for correct value to get expect result.
   - Design: Design with production codes.
   - Error: Error value test, test for error input, exception to get expect result.
 - Without particular reasons, test cases should be fully covered.
 - Test cases should be fully covered expect simply `getter /setter` methods, and declared static codes of SPI, such as: `getType / getOrder`.
 - Every test case need precised assertion, try not to use `not`, `containsString` to make assertions.
 - Environment preparation codes should be separate from test codes.
 - Only those that relate to `Mockito`, junit `Assertions`, hamcrest `CoreMatchers` and `MatcherAssert` can use static import.
 - Usage of assertion methods: for boolean type asserts, `assertTrue` and `assertFalse` should be used, `assertNull` and `assertNotNull` should be used to assert whether the assertion is null, and `assertThat` should be used for other scenarios.
 - Usage of assertion methods:
   - Use `assertTrue` and `assertFalse` for boolean value;
   - Use `assertNull` and `assertNotNull` for null value;
   - Use `assertThat` for other values.
 - Actual values of test cases should be named `actualXXX`, expected values `expectedXXX`.
 - Class for test case and `@Test` annotation do not need javadoc.
 - Mockito `mockStatic` and `mockConstruction` methods must be used with try-with-resource or closed in the teardown method to avoid leaks.
 - Using `mock` should follow following specifications:
   - Using `mock` when unit tests need to connect to an environment;
   - Using `mock` when unit tests contain objects that are not easy to build, for example, objects that are more than two layers nested and unrelated to the test.
   - When mocking static methods or constructors, it is recommended to use the testing framework's `AutoMockExtension` and `StaticMockSettings` to release resources automatically; If using Mockito's `mockStatic` and `mockConstruction` methods, please use `try-with-resource` or close them in the cleanup method to avoid resource leaks.
   - When verifying only one invocation, there is no need to use `times(1)` parameter, please use the single-argument method of `verify`.

## Contributor Covenant G4 of Conduct

 - Common Conduct
   - Every line cannot over `200` chars, guarantee every line have complete semantics.
 - Lexer Conduct
   - Every rule should be in single line, no empty line between rules.
   - Rule of lexer name should capitalization. If name composite with more than one word, use `underline` to separate. Rule name of `DataType` and `Symbol` should end with `underline`. If rule name is conflicted with ANTLR's keyword, should take an `underline` behind rule name.
   - For private rule in lexer should use `fragment`, rule with `fragment` should define behind of public rule which they served.
   - Common rule of lexer should put in file `Keyword.g4`, every database may have customized rule file by themselves. For example: `MySQLKeyword.g4`.
 - Parser Conduct
   - After every rule finish, blank line should no indents.
   - No space before rule name definition. One space between `colon` and rule, `semicolon` should take a new line and keep indents (including blank lines) consistent with the previous one.
   - If a rule's branch is over than `5`, every branch take a new line.
   - Rule name of parser should same with java variable's camel case.
   - Define separate files for every SQL type, file name should consist of `database` + `SQL type` + `Statement`. For example: `MySQLDQLStatement.g4`.

## GitHub Action of Conduct

- Workflow file name must end with `.yml`.
- Workflow file name must consist with the lowercase of `triggerType-actionType`, for example: `nightly-check.yml`. Omit trigger type for pull_request, for example: `check.yml`.
- Trigger type includes: pull_request (without prefix), nightly.
- Action type includes: check, ci, e2e, build.
- `name` property in workflow file should be same with file name, Words separated by `-`, add space between `-` and words, first letter of every word should be capital, for example: `Nightly - Check`.
- `job` property in workflow should be unique in that workflow file.
- When using `matrix` property, must add job parallelism limit to 5: `max-parallel: 5`.
- Must set timeout for job, max timeout is 1 hour. For example: `timeout-minutes: 10`.
