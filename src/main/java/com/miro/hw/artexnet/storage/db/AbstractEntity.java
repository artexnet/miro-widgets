package com.miro.hw.artexnet.storage.db;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractEntity implements Serializable {

    @Version
    @Column(name = "version")
    private int version;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_modified")
    private LocalDateTime dateModified;

    ////////////////////////////////////

    @PrePersist
    protected void prePersist() {
        dateCreated = LocalDateTime.now();
        dateModified = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        dateModified = LocalDateTime.now();
    }

    @PreRemove
    public void preRemove() {
        // Do nothing
    }
}
