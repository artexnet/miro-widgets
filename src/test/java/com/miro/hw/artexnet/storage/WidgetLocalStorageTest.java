package com.miro.hw.artexnet.storage;

import com.miro.hw.artexnet.BaseTestUnit;
import com.miro.hw.artexnet.DataHelper;
import com.miro.hw.artexnet.common.immutable.Area;
import com.miro.hw.artexnet.common.immutable.Point;
import com.miro.hw.artexnet.common.immutable.WidgetsCollection;
import com.miro.hw.artexnet.domain.Widget;
import com.miro.hw.artexnet.exception.NotFoundException;
import com.miro.hw.artexnet.exception.ValidationException;
import com.miro.hw.artexnet.storage.db.WidgetDatabaseStorage;
import com.miro.hw.artexnet.storage.db.WidgetEntity;
import com.miro.hw.artexnet.storage.local.WidgetLocalStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WidgetLocalStorageTest extends BaseTestUnit {

    private Widget widget;

    // Storage to test
    private WidgetLocalStorage storage;

    @Before
    public void setUp() {
        super.setUp();
        widget = DataHelper.createWidget(1, 50, 50, 100, 100, LocalDateTime.now());
        storage = new WidgetLocalStorage();
    }

    @After
    public void tearDown() { }

    ////////////////////////////////////

    /**
     * @see WidgetLocalStorage#createWidget(com.miro.hw.artexnet.domain.Widget)
     */
    @Test
    public void createWidget_withMissingZIndex() {
        // test
        Widget storedWidget = storage.createWidget(widget);

        // validate
        assertNotNull(storedWidget);
        assertNotNull(storedWidget.getId());
        assertNotNull(storedWidget.getDateModified());
        assertEquals(1, storedWidget.getId().longValue());
        assertEquals(1, storedWidget.getZ_index().intValue());
    }

    /**
     * @see WidgetLocalStorage#createWidget(com.miro.hw.artexnet.domain.Widget)
     */
    @Test
    public void createWidget_withDuplicateZIndex() {
        storage.createWidget(widget);
        Widget widget2 = DataHelper.createWidget(1, 0, 0, 50, 50, LocalDateTime.now());

        // test
        Widget storedWidget = storage.createWidget(widget2);

        // validate
        assertNotNull(storedWidget);
        assertNotNull(storedWidget.getId());
        assertNotNull(storedWidget.getDateModified());
        assertEquals(2, storedWidget.getId().longValue());
        assertEquals(1, storedWidget.getZ_index().intValue());
    }

    /**
     * @see WidgetLocalStorage#getWidgets(int, int, com.miro.hw.artexnet.common.immutable.Area)
     */
    @Test
    public void getWidgets_areaNotDefined() {
        Widget widget1 = storage.createWidget(DataHelper.createWidget(1, 0, 0, 20, 20));
        Widget widget2 = storage.createWidget(DataHelper.createWidget(2, 100, 100, 20, 20));
        List<Widget> list = Arrays.asList(widget2, widget1);
        long total = list.size();

        // test
        WidgetsCollection widgets = storage.getWidgets(0, 10, null);

        // validate
        assertNotNull(widgets);
        assertNotNull(widgets.getItems());
        assertEquals(list.size(), widgets.getItems().size());
        assertEquals(total, widgets.getTotalCount());
    }

    /**
     * @see WidgetLocalStorage#getWidgets(int, int, com.miro.hw.artexnet.common.immutable.Area)
     */
    @Test
    public void getWidgets_areaDefined() {
        Widget widget1 = storage.createWidget(DataHelper.createWidget(1, 0, 0, 20, 20));
        storage.createWidget(DataHelper.createWidget(2, 100, 100, 20, 20));
        List<Widget> list = Collections.singletonList(widget1);
        Area area = new Area(new Point(-50, -50), new Point(50, 50));
        long total = list.size();

        // test
        WidgetsCollection widgets = storage.getWidgets(0, 10, area);

        // validate
        assertNotNull(widgets);
        assertNotNull(widgets.getItems());
        assertEquals(list.size(), widgets.getItems().size());
        assertEquals(total, widgets.getTotalCount());
    }

    /**
     * @see WidgetLocalStorage#getById(long)
     */
    @Test
    public void getById_notFound() {
        // test
        Optional<Widget> widgetOptional = storage.getById(widget.getId());

        // validate
        assertNotNull(widgetOptional);
        assertTrue(widgetOptional.isEmpty());
    }

    /**
     * @see WidgetLocalStorage#getById(long)
     */
    @Test
    public void getById_found() {
        storage.createWidget(widget);

        // test
        Optional<Widget> widgetOptional = storage.getById(widget.getId());

        // validate
        assertNotNull(widgetOptional);
        assertTrue(widgetOptional.isPresent());
    }

    /**
     * @see WidgetLocalStorage#getWidgetsCount()
     */
    @Test
    public void getWidgetsCount() {
        // test
        long count = storage.getWidgetsCount();

        // validate
        assertEquals(0, count);

        // arrange
        storage.createWidget(widget);

        // test
        count = storage.getWidgetsCount();

        // validate
        assertEquals(1, count);
    }

    /**
     * @see WidgetLocalStorage#getWidgetsCount(com.miro.hw.artexnet.common.immutable.Area)
     */
    @Test
    public void getWidgetsCountInArea() {
        Area area = new Area(new Point(0, 0), new Point(50, 50));

        // test
        long count = storage.getWidgetsCount(area);

        // validate
        assertEquals(0, count);

        // arrange
        storage.createWidget(DataHelper.createWidget(1, 20, 20, 20, 20));
        storage.createWidget(DataHelper.createWidget(2, 100, 100, 20, 20));

        // test
        count = storage.getWidgetsCount(area);

        // validate
        assertEquals(1, count);
    }

    /**
     * @see WidgetLocalStorage#updateWidget(com.miro.hw.artexnet.domain.Widget)
     */
    @Test(expected = NotFoundException.class)
    public void updateWidget_notFound() {
        try {
            // test
            storage.updateWidget(widget);
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("not found"));
            throw ex;
        }
    }

    /**
     * @see WidgetLocalStorage#updateWidget(com.miro.hw.artexnet.domain.Widget)
     */
    @Test(expected = ValidationException.class)
    public void updateWidget_nothingToUpdate() {
        storage.createWidget(widget);

        try {
            // test
            storage.updateWidget(widget);
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("Nothing to update"));
            throw ex;
        }
    }

    /**
     * @see WidgetLocalStorage#updateWidget(com.miro.hw.artexnet.domain.Widget)
     */
    @Test
    public void updateWidget() {
        widget = storage.createWidget(widget);

        Widget widgetWithUpdates = DataHelper.createWidget(999, 10, 10, 10, 10);
        widgetWithUpdates.setId(widget.getId());

        // test
        Widget updatedWidget = storage.updateWidget(widgetWithUpdates);

        assertNotNull(updatedWidget);
        assertNotNull(updatedWidget.getDateModified());
        assertEquals(widget.getId(), updatedWidget.getId());
        assertEquals(999, updatedWidget.getZ_index().intValue());
        assertEquals(10, updatedWidget.getX().intValue());
        assertEquals(10, updatedWidget.getY().intValue());
        assertEquals(10, updatedWidget.getWidth());
        assertEquals(10, updatedWidget.getHeight());
    }

    /**
     * @see WidgetLocalStorage#deleteById(long)
     */
    @Test(expected = NotFoundException.class)
    public void deleteById_notFound() {
        try {
            // test
            storage.deleteById(widget.getId());
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("not found"));
            throw ex;
        }
    }

    /**
     * @see WidgetLocalStorage#deleteById(long)
     */
    @Test
    public void deleteById_found() {
        storage.createWidget(widget);

        // test
        storage.deleteById(widget.getId());
    }

}
