package dev.gigaherz.versible;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a version range.
 * @param minVersion The lower bound of the interval. Can be {@code null}.
 * @param minExclusive If the {@param minVersion} param is non-{@code null}, determines whether the lower bound is inclusive ({@code false}) or exclusive ({@code true}).
 * @param maxVersion The upper bound of the interval. Can be {@code null}.
 * @param maxExclusive If the {@param maxVersion} param is non-{@code null}, determines whether the upper bound is inclusive ({@code false}) or exclusive ({@code true}).
 */
public record VersibleRange(@Nullable VersibleVersion minVersion, boolean minExclusive,
                            @Nullable VersibleVersion maxVersion, boolean maxExclusive)
    implements Predicate<VersibleVersion>
{
    /**
     * Creates a version range which matches versions between the given minimum (inclusive) and maximum (inclusive).
     * @param min The lower bound (inclusive).
     * @param max The upperbound (inclusive).
     * @return A version range.
     */
    public static VersibleRange between(VersibleVersion min, VersibleVersion max)
    {
        return new VersibleRange(min, false, max, false);
    }

    /**
     * Creates a version range which matches versions between the given minimum (exclusive) and maximum (exclusive).
     * @param min The lower bound (exclusive).
     * @param max The upperbound (exclusive).
     * @return A version range.
     */
    public static VersibleRange betweenOpen(VersibleVersion min, VersibleVersion max)
    {
        return new VersibleRange(min, true, max, true);
    }

    /**
     * Creates a version range which matches versions between the given minimum (inclusive) and maximum (exclusive).
     * @param min The lower bound (inclusive).
     * @param max The upperbound (exclusive).
     * @return A version range.
     */
    public static VersibleRange betweenClosedOpen(VersibleVersion min, VersibleVersion max)
    {
        return new VersibleRange(min, false, max, true);
    }

    /**
     * Creates a version range which matches versions between the given minimum (exclusive) and maximum (inclusive).
     * @param min The lower bound (exclusive).
     * @param max The upperbound (inclusive).
     * @return A version range.
     */
    public static VersibleRange betweenOpenClosed(VersibleVersion min, VersibleVersion max)
    {
        return new VersibleRange(min, true, max, false);
    }

    /**
     * Creates a version range which matches versions greater or equal to the given minimum.
     * @param min The version to compare against.
     * @return A version range.
     */
    public static VersibleRange atLeast(VersibleVersion min)
    {
        return new VersibleRange(min, false, null, true);
    }

    /**
     * Creates a version range which matches versions strictly greater than the given minimum.
     * @param min The version to compare against.
     * @return A version range.
     */
    public static VersibleRange moreThan(VersibleVersion min)
    {
        return new VersibleRange(min, true, null, true);
    }

    /**
     * Creates a version range which matches versions less or equal to the given maximum.
     * @param max The version to compare against.
     * @return A version range.
     */
    public static VersibleRange atMost(VersibleVersion max)
    {
        return new VersibleRange(null, true, max, false);
    }

    /**
     * Creates a version range which matches versions strictly less than the given maximum.
     * @param max The version to compare against.
     * @return A version range.
     */
    public static VersibleRange lessThan(VersibleVersion max)
    {
        return new VersibleRange(null, true, max, true);
    }

    /**
     * Creates a version range which matches a given version exactly.
     * @param version The version to match.
     * @return A version range.
     */
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
