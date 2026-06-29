sap.ui.define([
    "sap/m/MessageToast",
    "sap/ui/thirdparty/jquery"
], function (MessageToast, jQuery) {
    "use strict";

    return {

        downloadTemplate: function () {
            jQuery.sap.includeScript(
                "https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js",
                "xlsxLib",
                function () {
                    if (typeof XLSX === "undefined") {
                        MessageToast.show("XLSX not loaded!");
                        return;
                    }

                    // ================= DATA =================

                    const Employees = [
                        {
                            "Employee ID": "",
                            "Employee Name": "",
                            "Email": "",
                            "Department": "",
                            "Designation": ""
                        }
                    ];

                    const EmployeeSkills = [
                        {
                            "Employee ID": "",
                            "Skill Code": ""
                        }
                    ];

                    //==========================
                    // Create Workbook
                    //==========================

                    const workbook = XLSX.utils.book_new();

                    function createSheet(data, columnWidths) {
                        const worksheet = XLSX.utils.json_to_sheet(data);
                        worksheet["!cols"] = columnWidths.map(width => ({
                            wch: width
                        }));
                        return worksheet;
                    }

                    //==========================
                    // Employees Worksheet
                    //==========================

                    const employeeSheet = createSheet(
                        Employees,
                        [20, 30, 35, 25, 30]
                    );

                    //==========================
                    // Employee Skills Worksheet
                    //==========================

                    const skillSheet = createSheet(
                        EmployeeSkills,
                        [25, 35]
                    );

                    //==========================
                    // Add Sheets
                    //==========================

                    XLSX.utils.book_append_sheet(
                        workbook,
                        employeeSheet,
                        "Employees"
                    );

                    XLSX.utils.book_append_sheet(
                        workbook,
                        skillSheet,
                        "Employee Skills"
                    );

                    //==========================
                    // Download Excel
                    //==========================

                    XLSX.writeFile(
                        workbook,
                        "Employees_Template.xlsx"
                    );

                    MessageToast.show("Employees Template Downloaded Successfully");

                },
                function () {
                    MessageToast.show("Failed to load XLSX library");
                }
            );
        }


    };
});