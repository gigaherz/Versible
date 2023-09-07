package dev.gigaherz.versible;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

public record VersibleRange(@Nullable VersibleVersion minVersion, boolean minExclusive,
                            @Nullable VersibleVersion maxVersion, boolean maxExclusive)
    implements Predicate<VersibleVersion>
{
    public static VersibleRange between(VersibleVersion min, VersibleVersion max)
    {
        return new VersibleRange(min, false, max, false);
    }
    public static VersibleRange betweenOpen(VersibleVersion min, VersibleVersion max)
    {
        return new VersibleRange(min, true, max, true);
    }
    public static VersibleRange betweenClosedOpen(VersibleVersion min, VersibleVersion max)
    {
        return new VersibleRange(min, false, max, true);
    }
    public static VersibleRange betweenOpenClosed(VersibleVersion min, VersibleVersion max)
    {
        return new VersibleRange(min, true, max, false);
    }
    public static VersibleRange atLeast(VersibleVersion min)
    {
        return new VersibleRange(min, false, null, true);
    }
    public static VersibleRange moreThan(VersibleVersion min)
    {
        return new VersibleRange(min, true, null, true);
    }
    public static VersibleRange atMost(VersibleVersion max)
    {
        return new VersibleRange(null, true, max, false);
    }
    public static VersibleRange lessThan(VersibleVersion max)
    {
        return new VersibleRange(null, true, max, true);
    }
    public static VersibleRange exactly(VersibleVersion version)
    {
        return new VersibleRange(version, false, version, false);
    }



    /**
     * Checks if a given version is included in the range.
     * @param version The version to check.
     * @return {@code true} if the version is included, {@code false} othersie.
     */
    @SuppressWarnings("RedundantIfStatement")
    public boolean contains(VersibleVersion version)
    {
        if (minVersion != null)
        {
            int minComparison = minVersion.compareTo(version);
            if (minComparison > 0 || (minExclusive && minComparison == 0))
                return false;
        }

        if (maxVersion != null)
        {
            int maxComparison = maxVersion.compareTo(version);
            if (maxComparison < 0 || (maxExclusive && maxComparison == 0))
                return false;
        }

        return true;
    }

    /* Javadoc Inherited */
    @Override
    public boolean test(VersibleVersion version)
    {
        return contains(version);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersibleRange that = (VersibleRange) o;
        return Objects.equals(minVersion, that.minVersion)
                && Objects.equals(maxVersion, that.maxVersion)
                && (minVersion == null || minExclusive == that.minExclusive)
                && (maxVersion == null || maxExclusive == that.maxExclusive);
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        if (minVersion != null)
            hash = hash * 31 + Objects.hash(minVersion, minExclusive);
        if (minVersion != null)
            hash = hash * 31 + Objects.hash(maxVersion, maxExclusive);
        return hash;
    }
}
