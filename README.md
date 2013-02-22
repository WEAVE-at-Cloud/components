# FoxWeave Component Modules
This repository is the home for all **public** FoxWeave Component Modules.

"Component Modules" are how we bundle Connectors and other components that extend the core functionality of
FoxWeave to "connect" to [Cloud APIs/Apps/DBs or On-Premise Apps/DBs][1] for the purpose of
synchronizing or migrating data.  You can use the modules in this repository as references for creating
new modules to connect to other [Cloud APIs/Apps/DBs or On-Premise Apps/DBs][1].  Alternatively, make
enhancements/fixes to existing modules.

If you create/enhance/fix a connector, please let us know by submitting a pull request on the github repository.

## Creating/Updating a Component Modules
The first thing you'll need to do is fork and clone this repository.  If you want to create a new module, the easiest
thing to do is copy one of the existing modules and work from that.  If it is a new module, you need to add
it in the *settings.gradle* file in the root of the repository.

Please check out the [Component Developer Guide][5] for details on developing a Component Module.

## Building and Uploading Component Modules
We assume you've forked and cloned this repository and added/enhanced a module (as outlined in the previous section) and would now like to
upload it to your account on FoxWeave.

### Step 1
We use [Gradle][3] v1.4 as our build tool.  You'll need to download and install Gradle (as well as Java v1.6).

To build/assemble the module, cd into the module directory and execute the following command:

    $ gradle clean distZip

After this command completes, the module will have a distribution *.zip* file located in the
*target/distributions* folder e.g. if your component is called "myfunkyapp", the distribution *.zip* file
would be *target/distributions/myfunkyapp.zip*.

### Step 2
You now need to upload the assembled distribution *.zip* file to your account on FoxWeave.  This is very easy.
You just POST it to “https://dev.foxweave.io/component“, including your apiKey as a query parameter.

The following is an example using curl:

    curl -X POST -T target/distributions/myfunkyapp.zip -H "Content-Type: application/zip" https://dev.foxweave.io/component/myfunkyapp?apiKey=XXXXXX

As you've probably guessed from above, the target URL is formatted as follows:

    https://dev.foxweave.io/component/{module-name}?apiKey={account-api-key}

Your account API key can be found in your profile after logging into your account on FoxWeave.  Just follow "Settings->API" from the top menu bar.

Note that when you upload a module to FoxWeave, it will:

1. Only be visible/usable from your account i.e. other FoxWeave accounts cannot use any of the components in your module.  You will need to [get in contact with us][4] if you'd like to make the module public.
2. Only be runnable on a [PaaS or On-Premise][1].  This is obviously for security reasons.

### Updating Individual Resources
You don't want the trouble of building and uploading a full module distribution *.zip* file every time you make a change while you are developing a module.
To avoid this, you can simply update and then PUT individual resources.  For example, to update and PUT local changes to the *foxweave-components.json*
module descriptor file:

    curl -X PUT -T src/main/resources/foxweave-components.json -H "Content-Type: application/json" https://dev.foxweave.io/component/myfunkyapp/foxweave-components.json?apiKey=XXXXXX

[1]: http://www.foxweave.com/integration-runtime-options/ "Integration Task Runtime Options"
[2]: http://www.foxweave.com/synchronization-vs-migration/ "Synchronization Vs Migration"
[3]: http://www.gradle.org/
[4]: mailto:support@foxweave.com
[5]: http://www.foxweave.com/component-developer-guide/