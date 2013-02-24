$.foxweave.addComponentView(function() {
    var component = this;

    component.onAccountChanged(function() {
        setRdbUrl();
    });
    $('#rdb_db_name').change(function() {
        setRdbUrl();
    });

    function setRdbUrl() {
        component.config('rdb_url', '');
        if (component.accountSelected) {
            var db = $('#rdb_db_name').val();
            component.config('rdb_url', "jdbc:postgresql://" + component.accountSelected.host + ":" + component.accountSelected.port + "/" + db);
        }
    };
});