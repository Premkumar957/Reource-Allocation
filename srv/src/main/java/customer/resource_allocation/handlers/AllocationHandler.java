package customer.resource_allocation.handlers;

import com.sap.cds.services.EventContext;              // ✅ correct import
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.cds.CdsUpdateEventContext;
import com.sap.cds.services.cds.CdsDeleteEventContext;
import com.sap.cds.services.persistence.PersistenceService;
import com.sap.cds.ql.CQL;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.cqn.CqnAnalyzer;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.CqnStatement;
import com.sap.cds.reflect.CdsModel;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.request.UserInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cds.gen.resourceallocationservice.Allocations;
import cds.gen.resourceallocationservice.Allocations_;
import cds.gen.resourceallocationservice.Employees_;
import cds.gen.resourceallocationservice.Projects;
import cds.gen.resourceallocationservice.Projects_;
import cds.gen.resourceallocationservice.ResourceAllocationService_;
import cds.gen.resourceallocationservice.AllocationsSubmitContext;
import cds.gen.resourceallocationservice.AllocationsApproveContext;
import cds.gen.resourceallocationservice.AllocationsRejectContext;

import com.sap.cds.ql.CQL;
import com.sap.cds.ql.Predicate;
import com.sap.cds.ql.cqn.CqnPredicate;
import com.sap.cds.ql.cqn.CqnSelect;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ServiceName(ResourceAllocationService_.CDS_NAME)
public class AllocationHandler implements EventHandler {

    @Autowired
    PersistenceService db;

    @Autowired
    CdsModel cdsModel;

    @Autowired
    private UserInfo userInfo;

    // ════════════════════════════════════════════════════════════
    //  EMPLOYEES
    // ════════════════════════════════════════════════════════════

    // ✅ Multiple events → use generic EventContext
    @Before(event = { CqnService.EVENT_CREATE, CqnService.EVENT_UPDATE, CqnService.EVENT_DELETE },
            entity = Employees_.CDS_NAME)
    public void restrictEmployeeWrite(EventContext context) {
        if (!userInfo.hasRole("Admin") && !userInfo.hasRole("HRManager")) {
            throw new ServiceException(ErrorStatuses.FORBIDDEN,
                "Only Admin or HRManager can modify Employee records.");
        }
    }

    // ════════════════════════════════════════════════════════════
    //  PROJECTS — READ
    // ════════════════════════════════════════════════════════════

    // @Before(event = CqnService.EVENT_READ, entity = Projects_.CDS_NAME)
    public void restrictProjectRead(CdsReadEventContext context) {
        // boolean isAdmin     = userInfo.hasRole("Admin");
        // boolean isHRManager = userInfo.hasRole("HRManager");

        // if (!isAdmin && !isHRManager) {
        //     String currentUser = userInfo.getName();
        //     context.setCqn(
        //         Select.from(Projects_.class)
        //               .where(p -> p.get("createdBy").eq(currentUser))
        //     );
        // }
    }

    // ════════════════════════════════════════════════════════════
    //  PROJECTS — UPDATE (ownership check)
    // ════════════════════════════════════════════════════════════

    @Before(event = CqnService.EVENT_UPDATE, entity = Projects_.CDS_NAME)
    public void restrictProjectUpdate(CdsUpdateEventContext context) {
        if (!userInfo.hasRole("Admin") && !userInfo.hasRole("ProjectManager")) {
            throw new ServiceException(ErrorStatuses.FORBIDDEN,
                "Only Admin or ProjectManager can update Projects.");
        }

        if (!userInfo.hasRole("Admin") && userInfo.hasRole("ProjectManager")) {
            String id          = extractId(context.getCqn());
            String currentUser = userInfo.getName();

            db.run(Select.from(Projects_.class).where(p -> p.ID().eq(id)))
              .first(Projects.class)
              .ifPresent(project -> {
                  Object manager = project.get("createdBy");
                  if (manager == null || !currentUser.equals(manager.toString())) {
                      throw new ServiceException(ErrorStatuses.FORBIDDEN,
                          "ProjectManager can only update their own projects.");
                  }
              });
        }
    }

