package com.webank.wecross.stub.web3.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web3AccountConfig {
  private static final Logger logger = LoggerFactory.getLogger(Web3AccountConfig.class);

  private Account account;

  private String accountConfigPath;

  public static class Account {
    private String type;
    private String accountFile;
    private String password;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getAccountFile() {
      return accountFile;
    }

    public void setAccountFile(String accountFile) {
      this.accountFile = accountFile;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    @Override
    public String toString() {
      return "Account{"
          + "type='"
          + type
          + '\''
          + ", accountFile='"
          + accountFile
          + '\''
          + ", password='"
          + password
          + '\''
          + '}';
    }
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }

  public String getAccountConfigPath() {
    return accountConfigPath;
  }

  public void setAccountConfigPath(String accountConfigPath) {
    this.accountConfigPath = accountConfigPath;
  }

  @Override
  public String toString() {
    return "Web3AccountConfig{"
        + "account="
        + account
        + ", accountConfigPath='"
        + accountConfigPath
        + '\''
        + '}';
  }
}
