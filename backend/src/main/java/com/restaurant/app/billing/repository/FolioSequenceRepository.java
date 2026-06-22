package com.restaurant.app.billing.repository;

import com.restaurant.app.billing.model.FolioSequence;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for FolioSequence with pessimistic locking. Implements INV-02: contiguous folios with
 * SELECT ... FOR UPDATE.
 */
@Repository
public interface FolioSequenceRepository extends JpaRepository<FolioSequence, String> {

    /**
     * Lock folio sequence row for update. Implements INV-02 with pessimistic locking. This method
     * must be called within @Transactional.
     *
     * <p>Uses SELECT ... FOR UPDATE to prevent concurrent invoice creation from creating gaps or
     * duplicates in the folio sequence.
     */
    @Query("SELECT fs FROM FolioSequence fs WHERE fs.restaurantId = :restaurantId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FolioSequence> lockByRestaurantId(@Param("restaurantId") String restaurantId);

    /** Find folio sequence without lock (read-only). */
    @Query("SELECT fs FROM FolioSequence fs WHERE fs.restaurantId = :restaurantId")
    Optional<FolioSequence> findByRestaurantId(@Param("restaurantId") String restaurantId);
}
