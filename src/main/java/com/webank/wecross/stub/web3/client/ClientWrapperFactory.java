package com.webank.wecross.stub.web3.client;

import com.webank.wecross.stub.web3.config.Web3StubConfig;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

public class ClientWrapperFactory {

  public static ClientWrapper createClientWrapperInstance(Web3StubConfig web3StubConfig) {
    String url = web3StubConfig.getService().getUrl();
    Web3j web3j = Web3j.build(new HttpService(url));
    return new ClientWrapperImpl(web3j);
  }
}
