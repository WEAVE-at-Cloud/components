$.foxweave.addComponentView(function() {
    var component = this;

    // Don't specify a DB name for Oracle...
    $('#rdb_db_name_row').remove();

    if (component.type === 'InputConnector') {
        // Uppercase the SQL statement for Oracle when it's an input connector.
        // Only do this for the input because it produces records for the pipeline
        // and so we need the "produces" variables (that the pipeline components after the
        // input connector sees) to match up with what actually goes into the pipeline,
        // at runtime, from the input connector...
        $('#rdb_sql_statement').addClass('toUppercase');
    }

    component.onAccountChanged(function() {
        component.config('rdb_url', '');
        if (component.accountSelected) {
            component.config('rdb_url', "jdbc:oracle:thin:@//" + component.accountSelected.host + ":" + component.accountSelected.port + "/" + component.accountSelected.service);
        }
    });
});