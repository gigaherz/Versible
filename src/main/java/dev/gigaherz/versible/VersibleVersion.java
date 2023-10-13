package dev.gigaherz.versible;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a version string as a collection of version components.
 */
public record VersibleVersion(@NotNull List<VersibleComponent> components) implements Comparable<VersibleVersion>
{
    /**
     * Returns a {@link VersibleVersion} with the given sequence of component values, by parsing each component value into the corresponding {@link VersibleComponent}.
     *
     * @param components A variadic array of component objects. Numbers (integers), Strings, and Characters are allowed.
     * @return The version containing the given sequence of components.
     * @throws IllegalArgumentException If an object in the array cannot be converted into a component.
     */
    public static VersibleVersion of(Object... components)
    {
        List<VersibleComponent> componentList = new ArrayList<>();
        appendArray(componentList, components);
        return new VersibleVersion(Collections.unmodifiableList(componentList));
    }

    /**
     * Constructs the version with the given components.
     * To parse a version from string, {@link VersibleParser#parseVersion(String)} should be used instead.
     * To construct an object, {@link #of(Object...)} should be used instead.
     *
     * @param components The list of components. Cannot be empty. In order to maintain immutability, this must be an unmodifiable or immutable list.
     */
    @ApiStatus.Internal
    public VersibleVersion
    {
        if (components.isEmpty())
            throw new IllegalArgumentException("The component list cannot be empty.");
    }

    /**
     * Returns the number of components in this version.
     *
     * @return The number of components in this version.
     */
    public int size()
    {
        return components.size();
    }

    /**
     * Returns the component at the specified position in the component list.
     *
     * @param index The index of the element to return.
     * @return The component at the specified position in the component list.
     * @throws IndexOutOfBoundsException If the index is out of range ({@code index < 0 || index >= size()}).
     */
    public VersibleComponent get(int index)
    {
        return components.get(index);
    }

    /**
     * Returns a sequential {@code Stream} with the component collection as its source.
     *
     * @return a sequential {@code Stream} over the elements in the component collection
     */
    public Stream<VersibleComponent> stream()
    {
        return components.stream();
    }

    @Override
    public int compareTo(VersibleVersion o)
    {
        int min = Math.min(size(), o.size());
        int i;
        for (i = 0; i < min; i++)
        {
            var a = get(i);
            var b = o.get(i);
            int c = a.compareTo(b);
            if (c != 0)
                return c;
        }
        if (size() > o.size())
        {
            return get(i) instanceof VersibleComponent.Suffix s && !s.positive() ? -1 : 1;
        }
        else if (size() < o.size())
        {
            return o.get(i) instanceof VersibleComponent.Suffix s && !s.positive() ? 1 : -1;
        }
        return 0;
    }

    /**
     * Returns a new {@link VersibleVersion} with the components of another version concatenated after the components of this version.
     *
     * @param other The version to append components from.
     * @return The version with the concatenated components.
     */
    public VersibleVersion append(VersibleVersion other)
    {
        List<VersibleComponent> mergedComponents = new ArrayList<>(components.size() + other.components.size());
        mergedComponents.addAll(components);
        mergedComponents.addAll(other.components);
        return new VersibleVersion(Collections.unmodifiableList(mergedComponents));
    }

    /**
     * Returns a new {@link VersibleVersion} with the components of another version concatenated after the components of this version.
     *
     * @param other The version to append components from.
     * @return The version with the concatenated components.
     */
    public VersibleVersion append(Object... other)
    {
        List<VersibleComponent> mergedComponents = new ArrayList<>(components.size() + other.length);
        mergedComponents.addAll(components);
        appendArray(mergedComponents, other);
        return new VersibleVersion(Collections.unmodifiableList(mergedComponents));
    }

    /**
     * Returns a new {@link VersibleVersion} with the given numeric component incremented by one.
     *
     * @param index The index of the component to increment.
     * @return The version string corresponding to the version with the incremented component.
     * @throws IllegalArgumentException If the component at the given index is not a numeric component.
     */
    public VersibleVersion bump(int index)
    {
        List<VersibleComponent> newList = new ArrayList<>(components);
        var component = newList.get(index);
        if (component instanceof VersibleComponent.Numeric num)
            newList.set(index, VersibleComponent.of(num.number() + 1));
        else throw new IllegalArgumentException("The component at index " + index + " is not a numeric component.");
        return new VersibleVersion(Collections.unmodifiableList(newList));
    }

    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder();
        boolean lastWasNumber = false;
        boolean lastWasWord = false;
        for (var component : components)
        {
            if (component instanceof VersibleComponent.Numeric n)
            {
                if (lastWasNumber)
                    b.append('.');
                b.append(n);
                lastWasNumber = true;
                lastWasWord = false;
            }
            else if (component instanceof VersibleComponent.Alphabetic a)
            {
                if (lastWasWord)
                    b.append('.');
                b.append(a);
                lastWasNumber = false;
                lastWasWord = true;
            }
            else if (component instanceof VersibleComponent.Suffix s)
            {
                b.append(s);
                lastWasNumber = false;
                lastWasWord = false;
            }
        }
        return b.toString();
    }

    private static void appendArray(List<VersibleComponent> componentList, Object[] components)
    {
        for (var obj : components)
        {
            if (obj instanceof Number n)
                componentList.add(VersibleComponent.of(n.longValue()));
            else if (obj instanceof String s)
            {
                if (s.equals("+"))
                    componentList.add(VersibleComponent.suffix(true));
                else if (s.equals("-"))
                    componentList.add(VersibleComponent.suffix(false));
                else
                    componentList.add(VersibleComponent.of(s));
            }
            else if (obj instanceof Character c)
            {
                if (c == '+')
                    componentList.add(VersibleComponent.suffix(true));
                else if (c == '-')
                    componentList.add(VersibleComponent.suffix(false));
                else
                    componentList.add(VersibleComponent.of(c.toString()));
            }
            else if (obj instanceof VersibleVersion v)
                componentList.addAll(v.components);
            else if (obj instanceof VersibleComponent c)
                componentList.add(c);
            else
            {
                throw new IllegalArgumentException("Cannot construct version component from " + obj.getClass().getName());
            }
        }
    }
}
