import dev.gigaherz.versible.VersibleParser;
import dev.gigaherz.versible.VersibleRange;
import dev.gigaherz.versible.VersibleVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RangeTests
{
    @Test
    public void testParsing()
    {
        var a = VersibleVersion.of(1,0);
        var b = VersibleVersion.of(2,0);
        var r1 = VersibleRange.between(a,b);
        var r2 = VersibleRange.betweenOpen(a,b);
        var r3 = VersibleRange.betweenClosedOpen(a,b);
        var r4 = VersibleRange.betweenOpenClosed(a,b);
        var r5 = VersibleRange.atLeast(a);
        var r6 = VersibleRange.moreThan(a);
        var r7 = VersibleRange.atMost(b);
        var r8 = VersibleRange.lessThan(b);
        var r9 = VersibleRange.exactly(b);
        var r10 = VersibleRange.approximately(b);

        // Intervals
        Assertions.assertEquals(r1, VersibleParser.parseRange("[1.0,2.0]"));
        Assertions.assertEquals(r2, VersibleParser.parseRange("(1.0,2.0)"));
        Assertions.assertEquals(r3, VersibleParser.parseRange("[1.0,2.0)"));
        Assertions.assertEquals(r4, VersibleParser.parseRange("(1.0,2.0]"));
        Assertions.assertEquals(r5, VersibleParser.parseRange("[1.0,)"));
        Assertions.assertEquals(r6, VersibleParser.parseRange("(1.0,)"));
        Assertions.assertEquals(r7, VersibleParser.parseRange("(,2.0]"));
        Assertions.assertEquals(r8, VersibleParser.parseRange("(,2.0)"));
        Assertions.assertEquals(r9, VersibleParser.parseRange("[2.0]"));

        // Comparisons
        Assertions.assertEquals(r5, VersibleParser.parseRange(">=1.0"));
        Assertions.assertEquals(r6, VersibleParser.parseRange(">1.0"));
        Assertions.assertEquals(r7, VersibleParser.parseRange("<=2.0"));
        Assertions.assertEquals(r8, VersibleParser.parseRange("<2.0"));
        Assertions.assertEquals(r9, VersibleParser.parseRange("=2.0"));

        // Single
        Assertions.assertEquals(r10, VersibleParser.parseRange("2.0"));

        // Wildcard
        Assertions.assertEquals(r3, VersibleParser.parseRange("1.*"));

        // Invalid
        Assertions.assertThrows(IllegalArgumentException.class, () -> VersibleParser.parseRange("[,]"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> VersibleParser.parseRange(">>1"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> VersibleParser.parseRange("-2"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> VersibleParser.parseRange("[[a]]"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> VersibleParser.parseRange(">1.*"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> VersibleParser.parseRange("1%1"));
    }

    @Test
    public void testContains()
    {
        var a = VersibleVersion.of(1,0);
        var b = VersibleVersion.of(2,0);
        var r1 = VersibleRange.between(a,b);
        var r2 = VersibleRange.betweenOpen(a,b);
        var r3 = VersibleRange.betweenClosedOpen(a,b);
        var r4 = VersibleRange.betweenOpenClosed(a,b);
        var r5 = VersibleRange.atLeast(a);
        var r6 = VersibleRange.moreThan(a);
        var r7 = VersibleRange.atMost(b);
        var r8 = VersibleRange.lessThan(b);
        var r9 = VersibleRange.exactly(b);
        var r10 = VersibleRange.approximately(b);

        // Between closed
        assertExcludes(r1, VersibleVersion.of(1));
        assertIncludes(r1, VersibleVersion.of(1,0));
        assertIncludes(r1, VersibleVersion.of(1,1));
        assertIncludes(r1, VersibleVersion.of(2,0));
        assertExcludes(r1, VersibleVersion.of(2,0,0));

        // Between open
        assertExcludes(r2, VersibleVersion.of(1,0));
        assertIncludes(r2, VersibleVersion.of(1,1));
        assertExcludes(r2, VersibleVersion.of(2,0));

        // Between closed-open
        assertIncludes(r3, VersibleVersion.of(1,0));
        assertIncludes(r3, VersibleVersion.of(1,1));
        assertExcludes(r3, VersibleVersion.of(2,0));

        // Between open-closed
        assertExcludes(r4, VersibleVersion.of(1,0));
        assertIncludes(r4, VersibleVersion.of(1,1));
        assertIncludes(r4, VersibleVersion.of(2,0));

        // At least
        assertIncludes(r5, VersibleVersion.of(1,0));
        assertExcludes(r5, VersibleVersion.of(1,0,'-'));
        assertIncludes(r5, VersibleVersion.of(1,0,'+'));
        assertIncludes(r5, VersibleVersion.of(1,0,0,'-'));

        // More than
        assertExcludes(r6, VersibleVersion.of(1,0));
        assertExcludes(r6, VersibleVersion.of(1,0,'-'));
        assertIncludes(r6, VersibleVersion.of(1,0,'+'));
        assertExcludes(r6, VersibleVersion.of(1,'-'));

        // At most
        assertIncludes(r7, VersibleVersion.of(2,0));
        assertIncludes(r7, VersibleVersion.of(2,0,'-'));
        assertExcludes(r7, VersibleVersion.of(2,0,'+'));
        assertExcludes(r7, VersibleVersion.of(2,0,0,'-'));

        // Less than
        assertExcludes(r8, VersibleVersion.of(2,0));
        assertIncludes(r8, VersibleVersion.of(2,0,'-'));
        assertExcludes(r8, VersibleVersion.of(2,0,'+'));
        assertIncludes(r8, VersibleVersion.of(2,'-'));
        assertIncludes(r8, VersibleVersion.of(2,'+'));

        // Exactly
        assertIncludes(r9, VersibleVersion.of(2,0));
        assertExcludes(r9, VersibleVersion.of(2,0,0));
        assertExcludes(r9, VersibleVersion.of(2,0,'-'));
        assertExcludes(r9, VersibleVersion.of(2,'-'));
        assertExcludes(r9, VersibleVersion.of(2,0,'+'));
        assertExcludes(r9, VersibleVersion.of(2,'+'));

        // Approximately
        assertIncludes(r10, VersibleVersion.of(2,0));
        assertExcludes(r10, VersibleVersion.of(2,0,0));
        assertExcludes(r10, VersibleVersion.of(2,0,'-'));
        assertExcludes(r10, VersibleVersion.of(2,'-'));
        assertIncludes(r10, VersibleVersion.of(2,0,'+'));
        assertExcludes(r10, VersibleVersion.of(2,'+'));
    }
    
    public static void assertIncludes(VersibleRange range, VersibleVersion version)
    {
        Assertions.assertTrue(range.contains(version), () -> range + " .contains( " + version + " )");
    }

    public static void assertExcludes(VersibleRange range, VersibleVersion version)
    {
        Assertions.assertFalse(range.contains(version), () -> range + " .contains( " + version + " )");
    }
}
