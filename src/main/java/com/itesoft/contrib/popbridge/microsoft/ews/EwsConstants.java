package com.itesoft.contrib.popbridge.microsoft.ews;

import java.util.Collections;
import java.util.Set;

public interface EwsConstants
{
  String OFFICE365_URL = "https://outlook.office365.com/EWS/Exchange.asmx";
  Set<String> EWS_SCOPE = Collections.singleton("https://outlook.office.com/EWS.AccessAsUser.All");
}
