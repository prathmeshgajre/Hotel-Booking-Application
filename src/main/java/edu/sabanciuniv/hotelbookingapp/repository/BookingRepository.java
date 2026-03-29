package edu.sabanciuniv.hotelbookingapp.repository;

import edu.sabanciuniv.hotelbookingapp.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findBookingsByCustomerId(Long customerId);

    Optional<Booking> findBookingByIdAndCustomerId(Long bookingId, Long customerId);

    List<Booking> findBookingsByHotelId(Long hotelId);

    Optional<Booking> findBookingByIdAndHotel_HotelManagerId(Long bookingId, Long hotelManagerId);

    List<Booking> findByCustomerId(Long customerId);

    // Get all bookings where stay period falls within the date range (no userId filter)
    @Query("SELECT b FROM Booking b WHERE " +
            "b.checkinDate >= :fromDate AND " +
            "b.checkoutDate <= :toDate " +
            "ORDER BY b.checkinDate ASC")
    List<Booking> findBookingsByDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    /// Get bookings where stay period falls within the date range for a specific user
    @Query("SELECT b FROM Booking b WHERE " +
            "b.checkinDate >= :fromDate AND " +
            "b.checkoutDate <= :toDate AND " +
            "b.customer.user.id = :userId " +
            "ORDER BY b.checkinDate ASC")
    List<Booking> findBookingsByDateRangeAndUserId(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.customer.user.id = :userId " +
            "ORDER BY b.bookingDate DESC")
    List<Booking> findAllBookingsByUserId(@Param("userId") Long userId);
}
