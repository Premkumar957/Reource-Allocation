sap.ui.define([
    "sap/fe/test/JourneyRunner",
	"com/company/project/employees/test/integration/pages/EmployeesList",
	"com/company/project/employees/test/integration/pages/EmployeesObjectPage",
	"com/company/project/employees/test/integration/pages/EmployeeSkillsObjectPage"
], function (JourneyRunner, EmployeesList, EmployeesObjectPage, EmployeeSkillsObjectPage) {
    'use strict';

    var runner = new JourneyRunner({
        launchUrl: sap.ui.require.toUrl('com/company/project/employees') + '/test/flpSandbox.html#comcompanyprojectemployees-tile',
        pages: {
			onTheEmployeesList: EmployeesList,
			onTheEmployeesObjectPage: EmployeesObjectPage,
			onTheEmployeeSkillsObjectPage: EmployeeSkillsObjectPage
        },
        async: true
    });

    return runner;
});