    // ════════════════════════════════════════════════════════════
    //  PROJECTS — DELETE
    // ════════════════════════════════════════════════════════════

    @Before(event = CqnService.EVENT_DELETE, entity = Projects_.CDS_NAME)
    public void restrictProjectDelete(CdsDeleteEventContext context) {
        if (!userInfo.hasRole("Admin")) {
            throw new ServiceException(ErrorStatuses.FORBIDDEN,
                "Only Admin can delete Projects.");
        }
    }

    // ════════════════════════════════════════════════════════════
    //  ALLOCATIONS — READ
    // ════════════════════════════════════════════════════════════

    // @Before(event = CqnService.EVENT_READ, entity = Allocations_.CDS_NAME)
    public void restrictAllocationRead(CdsReadEventContext context) {
        boolean isAdmin          = userInfo.hasRole("Admin");
        boolean isHRManager      = userInfo.hasRole("HRManager");
        boolean isProjectManager = userInfo.hasRole("ProjectManager");
        String  currentUser      = userInfo.getName();

        if (isAdmin || isHRManager) {
            return;
        }

        Predicate filter = isProjectManager
            ? CQL.get("project.createdBy").eq(currentUser)  // ProjectManager
            : CQL.get("employee.email").eq(currentUser);     // Employee

        CqnSelect original = context.getCqn();

        CqnSelect secured = original.where().isPresent()
            ? Select.from(original.ref())
                    .columns(original.items())
                    .where(CQL.and(original.where().get(), filter))
            : Select.from(original.ref())
                    .columns(original.items())
                    .where(filter);

        context.setCqn(secured);
    }

    // ════════════════════════════════════════════════════════════
    //  ALLOCATIONS — CREATE & UPDATE validation
    // ════════════════════════════════════════════════════════════

    // ✅ Entity POJO injection works fine for single-entity @Before
    @Before(event = { CqnService.EVENT_CREATE, CqnService.EVENT_UPDATE },
            entity = Allocations_.CDS_NAME)
    public void validateAllocation(Allocations allocation) {

        if (!userInfo.hasRole("Admin") && !userInfo.hasRole("ProjectManager")) {
            throw new ServiceException(ErrorStatuses.FORBIDDEN,
                "Only Admin or ProjectManager can create/update Allocations.");
        }

        if (allocation.getAllocationPercent() == null ||
            allocation.getAllocationPercent().compareTo(BigDecimal.ZERO) <= 0 ||
            allocation.getAllocationPercent().compareTo(new BigDecimal("100")) > 0) {
            throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                "Allocation percent must be between 1 and 100.");
        }

        if (allocation.getStartDate() != null && allocation.getEndDate() != null) {
            if (allocation.getStartDate().isAfter(allocation.getEndDate())) {
                throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                    "Start date cannot be after end date.");
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

    // ════════════════════════════════════════════════════════════
    //  ALLOCATIONS — DELETE
    // ════════════════════════════════════════════════════════════

    @Before(event = CqnService.EVENT_DELETE, entity = Allocations_.CDS_NAME)
    public void restrictAllocationDelete(CdsDeleteEventContext context) {
        if (!userInfo.hasRole("Admin")) {
            throw new ServiceException(ErrorStatuses.FORBIDDEN,
                "Only Admin can delete Allocations.");
        }
    }

    // ════════════════════════════════════════════════════════════
    //  ACTIONS
    // ════════════════════════════════════════════════════════════

    @On(event = "submit", entity = Allocations_.CDS_NAME)
    public void onSubmit(AllocationsSubmitContext context) {
        System.out.println("User : " + userInfo.getName());
        System.out.println("Is PM: " + userInfo.hasRole("ProjectManager"));
        System.out.println("Roles: " + userInfo.getRoles());
        if (!userInfo.hasRole("Admin") && !userInfo.hasRole("ProjectManager")) {
            throw new ServiceException(ErrorStatuses.FORBIDDEN,
                "Only Admin or ProjectManager can submit Allocations.");
        }
        String id = extractId(context.getCqn());
        Allocations alloc = getAllocationById(id);
        if (!"DRAFT".equals(alloc.getStatus())) {
            throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                "Only DRAFT allocations can be submitted.");
        }
        db.run(Update.entity(Allocations_.class)
            .data("status", "PENDING")
            .where(a -> a.ID().eq(id)));
        context.put("value", getAllocationById(id));
        context.getMessages().success("Allocation submitted successfully.").transition(true);
        context.setCompleted();
    }

