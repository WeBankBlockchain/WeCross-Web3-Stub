package com.webank.wecross.stub.web3.uaproof;

import static junit.framework.TestCase.assertTrue;

import org.junit.Test;
import org.web3j.crypto.Credentials;

public class SignerTest {

  @Test
  public void signerTest() {
    Credentials credentials = Credentials.create("0xafF0CA253b97e54440965855cec0A8a2E2399896");
    String message =
        "{\"proof\":null,\"uaPub\":\"5d614e4cc8dcb73b4326a933a8236bafc36c3219f1f22cac257b755e1d7ecff2ab5f2842b434cacee34d0a79a701b5f4c5c850c43e8f5c5e06af8dcdcfc53c8f\",\"caPub\":\"5d614e4cc8dcb73b4326a933a8236bafc36c3219f1f22cac257b755e1d7ecff2ab5f2842b434cacee34d0a79a701b5f4c5c850c43e8f5c5e06af8dcdcfc53c8f\",\"uaSig\":\"f4df220abbf913abd6228dd31888c1a14a716a64d42186d975f6ae23f23ce279526675411e8a1cb257f358eabf4ffc99e6dfc78c36f9feceaae3de5946e24b5e01\",\"caSig\":\"f4df220abbf913abd6228dd31888c1a14a716a64d42186d975f6ae23f23ce279526675411e8a1cb257f358eabf4ffc99e6dfc78c36f9feceaae3de5946e24b5e01\",\"timestamp\":1600866010281}";
    byte[] signData = Signer.sign(credentials.getEcKeyPair(), message.getBytes());
    boolean verify = Signer.verify(signData, message.getBytes(), credentials.getAddress());
    assertTrue(verify);
  }
}
