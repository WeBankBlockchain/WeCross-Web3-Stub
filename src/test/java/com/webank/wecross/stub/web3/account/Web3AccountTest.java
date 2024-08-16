package com.webank.wecross.stub.web3.account;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import org.junit.Test;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;

public class Web3AccountTest {
  @Test
  public void accountTest()
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    Credentials credentials = Credentials.create(Keys.createEcKeyPair());
    Web3Account account = new Web3Account("test", "type", credentials);
    assertEquals(account.getName(), "test");
    assertEquals(account.getType(), "type");
    assertFalse(account.getIdentity().isEmpty());
    assertFalse(account.getPublicKey().isEmpty());
  }
}
