package com.webank.wecross.stub.web3;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.web3.account.Web3Account;
import com.webank.wecross.stub.web3.account.Web3AccountFactory;
import java.io.IOException;
import java.util.Objects;
import org.junit.Test;
import org.web3j.crypto.CipherException;

public class Web3AccountFactoryTest {

  @Test
  public void buildAccountTest() throws CipherException, IOException {
    Web3Account web3Account = Web3AccountFactory.build("wallet", "./account");

    assertTrue(Objects.nonNull(web3Account));
    assertEquals(web3Account.getName(), "wallet");
    assertEquals(web3Account.getType(), "WEB3");
    assertEquals(web3Account.getIdentity(), "0x698d83382f9ffb72271cd0826479e8ab02077842");
    assertEquals(
        web3Account.getPublicKey(),
        "ab1c9d486b269adbb604766daf7d209440663299dd78b229ffcf728568b6b8a93c8a096ca4328d93040a0c8b4bd448e0e2da2566bf252845426f806a053d2cc6");
  }
}
