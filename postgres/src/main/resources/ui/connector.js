$.foxweave.addComponentView(function() {
    var component = this;

    component.onAccountAdded(function(e) {
        if (e.accountInfo !== undefined) {

            if (e.accountInfo.ssltoggle && e.accountInfo.ssltoggle === 'true') {
                e.accountInfo.ssl = true;
                e.accountInfo.sslfactory = "org.postgresql.ssl.NonValidatingFactory";
            } else {
                delete e.accountInfo.ssl;
                delete e.accountInfo.sslfactory;
            }
        }
    });

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