package com.miro.hw.artexnet.storage.db;

import com.miro.hw.artexnet.domain.Widget;
import com.miro.hw.artexnet.common.immutable.Area;
import com.miro.hw.artexnet.common.immutable.WidgetsCollection;
import com.miro.hw.artexnet.exception.NotFoundException;
import com.miro.hw.artexnet.exception.ValidationException;
import com.miro.hw.artexnet.storage.WidgetStorage;
import com.miro.hw.artexnet.storage.StorageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@Profile("!local")
public class WidgetDatabaseStorage implements WidgetStorage {

    private final WidgetRepository repository;

    /**
     * Initializes a new instance of the class.
     */
    @Autowired
    public WidgetDatabaseStorage(WidgetRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Widget createWidget(Widget widget) {
        return storeWidgetUpdates(widget);
    }

    @Override
    public WidgetsCollection getWidgets(int page, int size, Area area) {
        List<WidgetEntity> entityList;
        long total;
        if (area != null) {
            int leftX = area.getLeftBottom().getXAxis();
            int leftY = area.getLeftBottom().getYAxis();
            int rightX = area.getRightTop().getXAxis();
            int rightY = area.getRightTop().getYAxis();

            entityList = repository.findAllInAreaOrderedByZIndex(leftX, leftY, rightX, rightY, PageRequest.of(page, size));
            total = getWidgetsCount(area);
        } else {
            entityList = repository.findAllOrderedByZIndex(PageRequest.of(page, size));
            total = getWidgetsCount();
        }

        List<Widget> widgets = entityList.stream().map(this::fromEntity).collect(Collectors.toList());
        return new WidgetsCollection(total, widgets);
    }

    @Override
    public Optional<Widget> getById(long widgetId) {
        Optional<WidgetEntity> entityOptional = repository.findById(widgetId);
        return entityOptional.map(this::fromEntity);
    }

    @Override
    public long getWidgetsCount() {
        return repository.count();
    }

    @Override
    public long getWidgetsCount(Area area) {
        int leftX = area.getLeftBottom().getXAxis();
        int leftY = area.getLeftBottom().getYAxis();
        int rightX = area.getRightTop().getXAxis();
        int rightY = area.getRightTop().getYAxis();
        return repository.countByArea(leftX, leftY, rightX, rightY);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Widget updateWidget(Widget widget) {
        // make sure widget exists and updates are provided
        Widget storedWidget = getById(widget.getId()).orElseThrow(NotFoundException::new);
        if (storedWidget.equals(widget))
            throw new ValidationException("Nothing to update");

        return storeWidgetUpdates(widget);
    }

    @Override
    @Transactional
    public void deleteById(long widgetId) {
        final WidgetEntity widgetEntity = repository.findById(widgetId).orElseThrow(NotFoundException::new);
        repository.delete(widgetEntity);
    }

    // region <PERSIST/REARRANGE>
    // all functions execute in the transactional context

    private Widget storeWidgetUpdates(Widget widget) {
        if (widget.getZ_index() == null)
            return storeWidgetWithMissingZIndex(widget);

        boolean duplicateFound = repository.countByZIndex(widget.getZ_index()) != 0;
        if (!duplicateFound) {
            WidgetEntity entityWithUpdates = repository.save(toEntity(widget));
            return fromEntity(entityWithUpdates);
        }

        return saveWidgetAndRearrangeZIndexes(widget);
    }

    private Widget storeWidgetWithMissingZIndex(Widget widget) {
        WidgetEntity top = repository.getTopByOrderByZindexDesc();

        // top is missing for the very first record
        int zIndex = top == null ? 1 : 1 + top.getZindex();
        widget.setZ_index(zIndex);

        WidgetEntity entityWithUpdates = repository.save(toEntity(widget));
        return fromEntity(entityWithUpdates);
    }

    /**
     * Rearranges all related widgets' z-indexes
     * to exclude duplicates introduced by the provided widget.
     */
    private Widget saveWidgetAndRearrangeZIndexes(Widget widget) {
        List<WidgetEntity> entitiesToUpdate = new ArrayList<>();
        WidgetEntity targetEntity = toEntity(widget);

        int zIndex = widget.getZ_index();  // z-index already assigned by this point
        final List<WidgetEntity> entitiesOrderedByIndex = repository.findAllWithEqualOrGreaterZIndex(zIndex);

        for (int i = entitiesOrderedByIndex.size() - 1; i >= 0; i--) {
            final WidgetEntity entity = entitiesOrderedByIndex.get(i);
            if (zIndex == entity.getZindex()) {
                zIndex = 1 + entity.getZindex();
                entity.setZindex(zIndex);
                entitiesToUpdate.add(entity);
            } else {
                break;
            }
        }
        repository.saveAll(entitiesToUpdate);
        repository.flush();
        targetEntity = repository.save(targetEntity);
        return fromEntity(targetEntity);
    }

    // endregion

    // region <MAPPERS>

    public WidgetEntity toEntity(Widget widget) {
        if (widget == null)
            return null;

        return WidgetEntity.builder()
                .id(widget.getId())
                .zindex(widget.getZ_index())
                .x(widget.getX())
                .y(widget.getY())
                .width(widget.getWidth())
                .height(widget.getHeight())
                .build();
    }

    public Widget fromEntity(WidgetEntity entity) {
        if (entity == null)
            return null;

        return Widget.builder()
                .id(entity.getId())
                .z_index(entity.getZindex())
                .x(entity.getX())
                .y(entity.getY())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .dateModified(entity.getDateModified())
                .build();
    }

    // endregion

    @Override
    public StorageType getStorageType() {
        return StorageType.DATABASE;
    }
}
