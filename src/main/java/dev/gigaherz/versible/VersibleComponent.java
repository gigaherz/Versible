package dev.gigaherz.versible;

import org.jetbrains.annotations.NotNull;

public interface VersibleComponent extends Comparable<VersibleComponent>
{
    static Numeric of(long number)
    {
        return new Numeric(number);
    }
    static Alphabetic of(String word)
    {
        return new Alphabetic(word);
    }
    static Suffix suffix(boolean positive)
    {
        return new Suffix(positive);
    }

    record Numeric(long number) implements VersibleComponent {
        @Override
        public int compareTo(@NotNull VersibleComponent o)
        {
            if (o instanceof Numeric n)
                return Long.compare(this.number, n.number);
            return o instanceof Suffix s ? (s.positive ? 1 : -1) : 1;
        }

        @Override
        public String toString()
        {
            return Long.toString(number);
        }
    }

    record Alphabetic(String word) implements VersibleComponent {
        @Override
        public int compareTo(@NotNull VersibleComponent o)
        {
            if (o instanceof Alphabetic a)
                return word.compareTo(a.word);
            return -1;
        }

        @Override
        public String toString()
        {
            return word;
        }
    }

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
