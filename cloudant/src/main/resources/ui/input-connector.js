$.foxweave.addComponentView(function(config, viewConfig) {
    var datastoreProxy = viewConfig.datastoreProxy;
    var dbname = $("input#cloudant_database_name");
    var messageStructureRow = $("tr#cloudant_message_structure_row");
    var messageStructureTextArea = $("textarea#cloudant_message_structure");

    $('#authAccountSelector').change(function() {
        if ($(this).val() !== '') {
            config['cloudant_server_url'] = 'https://' + viewConfig.accountSelected.accountName + '.cloudant.com/';
        } else {
            config['cloudant_server_url'] = '';
        }
    });

    $("input#cloudant_database_name").change(function() {
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

        if (viewConfig.accountSelected !== '' && dbname.val() !== '') {
            if (messageStructureTextArea.val() === '') {
                function getCloudant(resource) {
                    var url =
                        '/commons/php/httpget.php?url=' + encodeURIComponent('https://' + encodeURIComponent(viewConfig.accountSelected.accountName) +
                        ':' + encodeURIComponent(viewConfig.accountSelected.password) + '@' + encodeURIComponent(viewConfig.accountSelected.accountName) +
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
                        datastoreProxy.consumes(jsonDoc);
                        updated = true;
                    }
                }
            }
        }

        if (!updated) {
            datastoreProxy.consumes({});
        }
    }

    function storeDocStructure(jsonDoc) {
        try {
            var jsonObj = JSON.parse(jsonDoc);
            if (jsonObj !== undefined) {
                datastoreProxy.consumes(jsonObj);
            }
            return true;
        } catch (e) {
            datastoreProxy.consumes({});
            return false;
        }
    }

    if (messageStructureTextArea.val() !== undefined && messageStructureTextArea !== '') {
        messageStructureRow.show();
    }

    var use_previous_produces = $('#use_previous_produces');
    use_previous_produces.click(function() {
        var previousProduces = datastoreProxy.previousProduces();
        previousProduces = $.foxweave.flattenMessageModel(previousProduces);
        previousProduces = $.foxweave.toSampleMessage(previousProduces);
        $.foxweave.mapSameNameFields(previousProduces, previousProduces, config);
        $('#cloudant_message_structure').val(JSON.stringify(previousProduces, undefined, 2));
    });
    use_previous_produces.tooltip();
});