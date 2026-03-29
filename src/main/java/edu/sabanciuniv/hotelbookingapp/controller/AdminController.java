package edu.sabanciuniv.hotelbookingapp.controller;

import edu.sabanciuniv.hotelbookingapp.exception.HotelAlreadyExistsException;
import edu.sabanciuniv.hotelbookingapp.exception.UsernameAlreadyExistsException;
import edu.sabanciuniv.hotelbookingapp.model.dto.BookingDTO;
import edu.sabanciuniv.hotelbookingapp.model.dto.HotelDTO;
import edu.sabanciuniv.hotelbookingapp.model.dto.UserAuditLogDTO;
import edu.sabanciuniv.hotelbookingapp.model.dto.UserDTO;
import edu.sabanciuniv.hotelbookingapp.repository.BookingRepository;
import edu.sabanciuniv.hotelbookingapp.service.BookingService;
import edu.sabanciuniv.hotelbookingapp.service.HotelService;
import edu.sabanciuniv.hotelbookingapp.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;
    private final HotelService hotelService;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    /* ================= DASHBOARD ================= */

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    /* ================= USERS ================= */

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findUserById(id));
        return "admin/users-edit";
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable Long id,
                           @Valid @ModelAttribute("user") UserDTO userDTO,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/users-edit";
        }

        try {
            userService.updateUser(userDTO);
        } catch (UsernameAlreadyExistsException e) {
            result.rejectValue("username", "user.exists", "Username is already registered!");
            return "admin/users-edit";
        }

        redirectAttributes.addFlashAttribute("updatedUserId", userDTO.getId());
        return "redirect:/admin/users?success";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin/users";
    }

    /* ================= HOTELS ================= */

    @GetMapping("/hotels")
    public String listHotels(Model model) {
        model.addAttribute("hotels", hotelService.findAllHotels());
        return "admin/hotels";
    }

    @GetMapping("/hotels/edit/{id}")
    public String showEditHotelForm(@PathVariable Long id, Model model) {
        model.addAttribute("hotel", hotelService.findHotelDtoById(id));
        return "admin/hotels-edit";
    }

    @PostMapping("/hotels/edit/{id}")
    public String editHotel(@PathVariable Long id,
                            @Valid @ModelAttribute("hotel") HotelDTO hotelDTO,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/hotels-edit";
        }

        try {
            hotelService.updateHotel(hotelDTO);
        } catch (HotelAlreadyExistsException e) {
            result.rejectValue("name", "hotel.exists", e.getMessage());
            return "admin/hotels-edit";
        }

        redirectAttributes.addFlashAttribute("updatedHotelId", hotelDTO.getId());
        return "redirect:/admin/hotels?success";
    }

    @PostMapping("/hotels/delete/{id}")
    public String deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotelById(id);
        return "redirect:/admin/hotels";
    }

    /* ================= BOOKINGS ================= */

    @GetMapping("/bookings")
    public String listBookings(Model model) {
        model.addAttribute("bookings", bookingService.findAllBookings());
        return "admin/bookings";
    }

    @GetMapping("/bookings/{id}")
    public String viewBookingDetails(@PathVariable Long id,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {

        try {
            BookingDTO bookingDTO = bookingService.findBookingById(id);
            model.addAttribute("bookingDTO", bookingDTO);

            long days = ChronoUnit.DAYS.between(
                    bookingDTO.getCheckinDate(),
                    bookingDTO.getCheckoutDate()
            );

            model.addAttribute("days", days);

            return "admin/bookings-details";

        } catch (EntityNotFoundException e) {
            log.error("Booking not found", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            log.error("Unexpected booking error", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error occurred.");
            return "redirect:/admin/dashboard";
        }
    }

    /* ================= REPORTS ================= */

    @GetMapping("/reports/search")
    public String showDataRangeSearchForm() {
        return "admin/reports-search";
    }

    @GetMapping("/reports/search/results")
    public String showDateRangeResults(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long userId,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            List<BookingDTO> bookings;

            if (userId != null) {

                bookings = bookingRepository
                        .findBookingsByDateRangeAndUserId(fromDate, toDate, userId)
                        .stream()
                        .map(b -> bookingService.findBookingById(b.getId()))
                        .collect(Collectors.toList());

                model.addAttribute("filteredUserId", userId);

            } else {

                bookings = bookingRepository
                        .findBookingsByDateRange(fromDate, toDate)
                        .stream()
                        .map(b -> bookingService.findBookingById(b.getId()))
                        .collect(Collectors.toList());
            }

            List<UserDTO> uniqueUsers = bookings.stream()
                    .map(b -> userService.findUserByCustomerId(b.getCustomerId()))
                    .filter(u -> u != null)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, Long> bookingCountByUser = bookings.stream()
                    .filter(b -> userService.findUserByCustomerId(b.getCustomerId()) != null)
                    .collect(Collectors.groupingBy(
                            b -> userService.findUserByCustomerId(b.getCustomerId()).getId(),
                            Collectors.counting()
                    ));

            model.addAttribute("bookings", bookings);
            model.addAttribute("uniqueUsers", uniqueUsers);
            model.addAttribute("bookingCountByUser", bookingCountByUser);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);

            return "admin/reports-results";

        } catch (Exception e) {

            log.error("Report generation failed", e);

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Error fetching report results."
                    + e.getMessage());

            return "redirect:/admin/reports/search";
        }
    }

    /* ================= USER BOOKING HISTORY ================= */

    @GetMapping("/users/{userId}/bookings")
    public String viewUserBookingHistory(@PathVariable Long userId,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        try {
            UserDTO user = userService.findUserById(userId);

            // FIX 3: use findAllBookingsByUserId which queries by USER id correctly
            List<BookingDTO> bookings = bookingRepository
                    .findAllBookingsByUserId(userId)
                    .stream()
                    .map(b -> bookingService.findBookingById(b.getId()))
                    .collect(Collectors.toList());

            log.info("Found {} bookings for userId={}", bookings.size(), userId);

            List<UserAuditLogDTO> auditHistory = userService.getUserAuditHistory(userId);

            model.addAttribute("user", user);
            model.addAttribute("bookings", bookings);
            model.addAttribute("auditHistory", auditHistory);

            return "admin/user-booking-history";

        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/reports/search";
        }
    }
}