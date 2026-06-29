using ResourceAllocationService as service from '../../srv/catalog-service';
annotate service.Employees with @(
    UI.FieldGroup #GeneratedGroup : {
        $Type : 'UI.FieldGroupType',
        Data : [
            {
                $Type : 'UI.DataField',
                Label : 'Employee Id',
                Value : employeeId,
            },
            {
                $Type : 'UI.DataField',
                Label : 'Employee Name',
                Value : employeeName,
            },
            {
                $Type : 'UI.DataField',
                Value : email,
                Label : 'Email',
            },
            {
                $Type : 'UI.DataField',
                Label : 'Department',
                Value : department,
            },
            {
                $Type : 'UI.DataField',
                Label : 'Designation',
                Value : designation,
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
        {
            $Type : 'UI.ReferenceFacet',
            Label : 'Skills',
            ID : 'Skills',
            Target : 'skills/@UI.LineItem#Skills',
        },
    ],
    UI.LineItem : [
        {
            $Type : 'UI.DataField',
            Label : 'Employee Id',
            Value : employeeId,
        },
        {
            $Type : 'UI.DataField',
            Label : 'Employee Name',
            Value : employeeName,
        },
        {
            $Type : 'UI.DataField',
            Label : 'Department',
            Value : department,
        },
        {
            $Type : 'UI.DataField',
            Label : 'Designation',
            Value : designation,
        },
    ],
    UI.HeaderInfo : {
        Title : {
            $Type : 'UI.DataField',
            Value : employeeId,
        },
        TypeName : 'Employee',
        TypeNamePlural : 'Employees',
        Description : {
            $Type : 'UI.DataField',
            Value : employeeName,
        },
    },
);

annotate service.EmployeeSkills with @(
    UI.LineItem #Skills : [
        {
            $Type : 'UI.DataField',
            Value : skill_ID,
            Label : 'Skill Code',
        },
    ],
    UI.Facets : [
        {
            $Type : 'UI.ReferenceFacet',
            Label : 'General Information',
            ID : 'GeneralInformation',
            Target : '@UI.FieldGroup#GeneralInformation',
        },
    ],
    UI.FieldGroup #GeneralInformation : {
        $Type : 'UI.FieldGroupType',
        Data : [
            {
                $Type : 'UI.DataField',
                Value : employee_ID,
                Label : 'Employee',
                @UI.Hidden,
            },
            {
                $Type : 'UI.DataField',
                Value : skill_ID,
                Label : 'Skill Code',
            },
        ],
    },
    UI.HeaderInfo : {
        Title : {
            $Type : 'UI.DataField',
            Value : skill.skillCode,
        },
        TypeName : 'Employee Skill',
        TypeNamePlural : 'Employee Skills',
    },
);

annotate service.EmployeeSkills with {
    skill @(
        Common.ValueList : {
            $Type : 'Common.ValueListType',
            CollectionPath : 'Skills',
            Parameters : [
                {
                    $Type : 'Common.ValueListParameterInOut',
                    LocalDataProperty : skill_ID,
                    ValueListProperty : 'ID',
                },
                {
                    $Type : 'Common.ValueListParameterDisplayOnly',
                    ValueListProperty : 'skillCode',
                },
                {
                    $Type : 'Common.ValueListParameterDisplayOnly',
                    ValueListProperty : 'skillName',
                }
            ],
        },
        Common.ValueListWithFixedValues : false,
)};


annotate service.Skills with {
    ID @(
        UI.Hidden : true
    );
};

annotate  service.EmployeeSkills with {
    skill @(
        Common.Text : skill.skillCode,
        Common.TextArrangement : #TextOnly
    );
};



