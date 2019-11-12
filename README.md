POP BRIDGE
==========


POP BRIDGE is a relay exposing a simple POP3 server and translating received commands using another protocol usually to connect to a remote server not offering POP3, or through a network not allowing POP3 protocol.


# Run the bridge

This software runs from the command line like

    java -jar util-pop-bridge.jar -d GMAIL -p 1110 -f markAsRead -o clientSecret=path/to/client_secret.json

Allowed parameters are

- `--driver <driver name>` (or `-d`) to specify the driver to use to connect (see *Available drivers*)

- `--port <port number>` (or `-p`) to specify the port the bridge will listen on for incoming POP3 connections (default 110). Same port must configured into the third-party email client that relies on the POP BRIDGE to fetch its mails. 

- `--flag <flag>[,<flag>,[<flag>]]` (or `-f`) to specify a driver specific flags (see *Available drivers*)

- `--option <key>=<value>` (or `-o`) to specify a driver specific option (see *Available drivers*)


# Available drivers

## GMail

The GMAIL driver connects to one single gmail / gsuite / google-apps mailbox using [Google Mail API](https://developers.google.com/gmail/api/)

### Driver specific driver and flags 

- OPTION **clientSecret** (no defaults, required): path of _client secret_ file (see below).
- OPTION **dataStoreDir** (default to <userhome>/pop-bridge/gmail-driver): path where authentication tokens (OAuth2 tokens) are stored.
- OPTION **user** (defaults to `me`): specify the mailbox (in case the logged user have access to many mailboxes). The special `me` value means using the personal mailbox of the logged user.
- OPTION **query** (defaults to `is:unread label:inbox`): a GMail search query (like the one that can be entered in the search box) 
- FLAG **markAsRead**: mark emails as read as soon as they are fetched by POP3 client
- FLAG **archiveOnDelete**: archive emails rather than trashing it when DELETE command is issued by the POP3 client 

Refer to command line options in *Run the bridge* to see how flags or options can be specified.


### Getting a Client Secret

To be able to use the GMAIL DRIVER, you have to create and get a  _client secret_ from Google API.

Each running instance of POP BRIDGE need its own _client secret_ to identify itself in the [OAuth2](https://developers.google.com/identity/protocols/OAuth2) authentication flow.

Follow following steps to get a _client secret_:
- connect to the [Google API Manager](https://console.developers.google.com)
- go to _Credentials_ section
- create an _OAuth client ID_ of type _Other_
- as a result you should be able to download a JSON file named like `client_secret_xxx-yyy.apps.googleusercontent.com.json`
- save it somewhere accessible to pop-bridge
- supply the full path of the save file using `--option clientSecret=<path>`
- never share/commit this file, it's *your* private key allowing to run the software identified as yourself

It is not required that the Google account used to create the _client secret_ to be the one holding the mailbox you want to read using POP BRIDGE.


### First Run

On the first run, GMAIL Driver will issue an URL on the console that you need to open in a web browser in order to authenticate to Google with the account of the inbox you want to access. 

Note that the account which is provided in this initialization phase will be the only Google account accessed by POP3 clients. The credentials supplied by POP3 clients are ignored (any user and password can be set in the POP3 client configuration).


## MS GRAPH

### Driver specific driver and flags 
- OPTION **authflow** (no default, required): the authentication flow to use (see dedicated chapter below)
- OPTION **clientid** (no default, required): the client-id of your Azure AD registered application (see below)
- OPTION **tenant** (no default, required): either the id of the Azure AD tenant of the associated domain name (the part which is after the '@' in your organization)
- OPTION **url** (required only if Exchange on premise): the EWS url of your on-premise Exchange server, or an e-mail address if you want to use autodiscovery. Do not provide the option if your using Office 365 (Exchange Online).
- OPTION **dataStoreDir** (default to <userhome>/pop-bridge/msgraph-driver): path where authentication tokens (OAuth2 tokens) are stored.

### Register your application

Before being able to use Azure AD authentication (aka Modern Authentication) using one the the authentication flows, you need
to register the POP Bridge application into your Azure AD tenant.

1. Sign into [Azure Portal - App Registrations](https://go.microsoft.com/fwlink/?linkid=2083908) using your organization account.

2. Choose **New registration**.

3. In the **Name** section, enter a meaningful application name that will be displayed when authenticating

4. In the **Supported account types** section, select **Accounts in any organizational directory**  

5. Select **Register** to create the application. 
	
   The application's Overview page shows the properties of your app.

6. Copy the **Application (client) Id**. This is the unique identifier for your app you'll have to provide as the **clientid** option of this driver

7. In the application's list of pages, select **Authentication**.

 1. Under **Redirect URIs** in the **Suggested Redirect URIs for public clients (mobile, desktop)** section, check the box next to **https://login.microsoftonline.com/common/oauth2/nativeclient**

 2. Under **Default client type** select **Yes** for the setting **Treat the application as public client**

8. Choose **Save**.

### Supported Authentication Flows

The option **authflow** of the driver may be one the following ones

- **PASSWORD** this authentication flow uses the login and password supplied by the POP3 client to authenticate to Azure AD and get a token. It's the most transparent for the POP3 client (you continue to set login and password of the mail account you want to use in the mail client) but not the most secure as it implies that the POP3 client is storing the password in a reversible way. This authentication method is definitely incompatible with MFA-enabled accounts.

- **INTERACTIVE** uses the regular OAuth authorization flow, that will run a browser to allow you to authenticated to the account you want to retrieve mail of, and transfer an authorization code to the POP Bridge. In that scenario, the POP Bridge has to be run using a desktop session allowing to run a web brower on the same machine.
 
- **CODE** (recommended) the pop bridge will issue on the first run a code that you will have to enter on [Microsoft Device Login](https://microsoft.com/devicelogin) into a any web browser to authenticate. Once the authentication has succeeded on the browser, the POP Bridge will run authenticated as this user and the credentials provided by the POP3 client will be ignored. This is the privileged scenario if you're running the POP Bridge remotely or on a headless environment as the web browser used to authenticate need not to be running on the same machine. The code to use to authenticate will be given on the console (standard out) or you can additionally provide an **httpport** option to the driver so as to expose it on an HTTP page accessible remotely on the specified port of the machine (usefull if you do not have access to the console attached to the POP Bridge).

- ** IWA** uses Integrated Windows Authentication to authenticate the user, so it will take the user account the POP Bridge has been run with. This means that the POP Bridge has to run directly with the account that has to be used with MS GRAPH. Moreover, as there's no way for the user to grant the application the permissions it requires to read the mails, this will have to be done before by an administrator of the Azure AD tenant. Moreover, this authentication method is definitely incompatible with MFA-enabled accounts.


## EWS

Uses Microsoft EWS (Exchange Webservices) protocol to access e-mails of Microsoft Exchange. Please note that MS GRAPH is recommended rather than EWS if you're using Office 365 with Exchange Online.

### Driver specific driver and flags 
- OPTION **url** (required only if Exchange on premise): the EWS url of your on-premise Exchange server. You may also provide an e-mail address if you want to use autodiscovery protocol to find the right URL automatically but the autodiscovery has to be configured on your domain. Do not provide the option if your using Office 365 (Exchange Online). 
- OPTION **authflow** (optional): Specify the authentication flow to use if you wish to get authenticate thanks to Modern Authentication or Hybrid Moden. If not specified, the EWS driver will use Basic authentication with the username and password provided by the POP3 client. If specified, please refer to MSGRAPH driver above as all options of the MSGRAPH driver as well as the application registration will be required.


## Others

Feel free to contribute to this project by adding new drivers


# Itesoft CaptureMail

The POP server exposed by the POP BRIDGE is compatible with Itesoft CaptureMail product. It adds to the product the ability to capture a gmail / gsuite mailbox using GMail APIs.


# License

Copyright (c) 2016-2019, ITESOFT S.A. 

This project is licensed under the terms of the MIT License (see LICENSE file)

Ce projet est licencié sous les termes de la licence MIT (voir le fichier LICENSE)