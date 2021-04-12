package com.miro.hw.artexnet.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.miro.hw.artexnet.common.immutable.Area;
import com.miro.hw.artexnet.common.immutable.Point;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Widget {

    private Long id;

    private Integer z_index;

    @NotNull
    private Integer x;

    @NotNull
    private Integer y;

    @Min(1)
    private int width;

    @Min(1)
    private int height;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern="yyyy-MM-dd hh:mm:ss.SSS")
    @EqualsAndHashCode.Exclude
    private LocalDateTime dateModified;

    /**
     * Checks if provided Widget fully fits into the
     * specified area.
     * @param area square defined by bottom/left and top/rights points.
     * @return <code>true</code> if the Widget fits in the area, <code>false</code> otherwise.
     */
    public static boolean fitsInArea(Widget widget, Area area) {
        final Point leftBottom = area.getLeftBottom();
        final Point rightTop = area.getRightTop();

        int leftX  = widget.x - widget.width / 2;   // p.X - (width / 2)
        int leftY  = widget.y - widget.height / 2;  // p.Y - (height / 2)
        int rightX = widget.x + widget.width / 2;   // p.X + (width / 2)
        int rightY = widget.y + widget.height / 2;  // p.Y + (height / 2)

        boolean fitsFromLeftBottom = leftX >= leftBottom.getXAxis() && leftY >= leftBottom.getYAxis();
        boolean fitsFromRightTop = rightX <= rightTop.getXAxis() && rightY <= rightTop.getYAxis();
        return fitsFromLeftBottom && fitsFromRightTop;
    }
}
