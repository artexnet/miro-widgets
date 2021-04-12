package com.miro.hw.artexnet.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AreaRequest {
    private Integer leftX;
    private Integer leftY;
    private Integer rightX;
    private Integer rightY;

    public boolean isDefined() {
        return leftX != null && leftY != null && rightX != null && rightY != null;
    }

    public boolean isValid() {
        if (!isDefined()) return false;
        if (leftX >= rightX) return false;
        if (leftY >= rightY) return false;
        return true;
    }

}
