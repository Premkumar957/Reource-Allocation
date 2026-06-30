package customer.resource_allocation.handlers;

import com.sap.cds.services.EventContext;              // ✅ correct import
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsUpdateEventContext;
import com.sap.cds.services.cds.CdsDeleteEventContext;
import com.sap.cds.services.persistence.PersistenceService;
import com.sap.cds.ql.CQL;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.cqn.CqnAnalyzer;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.ql.cqn.CqnStatement;
import com.sap.cds.reflect.CdsModel;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.request.UserInfo;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cds.gen.resourceallocationservice.Allocations;
import cds.gen.resourceallocationservice.Allocations_;
import cds.gen.resourceallocationservice.Employees;
import cds.gen.resourceallocationservice.Employees_;
import cds.gen.resourceallocationservice.Projects;
import cds.gen.resourceallocationservice.Projects_;
import cds.gen.resourceallocationservice.ResourceAllocationService_;
import cds.gen.resourceallocationservice.UploadEmployeeDataContext;
import customer.resource_allocation.util.PdfFooter;
import cds.gen.resourceallocationservice.AllocationsSubmitContext;
import cds.gen.resourceallocationservice.AllocationsApproveContext;
import cds.gen.resourceallocationservice.AllocationsRejectContext;


import com.sap.cds.ql.Predicate;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import java.io.ByteArrayOutputStream;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import cds.gen.resourceallocationservice.ExportAllocationPdfContext;

import com.sap.cds.ql.Select;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.Chunk;
import com.lowagie.text.Rectangle;

import com.lowagie.text.Image;

@Component
@ServiceName(ResourceAllocationService_.CDS_NAME)
public class AllocationHandler implements EventHandler {

    @Autowired
    PersistenceService db;

    @Autowired
    CdsModel cdsModel;

    @Autowired
    private UserInfo userInfo;

    @Autowired
    private EmailService emailService;

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

        String projectName = alloc.getProjectTitle();
        String employeeName = alloc.getEmployeeFullName();
        emailService.sendEmail(
            "massprem1438124@gmail.com",
            "New Allocation Request",
            """
            Dear HR,

            A new allocation request has been submitted.

            Project : %s
            Employee: %s
            Status  : Pending Approval

            Please review the request.

            Regards,
            Resource Allocation System
            """.formatted(projectName, employeeName)
        );

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
        
        String projectName = alloc.getProjectTitle();
        String employeeName = alloc.getEmployeeFullName();
        emailService.sendEmail(
            "kavi878787@gmail.com",
            "Allocation Approved",
            """
            Dear Project Manager,

            Your allocation request has been approved.

            Project : %s
            Employee: %s

            Regards,
            HR Team
            """.formatted(projectName, employeeName)
        );

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
        
        String projectName = alloc.getProjectTitle();
        String employeeName = alloc.getEmployeeFullName();
        emailService.sendEmail(
            "kavi878787@gmail.com",
            "Allocation Rejected",
            """
            Dear Project Manager,

            Your allocation request has been rejected.

            Project : %s
            Employee: %s

            Please review and resubmit if necessary.

            Regards,
            HR Team
            """.formatted(projectName, employeeName)
        );

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

