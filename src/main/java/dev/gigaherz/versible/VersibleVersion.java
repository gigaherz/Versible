package dev.gigaherz.versible;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a version string as a collection of version components.
 */
public class VersibleVersion implements Comparable<VersibleVersion>
{
    /**
     * Creates a version from the given sequence of component values, parsing each component value into the corresponding {@link VersibleComponent}.
     * @param components A variadic array of component objects. Numbers (integers), Strings, and Characters are allowed.
     * @return The version containing the given sequence of components.
     * @throws IllegalStateException If an object in the array cannot be converted into a component.
     */
    public static VersibleVersion of(Object... components)
    {
        List<VersibleComponent> componentList = new ArrayList<>();
        for(var obj : components)
        {
            if (obj instanceof Number n)
                componentList.add(VersibleComponent.of(n.longValue()));
            else if(obj instanceof String s)
            {
                if (s.equals("+"))
                    componentList.add(VersibleComponent.suffix(true));
                else if(s.equals("-"))
                    componentList.add(VersibleComponent.suffix(false));
                else
                    componentList.add(VersibleComponent.of(s));
            }
            else if(obj instanceof Character c)
            {
                if (c == '+')
                    componentList.add(VersibleComponent.suffix(true));
                else if(c == '-')
                    componentList.add(VersibleComponent.suffix(false));
                else
                    componentList.add(VersibleComponent.of(c.toString()));
            }
            else if(obj instanceof VersibleVersion v)
                componentList.addAll(v.components);
            else
            {
                throw new IllegalStateException("Cannot construct version component from " + obj.getClass().getName());
            }
        }
        return new VersibleVersion(Collections.unmodifiableList(componentList));
    }

    private final List<VersibleComponent> components;

    /**
     * Initializes the version with the given components.
     * To parse a version from string, {@link VersibleParser#parseVersion(String)} should be used instead.
     * To construct an object, {@link #of(Object...)} should be used instead.
     * @param components The list of components. In order to maintain the immutability, this should be an unmodifiable or immutable list.
     */
    @ApiStatus.Internal
    public VersibleVersion(List<VersibleComponent> components)
    {
        this.components = components;
    }

    /**
     * Returns the number of components in this version.
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
        for(i=0;i<min;i++)
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
        else if(size() < o.size())
        {
            return o.get(i) instanceof VersibleComponent.Suffix s && !s.positive() ? 1 : -1;
        }
        return 0;
    }

    /**
     * Creates a new version with the components of another version concatenated after the components of this version.
     * @param other The version to append components from.
     * @return The version with the concatenated components.
     */
    public VersibleVersion append(VersibleVersion other)
    {
        List<VersibleComponent> mergedComponents = new ArrayList<>();
        mergedComponents.addAll(components);
        mergedComponents.addAll(other.components);
        return new VersibleVersion(Collections.unmodifiableList(mergedComponents));
    }

    /**
     * Creates a new version with the given numeric component incremented by one.
     * @param index The index of the component to increment.
     * @return The version string corresponding to the version with the incremented component.
     * @throws IllegalStateException If the component at the given index is not a numeric component.
     */
    public VersibleVersion bump(int index)
    {
        List<VersibleComponent> newList = new ArrayList<>(components);
        var component = newList.get(index);
        if (component instanceof VersibleComponent.Numeric num)
            newList.set(index, VersibleComponent.of(num.number()+1));
        else throw new IllegalStateException("The component at index " + index + " is not a numeric component.");
        return new VersibleVersion(Collections.unmodifiableList(newList));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersibleVersion that = (VersibleVersion) o;
        return Objects.equals(components, that.components);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(components);
    }

    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder();
        boolean lastWasNumber = false;
        boolean lastWasWord = false;
        for(var component : components)
        {
            if (component instanceof VersibleComponent.Numeric n)
            {
                if (lastWasNumber)
                    b.append('.');
                b.append(n);
                lastWasNumber = true;
                lastWasWord = false;
            }
            else if(component instanceof VersibleComponent.Alphabetic a)
            {
                if (lastWasWord)
                    b.append('.');
                b.append(a);
                lastWasNumber = false;
                lastWasWord = true;
            }
            else if(component instanceof VersibleComponent.Suffix s)
            {
                b.append(s);
                lastWasNumber = false;
                lastWasWord = false;
            }
        }
        return b.toString();
    }
}
