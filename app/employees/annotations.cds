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
        TypeName : '',
        TypeNamePlural : '',
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
            Value : skill.skillCode,
            Label : 'Skill Code',
        },
        {
            $Type : 'UI.DataField',
            Value : skill.skillName,
            Label : 'Skill Name',
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
                Value : skill.skillCode,
                Label : 'Skill Code',
            },
            {
                $Type : 'UI.DataField',
                Value : skill.skillName,
                Label : 'Skill Name',
            },
        ],
    },
    UI.HeaderInfo : {
        Title : {
            $Type : 'UI.DataField',
            Value : skill.skillCode,
        },
        TypeName : '',
        TypeNamePlural : '',
    },
);

