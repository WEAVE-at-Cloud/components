$.foxweave.addComponentView(function(config, viewConfig) {

    // Don't specify a DB name for Oracle...
    $('#rdb_db_name_row').remove();

    if (viewConfig.componentDescriptor.type === 'InputConnector') {
        // Uppercase the SQL statement for Oracle when it's an input connector.
        // Only do this for the input because it produces records for the pipeline
        // and so we need the "produces" variables (that the pipeline components after the
        // input connector sees) to match up with what actually goes into the pipeline,
        // at runtime, from the input connector...
        $('#rdb_sql_statement').addClass('toUppercase');
    }

    $('#authAccountSelector').change(function() {
        if ($(this).val() !== '') {
            configureDBConnection();
        }
    });

    function configureDBConnection() {
        config['rdb_url'] = '';

        if (viewConfig.accountSelected) {
            config['rdb_url'] = "jdbc:oracle:thin:@//" + viewConfig.accountSelected.host + ":" + viewConfig.accountSelected.port + "/" + viewConfig.accountSelected.service;
            config['scheduler_delay'] = '60000';
        }
    };
});