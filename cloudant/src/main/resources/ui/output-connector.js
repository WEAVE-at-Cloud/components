$.foxweave.addComponentView(function() {
    var component = this;
    var cloudantAccount = component.accountSelected;
    var dbnameSelect = $("#cloudant_database_name");
    var messageStructureRow = $("#cloudant_message_structure_row");
    var messageStructureTextArea = $("#cloudant_message_structure");

    function getCloudantRes(resource, successCallback, loadingImgOn) {
        if (cloudantAccount !== undefined && cloudantAccount.accountName !== undefined) {
            var baseUrl = 'https://' + encodeURIComponent(cloudantAccount.accountName) + ':' + encodeURIComponent(cloudantAccount.password)
                + '@' + encodeURIComponent(cloudantAccount.accountName) + '.cloudant.com';
            component.wget(baseUrl + '/' + resource, 'json', successCallback, undefined, loadingImgOn);
        }
    }

    function getFirstDocInDB(callback) {
        getCloudantRes(dbnameSelect.val() + '/_all_docs?limit=1', function(jsonStructure) {
            if (jsonStructure && jsonStructure.rows && jsonStructure.rows.length === 1) {
                var docId = jsonStructure.rows[0].id;
                getCloudantRes(dbnameSelect.val() + '/' + docId, callback, $('#cloudant_message_structure_div'));
            }
        }, $('#cloudant_message_structure_div'));
    }

    function configureCloudantURL() {
        cloudantAccount = component.accountSelected;
        var cloudantAccountURL;
        if (cloudantAccount !== undefined && cloudantAccount.accountName !== undefined) {
            cloudantAccountURL = 'https://' + encodeURIComponent(cloudantAccount.accountName) + '.cloudant.com/';
        } else {
            cloudantAccountURL = '';
        }
        component.config('cloudant_server_url', cloudantAccountURL);
    }

    function configureDBList() {
        dbnameSelect.empty();
        if (cloudantAccount !== undefined && cloudantAccount.accountName !== undefined) {
            getCloudantRes('_all_dbs', function(allDBs) {
                if (allDBs !== undefined) {
                    dbnameSelect.append('<option>');
                    $.each(allDBs, function(index, dbname) {
                        dbnameSelect.append($('<option>', {'value': dbname}).text(dbname));
                    });
                }
                dbnameSelect.val(component.config(dbnameSelect.attr("id")));
            }, dbnameSelect);
        }
    }

    function storeDocStructure(jsonDoc) {
        try {
            var jsonObj = JSON.parse(jsonDoc);
            if (jsonObj !== undefined) {
                component.consumes(jsonObj);
            }
            return true;
        } catch (e) {
            component.consumes({});
            return false;
        }
    }

    // Listen for a cloudant user account change...
    component.onAccountChanged(function() {
        configureCloudantURL();
        configureDBList();
    });

    // Listen for a cloudant db change...
    dbnameSelect.change(function() {
        messageStructureTextArea.val('');
    });

    // Listen for sample message structure change...
    messageStructureTextArea.change(function() {
        var jsonDoc = $(this).val();
        if(!storeDocStructure(jsonDoc)) {
            $(this).addClass('invalidInput');
        }
    });

    if (messageStructureTextArea.val() !== undefined && messageStructureTextArea !== '') {
        messageStructureRow.show();
    }

    var use_previous_produces = $('#use_previous_produces');
    use_previous_produces.click(function() {
        component.consumes(component.previousProduces());
        component.mapSameNameFields();
        messageStructureTextArea.val(JSON.stringify(component.toSampleMessage(component.previousProduces()), undefined, 2));
    });
    use_previous_produces.tooltip();

    var use_cloudant_existing = $('#use_cloudant_existing');
    use_cloudant_existing.click(function() {
        getFirstDocInDB(function(jsonDoc) {
            if (jsonDoc !== undefined) {
                delete jsonDoc._id;
                delete jsonDoc._rev;

                jsonDoc = $.foxweave.mapFieldNamesToValues(jsonDoc);
                var stringifiedJson = JSON.stringify(jsonDoc, undefined, 2);

                messageStructureTextArea.val(stringifiedJson);
                messageStructureTextArea.removeClass('invalidInput');
                messageStructureRow.show();
                component.consumes(jsonDoc);
            }
        });
    });
    use_cloudant_existing.tooltip();
    
    configureCloudantURL();
    configureDBList();
});