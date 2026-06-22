package com.restaurant.app.cash.repository;

import com.restaurant.app.cash.model.Transaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Transaction entities. Enforces INV-03 via UNIQUE (reference_id) constraint. */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query(
            "SELECT t FROM Transaction t WHERE t.id = :id AND t.cashRegisterId IN "
                    + "(SELECT cr.id FROM CashRegister cr WHERE cr.restaurantId = :restaurantId)")
    Optional<Transaction> findByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);

    @Query("SELECT t FROM Transaction t WHERE t.referenceId = :referenceId")
    Optional<Transaction> findByReferenceId(@Param("referenceId") String referenceId);

    @Query(
            "SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Transaction t "
                    + "WHERE t.referenceId = :referenceId")
    boolean existsByReferenceId(@Param("referenceId") String referenceId);

    @Query("SELECT t FROM Transaction t WHERE t.cashRegisterId = :cashRegisterId")
    List<Transaction> findByCashRegisterId(@Param("cashRegisterId") String cashRegisterId);

    @Query(
            "SELECT t FROM Transaction t "
                    + "WHERE t.cashRegisterId IN "
                    + "(SELECT cr.id FROM CashRegister cr WHERE cr.restaurantId = :restaurantId)")
    List<Transaction> findAllByRestaurantId(@Param("restaurantId") String restaurantId);

    @Query(
            "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t "
                    + "WHERE t.cashRegisterId = :cashRegisterId AND t.type = 'INCOME'")
    BigDecimal sumIncomeByCashRegisterId(@Param("cashRegisterId") String cashRegisterId);

    @Query(
            "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t "
                    + "WHERE t.cashRegisterId = :cashRegisterId AND t.type = 'EXPENSE'")
    BigDecimal sumExpensesByCashRegisterId(@Param("cashRegisterId") String cashRegisterId);

    @Query(
            "SELECT COUNT(t) FROM Transaction t "
                    + "WHERE t.cashRegisterId = :cashRegisterId AND t.type = :type")
    long countByCashRegisterIdAndType(
            @Param("cashRegisterId") String cashRegisterId, @Param("type") String type);
}
