$.foxweave.addComponentView(function() {
        var component = this;

        component.onAccountAdded(function(e) {
            // The mailchimp data center id is tagged onto the end of the
            // apiKey.  Parse it off the key and add it to the auth info,
            // from where it can be referenced in url construction etc...

            if (e.accountInfo !== undefined) {
                var apiKey = e.accountInfo.apiKey;
                var dcTokenOffset = apiKey.lastIndexOf('-');

                e.accountInfo.dataCenter = undefined;
                if (dcTokenOffset !== -1) {
                    var dataCenter = apiKey.slice(dcTokenOffset + 1);
                    e.accountInfo.dataCenter = dataCenter;
                }
            }
        });
    }
);
