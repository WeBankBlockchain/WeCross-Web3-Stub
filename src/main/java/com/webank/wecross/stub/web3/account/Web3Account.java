package com.webank.wecross.stub.web3.account;

import com.webank.wecross.stub.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Numeric;

public class Web3Account implements Account {
  private static final Logger logger = LoggerFactory.getLogger(Web3Account.class);

  private final String name;
  private final String type;
  private final String identity;
  private int keyID;
  private boolean isDefault;

  private final String publicKey;
  private final Credentials credentials;

  public Web3Account(String name, String type, Credentials credentials) {
    this.name = name;
    this.type = type;
    this.identity = credentials.getAddress();
    this.credentials = credentials;
    this.publicKey = Numeric.toHexStringNoPrefix(credentials.getEcKeyPair().getPublicKey());
    logger.info(" name: {}, type: {}, publicKey: {}", name, type, publicKey);
  }

  public Credentials getCredentials() {
    return credentials;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setKeyID(int keyID) {
    this.keyID = keyID;
  }

  public void setDefault(boolean aDefault) {
    isDefault = aDefault;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getIdentity() {
    return identity;
  }

  @Override
  public int getKeyID() {
    return keyID;
  }

  @Override
  public boolean isDefault() {
    return isDefault;
  }
}
