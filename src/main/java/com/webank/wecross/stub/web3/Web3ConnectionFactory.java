package com.webank.wecross.stub.web3;

import com.webank.wecross.stub.web3.client.ClientWrapper;
import com.webank.wecross.stub.web3.client.ClientWrapperFactory;
import com.webank.wecross.stub.web3.config.Web3StubConfig;
import com.webank.wecross.stub.web3.config.Web3StubConfigParser;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web3ConnectionFactory {
  private static final Logger logger = LoggerFactory.getLogger(Web3ConnectionFactory.class);

  public static Web3Connection build(String stubConfigPath, String configName) throws Exception {
    Web3StubConfigParser web3StubConfigParser =
        new Web3StubConfigParser(stubConfigPath, configName);
    Web3StubConfig web3StubConfig = web3StubConfigParser.loadConfig();
    return build(web3StubConfig);
  }

  public static Web3Connection build(Web3StubConfig web3StubConfig) throws IOException {
    logger.info("web3StubConfig: {}", web3StubConfig);
    ClientWrapper clientWrapper = ClientWrapperFactory.createClientWrapperInstance(web3StubConfig);
    return new Web3Connection(clientWrapper, web3StubConfig);
  }
}
