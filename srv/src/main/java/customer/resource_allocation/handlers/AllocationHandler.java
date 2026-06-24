package customer.resource_allocation.handlers;

import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.persistence.PersistenceService;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.cqn.CqnAnalyzer;
import com.sap.cds.reflect.CdsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;

import cds.gen.resourceallocationservice.Allocations;
import cds.gen.resourceallocationservice.Allocations_;
import cds.gen.resourceallocationservice.ResourceAllocationService_;
import cds.gen.resourceallocationservice.AllocationsSubmitContext;
import cds.gen.resourceallocationservice.AllocationsApproveContext;
import cds.gen.resourceallocationservice.AllocationsRejectContext;

import java.math.BigDecimal;
import java.util.Map;

@Component
@ServiceName(ResourceAllocationService_.CDS_NAME)
public class AllocationHandler implements EventHandler {

    @Autowired
    PersistenceService db;

    @Autowired
    CdsModel cdsModel;  // ✅ needed for CqnAnalyzer

    // ─── VALIDATION: Before CREATE or UPDATE ─────────────────────────────────
    @Before(event = { CqnService.EVENT_CREATE, CqnService.EVENT_UPDATE },
            entity = Allocations_.CDS_NAME)
    public void validateAllocation(Allocations allocation) {

        if (allocation.getAllocationPercent() == null ||
            allocation.getAllocationPercent().compareTo(BigDecimal.ZERO) <= 0 ||
            allocation.getAllocationPercent().compareTo(new BigDecimal("100")) > 0) {
            throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                "Allocation percent must be between 1 and 100.");
        }

        if (allocation.getStartDate() != null && allocation.getEndDate() != null) {
            if (allocation.getStartDate().isAfter(allocation.getEndDate())) {
                throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                    "Allocation start date cannot be after end date.");
            }
        }

        validateTotalAllocation(allocation);
    }

    private void validateTotalAllocation(Allocations allocation) {

        if (allocation.getEmployeeId() == null) return;

        var existing = db.run(
            Select.from(Allocations_.class)
                .where(a -> a.employee_ID().eq(allocation.getEmployeeId())
                    .and(a.status().in("APPROVED", "PENDING"))
                    .and(a.ID().ne(
                        allocation.getId() != null ? allocation.getId() : ""
                    ))
                )
        );

        BigDecimal totalAllocated = BigDecimal.ZERO;
        for (Allocations a : existing.listOf(Allocations.class)) {
            if (a.getAllocationPercent() != null) {
                totalAllocated = totalAllocated.add(a.getAllocationPercent());
            }
        }

        BigDecimal newTotal = totalAllocated.add(allocation.getAllocationPercent());
        if (newTotal.compareTo(new BigDecimal("100")) > 0) {
            throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                "Total allocation exceeds 100%. " +
                "Already allocated: " + totalAllocated + "%. " +
                "Requested: " + allocation.getAllocationPercent() + "%.");
        }
    }

    // ─── Helper: Extract ID using CqnAnalyzer ────────────────────────────────
    private String extractId(com.sap.cds.ql.cqn.CqnSelect cqn) {
        // ✅ CqnAnalyzer is the official CAP Java API to extract keys from CQN
        Map<String, Object> keys = CqnAnalyzer.create(cdsModel)
                                    .analyze(cqn.ref())
                                    .targetKeys();
        return keys.get("ID").toString();
    }

    // ─── ACTION: Submit ───────────────────────────────────────────────────────
    @On(event = "submit", entity = Allocations_.CDS_NAME)
    public void onSubmit(AllocationsSubmitContext context) {

        String id = extractId(context.getCqn());  // ✅

        Allocations alloc = getAllocationById(id);

        if (!"DRAFT".equals(alloc.getStatus())) {
            throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                "Only DRAFT allocations can be submitted.");
        }

        db.run(Update.entity(Allocations_.class)
            .data("status", "PENDING")
            .where(a -> a.ID().eq(id)));

        Allocations updated = getAllocationById(id);
        context.put("value", updated);  // ✅
        context.setCompleted();
    }

    // ─── ACTION: Approve ──────────────────────────────────────────────────────
    @On(event = "approve", entity = Allocations_.CDS_NAME)
    public void onApprove(AllocationsApproveContext context) {

        String id = extractId(context.getCqn());

        Allocations alloc = getAllocationById(id);

        if (!"PENDING".equals(alloc.getStatus())) {
            throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                "Only PENDING allocations can be approved.");
        }

        db.run(Update.entity(Allocations_.class)
            .data("status", "APPROVED")
            .where(a -> a.ID().eq(id)));

        Allocations updated = getAllocationById(id);
        context.put("value", updated);
        context.setCompleted();
    }

    // ─── ACTION: Reject ───────────────────────────────────────────────────────
    @On(event = "reject", entity = Allocations_.CDS_NAME)
    public void onReject(AllocationsRejectContext context) {

        String id = extractId(context.getCqn());

        Allocations alloc = getAllocationById(id);

        if (!"PENDING".equals(alloc.getStatus())) {
            throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                "Only PENDING allocations can be rejected.");
        }

        db.run(Update.entity(Allocations_.class)
            .data("status", "REJECTED")
            .where(a -> a.ID().eq(id)));

        Allocations updated = getAllocationById(id);
        context.put("value", updated);
        context.setCompleted();
    }

    // ─── Helper: Fetch allocation by ID ──────────────────────────────────────
    private Allocations getAllocationById(String id) {
        return db.run(
            Select.from(Allocations_.class)
                .where(a -> a.ID().eq(id)))
            .first(Allocations.class)
            .orElseThrow(() -> new ServiceException(
                ErrorStatuses.NOT_FOUND, "Allocation not found."));
    }
}