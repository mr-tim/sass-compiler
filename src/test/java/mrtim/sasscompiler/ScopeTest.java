package mrtim.sasscompiler;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScopeTest {

    @Test
    public void testValueUndefinedInBlankRootScope() {
        Scope scope = new Scope();
        assertFalse("Expected variable to be undefined in blank root scope", scope.isDefined("foo"));
    }

    @Test
    public void testValueDefinedInRootScope() {
        Scope rootScope = new Scope();
        Scope scope = new Scope(rootScope);
        rootScope.define("foo", "bar");
        assertTrue("Expected variable to be defined in root scope", rootScope.isDefined("foo"));
        assertTrue("Expected variable to be define in child scope", scope.isDefined("foo"));
    }

    @Test
    public void testNewChildDefinitionsDontTouchRootScope() {
        Scope rootScope = new Scope();
        Scope childScope = new Scope();
        childScope.define("name", "tim");
        assertTrue("Expected variable to be defined in child scope", childScope.isDefined("name"));
        assertFalse("Variable should not be define in root scope", rootScope.isDefined("name"));
    }

    @Test
    public void testChildDefinitionsDontOverrideRootScope() {
        Scope rootScope = new Scope();
        rootScope.define("name", "tim");
        Scope childScope = new Scope();
        childScope.define("name", "bob");
        assertTrue("Expected variable to be define in child scope", childScope.isDefined("name"));
        assertTrue("Expected variable to be defined in root scope", rootScope.isDefined("name"));
        assertEquals("Expected variable in root scope to be unchanged when redefined in child scope", "tim", rootScope.get("name"));
        assertEquals("Incorrect variable value in child scope", "bob", childScope.get("name"));
    }


}
