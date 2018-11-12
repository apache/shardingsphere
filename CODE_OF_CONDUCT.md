# Contributor Covenant Code of Conduct

## Development Concept

 - Write codes with heart. Pursue clean, simplified and extremely elegant codes. Agree with concepts in &lt;Refactoring: Improving the Design of Existing Code&gt; and &lt;Clean Code: A Handbook of Agile Software Craftsmanship&gt;.
 - Be familiar with codes already had, to keep consistent with the style and use.
 - Highly reusable, no duplicated codes or configurations.
 - Delete codes out of use in time.

## Contributor Covenant Submitting of Conduct

 - Make sure all the test cases are passed, Make sure `mvn clean install` can be compiled and tested successfully.
 - Make sure the test coverage rate is not lower than the dev branch.
 - Make sure to check codes with Checkstyle. codes that violate check rules should have special reasons. Find checkstyle template from `sharding-sphere/src/resources/sharding_checks.xml`, please use checkstyle `8.8` to run the rules.

## Contributor Covenant Code of Conduct

 - Use linux line separators.
 - Keep indents (including blank lines) consistent with the previous one.
 - Keep one blank line after class definition.
 - No meaningless blank lines.
 - Use meaningful class, method and variable names, avoid to use abbreviate. 
 - Return values are named with `result`; Variables in the loop structure are named with `each`; Replace `each` with `entry` in map.
 - Exceptions when catch are named with `ex`; Exceptions when catch but do nothing are named with `ignored`.
 - Name property files with camel-case and lowercase first letters.
 - Have constants on the left and variable on the right in `=` and `equals` conditional expressions; Have variable on the left and constants on the right in `greater than` and `less than` conditional expressions.
 - Use `LinkedList`  in priority. Use `ArrayList` for use index to get element only.
 - Use capacity based `Collection` such as `ArrayList`, `HashMap` must indicate initial capacity to avoid recalculate capacity.
 - Design class as `final` class expect abstract class for extend.
 - Make nested loop structures a new method.
 - Use guard clauses in priority.
 - Minimize the access permission for classes and methods.
 - Private method should be just next to the method in which it is used; writing private methods should be in the same as the appearance order of private methods.
 - No `null` parameters or return values.
 - Split codes that need to add notes with it into small methods, which are explained with method names.
 - Replace constructors, getters, setter methods and log variable with lombok in priority.
 - Use English in all the logs and javadoc.
 - Include Javadoc, todo and fixme only in the comments.
 - Only `public` classes and methods need javadoc, other methods, classes and override methods do not need javadoc.

## Contributor Covenant Unit Test of Conduct

 - Test codes and production codes should follow the same kind of code of conduct.
 - Without particular reasons, test cases should be fully covered.
 - Environment preparation codes should be separate from test codes.
 - Only those that relate to junit `Assert`, hamcrest `CoreMatchers` and `Mockito` can use static import.
 - For single parameter asserts, `assertTrue`, `assertFalse`, `assertNull` and `assertNotNull` should be used.
 - For multiple parameter asserts, `assertThat` should be used.
 - For accurate asserts, try not to use `not`, `containsString` to make assertions.
 - Actual values of test cases should be named `actualXXX`, expected values `expectedXXX`.
 - Class for test case and `@Test` annotation do not need javadoc.
