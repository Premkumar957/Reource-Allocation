using com.employeemanagement as bc from '../db/employee.cds';
using com.projectmanagement as bp from '../db/project.cds';
using com.allocationmanagement as ba from '../db/allocation.cds';

service ResourceAllocationService {
    
    @restrict: [
        { grant: '*',    to: 'Admin'          },
        { grant: '*',    to: 'HRManager'      },
        { grant: 'READ', to: 'ProjectManager' },
        { grant: 'READ', to: 'Employee'       }
    ]
    @odata.draft.enabled
    entity Employees
            as projection on bc.Employees;

            
    @requires: ['Admin', 'HRManager']
    action uploadEmployeeData(file : LargeBinary);

    action exportAllocationPdf() returns LargeBinary;

    @restrict: [
        { grant: '*',    to: 'Admin'          },
        { grant: '*',    to: 'HRManager'      },
        { grant: 'READ', to: 'ProjectManager' },
        { grant: 'READ', to: 'Employee'       }
    ]
    entity EmployeeSkills 
            as projection on bc.EmployeeSkills;

    // ── PROJECTS ───────────────────────────────────────────────
    // ProjectManager sees own projects only (filtered in Java)
    // HRManager & Admin see all
    @restrict: [
        { grant: '*',    to: 'Admin'                                    },
        { grant: '*',    to: 'HRManager'                                },
        { grant: ['READ', 'UPDATE', 'CREATE'], to: 'ProjectManager', where: 'createdBy = $user' },
        { grant: 'READ', to: 'Employee' }
    ]
    @odata.draft.enabled
    entity Projects 
            as projection on bp.Projects;

    
    // ── ALLOCATIONS ────────────────────────────────────────────
    // All roles access — filtered in Java handler per role
    @restrict: [
        { grant: '*',    to: 'Admin'                                    },
        { grant: '*',    to: 'HRManager'                                },
        { grant: '*' , to: 'ProjectManager',
          where: 'projectCreatedBy = $user'                            },
        { grant: 'READ', to: 'Employee',
          where: 'employee.email = $user'                               }
    ]
    @odata.draft.enabled
    entity Allocations 
            as projection on ba.Allocations {
                *,
                project.createdBy as projectCreatedBy,
                employee.employeeId as employeeCode,
                employee.employeeName as employeeFullName,
                employee.designation as employeeRole,
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

    @restrict: [
        { grant: '*',    to: 'Admin'          },
        { grant: '*',    to: 'HRManager'      },
        { grant: 'READ', to: 'ProjectManager' },
        { grant: 'READ', to: 'Employee'       }
    ]
    entity Skills
            as projection on bc.Skills;
}


