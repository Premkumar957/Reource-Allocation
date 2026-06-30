sap.ui.define([
    "sap/m/MessageToast"
], function(MessageToast) {
    'use strict';

    return {
        /**
         * Generated event handler.
         *
         * @param oContext the context of the page on which the event was fired. `undefined` for list report page.
         * @param aSelectedContexts the selected contexts of the table rows.
         */
        exportPdf: async function(oContext, aSelectedContexts) {
            MessageToast.show("Custom handler invoked.");


            const oModel = this.getModel();

            try {

                const oAction = oModel.bindContext("/exportAllocationPdf(...)");

                await oAction.execute();

                const pdf = oAction.getBoundContext().getObject().value;

                const blob = new Blob(
                    [Uint8Array.from(atob(pdf), c => c.charCodeAt(0))],
                    { type: "application/pdf" }
                );

                const url = URL.createObjectURL(blob);

                const link = document.createElement("a");

                link.href = url;

                link.download = "EmployeeAllocationReport.pdf";

                link.click();

                URL.revokeObjectURL(url);

                MessageToast.show("PDF downloaded successfully");

            } catch (e) {

                console.log(e);

                MessageToast.show("Failed to export PDF");

            }

            
        }
    };
});
