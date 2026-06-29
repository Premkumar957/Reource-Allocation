sap.ui.define([
    "sap/m/MessageToast",
    "sap/m/Dialog",
    "sap/m/Button",
    "sap/m/upload/UploadSet",
], function(MessageToast, Dialog, Button, UploadSet) {
    'use strict';

    return {
        /**
         * Generated event handler.
         *
         * @param oContext the context of the page on which the event was fired. `undefined` for list report page.
         * @param aSelectedContexts the selected contexts of the table rows.
         */
        upload: function(oContext, aSelectedContexts) {
            let oSelectedItem = null;
            const oModel = this.getModel();

            const oUploadSet = new UploadSet({
                fileTypes: ["xlsx"],
                instantUpload: false,
                multiple: false,

                afterItemAdded: function (oEvent) {
                    oSelectedItem = oEvent.getParameter("item");
                }
            });

            const oDialog = new Dialog({
                title: "Upload Employee Excel",
                contentWidth: "30rem",
                content: oUploadSet,

                beginButton: new Button({
                    text: "Upload",
                    type: "Emphasized",

                    press: function () {
                        if (!oSelectedItem) {
                            MessageToast.show("Please select an Excel file");
                            return;
                        }

                        const oFile = oSelectedItem.getFileObject();
                        const reader = new FileReader();

                        reader.onload = async function (e) {
                            const base64 = e.target.result.split(",")[1];

                            try {

                                await oModel
                                        .bindContext("/uploadEmployeeData(...)")
                                        .setParameter("file", base64)
                                        .execute();

                                MessageToast.show("Employees data uploaded successfully");
                                oModel.refresh();
                            } catch (error) {
                                console.log(error);
                                MessageToast.show("Employees data upload failed");
                            }

                            oDialog.close();
                            oDialog.destroy();
                        };

                        reader.readAsDataURL(oFile);
                    }
                }),

                endButton: new Button({
                    text: "Cancel",
                    press: function () {
                        oDialog.close();
                        oDialog.destroy();
                    }
                })
            });

            oDialog.open();

        }
    };
});
