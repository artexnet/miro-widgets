package com.miro.hw.artexnet;

import com.miro.hw.artexnet.common.immutable.Area;
import com.miro.hw.artexnet.common.immutable.Point;
import com.miro.hw.artexnet.domain.Widget;

import java.time.LocalDateTime;

import static com.miro.hw.artexnet.AbstractTest.getRandomNumber;
import static com.miro.hw.artexnet.AbstractTest.objectGenerator;

public class DataHelper {

    public static Widget createWidget() {
        Widget widget = objectGenerator.nextObject(Widget.class);
        widget.setWidth(getRandomNumber());
        widget.setHeight(getRandomNumber());
        return widget;
    }

    public static Widget createWidget(int zIndex, int x, int y, int width, int height) {
        return createWidget(zIndex, x, y, width, height, null);
    }

    public static Widget createWidget(int zIndex, int x, int y, int width, int height, LocalDateTime dateTime) {
        Widget widget = Widget.builder()
                .z_index(zIndex)
                .x(x)
                .y(y)
                .width(width)
                .height(height)
                .build();
        widget.setId((long) getRandomNumber());
        widget.setDateModified(dateTime);
        return widget;
    }

    public static Area createArea(Point leftBottom, Point topRight) {
        return new Area(leftBottom, topRight);
    }

}
