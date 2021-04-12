package com.miro.hw.artexnet.storage.db;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WidgetRepository extends JpaRepository<WidgetEntity, Long> {

    WidgetEntity getTopByOrderByZindexDesc();

    @Query("SELECT COUNT(w.id) FROM WidgetEntity w WHERE w.zindex = ?1")
    long countByZIndex(int zIndex);

    @Query("SELECT w FROM WidgetEntity w WHERE w.zindex >= ?1 ORDER BY w.zindex DESC")
    List<WidgetEntity> findAllWithEqualOrGreaterZIndex(int zIndex);

    @Query("SELECT w FROM WidgetEntity w ORDER BY w.zindex DESC")
    List<WidgetEntity> findAllOrderedByZIndex(Pageable pageable);

    @Query("SELECT w FROM WidgetEntity w WHERE 1 = 1 " +  // aesthetic ))
            "AND w.x - (w.width / 2) >= :leftX " +
            "AND w.y - (w.height / 2) >= :leftY " +
            "AND w.x + (w.width / 2) <= :rightX " +
            "AND w.y + (w.height / 2) <= :rightY " +
            "ORDER BY w.zindex DESC")
    List<WidgetEntity> findAllInAreaOrderedByZIndex(
            @Param("leftX") int leftX, @Param("leftY") int leftY,
            @Param("rightX") int rightX, @Param("rightY") int rightY,
            Pageable pageable);

    @Query("SELECT COUNT(w.id) FROM WidgetEntity w " +
            "WHERE w.x - (w.width / 2) >= :leftX " +
            "AND w.y - (w.height / 2) >= :leftY " +
            "AND w.x + (w.width / 2) <= :rightX " +
            "AND w.y + (w.height / 2) <= :rightY")
    long countByArea(@Param("leftX") int leftX, @Param("leftY") int leftY,@Param("rightX") int rightX, @Param("rightY") int rightY);
}
