sap.ui.define([
    "sap/fe/test/JourneyRunner",
	"com/company/project/resourceallocation/test/integration/pages/AllocationsList",
	"com/company/project/resourceallocation/test/integration/pages/AllocationsObjectPage"
], function (JourneyRunner, AllocationsList, AllocationsObjectPage) {
    'use strict';

    var runner = new JourneyRunner({
        launchUrl: sap.ui.require.toUrl('com/company/project/resourceallocation') + '/test/flpSandbox.html#comcompanyprojectresourcealloc-tile',
        pages: {
			onTheAllocationsList: AllocationsList,
			onTheAllocationsObjectPage: AllocationsObjectPage
        },
        async: true
    });

    return runner;
});