    @On(event = "approve", entity = Allocations_.CDS_NAME)
    public void onApprove(AllocationsApproveContext context) {
        if (!userInfo.hasRole("Admin") && !userInfo.hasRole("HRManager")) {
            throw new ServiceException(ErrorStatuses.FORBIDDEN,
                "Only Admin or HRManager can approve Allocations.");
        }
        String id = extractId(context.getCqn());
        Allocations alloc = getAllocationById(id);
        if (!"PENDING".equals(alloc.getStatus())) {
            throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                "Only PENDING allocations can be approved.");
        }
        db.run(Update.entity(Allocations_.class)
            .data("status", "APPROVED")
            .where(a -> a.ID().eq(id)));
        context.put("value", getAllocationById(id));
        context.getMessages().success("Allocation approved successfully.").transition(true);
        context.setCompleted();
    }

    @On(event = "reject", entity = Allocations_.CDS_NAME)
    public void onReject(AllocationsRejectContext context) {
        if (!userInfo.hasRole("Admin") && !userInfo.hasRole("HRManager")) {
            throw new ServiceException(ErrorStatuses.FORBIDDEN,
                "Only Admin or HRManager can reject Allocations.");
        }
        String id = extractId(context.getCqn());
        Allocations alloc = getAllocationById(id);
        if (!"PENDING".equals(alloc.getStatus())) {
            throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                "Only PENDING allocations can be rejected.");
        }
        db.run(Update.entity(Allocations_.class)
            .data("status", "REJECTED")
            .where(a -> a.ID().eq(id)));
        context.put("value", getAllocationById(id));
        context.getMessages().success("Allocation rejected successfully.").transition(true);
        context.setCompleted();
    }

    // ════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════

    private String extractId(CqnStatement cqn) {
        Map<String, Object> keys = CqnAnalyzer.create(cdsModel)
                                    .analyze(cqn.ref())
                                    .targetKeys();
        return keys.get("ID").toString();
    }

    private Allocations getAllocationById(String id) {
        return db.run(
            Select.from(Allocations_.class)
                  .where(a -> a.ID().eq(id)))
            .first(Allocations.class)
            .orElseThrow(() -> new ServiceException(
                ErrorStatuses.NOT_FOUND, "Allocation not found."));
    }

    // ──  #1 — Filter projects for Employee ───────────────
    @Before(event = CqnService.EVENT_READ, entity = Projects_.CDS_NAME)
    public void filterProjectsForEmployee(List<Projects> projects) {
        System.out.println("Become the programmer you are meant to be!");
        if (!userInfo.hasRole("Employee")) return;
        if (projects == null || projects.isEmpty()) {
            return;
        }

        String currentUser = userInfo.getName();

        List<String> allocatedIds = db.run(
            Select.from(Allocations_.class)
                .columns(a -> a.project_ID())
                .where(a -> a.get("employee.email").eq(currentUser))
        )
        .stream()
        .map(row -> (String) row.get(Allocations.PROJECT_ID))
        .filter(id -> id != null)
        .collect(Collectors.toList());

        projects.removeIf(p -> !allocatedIds.contains(p.getId()));
        
        for (Projects project : projects) {
            System.out.println("User " + currentUser + " can see: " + project.getProjectName());
        }
    }


    @After(event = CqnService.EVENT_READ, entity = Allocations_.CDS_NAME)
    public void setActionAvailability(List<Allocations> allocations) {

        for (Allocations allocation : allocations) {

            allocation.setCanSubmit("DRAFT".equals(allocation.getStatus()));
            allocation.setCanApprove("PENDING".equals(allocation.getStatus()));
            allocation.setCanReject("PENDING".equals(allocation.getStatus()));
        }
    }

    

}