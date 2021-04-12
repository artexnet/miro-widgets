package com.miro.hw.artexnet.domain;

import com.miro.hw.artexnet.BaseTestUnit;
import com.miro.hw.artexnet.common.immutable.Area;
import com.miro.hw.artexnet.common.immutable.Point;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import java.time.LocalDateTime;
import java.util.Set;

import static com.miro.hw.artexnet.DataHelper.createArea;
import static com.miro.hw.artexnet.DataHelper.createWidget;
import static org.junit.Assert.*;

public class WidgetTest extends BaseTestUnit {
    private static final Point LEFT_BOTTOM = new Point(0, 0);
    private static final Point RIGHT_TOP = new Point(100, 100);

    private Widget widget;
    private Set<ConstraintViolation<Widget>> constraintViolations;

    @Before
    public void setUp() {
        super.setUp();

        widget = createWidget();
        constraintViolations = validator.validate(widget);
        assertEquals(0, constraintViolations.size());
    }

    @After
    public void tearDown() { }

    ////////////////////////////////////

    @Test
    public void testCoordinatesMissing() {
        // x coordinate cannot be null
        widget.setX(null);

        constraintViolations = validator.validate(widget);

        assertEquals(1, constraintViolations.size());
        assertEquals("must not be null", constraintViolations.iterator().next().getMessage());

        // y coordinate cannot be null
        widget.setX(100);
        widget.setY(null);

        constraintViolations = validator.validate(widget);

        assertEquals(1, constraintViolations.size());
        assertEquals("must not be null", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void testInvalidWithOrHeight() {
        // width must be positive
        widget.setWidth(0);

        constraintViolations = validator.validate(widget);

        assertEquals(1, constraintViolations.size());
        assertEquals("must be greater than or equal to 1", constraintViolations.iterator().next().getMessage());

        // height must be positive
        widget.setWidth(100);
        widget.setHeight(-1);

        constraintViolations = validator.validate(widget);

        assertEquals(1, constraintViolations.size());
        assertEquals("must be greater than or equal to 1", constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void fitsInArea_falseFromBottomPoint() {
        Widget widget = createWidget(999, 0, 0, 50, 50, LocalDateTime.now());
        Area area = createArea(LEFT_BOTTOM, RIGHT_TOP);

        // test
        boolean fitsInArea = Widget.fitsInArea(widget, area);

        assertFalse(fitsInArea);
    }

    @Test
    public void fitsInArea_falseFromTopPoint() {
        Widget widget = createWidget(999, 100, 100, 50, 50, LocalDateTime.now());
        Area area = createArea(LEFT_BOTTOM, RIGHT_TOP);

        // test
        boolean fitsInArea = Widget.fitsInArea(widget, area);

        assertFalse(fitsInArea);
    }

    @Test
    public void fitsInArea() {
        Widget widget = createWidget(999, 25, 25, 50, 50, LocalDateTime.now());
        Area area = createArea(LEFT_BOTTOM, RIGHT_TOP);

        // test
        boolean fitsInArea = Widget.fitsInArea(widget, area);

        assertTrue(fitsInArea);
    }

}
