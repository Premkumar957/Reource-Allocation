using ResourceAllocationService as service from '../../srv/catalog-service';
annotate service.Projects with @(
    UI.FieldGroup #GeneratedGroup : {
        $Type : 'UI.FieldGroupType',
        Data : [
            {
                $Type : 'UI.DataField',
                Label : 'Project Code',
                Value : projectCode,
            },
            {
                $Type : 'UI.DataField',
                Label : 'Project Name',
                Value : projectName,
            },
            {
                $Type : 'UI.DataField',
                Label : 'Client Name',
                Value : clientName,
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
            Label : 'Project Code',
            Value : projectCode,
        },
        {
            $Type : 'UI.DataField',
            Label : 'Project Name',
            Value : projectName,
        },
        {
            $Type : 'UI.DataField',
            Label : 'Client Name',
            Value : clientName,
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
    ],
    UI.HeaderInfo : {
        Title : {
            $Type : 'UI.DataField',
            Value : projectCode,
        },
        TypeName : '',
        TypeNamePlural : '',
    },
);

