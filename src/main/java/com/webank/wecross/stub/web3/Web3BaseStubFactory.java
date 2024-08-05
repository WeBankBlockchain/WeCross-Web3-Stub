package com.webank.wecross.stub.web3;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;
import com.webank.wecross.stub.web3.account.Web3AccountFactory;
import com.webank.wecross.stub.web3.common.Web3Constant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web3BaseStubFactory implements StubFactory {
  private static final Logger logger = LoggerFactory.getLogger(Web3BaseStubFactory.class);

  @Override
  public void init(WeCrossContext weCrossContext) {}

  @Override
  public Driver newDriver() {
    return null;
  }

  @Override
  public Connection newConnection(String path) {
    try {
      logger.info("New connection: {}", path);
      Web3Connection connection = Web3ConnectionFactory.build(path, Web3Constant.STUB_TOML_NAME);

      // check proxy contract
      if (!connection.hasProxyDeployed()) {
        String errorMsg = "WeCrossProxy error: WeCrossProxy contract has not been deployed!";
        System.out.println(errorMsg);
        throw new Exception(errorMsg);
      }

      // check hub contract
      if (!connection.hasHubDeployed()) {
        String errorMsg = "WeCrossHub error: WeCrossHub contract has not been deployed!";
        System.out.println(errorMsg);
        throw new Exception(errorMsg);
      }
      return connection;
    } catch (Exception e) {
      logger.error("New connection fail, e: ", e);
      return null;
    }
  }

  @Override
  public Account newAccount(Map<String, Object> properties) {
    return Web3AccountFactory.build(properties);
  }

  @Override
  public void generateAccount(String path, String[] args) {}

  @Override
  public void generateConnection(String path, String[] args) {}
}
