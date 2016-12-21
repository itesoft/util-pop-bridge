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


## Others

Feel free to contribute to this project by adding new drivers


# Itesoft CaptureMail

The POP server exposed by the POP BRIDGE is compatible with Itesoft CaptureMail product. It adds to the product the ability to capture a gmail / gsuite mailbox using GMail APIs.


# License

Copyright (c) 2016-2017, ITESOFT S.A. 

This project is licensed under the terms of the MIT License (see LICENSE file)

Ce projet est licencié sous les termes de la licence MIT (voir le fichier LICENSE)