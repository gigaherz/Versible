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
        Assertions.assertEquals(r9, VersibleParser.parseRange("2.0"));

        // Wildcard
        Assertions.assertEquals(r3, VersibleParser.parseRange("1.*"));
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

        // Between closed
        Assertions.assertTrue(r1.contains(VersibleVersion.of(1,0)));
        Assertions.assertTrue(r1.contains(VersibleVersion.of(1,1)));
        Assertions.assertTrue(r1.contains(VersibleVersion.of(2,0)));

        // Between open
        Assertions.assertFalse(r2.contains(VersibleVersion.of(1,0)));
        Assertions.assertTrue(r2.contains(VersibleVersion.of(1,1)));
        Assertions.assertFalse(r2.contains(VersibleVersion.of(2,0)));

        // Between closed-open
        Assertions.assertTrue(r3.contains(VersibleVersion.of(1,0)));
        Assertions.assertTrue(r3.contains(VersibleVersion.of(1,1)));
        Assertions.assertFalse(r3.contains(VersibleVersion.of(2,0)));

        // Between open-closed
        Assertions.assertFalse(r4.contains(VersibleVersion.of(1,0)));
        Assertions.assertTrue(r4.contains(VersibleVersion.of(1,1)));
        Assertions.assertTrue(r4.contains(VersibleVersion.of(2,0)));

        // At least
        Assertions.assertTrue(r5.contains(VersibleVersion.of(1,0)));
        Assertions.assertFalse(r5.contains(VersibleVersion.of(1,0,'-')));
        Assertions.assertTrue(r5.contains(VersibleVersion.of(1,0,'+')));
        Assertions.assertTrue(r5.contains(VersibleVersion.of(1,0,0,'-')));

        // More than
        Assertions.assertFalse(r6.contains(VersibleVersion.of(1,0)));
        Assertions.assertFalse(r6.contains(VersibleVersion.of(1,0,'-')));
        Assertions.assertTrue(r6.contains(VersibleVersion.of(1,0,'+')));
        Assertions.assertFalse(r6.contains(VersibleVersion.of(1,'-')));

        // At most
        Assertions.assertTrue(r7.contains(VersibleVersion.of(2,0)));
        Assertions.assertTrue(r7.contains(VersibleVersion.of(2,0,'-')));
        Assertions.assertFalse(r7.contains(VersibleVersion.of(2,0,'+')));
        Assertions.assertFalse(r7.contains(VersibleVersion.of(2,0,0,'-')));

        // Less than
        Assertions.assertFalse(r8.contains(VersibleVersion.of(2,0)));
        Assertions.assertTrue(r8.contains(VersibleVersion.of(2,0,'-')));
        Assertions.assertFalse(r8.contains(VersibleVersion.of(2,0,'+')));
        Assertions.assertTrue(r8.contains(VersibleVersion.of(2,'-')));
        Assertions.assertFalse(r8.contains(VersibleVersion.of(2,'+')));

        // Exactly
        Assertions.assertTrue(r9.contains(VersibleVersion.of(2,0)));
        Assertions.assertFalse(r9.contains(VersibleVersion.of(2,0,0)));
        Assertions.assertFalse(r9.contains(VersibleVersion.of(2,0,'-')));
        Assertions.assertFalse(r9.contains(VersibleVersion.of(2,'-')));
        Assertions.assertFalse(r9.contains(VersibleVersion.of(2,0,'+')));
        Assertions.assertFalse(r9.contains(VersibleVersion.of(2,'+')));
    }
}
