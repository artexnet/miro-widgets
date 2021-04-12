package com.miro.hw.artexnet.api.dto;

import com.miro.hw.artexnet.exception.ValidationException;
import lombok.Getter;

@Getter
public class PageRequest {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_ITEMS_CHUNK = 10;
    public static final int MIN_ITEMS_CHUNK = 1;
    public static final int MAX_ITEMS_CHUNK = 500;

    int page;
    int size;

    /**
     * Validates/Initializes a new instance of the class.
     */
    public PageRequest(Integer page, Integer size) {
        setPage(page);
        setSize(size);
    }

    private void setPage(Integer page) {
        this.page = page != null ? page : DEFAULT_PAGE;
        if (this.page < DEFAULT_PAGE) {
            throw new ValidationException("Page must be positive number");
        }
    }

    private void setSize(Integer size) {
        this.size = size != null ? size : DEFAULT_ITEMS_CHUNK;
        if (this.size < MIN_ITEMS_CHUNK || this.size > MAX_ITEMS_CHUNK) {
            throw new ValidationException("Items chunk must be between 1 and 500");
        }
    }
}
