package com.webank.wecross.stub.web3.common;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;
import java.util.Objects;
import org.junit.Test;

public class Web3TomlTest {

  @Test
  public void loadTomlTest() throws IOException {
    String filePath = "./stub-sample.toml";
    Web3Toml web3Toml = new Web3Toml(filePath);
    assertEquals(web3Toml.getPath(), filePath);
    assertTrue(Objects.nonNull(web3Toml.getToml()));
  }
}
