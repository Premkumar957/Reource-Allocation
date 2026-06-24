using ResourceAllocationService as service from '../../srv/catalog-service';
annotate service.Allocations with @(
    UI.FieldGroup #GeneratedGroup : {
        $Type : 'UI.FieldGroupType',
        Data : [
            {
                $Type : 'UI.DataField',
                Value : employee.employeeId,
                Label : 'Employee Id',
            },
            {
                $Type : 'UI.DataField',
                Value : employee.employeeName,
                Label : 'Employee Name',
            },
            {
                $Type : 'UI.DataField',
                Value : project.projectName,
                Label : 'Project Name',
            },
            {
                $Type : 'UI.DataField',
                Value : allocationPercent,
                Label : 'Allocation Percentage',
            },
            {
                $Type : 'UI.DataField',
                Label : 'Role',
                Value : role,
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
            Value : employee.employeeId,
            Label : 'Employee Id',
        },
        {
            $Type : 'UI.DataField',
            Value : employee.employeeName,
            Label : 'Employee Name',
        },
        {
            $Type : 'UI.DataField',
            Value : project.projectName,
            Label : 'Project Name',
        },
        {
            $Type : 'UI.DataFieldForAnnotation',
            Target : '@UI.DataPoint#allocationPercent',
            Label : 'Allocation Percentage',
        },
        {
            $Type : 'UI.DataField',
            Label : 'Role',
            Value : role,
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
    UI.HeaderInfo : {
        Title : {
            $Type : 'UI.DataField',
            Value : employee.employeeName,
        },
        TypeName : '',
        TypeNamePlural : '',
    },
    UI.DataPoint #allocationPercent : {
        Value : allocationPercent,
        Visualization : #Progress,
        TargetValue : 100,
    },
);

annotate service.Allocations with {
    employee @Common.ValueList : {
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
                $Type : 'Common.ValueListParameterDisplayOnly',
                ValueListProperty : 'employeeName',
            },
        ],
    }
};

annotate service.Allocations with {
    project @Common.ValueList : {
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
                $Type : 'Common.ValueListParameterDisplayOnly',
                ValueListProperty : 'projectName',
            },
        ],
    }
};

annotate service.Employees with {
    employeeId @(
        Common.ValueList : {
            $Type : 'Common.ValueListType',
            CollectionPath : 'Employees',
            Parameters : [
                {
                    $Type : 'Common.ValueListParameterInOut',
                    LocalDataProperty : employeeId,
                    ValueListProperty : 'employeeId',
                },
            ],
        },
        Common.ValueListWithFixedValues : false
)};

annotate service.Projects with {
    projectName @(
        Common.ValueList : {
            $Type : 'Common.ValueListType',
            CollectionPath : 'Projects',
            Parameters : [
                {
                    $Type : 'Common.ValueListParameterInOut',
                    LocalDataProperty : projectName,
                    ValueListProperty : 'projectCode',
                },
            ],
        },
        Common.ValueListWithFixedValues : false,
)};

annotate service.Projects with {
    projectCode @(
        Common.Text : projectName,
        Common.Text.@UI.TextArrangement : #TextSeparate,
)};





