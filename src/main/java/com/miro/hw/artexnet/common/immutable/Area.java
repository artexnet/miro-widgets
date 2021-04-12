package com.miro.hw.artexnet.common.immutable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Area {
    private final Point leftBottom;
    private final Point rightTop;
}
