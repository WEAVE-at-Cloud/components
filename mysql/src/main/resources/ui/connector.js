$.foxweave.addComponentView(function(config, viewConfig) {

    $('#authAccountSelector, #rdb_db_name').change(function() {
        configureDBConnection();
    });

    function configureDBConnection() {
        config['rdb_url'] = '';

        var db = $('#rdb_db_name').val();
        if (viewConfig.accountSelected) {
            config['rdb_url'] = "jdbc:mysql://" + viewConfig.accountSelected.host + ":" + viewConfig.accountSelected.port + "/" + db;
            config['scheduler_delay'] = '60000';
        }
    };

});