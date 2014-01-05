package mrtim.sasscompiler;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class SelectorStackTest {

    @Test
    public void testSimpleSelectorsJoined() {
        SelectorStack stack = new SelectorStack();

        stack.push(asList("div"));
        stack.push(asList("#home"));
        stack.push(asList(".something"));

        assertEquals("Selectors joined incorrectly", "div #home .something", stack.expandAndJoin());
    }

    @Test
    public void testSelectorsCombinedCorrectly() {
        SelectorStack stack = new SelectorStack();

        stack.push(asList("div"));
        stack.push(asList("#home", "#about"));
        stack.push(asList(".something", ".anotherThing"));

        assertEquals("Selectors combined incorrectly", "div #home .something, div #home .anotherThing, div #about .something, div #about .anotherThing", stack.expandAndJoin());
    }

    @Test
    public void testSingleBackRef() {
        SelectorStack stack = new SelectorStack();
        stack.push(asList("div", "a", "p"));
        stack.push(asList("&.invisible"));

        assertEquals("Single back-ref in selector stack not expanded correctly", "div.invisible, a.invisible, p.invisible", stack.expandAndJoin());
    }

    @Test
    public void testMultipleBackRefs() {
        SelectorStack stack = new SelectorStack();
        stack.push(asList("div", "a", "p"));
        stack.push(asList("&.invisible", ".foo", ".bar"));
        stack.push(asList(".baz", ".quux"));

        assertEquals("Multiple back-refs in selector stack not expanded correctly",
                "div.invisible .baz, div.invisible .quux, div .foo .baz, div .foo .quux, div .bar .baz, div .bar .quux, "
                + "a.invisible .baz, a.invisible .quux, a .foo .baz, a .foo .quux, a .bar .baz, a .bar .quux, "
                + "p.invisible .baz, p.invisible .quux, p .foo .baz, p .foo .quux, p .bar .baz, p .bar .quux",
                stack.expandAndJoin());
    }

}
