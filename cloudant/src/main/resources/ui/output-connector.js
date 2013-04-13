$.foxweave.addComponentView(function() {
    var component = this;
    var dbname = $("#cloudant_database_name");
    var messageStructureRow = $("#cloudant_message_structure_row");
    var messageStructureTextArea = $("#cloudant_message_structure");

    component.onAccountChanged(function() {
        if (component.accountSelected !== undefined && component.accountSelected.accountName !== undefined) {
            component.config('cloudant_server_url', 'https://' + component.accountSelected.accountName + '.cloudant.com/');
        } else {
            component.config('cloudant_server_url', '');
        }
    });

    dbname.change(function() {
        messageStructureTextArea.val('');
        getDocumentStructure();
    });

    messageStructureTextArea.change(function() {
        var jsonDoc = $(this).val();
        if(!storeDocStructure(jsonDoc)) {
            $(this).addClass('invalidInput');
        }
    });

    function getDocumentStructure() {
        var updated = false;

        if (component.accountSelected !== '' && dbname.val() !== '') {
            if (messageStructureTextArea.val() === '') {
                function getCloudant(resource) {
                    var url =
                        '/commons/php/httpget.php?url=' + encodeURIComponent('https://' + encodeURIComponent(component.accountSelected.accountName) +
                        ':' + encodeURIComponent(component.accountSelected.password) + '@' + encodeURIComponent(component.accountSelected.accountName) +
                        '.cloudant.com/' + dbname.val() + '/' + resource);

                    return $.foxweave.getSync(url, 'json', false);
                }

                var jsonStructure = getCloudant('_all_docs?limit=1');
                if (jsonStructure && jsonStructure.rows && jsonStructure.rows.length === 1) {
                    var docId = jsonStructure.rows[0].id;
                    var jsonDoc = getCloudant(docId);

                    if (jsonDoc !== undefined) {
                        delete jsonDoc._id;
                        delete jsonDoc._rev;

                        jsonDoc = $.foxweave.mapFieldNamesToValues(jsonDoc);
                        var stringifiedJson = JSON.stringify(jsonDoc, undefined, 2);

                        messageStructureTextArea.val(stringifiedJson);
                        messageStructureTextArea.removeClass('invalidInput');
                        messageStructureRow.show();
                        component.consumes(jsonDoc);
                        updated = true;
                    }
                }
            }
        }

        if (!updated) {
            component.consumes({});
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

    if (messageStructureTextArea.val() !== undefined && messageStructureTextArea !== '') {
        messageStructureRow.show();
    }

    var use_previous_produces = $('#use_previous_produces');
    use_previous_produces.click(function() {
        var previousProduces = component.previousProduces();
        previousProduces = $.foxweave.flattenMessageModel(previousProduces);
        previousProduces = $.foxweave.toSampleMessage(previousProduces);
        $.foxweave.mapSameNameFields(previousProduces, previousProduces, component.configObj);
        $('#cloudant_message_structure').val(JSON.stringify(previousProduces, undefined, 2));
    });
    use_previous_produces.tooltip();
});