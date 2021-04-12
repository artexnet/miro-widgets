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
import com.miro.hw.artexnet.storage.db.WidgetRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WidgetDatabaseStorageTest extends BaseTestUnit {

    @Mock
    private WidgetRepository repository;
    private Widget widget;

    // Storage to test
    private WidgetDatabaseStorage storage;

    @Before
    public void setUp() {
        super.setUp();
        widget = DataHelper.createWidget(1, 50, 50, 100, 100, LocalDateTime.now());
        storage = new WidgetDatabaseStorage(repository);
    }

    @After
    public void tearDown() { }

    ////////////////////////////////////

    /**
     * @see WidgetDatabaseStorage#createWidget(com.miro.hw.artexnet.domain.Widget)
     */
    @Test
    public void createWidget_withMissingZIndex() {
        WidgetEntity entity = storage.toEntity(widget);

        // setup mocks
        doReturn(null).when(repository).getTopByOrderByZindexDesc();
        doReturn(entity).when(repository).save(any(WidgetEntity.class));

        // test
        widget.setZ_index(null);
        Widget storedWidget = storage.createWidget(widget);

        // validate
        assertNotNull(storedWidget);
        assertEquals(1, storedWidget.getZ_index().longValue());

        verify(repository, times(1)).getTopByOrderByZindexDesc();

        ArgumentCaptor<WidgetEntity> entityCaptor = ArgumentCaptor.forClass(WidgetEntity.class);
        verify(repository, times(1)).save(entityCaptor.capture());
        WidgetEntity entityArgument = entityCaptor.getValue();
        assertNotNull(entityArgument.getId());  // ID assigned
        assertEquals(1, entityArgument.getZindex());  // first auto-assigned z-index
    }

    /**
     * @see WidgetDatabaseStorage#createWidget(com.miro.hw.artexnet.domain.Widget)
     */
    @Test
    public void createWidget_withDuplicateZIndex() {
        WidgetEntity entity = storage.toEntity(widget);

        // setup mocks
        doReturn(0L).when(repository).countByZIndex(anyInt());
        doReturn(entity).when(repository).save(any(WidgetEntity.class));

        // test
        Widget storedWidget = storage.createWidget(widget);

        // validate
        assertNotNull(storedWidget);
        assertEquals(1, storedWidget.getZ_index().longValue());

        verify(repository, times(1)).countByZIndex(eq(widget.getZ_index()));
        verify(repository, times(1)).save(any(WidgetEntity.class));
        verifyNoMoreInteractions(repository);
    }

    /**
     * @see WidgetDatabaseStorage#getWidgets(int, int, com.miro.hw.artexnet.common.immutable.Area)
     */
    @Test
    public void getWidgets_areaNotDefined() {
        Widget widget1 = DataHelper.createWidget(1, 0, 0, 20, 20, LocalDateTime.now());
        Widget widget2 = DataHelper.createWidget(2, 100, 100, 20, 20, LocalDateTime.now());
        List<WidgetEntity> list = Arrays.asList(storage.toEntity(widget2), storage.toEntity(widget1));
        long total = 10;

        // setup mocks
        doReturn(list).when(repository).findAllOrderedByZIndex(any(Pageable.class));
        doReturn(total).when(repository).count();

        // test
        WidgetsCollection widgets = storage.getWidgets(0, 10, null);

        // validate
        assertNotNull(widgets);
        assertNotNull(widgets.getItems());
        assertEquals(list.size(), widgets.getItems().size());
        assertEquals(total, widgets.getTotalCount());

        verify(repository, times(1)).findAllOrderedByZIndex(eq(PageRequest.of(0, 10)));
        verify(repository, times(1)).count();
        verifyNoMoreInteractions(repository);
    }

    /**
     * @see WidgetDatabaseStorage#getWidgets(int, int, com.miro.hw.artexnet.common.immutable.Area)
     */
    @Test
    public void getWidgets_areaDefined() {
        Widget widget1 = DataHelper.createWidget(1, 0, 0, 20, 20, LocalDateTime.now());
        Widget widget2 = DataHelper.createWidget(2, 100, 100, 20, 20, LocalDateTime.now());
        List<WidgetEntity> list = Arrays.asList(storage.toEntity(widget2), storage.toEntity(widget1));
        long total = 10;
        Area area = new Area(new Point(-50, -50), new Point(50, 50));

        // setup mocks
        doReturn(list).when(repository).findAllInAreaOrderedByZIndex(anyInt(), anyInt(), anyInt(), anyInt(), any(Pageable.class));
        doReturn(total).when(repository).countByArea(anyInt(), anyInt(), anyInt(), anyInt());

        // test
        WidgetsCollection widgets = storage.getWidgets(0, 10, area);

        // validate
        assertNotNull(widgets);
        assertNotNull(widgets.getItems());
        assertEquals(list.size(), widgets.getItems().size());
        assertEquals(total, widgets.getTotalCount());

        ArgumentCaptor<Integer> leftXCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> leftYCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> rightXCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> rightYCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(repository, times(1)).findAllInAreaOrderedByZIndex(leftXCaptor.capture(),
                leftYCaptor.capture(), rightXCaptor.capture(), rightYCaptor.capture(), any(Pageable.class));
        assertEquals(area.getLeftBottom().getXAxis(), leftXCaptor.getValue().intValue());
        assertEquals(area.getLeftBottom().getYAxis(), leftYCaptor.getValue().intValue());
        assertEquals(area.getRightTop().getXAxis(), rightXCaptor.getValue().intValue());
        assertEquals(area.getRightTop().getYAxis(), rightYCaptor.getValue().intValue());

        verify(repository, times(1)).countByArea(leftXCaptor.capture(), leftYCaptor.capture(),
                rightXCaptor.capture(), rightYCaptor.capture());
        assertEquals(area.getLeftBottom().getXAxis(), leftXCaptor.getValue().intValue());
        assertEquals(area.getLeftBottom().getYAxis(), leftYCaptor.getValue().intValue());
        assertEquals(area.getRightTop().getXAxis(), rightXCaptor.getValue().intValue());
        assertEquals(area.getRightTop().getYAxis(), rightYCaptor.getValue().intValue());

        verifyNoMoreInteractions(repository);
    }

    /**
     * @see WidgetDatabaseStorage#getById(long)
     */
    @Test
    public void getById_notFound() {
        // setup mocks
        doReturn(Optional.empty()).when(repository).findById(anyLong());

        // test
        Optional<Widget> widgetOptional = storage.getById(widget.getId());

        // validate
        assertNotNull(widgetOptional);
        assertTrue(widgetOptional.isEmpty());

        verify(repository, times(1)).findById(eq(widget.getId()));
        verifyNoMoreInteractions(repository);
    }

    /**
     * @see WidgetDatabaseStorage#getById(long)
     */
    @Test
    public void getById_found() {
        // setup mocks
        WidgetEntity entity = storage.toEntity(widget);
        doReturn(Optional.of(entity)).when(repository).findById(widget.getId());

        // test
        Optional<Widget> widgetOptional = storage.getById(widget.getId());

        // validate
        assertNotNull(widgetOptional);
        assertTrue(widgetOptional.isPresent());
        Widget acquiredWidget = widgetOptional.get();
        assertEquals(widget.getId(), acquiredWidget.getId());

        verify(repository, times(1)).findById(eq(widget.getId()));
        verifyNoMoreInteractions(repository);
    }

    /**
     * @see WidgetDatabaseStorage#getWidgetsCount()
     */
    @Test
    public void getWidgetsCount() {
        // setup mocks
        doReturn(10L).when(repository).count();

        // test
        long count = storage.getWidgetsCount();

        // validate
        assertEquals(10L, count);
        verify(repository, times(1)).count();
        verifyNoMoreInteractions(repository);
    }

    /**
     * @see WidgetDatabaseStorage#getWidgetsCount(com.miro.hw.artexnet.common.immutable.Area)
     */
    @Test
    public void getWidgetsCountInArea() {
        Area area = new Area(new Point(0, 0), new Point(50, 50));

        // setup mocks
        doReturn(10L).when(repository).countByArea(anyInt(), anyInt(), anyInt(), anyInt());

        // test
        long count = storage.getWidgetsCount(area);

        // validate
        assertEquals(10L, count);

        ArgumentCaptor<Integer> leftXCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> leftYCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> rightXCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> rightYCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(repository, times(1)).countByArea(leftXCaptor.capture(), leftYCaptor.capture(),
                rightXCaptor.capture(), rightYCaptor.capture());
        assertEquals(area.getLeftBottom().getXAxis(), leftXCaptor.getValue().intValue());
        assertEquals(area.getLeftBottom().getYAxis(), leftYCaptor.getValue().intValue());
        assertEquals(area.getRightTop().getXAxis(), rightXCaptor.getValue().intValue());
        assertEquals(area.getRightTop().getYAxis(), rightYCaptor.getValue().intValue());

        verifyNoMoreInteractions(repository);
    }

    /**
     * @see WidgetDatabaseStorage#updateWidget(com.miro.hw.artexnet.domain.Widget)
     */
    @Test(expected = NotFoundException.class)
    public void updateWidget_notFound() {
        // setup mocks
        doReturn(Optional.empty()).when(repository).findById(anyLong());

        // test
        try {
            storage.updateWidget(widget);
        } catch (Exception ex) {
            verify(repository, times(1)).findById(eq(widget.getId()));
            verifyNoMoreInteractions(repository);

            throw ex;
        }
    }

    /**
     * @see WidgetDatabaseStorage#updateWidget(com.miro.hw.artexnet.domain.Widget)
     */
    @Test(expected = ValidationException.class)
    public void updateWidget_nothingToUpdate() {
        WidgetEntity entity = storage.toEntity(widget);

        // setup mocks
        doReturn(Optional.of(entity)).when(repository).findById(anyLong());

        // test
        try {
            storage.updateWidget(widget);
        } catch (Exception ex) {
            verify(repository, times(1)).findById(eq(widget.getId()));
            verifyNoMoreInteractions(repository);

            throw ex;
        }
    }

    /**
     * @see WidgetDatabaseStorage#deleteById(long)
     */
    @Test(expected = NotFoundException.class)
    public void deleteById_notFound() {
        // setup mocks
        doReturn(Optional.empty()).when(repository).findById(anyLong());

        // test
        try {
            storage.deleteById(widget.getId());
        } catch (Exception ex) {
            assertTrue(ex instanceof NotFoundException);

            verify(repository, times(1)).findById(eq(widget.getId()));

            throw ex;
        }
    }

    /**
     * @see WidgetDatabaseStorage#deleteById(long)
     */
    @Test
    public void deleteById_found() {
        // setup mocks
        WidgetEntity entity = storage.toEntity(widget);
        doReturn(Optional.of(entity)).when(repository).findById(widget.getId());

        // test
        storage.deleteById(widget.getId());

        // validate
        verify(repository, times(1)).findById(eq(widget.getId()));
    }

}