    @Before(event = CqnService.EVENT_CREATE, entity = Projects_.CDS_NAME)
    public void validateProjectValidation(Projects context) {
        

        LocalDate startDate = context.getStartDate();
        LocalDate endDate = context.getEndDate();

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException(ErrorStatuses.BAD_REQUEST,
                "Project start date cannot be after end date.");
        }
    }


    @On(event = "uploadEmployeeData")
    public void uploadEmployeeData(UploadEmployeeDataContext context) {
        System.out.println("uploadEmployeeData called");

        byte[] file = context.getFile();

        System.out.println(file == null ? "File is NULL" : "File size = " + file.length);

        if (file == null || file.length == 0) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Uploaded file is empty.");
        }

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {

            Sheet sheet = workbook.getSheet("Employees");

            if (sheet == null) {
                throw new ServiceException(
                        ErrorStatuses.BAD_REQUEST,
                        "Employees sheet not found.");
            }

            DataFormatter formatter = new DataFormatter();

            // Skip Header Row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                String employeeId = formatter.formatCellValue(row.getCell(0)).trim();
                String employeeName = formatter.formatCellValue(row.getCell(1)).trim();
                String email = formatter.formatCellValue(row.getCell(2)).trim();
                String department = formatter.formatCellValue(row.getCell(3)).trim();
                String designation = formatter.formatCellValue(row.getCell(4)).trim();
                

                if (employeeId.isBlank()) {
                    continue;
                }

                Employees employee = Employees.create();

                employee.setEmployeeId(employeeId);
                employee.setEmployeeName(employeeName);
                employee.setEmail(email);
                employee.setDepartment(department);
                employee.setDesignation(designation);
                

                db.run(
                    Insert.into(Employees_.class)
                        .entry(employee)
                );
            }

            context.getMessages().success("Employees uploaded successfully.");

            context.setCompleted();

        } catch (IOException e) {
            throw new ServiceException(
                    ErrorStatuses.BAD_REQUEST,
                    "Invalid Excel file.");
        }
    }


    @On(event = "exportAllocationPdf")
    public void exportAllocationPdf(ExportAllocationPdfContext context) throws Exception {
        List<Allocations> allocations =
                                db.run(
                                    Select.from(Allocations_.class)
                                ).listOf(Allocations.class);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


        Document document =
                new Document(PageSize.A4.rotate());

        PdfWriter writer =
                PdfWriter.getInstance(
                        document,
                        outputStream);

        writer.setPageEvent(new PdfFooter());


        document.open();

        Image logo = Image.getInstance(
            getClass()
                .getResource("/static/images/logo.png")
        );

        logo.scaleToFit(80, 80);
        logo.setAlignment(Element.ALIGN_CENTER);

        document.add(logo);

        // PDF Design comes here

        // 1. Create Fonts
        Font companyFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        22,
                        Color.BLUE);

        Font titleFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        16,
                        Color.BLACK);

        Font headerFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        11,
                        Color.WHITE);

        Font normalFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        11,
                        Color.BLACK);

        Font infoFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        10,
                        Color.DARK_GRAY);

        // 2.Company Name
        Paragraph company =
                new Paragraph(
                        "Inflexion Infotech Pvt Ltd",
                        companyFont);

        company.setAlignment(Element.ALIGN_CENTER);

        document.add(company);

        // 3. Report Title
        Paragraph title =
                new Paragraph(
                        "EMPLOYEE RESOURCE ALLOCATION REPORT",
                        titleFont);

        title.setAlignment(Element.ALIGN_CENTER);

        document.add(title);

        // 4.Space
        document.add(new Paragraph(" "));

        // 5. Generated By
        document.add(
                new Paragraph(
                        "Generated By : "
                                + userInfo.getName(),
                        infoFont));

        // 6. Generated Date
        String generatedDate =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "dd-MM-yyyy HH:mm"));

        document.add(
                new Paragraph(
                        "Generated On : "
                                + generatedDate,
                        infoFont));
        // 7.Total Records
        document.add(
                new Paragraph(
                        "Total Records : "
                                + allocations.size(),
                        infoFont));
        
        // 8. Horizontal Line
        document.add(new Chunk(
                        "---------------------------------------------------------------"));
        
        // 9.Create Table
        PdfPTable table =
                new PdfPTable(7);

        table.setWidthPercentage(100);
            
        // 10. Set column width
        table.setWidths(
            new float[]{
                    2f,
                    4f,
                    4f,
                    3f,
                    2f,
                    3f,
                    2f
            });
        
        table.addCell(
                headerCell("Employee Code"));

        table.addCell(
                headerCell("Employee Name"));

        table.addCell(
                headerCell("Project"));

        table.addCell(
                headerCell("Role"));

        table.addCell(
                headerCell("Allocation %"));

        table.addCell(
                headerCell("Status"));

        table.addCell(
                headerCell("Start Date"));
        

        for(Allocations allocation : allocations){

            table.addCell(
                    allocation.getEmployeeCode());

            table.addCell(
                    allocation.getEmployeeFullName());

            table.addCell(
                    allocation.getProjectTitle());

            table.addCell(
                    allocation.getRole());

            table.addCell(
                    allocation.getAllocationPercent().toString());

            PdfPCell statusCell =
                    new PdfPCell(
                            new Paragraph(
                                    allocation.getStatus()));

            switch (allocation.getStatus()) {

                case "APPROVED":
                    statusCell.setBackgroundColor(Color.GREEN);
                    break;

                case "PENDING":
                    statusCell.setBackgroundColor(Color.ORANGE);
                    break;

                case "REJECTED":
                    statusCell.setBackgroundColor(Color.RED);
                    break;

                default:
                    statusCell.setBackgroundColor(Color.LIGHT_GRAY);

            }

            table.addCell(statusCell);

            table.addCell(
                    allocation.getStartDate().toString());

        }

        document.add(table);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(
                "DETAILED ALLOCATION INFORMATION",
                titleFont));

        document.add(new Paragraph(" "));

        //Print every allocation
        for (Allocations allocation : allocations) {

            document.add(new Paragraph(
                    "Employee Name : "
                            + allocation.getEmployeeFullName(),
                    normalFont));

            document.add(new Paragraph(
                    "Employee Code : "
                            + allocation.getEmployeeCode(),
                    normalFont));

            document.add(new Paragraph(
                    "Project Name : "
                            + allocation.getProjectTitle(),
                    normalFont));

            document.add(new Paragraph(
                    "Project Code : "
                            + allocation.getProjectCodeValue(),
                    normalFont));

            document.add(new Paragraph(
                    "Role : "
                            + allocation.getRole(),
                    normalFont));

            document.add(new Paragraph(
                    "Allocation % : "
                            + allocation.getAllocationPercent(),
                    normalFont));

            document.add(new Paragraph(
                    "Start Date : "
                            + allocation.getStartDate(),
                    normalFont));

            document.add(new Paragraph(
                    "End Date : "
                            + allocation.getEndDate(),
                    normalFont));

            document.add(new Paragraph(
                    "Status : "
                            + allocation.getStatus(),
                    normalFont));

            document.add(new Paragraph(
                    "--------------------------------------------------------"));
        }


        long approved =
                allocations.stream()
                        .filter(a ->
                                "APPROVED".equals(a.getStatus()))
                        .count();

        long pending =
                allocations.stream()
                        .filter(a ->
                                "PENDING".equals(a.getStatus()))
                        .count();

        long rejected =
                allocations.stream()
                        .filter(a ->
                                "REJECTED".equals(a.getStatus()))
                        .count();

        document.add(new Paragraph(" "));

        document.add(new Paragraph(
                "STATUS SUMMARY",
                titleFont));

        document.add(new Paragraph(" "));

        document.add(new Paragraph(
                "Approved : " + approved));

        document.add(new Paragraph(
                "Pending : " + pending));

        document.add(new Paragraph(
                "Rejected : " + rejected));

        document.add(new Paragraph(
                "Total : " + allocations.size()));


        document.add(new Paragraph(" "));

        Paragraph footer =
                new Paragraph(
                        "Generated by Resource Allocation System",
                        infoFont);

        footer.setAlignment(
                Element.ALIGN_CENTER);

        document.add(footer);

        Paragraph confidential =
                new Paragraph(
                        "CONFIDENTIAL",
                        infoFont);

        confidential.setAlignment(
                Element.ALIGN_CENTER);

        document.add(confidential);
                        

        document.close();

        context.setResult(outputStream.toByteArray());

        context.setCompleted();

    }

    private PdfPCell headerCell(String text){

        Font font =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        11,
                        Color.WHITE);

        PdfPCell cell =
                new PdfPCell(
                        new Paragraph(text,font));

        cell.setBackgroundColor(Color.GRAY);

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        cell.setPadding(8);

        return cell;

    }

    

}