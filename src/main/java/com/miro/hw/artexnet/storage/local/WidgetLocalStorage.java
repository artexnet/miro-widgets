package com.miro.hw.artexnet.storage.local;

import com.miro.hw.artexnet.domain.Widget;
import com.miro.hw.artexnet.common.immutable.Area;
import com.miro.hw.artexnet.common.immutable.WidgetsCollection;
import com.miro.hw.artexnet.exception.NotFoundException;
import com.miro.hw.artexnet.exception.ValidationException;
import com.miro.hw.artexnet.storage.WidgetStorage;
import com.miro.hw.artexnet.storage.StorageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static com.miro.hw.artexnet.common.ErrorCode.ENTITY_NOT_FOUND;

@Slf4j
@Service
@Profile("!database")
public class WidgetLocalStorage implements WidgetStorage {

    // Main container for stored Widgets
    private transient volatile Widget[] widgetsContainer;

    // Sequence container for Widgets quick retrieval {ID/Index}
    private final ConcurrentNavigableMap<Long, Integer> widgetsSequence;

    // Unique ID sequence provider
    private final AtomicLong idSequence;

    // Global shared lock
    private final transient ReentrantLock lock;

    /**
     * Initializes a new instance of the class.
     */
    public WidgetLocalStorage() {
        widgetsContainer = new Widget[0];
        widgetsSequence = new ConcurrentSkipListMap<>(Collections.reverseOrder());
        idSequence = new AtomicLong(0);
        lock = new ReentrantLock(true);
    }

