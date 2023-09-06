package dev.gigaherz.versible;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public record VersibleRange(@Nullable VersibleVersion minVersion, boolean minExclusive,
                            @Nullable VersibleVersion maxVersion, boolean maxExclusive)
    implements Predicate<VersibleVersion>
{
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
            if (minComparison < 0 || (minExclusive && minComparison == 0))
                return false;
        }

        if (maxVersion != null)
        {
            int maxComparison = maxVersion.compareTo(version);
            if (maxComparison > 0 || (maxExclusive && maxComparison == 0))
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
}
