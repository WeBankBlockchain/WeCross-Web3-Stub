package com.webank.wecross.stub.web3.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.stub.web3.common.Web3Toml;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web3StubConfigParser extends AbstractWeb3ConfigParser {

  private static final Logger logger = LoggerFactory.getLogger(Web3StubConfigParser.class);

  public Web3StubConfigParser(String configPath, String configName) {
    super(configPath + File.separator + configName);
  }

  public Web3StubConfig loadConfig() throws IOException {
    Web3Toml web3Toml = new Web3Toml(getConfigPath());
    Toml toml = web3Toml.getToml();
    return toml.to(Web3StubConfig.class);
  }
}
