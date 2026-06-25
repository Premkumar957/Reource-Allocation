using com.employeemanagement as bc from '../db/employee.cds';
using com.projectmanagement as bp from '../db/project.cds';
using com.allocationmanagement as ba from '../db/allocation.cds';

service ResourceAllocationService {
    
    @requires: ['Admin', 'HRManager', 'ProjectManager', 'Employee']
    @odata.draft.enabled
    entity Employees
            as projection on bc.Employees;

    @requires: ['Admin', 'HRManager']
    entity EmployeeSkills 
            as projection on bc.EmployeeSkills;

    // ── PROJECTS ───────────────────────────────────────────────
    // ProjectManager sees own projects only (filtered in Java)
    // HRManager & Admin see all
    @requires: ['Admin', 'HRManager', 'ProjectManager']
    @odata.draft.enabled
    entity Projects 
            as projection on bp.Projects;

    
    // ── ALLOCATIONS ────────────────────────────────────────────
    // All roles access — filtered in Java handler per role
    @requires: ['Admin', 'HRManager', 'ProjectManager', 'Employee']
    @odata.draft.enabled
    entity Allocations 
            as projection on ba.Allocations {
                *,
                employee.employeeId as employeeCode,
                employee.employeeName as employeeFullName,
                project.projectCode as projectCodeValue,
                project.projectName as projectTitle,
            } actions {
                // ProjectManager submits allocation
                @requires: ['Admin', 'ProjectManager']
                action submit();

                // HRManager approves allocation
                @requires: ['Admin', 'HRManager']
                action approve();

                // HRManager rejects allocation
                @requires: ['Admin', 'HRManager']
                action reject();
            };

    entity Skills
            as projection on bc.Skills;
}


