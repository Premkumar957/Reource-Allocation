namespace com.projectmanagement;


using { cuid, managed } from '@sap/cds/common';


entity Projects : cuid, managed {

    projectCode : String(20);

    projectName : String(100);

    clientName : String(100);

    startDate : Date;
    endDate   : Date;

    status : String(20) default 'ACTIVE';
}