package org.affarituoi.assets;

import java.util.Comparator;

class Box implements Comparable<Box>,Comparator<Box>
{
    private int id;

    private int value;

    public Box(int id,int value)
    {
        this.id=id;
        this.value=value;
    }

    public int getId()
    {
        return id;
    }

    public int getValue()
    {
        return value;
    }

    @Override
    public int compareTo(Box o) {
        if(this.id==o.getId())
            return 0;
        else if(this.id>o.getId())
            return 1;
        else return -1;
    }

    @Override
    public int compare(Box o1, Box o2) {
        return o1.compareTo(o2);
    }
}