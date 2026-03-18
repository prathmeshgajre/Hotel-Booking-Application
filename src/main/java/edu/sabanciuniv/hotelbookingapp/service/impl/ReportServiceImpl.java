package edu.sabanciuniv.hotelbookingapp.service.impl;

import edu.sabanciuniv.hotelbookingapp.model.Booking;
import edu.sabanciuniv.hotelbookingapp.model.dto.BookingDTO;
import edu.sabanciuniv.hotelbookingapp.repository.BookingRepository;
import edu.sabanciuniv.hotelbookingapp.service.ReportService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public byte[] generateBookingReport() {
        try {
            List<BookingDTO> bookings = bookingService.findAllBookings();
            log.info("Generating admin report with {} bookings", bookings.size());
            return generateReport(bookings);
        } catch (Exception e) {
            log.error("Error generating admin report", e);
            throw new RuntimeException("Error generating report", e);
        }
    }

    @Override
    public byte[] generateCustomerBookingReport(Long customerId) {
        try {
            List<BookingDTO> bookings = bookingService.findBookingsByCustomerId(customerId);
            log.info("Generating customer report with {} bookings for customer ID: {}", bookings.size(), customerId);
            return generateReport(bookings);
        } catch (Exception e) {
            log.error("Error generating customer report", e);
            throw new RuntimeException("Error generating report", e);
        }
    }

    @Override
    public byte[] generateBookingReportByDateRange(LocalDate fromDate, LocalDate toDate, Long userId) {
        try {
            List<Booking> bookings;

            if (userId == null) {
                // No userId provided - get ALL users bookings between dates
                log.info("Generating date range report from {} to {} for ALL users", fromDate, toDate);
                bookings = bookingRepository.findBookingsByDateRange(fromDate, toDate);
            } else {
                // userId provided - get only that user's bookings between dates
                log.info("Generating date range report from {} to {} for user ID: {}", fromDate, toDate, userId);
                bookings = bookingRepository.findBookingsByDateRangeAndUserId(fromDate, toDate, userId);
            }

            log.info("Found {} bookings for date range report", bookings.size());

            // Map Booking entities to BookingDTOs
            List<BookingDTO> bookingDTOs = bookings.stream()
                    .map(bookingService::mapBookingModelToBookingDto)
                    .toList();

            return generateReport(bookingDTOs);

        } catch (Exception e) {
            log.error("Error generating date range report", e);
            throw new RuntimeException("Error generating date range report", e);
        }
    }

    private byte[] generateReport(List<BookingDTO> bookings) throws Exception {
        // Use getResourceAsStream - works reliably unlike ResourceUtils.getFile()
        InputStream stream = getClass().getResourceAsStream("/reports/booking_report.jrxml");
        if (stream == null) {
            throw new RuntimeException("booking_report.jrxml not found in classpath:/reports/");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(stream);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(bookings);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("createdBy", "Hotel Booking System");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
