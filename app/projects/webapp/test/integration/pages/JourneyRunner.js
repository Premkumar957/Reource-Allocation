sap.ui.define([
    "sap/fe/test/JourneyRunner",
	"com/company/project/projects/test/integration/pages/ProjectsList",
	"com/company/project/projects/test/integration/pages/ProjectsObjectPage"
], function (JourneyRunner, ProjectsList, ProjectsObjectPage) {
    'use strict';

    var runner = new JourneyRunner({
        launchUrl: sap.ui.require.toUrl('com/company/project/projects') + '/test/flpSandbox.html#comcompanyprojectprojects-tile',
        pages: {
			onTheProjectsList: ProjectsList,
			onTheProjectsObjectPage: ProjectsObjectPage
        },
        async: true
    });

    return runner;
});

