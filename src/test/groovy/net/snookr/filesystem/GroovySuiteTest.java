/*
 * This Just Make Netbeans invoke the Groovy Tests
 */
package net.snookr.filesystem;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TraversalMemoryTest.class
})
public class GroovySuiteTest {
}

