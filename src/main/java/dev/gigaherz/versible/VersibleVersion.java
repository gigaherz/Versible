package dev.gigaherz.versible;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class VersibleVersion implements Comparable<VersibleVersion>
{
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

    public VersibleVersion(List<VersibleComponent> components)
    {
        this.components = components;
    }

    public int size()
    {
        return components.size();
    }

    public VersibleComponent get(int index)
    {
        return components.get(index);
    }

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

    public VersibleVersion append(VersibleVersion other)
    {
        List<VersibleComponent> mergedComponents = new ArrayList<>();
        mergedComponents.addAll(components);
        mergedComponents.addAll(other.components);
        return new VersibleVersion(Collections.unmodifiableList(mergedComponents));
    }

    public VersibleVersion bump(int index)
    {
        List<VersibleComponent> newList = new ArrayList<>(components);
        var component = newList.get(index);
        if (component instanceof VersibleComponent.Numeric num)
            newList.set(index, VersibleComponent.of(num.number()+1));
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
