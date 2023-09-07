package dev.gigaherz.versible;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a component within a version string.
 */
public interface VersibleComponent extends Comparable<VersibleComponent>
{
    /**
     * Creates a numeric component from the given number. The number should be positive.
     * @param number The number to use for the numeric component.
     * @return A numeric component with the corresponding number.
     */
    static Numeric of(long number)
    {
        return new Numeric(number);
    }

    /**
     * Creates an alphabetic component from the given string. The string should only contain letters.
     * @param word The word to use for the alphabetic component
     * @return An alphabetic component with the corresponding word.
     */
    static Alphabetic of(String word)
    {
        return new Alphabetic(word);
    }

    /**
     * Creates a suffix component for the corresponding positive/negative tag type.
     * @param positive Whether the tag type is positive {@code true} or negetive {@code false}.
     * @return A suffix component with the corresponding tag type.
     */
    static Suffix suffix(boolean positive)
    {
        return new Suffix(positive);
    }

    /**
     * Represents a numeric component.
     */
    record Numeric(long number) implements VersibleComponent {
        @Override
        public int compareTo(@NotNull VersibleComponent o)
        {
            if (o instanceof Numeric n)
                return Long.compare(this.number, n.number);
            return o instanceof Suffix s ? (s.positive ? -1 : 1) : 1;
        }

        @Override
        public String toString()
        {
            return Long.toString(number);
        }
    }

    /**
     * Represents an alphabetic component.
     */
    record Alphabetic(String word) implements VersibleComponent {
        @Override
        public int compareTo(@NotNull VersibleComponent o)
        {
            if (o instanceof Alphabetic a)
                return word.compareTo(a.word);
            return o instanceof Suffix s ? (s.positive ? -1 : 1) : -1;
        }

        @Override
        public String toString()
        {
            return word;
        }
    }

    /**
     * A component that represents the start of a suffix tag.
     */
    record Suffix(boolean positive) implements VersibleComponent {

        @Override
        public int compareTo(@NotNull VersibleComponent o)
        {
            if (o instanceof Suffix s && s.positive == positive)
                return 0;
            return positive ? 1 : -1;
        }

        @Override
        public String toString()
        {
            return positive ? "+" : "-";
        }
    }
}
