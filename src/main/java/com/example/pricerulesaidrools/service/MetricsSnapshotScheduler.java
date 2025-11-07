package com.example.pricerulesaidrools.service;

import com.example.pricerulesaidrools.model.FinancialMetricsSnapshot;
import com.example.pricerulesaidrools.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for scheduling periodic financial metrics snapshots
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsSnapshotScheduler {

    private final FinancialMetricsCalculator metricsCalculator;
    private final CustomerRepository customerRepository;

    /**
     * Create daily snapshots for all customers at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?") // Midnight every day
    @Transactional
    public void createDailySnapshots() {
        log.info("Creating daily financial metrics snapshots");
        int count = 0;

        // Get all customer IDs
        List<String> customerIds = customerRepository.findAll().stream()
                .map(c -> c.getCustomerId())
                .toList();

        // Create snapshots for each customer
        for (String customerId : customerIds) {
            try {
                metricsCalculator.createMetricsSnapshot(customerId, FinancialMetricsSnapshot.SnapshotType.DAILY);
                count++;
            } catch (Exception e) {
                log.error("Error creating daily snapshot for customer {}: {}", customerId, e.getMessage());
            }
        }

        log.info("Created {} daily snapshots", count);
    }

    /**
     * Create weekly snapshots for all customers on Sunday at 1 AM
     */
    @Scheduled(cron = "0 0 1 * * SUN") // 1 AM every Sunday
    @Transactional
    public void createWeeklySnapshots() {
        log.info("Creating weekly financial metrics snapshots");
        int count = 0;

        // Get all customer IDs
        List<String> customerIds = customerRepository.findAll().stream()
                .map(c -> c.getCustomerId())
                .toList();

        // Create snapshots for each customer
        for (String customerId : customerIds) {
            try {
                metricsCalculator.createMetricsSnapshot(customerId, FinancialMetricsSnapshot.SnapshotType.WEEKLY);
                count++;
            } catch (Exception e) {
                log.error("Error creating weekly snapshot for customer {}: {}", customerId, e.getMessage());
            }
        }

        log.info("Created {} weekly snapshots", count);
    }

    /**
     * Create monthly snapshots for all customers on the 1st of each month at 2 AM
     */
    @Scheduled(cron = "0 0 2 1 * ?") // 2 AM on the 1st day of each month
    @Transactional
    public void createMonthlySnapshots() {
        log.info("Creating monthly financial metrics snapshots");
        int count = 0;

        // Get all customer IDs
        List<String> customerIds = customerRepository.findAll().stream()
                .map(c -> c.getCustomerId())
                .toList();

        // Create snapshots for each customer
        for (String customerId : customerIds) {
            try {
                metricsCalculator.createMetricsSnapshot(customerId, FinancialMetricsSnapshot.SnapshotType.MONTHLY);
                count++;
            } catch (Exception e) {
                log.error("Error creating monthly snapshot for customer {}: {}", customerId, e.getMessage());
            }
        }

        log.info("Created {} monthly snapshots", count);
    }

    /**
     * Create quarterly snapshots for all customers on the 1st day of each quarter
     * at 3 AM
     */
    @Scheduled(cron = "0 0 3 1 1,4,7,10 ?") // 3 AM on Jan 1, Apr 1, Jul 1, Oct 1
    @Transactional
    public void createQuarterlySnapshots() {
        log.info("Creating quarterly financial metrics snapshots");
        int count = 0;

        // Get all customer IDs
        List<String> customerIds = customerRepository.findAll().stream()
                .map(c -> c.getCustomerId())
                .toList();

        // Create snapshots for each customer
        for (String customerId : customerIds) {
            try {
                metricsCalculator.createMetricsSnapshot(customerId, FinancialMetricsSnapshot.SnapshotType.QUARTERLY);
                count++;
            } catch (Exception e) {
                log.error("Error creating quarterly snapshot for customer {}: {}", customerId, e.getMessage());
            }
        }

        log.info("Created {} quarterly snapshots", count);
    }

    /**
     * Create annual snapshots for all customers on January 1st at 4 AM
     */
    @Scheduled(cron = "0 0 4 1 1 ?") // 4 AM on January 1st
    @Transactional
    public void createAnnualSnapshots() {
        log.info("Creating annual financial metrics snapshots");
        int count = 0;

        // Get all customer IDs
        List<String> customerIds = customerRepository.findAll().stream()
                .map(c -> c.getCustomerId())
                .toList();

        // Create snapshots for each customer
        for (String customerId : customerIds) {
            try {
                metricsCalculator.createMetricsSnapshot(customerId, FinancialMetricsSnapshot.SnapshotType.ANNUAL);
                count++;
            } catch (Exception e) {
                log.error("Error creating annual snapshot for customer {}: {}", customerId, e.getMessage());
            }
        }

        log.info("Created {} annual snapshots", count);
    }
}