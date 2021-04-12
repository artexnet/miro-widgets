package com.miro.hw.artexnet.storage.db;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;

@Entity
@Table(name = "t_widget", uniqueConstraints = {
        @UniqueConstraint(name = "IDX_Z_INDEX", columnNames = "z_index")},
        indexes = {
        @Index(name = "IDX_X_RANGE", columnList = "x_coordinate"),
        @Index(name = "IDX_Y_RANGE", columnList = "y_coordinate"),
        @Index(name = "IDX_WIDTH", columnList = "width"),
        @Index(name = "IDX_HEIGHT", columnList = "height"),
})
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WidgetEntity extends AbstractEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "z_index")
    private int zindex;

    @Column(name = "x_coordinate")
    private int x;

    @Column(name = "y_coordinate")
    private int y;

    @Min(1)
    @Column(name = "width")
    private int width;

    @Min(1)
    @Column(name = "height")
    private int height;

}
