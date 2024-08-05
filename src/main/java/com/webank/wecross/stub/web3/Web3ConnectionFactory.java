package com.webank.wecross.stub.web3;

import com.webank.wecross.stub.web3.client.ClientWrapper;
import com.webank.wecross.stub.web3.client.ClientWrapperFactory;
import com.webank.wecross.stub.web3.common.Web3Constant;
import com.webank.wecross.stub.web3.config.Web3StubConfig;
import com.webank.wecross.stub.web3.config.Web3StubConfigParser;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
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
    ClientWrapper clientWrapper = ClientWrapperFactory.createClientWrapperInstance(web3StubConfig);
    return build(web3StubConfig, clientWrapper);
  }

  public static Web3Connection build(Web3StubConfig web3StubConfig, ClientWrapper clientWrapper)
      throws IOException {
    logger.info("web3StubConfig: {}", web3StubConfig);
    BigInteger chainId = clientWrapper.ethChainId();

    Web3Connection connection = new Web3Connection(clientWrapper);
    connection.setResourceInfoList(web3StubConfig.convertToResourceInfos());
    connection.addProperty(Web3Constant.WEB3_PROPERTY_CHAIN_ID, chainId.toString());
    connection.addProperty(
        Web3Constant.WEB3_PROPERTY_STUB_TYPE, web3StubConfig.getCommon().getType());
    connection.addProperty(
        Web3Constant.WEB3_PROPERTY_CHAIN_URL, web3StubConfig.getService().getUrl());
    // from config build resources
    List<Web3StubConfig.Resource> resources = web3StubConfig.getResources();
    if (!resources.isEmpty()) {
      // addProperty
      for (Web3StubConfig.Resource resource : resources) {
        String name = resource.getName();
        // name->address
        connection.addProperty(name, resource.getAddress());
        // name+ABI->abi
        connection.addAbi(name, resource.getAbi());
      }
    }
    return connection;
  }
}
