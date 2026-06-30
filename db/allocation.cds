namespace com.allocationmanagement;


using { cuid, managed } from '@sap/cds/common';
using { com.employeemanagement as be} from './employee.cds';
using { com.projectmanagement as bp} from './project.cds';


entity Allocations : cuid, managed {

    employee          : Association to be.Employees;
    project           : Association to bp.Projects;

    role              : String(50);

    @assert.range: [1, 100]
    allocationPercent : Decimal(5,2);

    startDate         : Date;
    endDate           : Date;

    status            : String(20) default 'DRAFT';

    virtual canSubmit         : Boolean;
    virtual canApprove        : Boolean;
    virtual canReject         : Boolean;
}