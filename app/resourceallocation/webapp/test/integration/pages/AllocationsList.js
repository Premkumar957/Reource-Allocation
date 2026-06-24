sap.ui.define(['sap/fe/test/ListReport'], function(ListReport) {
    'use strict';

    var CustomPageDefinitions = {
        actions: {},
        assertions: {}
    };

    return new ListReport(
        {
            appId: 'com.company.project.resourceallocation',
            componentId: 'AllocationsList',
            contextPath: '/Allocations'
        },
        CustomPageDefinitions
    );
});