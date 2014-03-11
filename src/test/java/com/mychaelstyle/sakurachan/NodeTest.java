/**
 * 
 */
package com.mychaelstyle.sakurachan;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Node test
 * @author Masanori Nakashima
 */
public class NodeTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link com.mychaelstyle.sakurachan.Node}.
     */
    @Test
    public void test() {

        String host = System.getenv("TEST_HOST");
        String user = System.getenv("TEST_USER");
        String password = System.getenv("TEST_PASS");

        Node node = new Node();
        try {
            node.connect(host, user, password, null);
            node.execCommand("ls -la", null);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            node.close();
        }
    }

}
