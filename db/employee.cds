namespace com.employeemanagement;


using { cuid, managed } from '@sap/cds/common';



entity Employees : cuid, managed {

    employeeId   : String(20);
    employeeName : String(100);

    department   : String(50);
    designation  : String(50);
    email        : String(50);

    skills       : Composition of many EmployeeSkills on skills.employee = $self;

}

entity Skills : cuid {
    
    skillCode : String(20);
    skillName : String(100);
}


entity EmployeeSkills : cuid {
    employee : Association to Employees;
    skill    : Association to Skills;
}