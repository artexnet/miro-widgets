package com.miro.hw.artexnet.api;

import com.miro.hw.artexnet.api.dto.AreaRequest;
import com.miro.hw.artexnet.api.dto.PageRequest;
import com.miro.hw.artexnet.common.ErrorCode;
import com.miro.hw.artexnet.configuration.StorageProviderConfig;
import com.miro.hw.artexnet.domain.Widget;
import com.miro.hw.artexnet.common.immutable.Area;
import com.miro.hw.artexnet.common.immutable.Point;
import com.miro.hw.artexnet.common.immutable.WidgetsCollection;
import com.miro.hw.artexnet.exception.ValidationException;
import com.miro.hw.artexnet.storage.WidgetStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/v1/widgets", produces = MediaType.APPLICATION_JSON_VALUE)
public class WidgetController {

    private final WidgetStorage storage;

    @Autowired
    public WidgetController(StorageProviderConfig storageProviderConfig) {
        this.storage = storageProviderConfig.getConfiguredStorage();
    }

    // region <API>

    @PostMapping
    public ResponseEntity<Widget> createWidget(@RequestBody @Valid Widget widget) {
        if (widget.getId() != null) {
            throw new ValidationException("Existing widget provided for insertion", ErrorCode.REQUEST_NOT_VALID);
        }
        Widget storedWidget = storage.createWidget(widget);
        return new ResponseEntity<>(storedWidget, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<WidgetsCollection> getWidgets(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            AreaRequest areaRequest) {

        PageRequest pageRequest = new PageRequest(page, size);
        Area area = validateAndGetArea(areaRequest);
        WidgetsCollection widgets = storage.getWidgets(pageRequest.getPage(), pageRequest.getSize(), area);
        return new ResponseEntity<>(widgets, HttpStatus.OK);
    }

    @GetMapping("/{widgetId}")
    public ResponseEntity<Widget> getWidget(@PathVariable("widgetId") long widgetId) {
        return ResponseEntity.of(storage.getById(widgetId));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getWidgetsCount() {
        long count = storage.getWidgetsCount();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @PutMapping("/{widgetId}")
    public ResponseEntity<Widget> updateWidget(@PathVariable("widgetId") long widgetId, @Valid @RequestBody Widget widget) {
        if (widgetId != widget.getId())
            throw new ValidationException("Widget ID mismatch", ErrorCode.REQUEST_NOT_VALID);

        Widget updatedWidget = storage.updateWidget(widget);
        return new ResponseEntity<>(updatedWidget, HttpStatus.OK);
    }

    @DeleteMapping("/{widgetId}")
    public ResponseEntity<Void> removeWidget(@PathVariable("widgetId") long widgetId) {
        storage.deleteById(widgetId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // endregion

    private @Nullable Area validateAndGetArea(AreaRequest areaRequest) {
        if (areaRequest == null)
            return null;
        if (areaRequest.getLeftX() == null && areaRequest.getLeftY() == null & areaRequest.getRightX() == null && areaRequest.getRightY() == null)
            return null;
        if (!areaRequest.isValid()) {
            throw new ValidationException("Requested area is not valid " +
                    "(left/bottom and right/top coordinates are required)", ErrorCode.REQUEST_NOT_VALID);
        }
        return new Area(
                new Point(areaRequest.getLeftX(), areaRequest.getLeftY()),
                new Point(areaRequest.getRightX(), areaRequest.getRightY())
        );
    }

}
