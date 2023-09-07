package dev.gigaherz.versible;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilities for parsing versions and ranges. This class cannot be instantiated.
 */
public class VersibleParser
{
    /**
     * Parses a version range into an object that can test versions.
     *
     * <h4>Grammar</h4>
     *     <pre>{@code
     *     <range> ::= <version>
     *              |  <version> '.' '*'
     *              |  <comparison-operator> <version>
     *              |  <interval>
     *
     *     <comparison-operator> ::= '>=' | '>' | '<=' | '<' | '='
     *
     *     <interval>       ::= <left-interval> (<version> ',' <version>? | ',' <version>) <right-interval>
     *     <left-interval>  ::= '[' | '('
     *     <right-interval> ::= ']' | ')'
     * }</pre>
     *
     * <h4>Examples</h4>
     * <pre>
     *     String      -> Parses into
     *    ------------------------------------
     *     1.*         -> [1.0,2.0)
     *     >1.0        -> (1.0,)
     *     1.0         -> [1.0,1.0]
     *     =1.0.1      -> [1.0.1,1.0.1]
     *     [1,2]       -> [1,2]
     *     (1.23,1.37) -> (1.23,1.37)
     * </pre>
     *
     * @param range The string containing the range to be parsed.
     * @return The range representing the given string.
     * @throws IllegalStateException If the string cannot be converted into a valid range.
     */
    public static VersibleRange parseRange(String range)
    {
        int state = 0;
        VersibleVersion minVersion = null;
        VersibleVersion maxVersion = null;
        boolean minExclusive = false;
        boolean maxExclusive = false;
        loop: for(int i=0;i<range.length();i++)
        {
            var c = range.charAt(i);
            switch(state)
            {
                case 0: // start
                    if (Character.isLetterOrDigit(c))
                    {
                        int[] endIndex = {0};
                        minVersion = maxVersion = parseVersionInternal(range, i, range.length(), endIndex);
                        i = endIndex[0];
                        if (i < range.length())
                        {
                            c = range.charAt(i);
                            if (c == '.')
                            {
                                i++;

                                if (i >= range.length())
                                {
                                    throw new IllegalStateException("Unexpected end of string in version pattern.");
                                }

                                c = range.charAt(i);

                                if (c == '*')
                                {
                                    minVersion = minVersion.append(VersibleVersion.of(0));
                                    maxVersion = maxVersion.bump(maxVersion.size()-1).append(VersibleVersion.of(0));;
                                    maxExclusive = true;

                                    i++;
                                    if (i < range.length())
                                    {
                                        c = range.charAt(i);

                                        throw new IllegalStateException("Unexpected character '" + c + "' in version component.");
                                    }
                                }
                                else
                                {
                                    throw new IllegalStateException("Unexpected character '" + c + "' in version component.");
                                }
                            }
                            else
                            {
                                throw new IllegalStateException("Unexpected character '" + c + "' in version component.");
                            }
                        }
                        break loop;
                    }
                    else if (c == '>')
                    {
                        state = 1;
                    }
                    else if(c == '<')
                    {
                        state = 2;
                    }
                    else if(c == '=')
                    {
                        state = 3;
                    }
                    else if(c == '(')
                    {
                        state = 6;
                        minExclusive = true;
                    }
                    else if(c == '[')
                    {
                        state = 6;
                    }
                    else
                    {
                        throw new IllegalStateException("Unexpected character '" + c + "' in version range.");
                    }
                    break;
                case 1,2: // single version relative (partial)
                    if (c == '=')
                    {
                        state = state + 3;
                        break;
                    }
                    /* fallthrough */
                case 3,4,5: // single version relative
                    if (Character.isLetterOrDigit(c))
                    {
                        int[] endIndex = {0};
                        var v = parseVersionInternal(range, i, range.length(), endIndex);
                        i = endIndex[0];
                        if (i < range.length())
                        {
                            c = range.charAt(i);
                            throw new IllegalStateException("Unexpected character '" + c + "' in version component.");
                        }

                        switch (state)
                        {
                            case 1 ->
                            {
                                minVersion = v;
                                minExclusive = true;
                            }
                            case 2 ->
                            {
                                maxVersion = v;
                                maxExclusive = true;
                            }
                            case 3 ->
                            {
                                minVersion = maxVersion = v;
                            }
                            case 4 ->
                            {
                                minVersion = v;
                            }
                            case 5 ->
                            {
                                maxVersion = v;
                            }
                        }

                        break loop;
                    }
                    else
                    {
                        throw new IllegalStateException("Unexpected character '" + c + "' in version range.");
                    }
                case 6: // interval start
                    if (Character.isLetterOrDigit(c))
                    {
                        int[] endIndex = {0};
                        minVersion = parseVersionInternal(range, i, range.length(), endIndex);
                        i = endIndex[0];

                        if (i >= range.length())
                        {
                            throw new IllegalStateException("Unexpected end of string in version interval.");
                        }

                        c = range.charAt(i);

                        if (c == ',')
                        {
                            i++;
                            if (i >= range.length())
                            {
                                throw new IllegalStateException("Unexpected end of string in version interval.");
                            }
                            c = range.charAt(i);

                            if (Character.isLetterOrDigit(c))
                            {
                                maxVersion = parseVersionInternal(range, i, range.length(), endIndex);
                                i = endIndex[0];

                                if (i >= range.length())
                                {
                                    throw new IllegalStateException("Unexpected end of string in version interval.");
                                }

                                c = range.charAt(i);
                            }
                        }
                        else
                        {
                            maxVersion = minVersion;
                        }

                        if (c == ')')
                        {
                            maxExclusive = true;
                        }
                        else if (c != ']')
                        {
                            throw new IllegalStateException("Unexpected character '" + c + "' in version interval.");
                        }

                        i++;
                        if (i < range.length())
                        {
                            c = range.charAt(i);
                            throw new IllegalStateException("Unexpected character '" + c + "' after version interval.");
                        }

                        break loop;
                    }
                    else if (c == ',')
                    {
                        int[] endIndex = {0};
                        maxVersion = parseVersionInternal(range, i + 1, range.length(), endIndex);
                        i = endIndex[0];

                        if (i >= range.length())
                        {
                            throw new IllegalStateException("Unexpected end of string in version interval.");
                        }

                        c = range.charAt(i);

                        if (c == ')')
                        {
                            maxExclusive = true;
                        }
                        else if (c != ']')
                        {
                            throw new IllegalStateException("Unexpected character '" + c + "' in version interval.");
                        }

                        i++;
                        if (i < range.length())
                        {
                            c = range.charAt(i);
                            throw new IllegalStateException("Unexpected character '" + c + "' after version interval.");
                        }

                        break loop;
                    }
                    else
                    {
                        throw new IllegalStateException("Unexpected character '" + c + "' in version interval.");
                    }
            }
        }
        return new VersibleRange(minVersion, minExclusive, maxVersion, maxExclusive);
    }

