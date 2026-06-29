using ResourceAllocationService as service from '../../srv/catalog-service';
annotate service.Allocations with @(
    UI.FieldGroup #GeneratedGroup : {
        $Type : 'UI.FieldGroupType',
        Data : [
            {
                $Type : 'UI.DataField',
                Value : employee_ID,
                Label : 'Employee Id',
            },
            {
                $Type : 'UI.DataField',
                Value : employeeFullName,
                Label : 'Employee Name',
            },
            {
                $Type : 'UI.DataField',
                Label : 'Role',
                Value : employeeRole,
            },
            {
                $Type : 'UI.DataField',
                Value : project_ID,
                Label : 'Project Id',
            },
            {
                $Type : 'UI.DataField',
                Value : projectTitle,
                Label : 'Project Title',
            },
            {
                $Type : 'UI.DataField',
                Value : allocationPercent,
                Label : 'Allocation Percentage',
            },
            {
                $Type : 'UI.DataField',
                Label : 'Start Date',
                Value : startDate,
            },
            {
                $Type : 'UI.DataField',
                Label : 'End Date',
                Value : endDate,
            },
            {
                $Type : 'UI.DataField',
                Label : 'Status',
                Value : status,
            },
        ],
    },
    UI.Facets : [
        {
            $Type : 'UI.ReferenceFacet',
            ID : 'GeneratedFacet1',
            Label : 'General Information',
            Target : '@UI.FieldGroup#GeneratedGroup',
        },
    ],
    UI.LineItem : [
        {
            $Type : 'UI.DataField',
            Value : employee_ID,
            Label : 'Employee Id',
        },
        {
            $Type : 'UI.DataField',
            Value : employeeFullName,
            Label : 'Employee Name',
        },
        {
            $Type : 'UI.DataField',
            Value : project_ID,
            Label : 'Project Id',
        },
        {
            $Type : 'UI.DataField',
            Value : projectTitle,
            Label : 'Project Title',
        },
        {
            $Type : 'UI.DataFieldForAnnotation',
            Target : '@UI.DataPoint#allocationPercent',
            Label : 'Allocation Percentage',
        },
        {
            $Type : 'UI.DataField',
            Label : 'Role',
            Value : employeeRole,
        },
        {
            $Type : 'UI.DataField',
            Label : 'Status',
            Value : status,
        },
        {
            $Type : 'UI.DataFieldForAction',
            Action : 'ResourceAllocationService.submit',
            Label : 'Submit'
        },
        {
            $Type : 'UI.DataFieldForAction',
            Action : 'ResourceAllocationService.approve',
            Label : 'Approve'
        },
        {
            $Type : 'UI.DataFieldForAction',
            Action : 'ResourceAllocationService.reject',
            Label : 'Reject'
        }
    ],
    UI.HeaderInfo : {
        Title : {
            $Type : 'UI.DataField',
            Value : employee.employeeName,
        },
        TypeName : 'Resource Allocation',
        TypeNamePlural : 'Resource Allocations',
    },
    UI.DataPoint #allocationPercent : {
        Value : allocationPercent,
        Visualization : #Progress,
        TargetValue : 100,
    },
);


annotate service.Allocations with {
    employee @(
        Common.ValueList : {
            $Type : 'Common.ValueListType',
            CollectionPath : 'Employees',
            Parameters : [
                {
                    $Type : 'Common.ValueListParameterInOut',
                    LocalDataProperty : employee_ID,
                    ValueListProperty : 'ID',
                },
                {
                    $Type : 'Common.ValueListParameterDisplayOnly',
                    ValueListProperty : 'employeeId',
                },
                {
                    $Type : 'Common.ValueListParameterOut',
                    LocalDataProperty : employeeFullName,
                    ValueListProperty : 'employeeName',
                },
                {
                    $Type : 'Common.ValueListParameterDisplayOnly',
                    ValueListProperty : 'department',
                },
                {
                    $Type : 'Common.ValueListParameterOut',
                    LocalDataProperty : employeeRole,
                    ValueListProperty : 'designation',
                }
            ],
        },
        Common.ValueListWithFixedValues : false,
    )
};

annotate service.Employees with {
    ID @(
        UI.Hidden : true
    );
};

annotate service.Allocations with {
    employee @(
        Common.Text : employee.employeeId,
        Common.TextArrangement : #TextOnly
    );
};


annotate service.Allocations with {
    project @(
        Common.ValueList : {
            $Type : 'Common.ValueListType',
            CollectionPath : 'Projects',
            Parameters : [
                {
                    $Type : 'Common.ValueListParameterInOut',
                    LocalDataProperty : project_ID,
                    ValueListProperty : 'ID',
                },
                {
                    $Type : 'Common.ValueListParameterDisplayOnly',
                    ValueListProperty : 'projectCode',
                },
                {
                    $Type : 'Common.ValueListParameterOut',
                    LocalDataProperty : projectTitle,
                    ValueListProperty : 'projectName',
                }
            ],
        },
        Common.ValueListWithFixedValues : false,
)};

annotate service.Projects with {
    ID @(
        UI.Hidden : true
    );
};


annotate  service.Allocations with {
    project @(
        Common.Text : project.projectCode,
        Common.TextArrangement : #TextOnly
    );
};

annotate service.Allocations actions {

    submit @Core.OperationAvailable : canSubmit;

    approve @Core.OperationAvailable : canApprove;

    reject @Core.OperationAvailable : canReject;

};


