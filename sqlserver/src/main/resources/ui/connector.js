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
            var rdb_url = "jdbc:jtds:sqlserver://" + component.accountSelected.host + ":" + component.accountSelected.port + "/" + db;

            if (component.accountSelected.domain && component.accountSelected.domain !== '') {
                rdb_url += ";domain="+ component.accountSelected.domain;
            }

            component.config('rdb_url', rdb_url);
        }
    }
});