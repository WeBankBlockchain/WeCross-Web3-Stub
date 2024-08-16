package com.webank.wecross.stub.web3.account;

import com.webank.wecross.stub.web3.common.Web3Constant;
import com.webank.wecross.stub.web3.config.Web3AccountConfig;
import com.webank.wecross.stub.web3.config.Web3AccountConfigParser;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

public class Web3AccountFactory {
  private static final Logger logger = LoggerFactory.getLogger(Web3AccountFactory.class);

  public static Web3Account build(Map<String, Object> properties) {
    String username = (String) properties.get("username");
    Integer keyID = (Integer) properties.get("keyID");
    String type = (String) properties.get("type");
    Boolean isDefault = (Boolean) properties.get("isDefault");
    String secKey = (String) properties.get("secKey");
    String pubKey = (String) properties.get("pubKey");
    String address = (String) properties.get("ext0");

    if (StringUtils.isBlank(username)) {
      logger.error("username has not given");
      return null;
    }

    if (keyID == null) {
      logger.error("keyID has not given");
      return null;
    }

    if (StringUtils.isBlank(type)) {
      logger.error("type has not given");
      return null;
    }

    if (isDefault == null) {
      logger.error("isDefault has not given");
      return null;
    }

    if (StringUtils.isBlank(secKey)) {
      logger.error("secKey has not given");
      return null;
    }

    if (StringUtils.isBlank(pubKey)) {
      logger.error("pubKey has not given");
      return null;
    }

    if (StringUtils.isBlank(address)) {
      logger.error("address has not given in ext0");
      return null;
    }

    try {
      // build credentials from privateKey
      Credentials credentials = Credentials.create(secKey);
      Web3Account web3Account = new Web3Account(username, type, credentials);
      web3Account.setDefault(isDefault);
      web3Account.setKeyID(keyID);

      // check publicKey
      if (!Objects.equals(web3Account.getPublicKey(), pubKey)) {
        throw new Exception("Given pubKey is not belongs to the secKey of " + username);
      }

      // check address
      if (!Objects.equals(web3Account.getIdentity(), address)) {
        throw new Exception("Given address is not belongs to the secKey of " + username);
      }
      return web3Account;
    } catch (Exception e) {
      logger.error("build account exception: {}", e.getMessage());
      return null;
    }
  }

  public static Web3Account build(String name, String accountPath)
      throws IOException, CipherException {
    Web3AccountConfigParser parser =
        new Web3AccountConfigParser(accountPath, Web3Constant.ACCOUNT_TOML_NAME);
    Web3AccountConfig web3AccountConfig = parser.loadConfig();
    Web3AccountConfig.Account account = web3AccountConfig.getAccount();
    String type = account.getType();
    String accountFile = account.getAccountFile();
    String password = account.getPassword();

    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource accountFileResource = resolver.getResource(accountPath + File.separator + accountFile);

    Credentials credentials;

    if (StringUtils.endsWith(accountFile, ".json")) {
      credentials = WalletUtils.loadCredentials(password, accountFileResource.getFile());
    } else {
      return null;
    }
    return new Web3Account(name, type, credentials);
  }
}
