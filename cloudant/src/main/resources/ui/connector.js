$.foxweave.addComponentView(function() {
    var component = this;
    var cloudantAccount = component.accountSelected;
    var dbnameSelect = $("#cloudant_database_name");
    var messageStructureRow = $("#cloudant_message_structure_row");
    var produces_consumes_div = $("#cloudant_message_structure_div div.uiComp");

    function getCloudantRes(resource, successCallback, loadingImgOn) {
        if (cloudantAccount !== undefined && cloudantAccount.accountName !== undefined) {
            var baseUrl = 'https://' + encodeURIComponent(cloudantAccount.accountName) + ':' + encodeURIComponent(cloudantAccount.password)
                + '@' + encodeURIComponent(cloudantAccount.accountName) + '.cloudant.com';
            component.wget(baseUrl + '/' + resource, 'json', successCallback, undefined, loadingImgOn);
        }
    }

    function getMessageStructureTextArea() {
        return $('#cloudant_message_structure');
    }

    function getFirstDocInDB(callback) {
        getCloudantRes(dbnameSelect.val() + '/_all_docs?limit=1', function(jsonStructure) {
            if (jsonStructure && jsonStructure.rows && jsonStructure.rows.length === 1) {
                var docId = jsonStructure.rows[0].id;
                getCloudantRes(dbnameSelect.val() + '/' + docId, callback, getMessageStructureTextArea());
            }
        }, getMessageStructureTextArea());
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

    function showHideSourceButtons() {
        if ($('textarea', produces_consumes_div).size() === 0) {
            // We're not editing a sample.  Hide the buttons...
            $('.sampleSourceBtns').hide();
        } else {
            // We're editing a sample.  Show the buttons...
            $('.sampleSourceBtns').show();
        }
    }

    // Listen for a cloudant user account change...
    component.onAccountChanged(function() {
        configureCloudantURL();
        configureDBList();
    });

    // Listen for a cloudant db change...
    dbnameSelect.change(function() {
        getMessageStructureTextArea().val('');
    });

    if (getMessageStructureTextArea().val() !== undefined && getMessageStructureTextArea() !== '') {
        messageStructureRow.show();
    }

    produces_consumes_div.onDataModelChange(function(e) {
        showHideSourceButtons();
    });

    var use_previous_produces = $('#use_previous_produces');
    use_previous_produces.click(function() {
        var previousProduces = component.previousProduces();

        component.mapSameNameFields();
        getMessageStructureTextArea().val(JSON.stringify(component.toSampleMessage(previousProduces), undefined, 4));
    });
    use_previous_produces.tooltip();

    var use_cloudant_existing = $('#use_cloudant_existing');
    use_cloudant_existing.click(function() {
        getFirstDocInDB(function(jsonDoc) {
            if (jsonDoc !== undefined) {
                delete jsonDoc._id;
                delete jsonDoc._rev;

                var stringifiedJson = JSON.stringify(jsonDoc, undefined, 2);

                getMessageStructureTextArea().val(stringifiedJson);
                getMessageStructureTextArea().removeClass('invalidInput');
                messageStructureRow.show();
            }
        });
    });
    use_cloudant_existing.tooltip();

    showHideSourceButtons();
    configureCloudantURL();
    configureDBList();
});