    /**
     * Parses a version string into a comparable version object.
     *
     * <h4>Grammar</h4>
     * <pre>{@code
     *     <version> ::= <component-list> ( [+-] <component-list> )*
     *     <component-list> ::= <component> ('.'? <component>)*
     *     <component> ::= <number> | <word>
     *     <number> ::= {Digit}+
     *     <word> ::= {Letter}+
     * }</pre>
     *
     * <h4>Examples</h4>
     * <pre>
     *     String      -> Parses into
     *    ------------------------------------
     *     1           -> [ 1 ]
     *     1.0         -> [ 1, 0 ]
     *     1.0-1       -> [ 1, 0, -, 1 ]
     *     1.0+2       -> [ 1, 0, +, 1 ]
     *     1.0.2a3     -> [ 1, 0, 2, a, 3 ]
     *     23w32a      -> [ 23, w, 32, a ]
     * </pre>
     *
     * @param version The string containing the version to be parsed.
     * @return The version representing the given string.
     * @throws IllegalStateException If the string cannot be converted into a valid version.
     */
    @NotNull
    public static VersibleVersion parseVersion(String version)
    {
        return parseVersionInternal(version, 0, version.length(), null);
    }

    @NotNull
    private static VersibleVersion parseVersionInternal(@NotNull CharSequence version, int start, int end, int @Nullable [] outIndex)
    {
        List<VersibleComponent> components = new ArrayList<>();
        int state = 0;
        int wordStart = start;
        int lastGood = 0;
        int i;
        loop: for(i=start;i < end;i++)
        {
            char c = version.charAt(i);
            switch (state)
            {
                case 0 -> // start of component
                {
                    if (Character.isDigit(c))
                    {
                        state = 1;
                        wordStart = i;
                        lastGood = i+1;
                    }
                    else if (Character.isLetter(c))
                    {
                        state = 2;
                        wordStart = i;
                        lastGood = i+1;
                    }
                    else
                    {
                        if (outIndex != null)
                        {
                            outIndex[0] = lastGood;
                            break loop;
                        }
                        else
                        {
                            throw new IllegalStateException("Unexpected character '" + c + "' at the start of a version component.");
                        }
                    }
                }
                case 1 -> // number
                {
                    if (Character.isDigit(c))
                    {
                        lastGood = i+1;
                    }
                    else
                    {
                        long number = Long.parseUnsignedLong(version, wordStart, i, 10);
                        components.add(VersibleComponent.of(number));

                        if (Character.isLetter(c))
                        {
                            state = 2;
                            wordStart = i;
                        }
                        else if (c == '.')
                        {
                            state = 0;
                        }
                        else if (c == '-')
                        {
                            components.add(VersibleComponent.suffix(false));
                            state = 0;
                        }
                        else if (c == '+')
                        {
                            components.add(VersibleComponent.suffix(true));
                            state = 0;
                        }
                        else
                        {
                            state = 0;
                            if (outIndex != null)
                            {
                                outIndex[0] = lastGood;
                                break loop;
                            }
                            else
                            {
                                throw new IllegalStateException("Unexpected character '" + c + "' in version component.");
                            }
                        }
                    }
                }
                case 2 -> // alphabetic
                {
                    if (Character.isLetter(c))
                    {
                        lastGood = i+1;
                    }
                    else
                    {
                        String word = version.subSequence(wordStart, i).toString();
                        components.add(VersibleComponent.of(word));

                        if (Character.isDigit(c))
                        {
                            state = 1;
                            wordStart = i;
                        }
                        else if (c == '.')
                        {
                            state = 0;
                        }
                        else if (c == '-')
                        {
                            components.add(VersibleComponent.suffix(false));
                            state = 0;
                        }
                        else if (c == '+')
                        {
                            components.add(VersibleComponent.suffix(true));
                            state = 0;
                        }
                        else
                        {
                            state = 0;
                            if (outIndex != null)
                            {
                                outIndex[0] = lastGood;
                                break loop;
                            }
                            else
                            {
                                throw new IllegalStateException("Unexpected character '" + c + "' in version component.");
                            }
                        }
                    }
                }
            }
        }

        switch (state)
        {
            case 1 ->
            {
                long number = Long.parseUnsignedLong(version, wordStart, i, 10);
                components.add(VersibleComponent.of(number));
            }
            case 2 ->
            {
                String word = version.subSequence(wordStart, i).toString();
                components.add(VersibleComponent.of(word));
            }
        }

        if (components.size() == 0)
        {
            throw new IllegalStateException("Version string cannot be empty.");
        }

        if (outIndex != null)
            outIndex[0] = lastGood;
        return new VersibleVersion(Collections.unmodifiableList(components));
    }

    private VersibleParser()
    {
        throw new IllegalStateException("This class cannot be instantiated.");
    }
}
