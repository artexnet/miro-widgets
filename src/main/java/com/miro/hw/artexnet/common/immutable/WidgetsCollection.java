package com.miro.hw.artexnet.common.immutable;

import com.miro.hw.artexnet.domain.Widget;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WidgetsCollection {
    private final long totalCount;
    private final List<Widget> items;
}
