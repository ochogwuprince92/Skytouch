package com.backend.Skytouch.subscription.service;

import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.repository.CompanyRepository;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.subscription.apimodel.*;
import com.backend.Skytouch.subscription.entity.EmployerSubscription;
import com.backend.Skytouch.subscription.enums.*;
import com.backend.Skytouch.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    private static final Map<PlanType, PlanConfig> PLAN_CONFIGS = Map.of(
        PlanType.FREE, new PlanConfig("Free Tier", "Try Skytouch with 3 job posts", BigDecimal.ZERO, BillingCycle.MONTHLY, 3, false),
        PlanType.BASIC, new PlanConfig("Basic", "Perfect for small businesses", new BigDecimal("20000"), BillingCycle.MONTHLY, 5, false),
        PlanType.STANDARD, new PlanConfig("Standard", "Ideal for growing companies", new BigDecimal("50000"), BillingCycle.MONTHLY, 15, false),
        PlanType.PREMIUM, new PlanConfig("Premium", "For large organizations", new BigDecimal("100000"), BillingCycle.YEARLY, 0, true)
    );

    @Transactional
    public SubscriptionResponse subscribe(UUID companyId, SubscribeRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (subscriptionRepository.existsByCompanyId(companyId)) {
            throw new BadRequestException("Company already has a subscription");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = calculateExpiryDate(now, request.getBillingCycle());
        PlanConfig config = PLAN_CONFIGS.get(request.getPlan());

        EmployerSubscription subscription = EmployerSubscription.builder()
                .company(company)
                .plan(request.getPlan())
                .status(SubscriptionStatus.PENDING)
                .startDate(now)
                .expiresAt(expiresAt)
                .billingCycle(request.getBillingCycle())
                .slotsAllocated(config.maxJobSlots())
                .slotsUsed(0)
                .build();

        EmployerSubscription saved = subscriptionRepository.save(subscription);
        return mapToResponse(saved);
    }

    @Transactional
    public SubscriptionResponse activateSubscription(UUID companyId) {
        EmployerSubscription subscription = subscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new BadRequestException("Subscription is not in PENDING status");
        }

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDateTime.now());
        EmployerSubscription saved = subscriptionRepository.save(subscription);
        return mapToResponse(saved);
    }

    @Transactional
    public SubscriptionResponse assignFreeTier(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (subscriptionRepository.existsByCompanyId(companyId)) {
            throw new BadRequestException("Company already has a subscription");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMonths(1);
        PlanConfig config = PLAN_CONFIGS.get(PlanType.FREE);

        EmployerSubscription subscription = EmployerSubscription.builder()
                .company(company)
                .plan(PlanType.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(now)
                .expiresAt(expiresAt)
                .billingCycle(BillingCycle.MONTHLY)
                .slotsAllocated(config.maxJobSlots())
                .slotsUsed(0)
                .build();

        EmployerSubscription saved = subscriptionRepository.save(subscription);
        log.info("Assigned FREE tier subscription to company {}", companyId);
        return mapToResponse(saved);
    }

    @Transactional
    public SubscriptionResponse renewSubscription(UUID companyId) {
        EmployerSubscription subscription = subscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE && subscription.getStatus() != SubscriptionStatus.EXPIRED) {
            throw new BadRequestException("Cannot renew subscription in current status");
        }

        LocalDateTime newExpiry = calculateExpiryDate(subscription.getExpiresAt(), subscription.getBillingCycle());
        subscription.setExpiresAt(newExpiry);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        EmployerSubscription saved = subscriptionRepository.save(subscription);
        return mapToResponse(saved);
    }

    @Transactional
    public SubscriptionResponse upgradeSubscription(UUID companyId, PlanType newPlan) {
        EmployerSubscription subscription = subscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new BadRequestException("Subscription must be ACTIVE to upgrade");
        }

        if (newPlan.ordinal() <= subscription.getPlan().ordinal()) {
            throw new BadRequestException("New plan must be higher than current plan");
        }

        subscription.setPlan(newPlan);
        subscription.setBillingCycle(PLAN_CONFIGS.get(newPlan).billingCycle());
        EmployerSubscription saved = subscriptionRepository.save(subscription);
        return mapToResponse(saved);
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(UUID companyId) {
        EmployerSubscription subscription = subscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        EmployerSubscription saved = subscriptionRepository.save(subscription);
        return mapToResponse(saved);
    }

    public SubscriptionResponse getCurrentSubscription(UUID companyId) {
        EmployerSubscription subscription = subscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        return mapToResponse(subscription);
    }

    public List<PlanResponse> getAvailablePlans() {
        return PLAN_CONFIGS.entrySet().stream()
                .map(entry -> PlanResponse.builder()
                        .plan(entry.getKey())
                        .name(entry.getValue().name())
                        .description(entry.getValue().description())
                        .price(entry.getValue().price())
                        .billingCycle(entry.getValue().billingCycle())
                        .maxJobSlots(entry.getValue().maxJobSlots())
                        .unlimited(entry.getValue().unlimited())
                        .build())
                .toList();
    }

    public UsageResponse getUsage(UUID companyId) {
        EmployerSubscription subscription = subscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        int activeJobs = jobRepository.countByCompanyIdAndStatus(companyId, JobStatus.ACTIVE);
        PlanConfig config = PLAN_CONFIGS.get(subscription.getPlan());

        boolean unlimited = config.unlimited();
        Integer remainingSlots = unlimited ? null : Math.max(0, subscription.getSlotsAllocated() - subscription.getSlotsUsed());
        boolean canPublish = unlimited || remainingSlots > 0;

        boolean isExpired = subscription.getExpiresAt().isBefore(LocalDateTime.now());
        if (isExpired && subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
        }

        return UsageResponse.builder()
                .plan(subscription.getPlan())
                .status(subscription.getStatus())
                .expiresAt(subscription.getExpiresAt())
                .activeJobs(activeJobs)
                .slotsAllocated(subscription.getSlotsAllocated())
                .slotsUsed(subscription.getSlotsUsed())
                .remainingSlots(remainingSlots)
                .unlimited(unlimited)
                .canPublish(canPublish && subscription.getStatus() == SubscriptionStatus.ACTIVE && !isExpired)
                .build();
    }

    public void validateCanPublishJob(UUID companyId) {
        UsageResponse usage = getUsage(companyId);
        if (!usage.isCanPublish()) {
            if (usage.getStatus() != SubscriptionStatus.ACTIVE) {
                throw new BadRequestException("Subscription is not active. Please renew your subscription to publish jobs.");
            }
            throw new BadRequestException("Job slot limit reached. Upgrade your subscription to publish more jobs.");
        }
    }

    @Transactional
    public void incrementSlotsUsed(UUID companyId) {
        EmployerSubscription subscription = subscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        
        PlanConfig config = PLAN_CONFIGS.get(subscription.getPlan());
        if (config.unlimited()) {
            return; // Unlimited plans don't track slots
        }
        
        if (subscription.getSlotsUsed() < subscription.getSlotsAllocated()) {
            subscription.setSlotsUsed(subscription.getSlotsUsed() + 1);
            subscriptionRepository.save(subscription);
            log.info("Incremented slots used for company {}: {}/{}", companyId, subscription.getSlotsUsed(), subscription.getSlotsAllocated());
        }
    }

    @Transactional
    public void decrementSlotsUsed(UUID companyId) {
        EmployerSubscription subscription = subscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        
        PlanConfig config = PLAN_CONFIGS.get(subscription.getPlan());
        if (config.unlimited()) {
            return; // Unlimited plans don't track slots
        }
        
        if (subscription.getSlotsUsed() > 0) {
            subscription.setSlotsUsed(subscription.getSlotsUsed() - 1);
            subscriptionRepository.save(subscription);
            log.info("Decremented slots used for company {}: {}/{}", companyId, subscription.getSlotsUsed(), subscription.getSlotsAllocated());
        }
    }

    private LocalDateTime calculateExpiryDate(LocalDateTime fromDate, BillingCycle billingCycle) {
        return switch (billingCycle) {
            case MONTHLY -> fromDate.plusMonths(1);
            case YEARLY -> fromDate.plusYears(1);
        };
    }

    private SubscriptionResponse mapToResponse(EmployerSubscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .companyId(subscription.getCompany().getId())
                .plan(subscription.getPlan())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .expiresAt(subscription.getExpiresAt())
                .billingCycle(subscription.getBillingCycle())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    private record PlanConfig(String name, String description, BigDecimal price, BillingCycle billingCycle, int maxJobSlots, boolean unlimited) {}
}
