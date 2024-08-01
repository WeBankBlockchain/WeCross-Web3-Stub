package com.webank.wecross.stub.web3.common;

import com.webank.wecross.exception.WeCrossException;

public class Web3StubException extends WeCrossException {
  public Web3StubException(Integer errorCode, String message) {
    super(errorCode, message);
  }
}
