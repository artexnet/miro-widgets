package com.miro.hw.artexnet.api.dto;

import com.miro.hw.artexnet.BaseTestUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AreaRequestTest extends BaseTestUnit {

    private AreaRequest areaRequest;

    @Before
    public void setUp() {
        areaRequest = objectGenerator.nextObject(AreaRequest.class);
    }

    @After
    public void tearDown() { }

    ////////////////////////////////////

    @Test
    public void isDefined() {
        areaRequest.setLeftX(null);
        areaRequest.setLeftY(null);
        areaRequest.setRightX(null);
        areaRequest.setRightY(null);

        assertFalse(areaRequest.isDefined());

        areaRequest.setRightY(10);
        assertFalse(areaRequest.isDefined());

        areaRequest.setRightX(10);
        assertFalse(areaRequest.isDefined());

        areaRequest.setLeftY(10);
        assertFalse(areaRequest.isDefined());

        areaRequest.setLeftX(10);
        assertTrue(areaRequest.isDefined());
    }

    @Test
    public void isValid() {
        areaRequest.setLeftX(10);
        areaRequest.setLeftY(10);
        areaRequest.setRightX(10);
        areaRequest.setRightY(10);

        assertTrue(areaRequest.isDefined());
        assertFalse(areaRequest.isValid());

        areaRequest.setRightX(100);
        assertFalse(areaRequest.isValid());

        areaRequest.setRightY(100);
        assertTrue(areaRequest.isValid());
    }

}
