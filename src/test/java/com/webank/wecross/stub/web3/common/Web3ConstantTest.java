package com.webank.wecross.stub.web3.common;

import static junit.framework.TestCase.assertEquals;

import com.webank.wecross.stub.StubConstant;
import org.junit.Test;

public class Web3ConstantTest {

  @Test
  public void constantValueTest() {
    assertEquals(Web3Constant.STUB_TOML_NAME, "stub.toml");
    assertEquals(Web3Constant.ACCOUNT_TOML_NAME, "account.toml");
    assertEquals(Web3Constant.WEB3_STUB_TYPE, "WEB3");
    assertEquals(Web3Constant.WEB3_CONTRACT_TYPE, "WEB3_CONTRACT");
    assertEquals(Web3Constant.WEB3_PROXY_NAME, StubConstant.PROXY_NAME);
    assertEquals(Web3Constant.WEB3_HUB_NAME, StubConstant.HUB_NAME);
    assertEquals(Web3Constant.WEB3_PROPERTY_ABI_SUFFIX, "ABI");
    assertEquals(Web3Constant.WEB3_PROPERTY_CHAIN_ID, "WEB3_PROPERTY_CHAIN_ID");
    assertEquals(Web3Constant.WEB3_PROPERTY_STUB_TYPE, "WEB3_PROPERTY_STUB_TYPE");
    assertEquals(Web3Constant.WEB3_PROPERTY_CHAIN_URL, "WEB3_PROPERTY_CHAIN_URL");
    assertEquals(Web3Constant.CUSTOM_COMMAND_REGISTER, "register");
  }

  @Test
  public void requestTypeTest() {
    assertEquals(Web3RequestType.CALL, 1000);
    assertEquals(Web3RequestType.SEND_TRANSACTION, 1001);
    assertEquals(Web3RequestType.GET_BLOCK_NUMBER, 1002);
    assertEquals(Web3RequestType.GET_BLOCK_BY_NUMBER, 1003);
    assertEquals(Web3RequestType.GET_TRANSACTION, 1004);
  }
}
