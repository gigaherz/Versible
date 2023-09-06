package dev.gigaherz.versible;

import java.util.List;
import java.util.stream.Stream;

public class VersibleVersion implements Comparable<VersibleVersion>
{
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
            return o.get(i) instanceof VersibleComponent.Suffix s && !s.positive() ? -1 : 1;
        }
        return 0;
    }
}
