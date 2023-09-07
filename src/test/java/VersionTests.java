import dev.gigaherz.versible.VersibleParser;
import dev.gigaherz.versible.VersibleVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionTests
{

    @Test
    public void testComparison()
    {
        // Two versions are equal if they have the same number of components, and each component is equal
        Assertions.assertEquals(0, VersibleVersion.of(1).compareTo(VersibleVersion.of(1)));
        Assertions.assertEquals(0, VersibleVersion.of(1,0).compareTo(VersibleVersion.of(1,0)));
        Assertions.assertEquals(0, VersibleVersion.of(1,'a',2).compareTo(VersibleVersion.of(1,'a',2)));
        Assertions.assertEquals(0, VersibleVersion.of(1,'-',"a",2).compareTo(VersibleVersion.of(1,'-',"a",2)));

        // A version is bigger if it has a component that compares bigger
        Assertions.assertEquals(-1, VersibleVersion.of(1).compareTo(VersibleVersion.of(2)));
        Assertions.assertEquals(1, VersibleVersion.of(2).compareTo(VersibleVersion.of(1)));

        // A version is bigger if it has more components (excluding negative suffixes)
        Assertions.assertEquals(-1, VersibleVersion.of(1).compareTo(VersibleVersion.of(1,0)));
        Assertions.assertEquals(-1, VersibleVersion.of(1,0).compareTo(VersibleVersion.of(1,0,0)));
        Assertions.assertEquals(-1, VersibleVersion.of(1,0).compareTo(VersibleVersion.of(1,0,0,'-',2)));

        // A version is smaller if it has a negative suffix
        Assertions.assertEquals(1, VersibleVersion.of(1,0,0).compareTo(VersibleVersion.of(1,0,0,'-',2)));
    }

    @Test
    public void testParsing()
    {
        Assertions.assertEquals(VersibleVersion.of(1), VersibleParser.parseVersion("1"));
        Assertions.assertEquals(VersibleVersion.of(1,0), VersibleParser.parseVersion("1.0"));
        Assertions.assertEquals(VersibleVersion.of(1,"a"), VersibleParser.parseVersion("1a"));
        Assertions.assertEquals(VersibleVersion.of(1,0,1), VersibleParser.parseVersion("1.0.1"));
        Assertions.assertEquals(VersibleVersion.of(1,0,'-',"alpha",1), VersibleParser.parseVersion("1.0-alpha.1"));
        Assertions.assertEquals(VersibleVersion.of(0,0,'+',"snapshot",2,1), VersibleParser.parseVersion("0.0+snapshot2.1"));
        Assertions.assertEquals(VersibleVersion.of("b",3), VersibleParser.parseVersion("b3"));
    }
}
