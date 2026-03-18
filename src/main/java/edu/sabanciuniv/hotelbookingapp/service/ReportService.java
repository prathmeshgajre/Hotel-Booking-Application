package edu.sabanciuniv.hotelbookingapp.service;

import java.time.LocalDate;

public interface ReportService {

    byte[] generateBookingReport();

    byte[] generateCustomerBookingReport(Long customerId);

    byte[] generateBookingReportByDateRange(LocalDate fromDate, LocalDate toDate, Long userId);

}
