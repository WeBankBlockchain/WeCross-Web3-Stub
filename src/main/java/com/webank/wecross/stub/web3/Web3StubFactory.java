package com.webank.wecross.stub.web3;

import static com.webank.wecross.stub.web3.common.Web3Constant.WEB3_STUB_TYPE;

import com.webank.wecross.stub.Stub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stub(WEB3_STUB_TYPE)
public class Web3StubFactory extends Web3BaseStubFactory {
  private static final Logger logger = LoggerFactory.getLogger(Web3StubFactory.class);

  public static void main(String[] args) {
    System.out.printf(
        "This is %s Stub Plugin. Please copy this file to router/plugin/%n", WEB3_STUB_TYPE);
  }
}
