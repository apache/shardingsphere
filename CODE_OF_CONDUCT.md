# Contributor Covenant Code of Conduct

The following code of conduct is based on full compliance with the [Apache Software Foundation Code of Conduct](https://www.apache.org/foundation/policies/conduct.html).

## Development Philosophy

- **Dedication** Maintain responsibility and reverence, continuously crafting with artisanal spirit.
- **Readability** Code should be unambiguous, revealing its intent through reading rather than debugging.
- **Cleanliness** Embrace the concepts from "Refactoring" and "Clean Code", pursuing clean and elegant code.
- **Consistency** Maintain complete consistency in code style, naming, and usage patterns.
- **Simplicity** Minimalist code, expressing the most correct meaning with the least code. Highly reusable, with no duplicate code or configuration. Delete unused code promptly.
- **Abstraction** Clear hierarchy division and reasonable concept extraction. Keep methods, classes, packages, and modules at the same abstraction level.
- **Excellence** Reject randomness, ensuring every line of code, every letter, and every space has its existential value.

## Code Submission Guidelines

- Ensure compliance with coding standards.
- Ensure all steps in the build process complete successfully, including: Apache license header check, Checkstyle check, compilation, unit tests, etc. Build process command: `./mvnw clean install -B -T1C -Pcheck`.
- Unify code style through Spotless, execute `./mvnw spotless:apply -Pcheck` to format code.
- Ensure coverage is not lower than the master branch.
- Try to refine design with fine-grained splitting; achieve small modifications with multiple commits, but ensure the completeness of each commit.
- If you use IDEA, you can import `src/resources/idea/code-style.xml` to maintain code style consistency.
- If you use IDEA, you can import `src/resources/idea/inspections.xml` to detect potential code issues.

## Coding Standards

- Use Linux line endings.
- No line breaks are needed if each line of code does not exceed 200 characters.
- There should be no meaningless blank lines. Please extract private methods instead of using blank line spacing for overly long method bodies or logically closed code segments.
- Naming conventions:
   - Naming should be self-explanatory.
   - Class and method names should avoid abbreviations, some variable names can use abbreviations.
      - Variable name `arguments` abbreviated as `args`;
      - Variable name `parameters` abbreviated as `params`;
      - Variable name `environment` abbreviated as `env`;
      - Variable name `properties` abbreviated as `props`;
      - Variable name `configuration` abbreviated as `config`.
   - Proper noun abbreviations of three characters or less use uppercase, abbreviations over three characters use camelCase.
      - Examples of class and method name abbreviations with three characters or less: SQL92Lexer, XMLTransfer, MySQLAdminExecutorCreator;
      - Examples of class and method name abbreviations over three characters: JdbcUrlAppender, YamlAgentConfigurationSwapper;
      - Variables should use lowercase camelCase: mysqlAuthenticationMethod, sqlStatement, mysqlConfig.
   - Local variables meeting the following conditions should be named according to these rules:
      - Except for directly returning method parameters, return variables should be named `result`;
      - Use `each` to name loop variables in loops;
      - Use `entry` instead of `each` in maps;
      - Captured exception names should be named `ex`;
      - When capturing exceptions and doing nothing, the exception name should be named `ignored`.
   - Method parameter names are forbidden from using `result`, `each`, `entry`.
   - Utility class names should be named `xxUtils`.
   - Configuration files use `Spinal Case` naming (a special `Snake Case` that uses `-` to separate words).
- Code that needs comments to explain should be extracted into small methods, using method names for explanation.
- In `equals` and `==` conditional expressions, constants on the left, variables on the right; in conditional expressions like greater than or less than, variables on the left, constants on the right.
- Avoid using `this` modifier except for assignment statements where constructor parameters have the same name as global variables.
- Local variables should not be set as final.
- Try to design classes as `final` except for abstract classes used for inheritance.
- Nested loops should be extracted into methods.
- The order of member variable definitions and parameter passing should remain consistent across all classes and methods.
- Prefer guard clauses.
- Access control for classes and methods should be minimal.
- Private methods used by a method should immediately follow that method. If there are multiple private methods, they should be written in the same order as they appear in the original method.
- Method parameters and return values are not allowed to be `null`.
- Prefer using lombok instead of constructors, getter, setter methods and log variables.
- Do not leave fully-qualified class names inline; add import statements instead.
- Consider using `LinkedList` first, only use `ArrayList` when you need to get element values from the collection by index.
- Collection types that may cause expansion like `ArrayList`, `HashMap` must specify initial collection size to avoid expansion.
- Prefer using ternary operators instead of if else return and assignment statements.
- Nested use of ternary operators is forbidden.
- In conditional expressions, prefer positive semantics for easier code logic understanding. For example: `if (null == param) {} else {}`.
- Use specific `@SuppressWarnings("xxx")` instead of `@SuppressWarnings("all")`.
- Use `@HighFrequencyInvocation` annotation reasonably to focus on performance optimization of key methods.
   - When to use `@HighFrequencyInvocation` annotation:
      - In frequently called request chains, mark the high-frequency called classes, methods or constructors, with precise matching of scope;
      - When the `canBeCached` attribute is `true`, it indicates the target is a reusable cache resource, for example: database connections.
   - Code segments marked with `@HighFrequencyInvocation` must strictly guarantee code performance, the following are prohibited items in marked code segments:
      - Forbidden to call Java Stream API;
      - Forbidden to concatenate strings through `+`;
      - Forbidden to call LinkedList's `get(int index)` method.
- Comments & Logging standards:
   - Logs and comments must be in English.
   - Comments can only contain JAVADOC, TODO and FIXME.
   - Public classes and methods must have JAVADOC. JAVADOC for user-facing APIs and SPIs needs to be clear and comprehensive. Other classes, methods, and methods overriding parent classes do not need JAVADOC.

## Unit Testing Standards

- Test code and production code need to follow the same coding standards.
- Unit tests need to follow the AIR (Automatic, Independent, Repeatable) design philosophy.
   - Automatic: Unit tests should be fully automated, not interactive. Manual inspection of output results is forbidden, use of `System.out`, `log`, etc. is not allowed, assertions must be used for verification.
   - Independent: Forbid mutual calls between unit test cases, forbid dependency on execution order. Each unit test can run independently.
   - Repeatable: Unit tests cannot be affected by the external environment and can be executed repeatedly.
- Unit tests need to follow the BCDE (Border, Correct, Design, Error) design principles.
   - Border testing: Get expected results through boundary inputs such as loop boundaries, special values, data order, etc.
   - Correctness testing: Get expected results through correct inputs.
   - Reasonable design: Combined with production code design, design high-quality unit tests.
   - Error tolerance testing: Get expected results through incorrect inputs such as illegal data, exception flows, etc.
- Use `assert` prefix for all test method names.
- Unit tests must exercise behavior through public APIs only; do not use reflection or other means to access private members.
- Except for simple `getter /setter` methods, unit tests need full coverage.
- When a production method is covered by only one test case, name that test method `assert<MethodName>` without extra suffixes, and prefer isolating one public production method per dedicated test method; when practical, keep test method ordering aligned with the corresponding production methods.
- For parameterized tests, provide display names via parameters and prefix each with `{index}:` to include the sequence number.
- Each test case needs precise assertions, try not to use `not`, `containsString` assertions.
- Separate environment preparation code from test code.
- Only Mockito, junit `Assertions`, hamcrest `CoreMatchers` and `MatcherAssert` related can use static import.
- Data assertion standards should follow:
   - Boolean type assertions should use `assertTrue` and `assertFalse`;
   - Null value assertions should use `assertNull` and `assertNotNull`;
   - Other types should use `assertThat(actual, is(expected))` instead of `assertEquals`;
   - Use `assertThat(..., isA(...))` instead of `instanceOf`;
   - Do not use `assertSame` / `assertNotSame`; use instead of `assertThat(actual, is(expected))` or `assertThat(actual, not(expected))`;
   - Use Hamcrest matchers like `is()`, `not()` for precise and readable assertions.
- The actual values in test cases should be named actual XXX, and expected values should be named expected XXX.
- Test classes and methods marked with `@Test` do not need JAVADOC.
- Using `mock` should follow the following specifications:
   - When unit tests need to connect to a certain environment, `mock` should be used;
   - When unit tests contain objects that are not easy to construct, for example: objects with more than two levels of nesting and unrelated to testing, `mock` should be used.
   - For mocking static methods or constructors, consider using `AutoMockExtension` and `StaticMockSettings` provided by the testing framework for automatic resource release; if using Mockito's `mockStatic` and `mockConstruction` methods, must be paired with `try-with-resource` or closed in cleanup methods to avoid leaks.
   - When verifying only one call, there's no need to use `times(1)` parameter, the single-parameter method of `verify` is sufficient.
- For deep chained interactions, use Mockito’s `RETURNS_DEEP_STUBS` instead of layering intermediate mocks.
- Test data should use standardized prefixes (e.g., `foo_`/`bar_`) to clearly identify their test purpose
- Use `PropertiesBuilder` simplify `Properties` building.

## SQL Parsing Standards

### Maintenance Standards

- The `G4` grammar files and `SQLVisitor` implementation classes involved in the SQL parsing module need to be marked with differential code according to the following database relationships. When database A does not provide corresponding database drivers and protocols, but directly uses database B's drivers and protocols, database A can be considered a branch database of database B.
  Usually branch databases will directly use the SQL parsing logic of the trunk database, but to adapt to the unique syntax of branch databases, some branch databases will copy from the trunk database and maintain their own SQL parsing logic. At this time, for the unique syntax of branch databases, comments need to be used for marking, and other parts need to be consistent with the implementation of the trunk database;

  | Trunk Database | Branch Database |
    |----------------|-----------------|
  | MySQL          | MariaDB, Doris  |
  | PostgreSQL     | -               |
  | openGauss      | -               |
  | Oracle         | -               |
  | SQLServer      | -               |
  | ClickHouse     | -               |
  | Hive           | -               |
  | Presto         | -               |
  | SQL92          | -               |

- Differential code marking syntax, replace `{DatabaseType}` with the database type uppercase name when adding, for example: `DORIS`.
   - Add syntax: `// {DatabaseType} ADDED BEGIN` and `// {DatabaseType} ADDED END`;
   - Modify syntax: `// {DatabaseType} CHANGED BEGIN` and `// {DatabaseType} CHANGED END`.

### G4 Standards

- Lexical parsing specifications
   - Each rule on one line, no blank lines needed between rules.
   - Rule names use uppercase letters. If the name consists of multiple words, use `underscore` separation. `DataType` and `Symbol` rule names end with `underscore`. Rules with the same name as ANTLR built-in variables or keywords add `underscore` at the end for distinction.
   - Rules not exposed externally use `fragment`, `fragment` defined rules need to be declared after the rules they serve.
   - Common rule definitions are placed in `Keyword.g4`, each database can have its own specific rule definitions. For example: `MySQLKeyword.g4`.
- Syntax parsing specifications
   - Leave a blank line after each rule, blank lines do not need indentation.
   - No space before the rule name, space after `colon` before starting to write the rule, `semicolon` on a separate line and maintain the same indentation as the previous line.
   - If a rule has more than `5` branches, each branch should be on a separate line.
   - Rule naming uses Java variable camelCase form.
   - Define an independent grammar file for each SQL statement type, file name consists of `database name` + `statement type name` + `Statement`. For example: `MySQLDQLStatement.g4`.

## GitHub Action Standards

- Workflow file names end with `.yml`.
- Workflow file names consist of lowercase letters of `trigger method-execution operation`. For example: `nightly-check.yml`. pull_request triggered tasks omit the trigger method, for example: `check.yml`.
- Trigger methods include: pull_request (no prefix), nightly, schedule.
- Execution operations include: check, ci, e2e, build, report.
- The `name` attribute naming in Workflow files should be consistent with the file name, words separated by `-` with spaces on both sides of the separator, and the first letter of each word capitalized. For example: `Nightly - Check`.
- The `name` attribute under Step should describe the function of the step, with the first letter of each word capitalized and prepositions in lowercase. For example: `Build Project with Maven`.
- The `job` attribute naming in Workflow must be unique within the Workflow.
- When using `matrix`, you must add job parallelism limit of 20. For example: `max-parallel: 20`.
- Must set timeout for jobs, maximum not exceeding 1 hour. For example: `timeout-minutes: 10`.
