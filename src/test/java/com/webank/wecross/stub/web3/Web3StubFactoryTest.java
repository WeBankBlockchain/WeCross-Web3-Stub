package com.webank.wecross.stub.web3;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.web3.account.Web3Account;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.Test;

public class Web3StubFactoryTest {

  private final Web3StubFactory web3StubFactory = new Web3StubFactory();

  @Test
  public void newConnectionTest() {
    Connection connection = web3StubFactory.newConnection("./");
    assertTrue(Objects.isNull(connection));
  }

  @Test
  public void newDriverTest() {
    Driver driver = web3StubFactory.newDriver();
    assertTrue(Objects.nonNull(driver));
    assertTrue(driver instanceof Web3Driver);
  }

  @Test
  public void newAccountTest() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("username", "wallet");
    properties.put("keyID", 1);
    properties.put("type", "WEB3");
    properties.put("isDefault", false);
    properties.put("secKey", "cd9d9b47e26ec14e4b86048d24db6a710770967d6f37bead3957ce2ebd8ba028");
    properties.put(
        "pubKey",
        "ab1c9d486b269adbb604766daf7d209440663299dd78b229ffcf728568b6b8a93c8a096ca4328d93040a0c8b4bd448e0e2da2566bf252845426f806a053d2cc6");
    properties.put("ext0", "0x698d83382f9ffb72271cd0826479e8ab02077842");

    Account account = web3StubFactory.newAccount(properties);
    assertTrue(account instanceof Web3Account);

    assertEquals(account.getName(), "wallet");
    assertEquals(account.getType(), "WEB3");
    assertEquals(account.getKeyID(), 1);
    assertEquals(account.getIdentity(), "0x698d83382f9ffb72271cd0826479e8ab02077842");
    assertEquals(
        ((Web3Account) account).getPublicKey(),
        "ab1c9d486b269adbb604766daf7d209440663299dd78b229ffcf728568b6b8a93c8a096ca4328d93040a0c8b4bd448e0e2da2566bf252845426f806a053d2cc6");
    assertFalse(account.isDefault());
  }
}
