package com.miro.hw.artexnet.api.dto;

import com.miro.hw.artexnet.BaseTestUnit;
import com.miro.hw.artexnet.exception.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PageRequestTest extends BaseTestUnit {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() { }

    ////////////////////////////////////

    @Test(expected = ValidationException.class)
    public void testPageValidation() {
        PageRequest pageRequest;
        try {
            pageRequest = new PageRequest(-1, 100);
            assertNull(pageRequest);
        } catch (Exception ex) {
            assertTrue(ex instanceof ValidationException);
            assertEquals("Page must be positive number", ex.getMessage());
            throw ex;
        }
    }

    @Test(expected = ValidationException.class)
    public void testPageSizeValidation() {
        PageRequest pageRequest;
        try {
            pageRequest = new PageRequest(PageRequest.DEFAULT_PAGE, 1000);
            assertNull(pageRequest);
        } catch (Exception ex) {
            assertTrue(ex instanceof ValidationException);
            assertEquals("Items chunk must be between 1 and 500", ex.getMessage());
            throw ex;
        }
    }

    @Test
    public void testInitialization() {
        PageRequest pageRequest = new PageRequest(10, 100);

        assertEquals(10, pageRequest.getPage());
        assertEquals(100, pageRequest.getSize());
    }

    @Test
    public void testDefaultInitialization() {
        PageRequest pageRequest = new PageRequest(null, null);

        assertEquals(PageRequest.DEFAULT_PAGE, pageRequest.getPage());
        assertEquals(PageRequest.DEFAULT_ITEMS_CHUNK, pageRequest.getSize());
    }

}