    @Override
    public Widget createWidget(Widget widget) {
        lock.lock();
        try {
            // assign next ID
            widget.setId(idSequence.incrementAndGet());

            // store/get created widget
            return save(widget);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public WidgetsCollection getWidgets(int page, int size, Area area) {
        Widget[] container = area == null ? getWidgetsContainer() : getWidgetsContainer(area);
        int totalCount = container.length;
        if (page * size > totalCount)
            return new WidgetsCollection(0, Collections.emptyList());

        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalCount);

        List<Widget> resultList = new ArrayList<>(size);
        for (int i = startIndex; i < endIndex; i++) {
            try {
                resultList.add(container[i]);
            } catch (ArrayIndexOutOfBoundsException ex) {
                log.debug(ex.getMessage());
                break;  // skip the rest
            }
        }
        return new WidgetsCollection(totalCount, resultList);
    }

    @Override
    public Optional<Widget> getById(long widgetId) {
        try {
            Integer containerIndex = widgetsSequence.get(widgetId);
            return containerIndex == null
                    ? Optional.empty()
                    : Optional.of(getWidgetsContainer()[containerIndex]);
        } catch (IndexOutOfBoundsException ex) {
            log.debug(ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public long getWidgetsCount() {
        return getWidgetsContainer().length;
    }

    @Override
    public long getWidgetsCount(Area area) {
        return getWidgetsContainer(area).length;
    }

    @Override
    public Widget updateWidget(Widget widget) {
        lock.lock();
        try {
            final long widgetId = widget.getId();
            if (!widgetsSequence.containsKey(widgetId))
                throw new NotFoundException(String.format("Widget [%s] not found", widgetId));

            Widget[] elements = getWidgetsContainer();
            Widget storedWidget = elements[widgetsSequence.get(widgetId)];

            // make sure updates provided
            if (storedWidget.equals(widget))
                throw new ValidationException("Nothing to update");

            // apply/get updated widget
            return save(widget);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void deleteById(long widgetId) {
        lock.lock();
        try {
            if (!widgetsSequence.containsKey(widgetId))
                throw new NotFoundException(String.format("Widget [%s] not found", widgetId), ENTITY_NOT_FOUND);

            final Widget[] elements = getWidgetsContainer();
            final int total = elements.length;
            final int index = widgetsSequence.get(widgetId);

            Widget[] newElements = new Widget[total - 1];
            System.arraycopy(elements, 0, newElements, 0, index);
            System.arraycopy(elements, index + 1, newElements, index, total - index - 1);

            widgetsSequence.remove(widgetId);
            for (int i = 0; i < newElements.length; i++) {
                widgetsSequence.put(elements[i].getId(), i);
            }
            setWidgetsContainer(newElements);
        } finally {
            lock.unlock();
        }
    }

    // region <PERSISTENCE>

    private Widget save(Widget widget) {
        final Widget[] elements = getWidgetsContainer();

        // make sure Widget has Z-Index and Update Time assigned
        if (widget.getZ_index() == null)
            widget.setZ_index(elements.length == 0 ? 1 : 1 + elements[0].getZ_index());
        widget.setDateModified(LocalDateTime.now());

        // initialize a new container for the very first Widget
        final int total = elements.length;
        if (total == 0) {
            setWidgetsContainer(new Widget[] { widget });
            widgetsSequence.put(widget.getId(), 0);
            return widget;
        }

        // assign new widget and shift z-index for existing widgets if applicable
        Widget[] newElements = storeAndGetShiftedWidgetsSequence(widget, elements);
        setWidgetsContainer(newElements);

        return widget;
    }

    private Widget[] storeAndGetShiftedWidgetsSequence(final Widget widget, final Widget[] elements) {
        final int total = elements.length;
        final int zIndex = widget.getZ_index();
        final int zMax = getMaxZIndex(elements);
        final int zMin = getMinZIndex(elements);

        Widget[] newElements;
        if (zIndex > zMax) {
            // adding widget with the greatest z-index to the beginning of the sequence
            newElements = new Widget[total + 1];
            newElements[0] = widget;
            System.arraycopy(elements, 0, newElements, 1, total);

            widgetsSequence.put(widget.getId(), 0);
            for (int i = 0; i < elements.length; i++) {
                widgetsSequence.put(elements[i].getId(), i + 1);
            }
        } else if (zIndex < zMin) {
            // adding widget with the lowest z-index to the end of the sequence
            newElements = Arrays.copyOf(elements, total + 1);
            newElements[total] = widget;

            widgetsSequence.put(widget.getId(), total);
        } else {
            // assigning the corresponding position on the sequence for the new widget,
            // incrementing z-indexes for widgets that get duplicate values because of sequence shifting.
            newElements = new Widget[total + 1];
            int shiftIndex = 0;
            for (int i = total - 1; i >= 0; i--) {
                Widget nextWidget = elements[i];
                if (nextWidget.getZ_index() >= zIndex) {
                    shiftIndex = i;
                    newElements[i + 1] = widget;
                    widgetsSequence.put(widget.getId(), i + 1);
                    break;
                } else {
                    newElements[i + 1] = nextWidget;
                    widgetsSequence.put(nextWidget.getId(), i + 1);
                }
            }

            int breakIndex = shiftIndex;
            for (; breakIndex >= 0; breakIndex--) {
                Widget shiftingWidget = elements[breakIndex];
                Widget assignedWidget = newElements[breakIndex + 1];

                if (shiftingWidget.getZ_index().equals(assignedWidget.getZ_index())) {
                    Widget copyWidget = clone(shiftingWidget);
                    copyWidget.setZ_index(shiftingWidget.getZ_index() + 1);
                    copyWidget.setDateModified(LocalDateTime.now());
                    newElements[breakIndex] = copyWidget;
                    widgetsSequence.put(shiftingWidget.getId(), breakIndex);
                } else {
                    break;  // Gap found
                }
            }

            if (breakIndex >= 0) {
                System.arraycopy(elements, 0, newElements, 0, breakIndex + 1);
            }
        }
        return newElements;
    }

    // endregion

    // region <HELPERS>

    final Widget clone(Widget widget) {
        return Widget.builder()
                .id(widget.getId())
                .z_index(widget.getZ_index())
                .x(widget.getX())
                .y(widget.getY())
                .width(widget.getWidth())
                .height(widget.getHeight())
                .dateModified(widget.getDateModified())
                .build();
    }

    final Widget[] getWidgetsContainer(Area area) {
        return Arrays.stream(getWidgetsContainer())
                .filter(widget -> Widget.fitsInArea(widget, area))
                .toArray(Widget[]::new);
    }

    final Widget[] getWidgetsContainer() {
        return widgetsContainer;
    }

    final void setWidgetsContainer(Widget[] container) {
        widgetsContainer = container;
    }

    private static int getMinZIndex(Widget[] elements) {
        return elements.length == 0 ? Integer.MIN_VALUE : elements[elements.length - 1].getZ_index();
    }

    private static  int getMaxZIndex(Widget[] elements) {
        return elements.length == 0 ? Integer.MAX_VALUE : elements[0].getZ_index();
    }

    // endregion

    @Override
    public StorageType getStorageType() {
        return StorageType.LOCAL;
    }

}
