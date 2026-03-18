package edu.sabanciuniv.hotelbookingapp.controller;

import edu.sabanciuniv.hotelbookingapp.model.dto.UserDTO;
import edu.sabanciuniv.hotelbookingapp.service.ReportService;
import edu.sabanciuniv.hotelbookingapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    @GetMapping("/bookings")
    public ResponseEntity<byte[]> generateBookingReport() {

        byte[] report = reportService.generateBookingReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=booking-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report);

    }

    @GetMapping("/admin/reports/bookings")
    public ResponseEntity<byte[]> generateAdminBookingReport() {
        byte[] report = reportService.generateBookingReport();

        /*return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=admin-booking-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report);*/

        return buildPdfResponse(report, "admin-booking-report.pdf");
    }

    @GetMapping("/customer/reports/bookings")
    public ResponseEntity<byte[]> generateCustomerBookingReport() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        UserDTO user = userService.findUserDTOByUsername(auth.getName());

        byte[] report = reportService.generateCustomerBookingReport(user.getId());

        /*return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=my-bookings.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report);*/

        return buildPdfResponse(report, "my-bookings.pdf");
    }

    /**
     * Date range report endpoint
     *
     * Usage examples:
     *
     * All users between dates:
     * GET /admin/reports/bookings/date-range?fromDate=2026-01-01&toDate=2026-03-31
     *
     * Specific user between dates:
     * GET /admin/reports/bookings/date-range?fromDate=2026-01-01&toDate=2026-03-31&userId=2
     */

    public ResponseEntity<byte[]> generateDateRangeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate toDate,
            @RequestParam(required = false) Long userId){

        log.info("Date range report request - fromDate: {}, toDate: {}, userId: {}", fromDate, toDate, userId);

        if (fromDate.isAfter(toDate)) {
            return ResponseEntity.badRequest().build();
        }

        byte[] report = reportService.generateBookingReportByDateRange(fromDate, toDate, userId);

        String filename = userId != null
                ? "user-" + userId + "-bookings-report.pdf"
                : "all-bookings-" + fromDate + "-to-" + toDate + ".pdf";

        return buildPdfResponse(report, filename);
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] report, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(report.length));
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(report);
    }
}