using com.employeemanagement as bc from '../db/employee.cds';
using com.projectmanagement as bp from '../db/project.cds';
using com.allocationmanagement as ba from '../db/allocation.cds';

service ResourceAllocationService {
    
    @odata.draft.enabled
    entity Employees
            as projection on bc.Employees;

    entity EmployeeSkills 
            as projection on bc.EmployeeSkills;

    @odata.draft.enabled
    entity Projects 
            as projection on bp.Projects;
    
    @odata.draft.enabled
    entity Allocations 
            as projection on ba.Allocations
            actions {
                action submit();
                action approve();
                action reject();
            };

    entity Skills
            as projection on bc.Skills;
}


