package com.webank.wecross.stub.web3.custom;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.web3.Web3Connection;
import com.webank.wecross.stub.web3.common.Web3Constant;
import com.webank.wecross.stub.web3.common.Web3StatusCode;
import com.webank.wecross.stub.web3.config.Web3StubConfig;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class RegisterResourceHandler implements CommandHandler {
  private static final Logger logger = LoggerFactory.getLogger(RegisterResourceHandler.class);

  private static final TomlWriter tomlWriter =
      new TomlWriter.Builder().indentValuesBy(4).padArrayDelimitersBy(1).build();

  @Override
  public synchronized void handle(
      Path path,
      Object[] args,
      Account account,
      BlockManager blockManager,
      Connection connection,
      Driver.CustomCommandCallback callback) {
    try {
      if (!(connection instanceof Web3Connection)) {
        callback.onResponse(
            new TransactionException(
                Web3StatusCode.RegisterContractFailed, "connection not instanceof web3"),
            null);
        return;
      }
      if (Objects.isNull(args) || args.length < 3) {
        callback.onResponse(
            new TransactionException(Web3StatusCode.RegisterContractFailed, "incomplete args"),
            null);
        return;
      }
      Web3Connection web3Connection = (Web3Connection) connection;
      String contractName = (String) args[0];
      String contractAddress = (String) args[1];
      String contractABI = (String) args[2];
      if (StringUtils.isAnyBlank(contractName, contractAddress, contractABI)) {
        callback.onResponse(
            new TransactionException(Web3StatusCode.RegisterContractFailed, "hava blank args"),
            null);
        return;
      }

      String stubConfigFilePath = web3Connection.getStubConfigFilePath();
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource stubConfigResource = resolver.getResource(stubConfigFilePath);
      Web3StubConfig web3StubConfig =
          new Toml().read(stubConfigResource.getInputStream()).to(Web3StubConfig.class);

      List<Web3StubConfig.Resource> resources = web3StubConfig.getResources();

      boolean existResource = false;
      for (Web3StubConfig.Resource resource : resources) {
        if (Objects.equals(contractName, resource.getName())) {
          existResource = true;
          resource.setAbi(contractABI);
          resource.setAddress(contractAddress);
        }
      }
      if (!existResource) {
        Web3StubConfig.Resource resource = new Web3StubConfig.Resource();
        resource.setName(contractName);
        resource.setType(Web3Constant.WEB3_CONTRACT_TYPE);
        resource.setAddress(contractAddress);
        resource.setAbi(contractABI);
        resources.add(resource);
      }
      // update config file
      tomlWriter.write(web3StubConfig, stubConfigResource.getFile());

      // update local and remote
      web3Connection.refreshStubConfig(web3StubConfig);

      callback.onResponse(null, Web3StatusCode.getStatusMessage(Web3StatusCode.Success));
    } catch (Exception e) {
      logger.error(" register contract error", e);
      callback.onResponse(
          new TransactionException(
              Web3StatusCode.RegisterContractFailed, "register contract error"),
          null);
    }
  }
}
