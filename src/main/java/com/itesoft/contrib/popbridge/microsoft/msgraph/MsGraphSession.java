package com.itesoft.contrib.popbridge.microsoft.msgraph;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.itesoft.contrib.popbridge.PopException;
import com.itesoft.contrib.popbridge.Session;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.Message;
import com.microsoft.graph.requests.extensions.CustomRequestBuilder;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IMessageCollectionPage;

import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;

public class MsGraphSession implements Session
{

  private static Logger _logger = Logger.getLogger(MsGraphSession.class.getName());

  private IGraphServiceClient _graphServiceClient;

  public MsGraphSession(String accessToken) throws PopException
  {
    IAuthenticationProvider graphAuthenticationManager = new AuthorizationBearerAuthenticationProvider(accessToken);
    
    _graphServiceClient = 
        GraphServiceClient
          .builder()
          .authenticationProvider(graphAuthenticationManager)
          .buildClient();
  }

  @Override
  public List<String> list() throws PopException
  {
    List<String> idList = new ArrayList<String>();
    IMessageCollectionPage messageCollectionPage = 
        _graphServiceClient
        .me()
        .mailFolders(WellKnownFolderName.Inbox.name())
        .messages()
        .buildRequest()
        .get();
    List<Message> messages = messageCollectionPage.getCurrentPage();
    for (Message message : messages)
    {
      idList.add(message.id);
    }
    return idList;
  }
  
  @Override
  public String retrieve(String id) throws PopException
  {
      String url = _graphServiceClient.me().messages(id).getRequestUrlWithAdditionalSegment("$value");
      CustomRequestBuilder<BufferedInputStream> requestBuilder = new CustomRequestBuilder<BufferedInputStream>(url, _graphServiceClient, null, BufferedInputStream.class);
      BufferedInputStream input = requestBuilder.buildRequest().get();
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[10240];
      int readSize = 0;
      while (readSize >= 0)
      {
        try
        {
          readSize = input.read(buffer);
        }
        catch (IOException e)
        {
          throw new PopException(e);
        }
        if (readSize < 0)
        {
          break;
        }
        else
        {
          output.write(buffer, 0 , readSize);
        }
      }
      return output.toString();
  }
  
  @Override
  public void delete(String id) throws PopException
  {
    _graphServiceClient.me().messages(id).buildRequest().delete();
  }
  
  @Override
  public int sizeOf(String id) throws PopException
  {
    return 0;
  }

}
