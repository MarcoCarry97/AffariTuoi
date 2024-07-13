package org.affarituoi.assets;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BoxTest {

    @Test
    public void testCreate()
    {
        Box box=new Box(1,1000);
        assertNotNull(box);
    }

    @Test
    public void testGetId()
    {
        Box box=new Box(1,1000);
        assertEquals(box.getId(),1);
    }

    @Test
    public void testGetValue()
    {
        Box box=new Box(1,1000);
        assertEquals(box.getValue(),1000);
    }
}