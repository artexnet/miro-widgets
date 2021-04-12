package com.miro.hw.artexnet.storage;

import com.miro.hw.artexnet.domain.Widget;
import com.miro.hw.artexnet.common.immutable.Area;
import com.miro.hw.artexnet.common.immutable.WidgetsCollection;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface WidgetStorage extends Storage {

    /**
     * Creates/stores a new widget.
     */
    Widget createWidget(Widget widget);

    /**
     * Gets specified paged result of widgets list. The result list can be optionally filtered
     * to include only widgets that fully fall into the specified area.
     *
     * @param page the requested page (0-based)
     * @param size items count per page
     * @param area *optional: specifies area filter for widgets
     * @return the list of widgets.
     */
    WidgetsCollection getWidgets(int page, int size, @Nullable Area area);

    /**
     * Gets the widget by specified ID.
     */
    Optional<Widget> getById(long widgetId);

    /**
     * Gets the total count of stored widgets.
     */
    long getWidgetsCount();

    /**
     * Gets the count of stored widgets filtered by inclusive area.
     */
    long getWidgetsCount(Area area);

    /**
     * Check/Applies provided updates to the specified widget.
     * @return updates widget.
     */
    Widget updateWidget(Widget widget);

    /**
     * Deleted the widget by te specified ID.
     */
    void deleteById(long widgetId);

}
