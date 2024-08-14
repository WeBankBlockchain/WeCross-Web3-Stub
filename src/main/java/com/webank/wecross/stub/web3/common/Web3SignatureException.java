package com.webank.wecross.stub.web3.common;

public class Web3SignatureException extends RuntimeException {
  public Web3SignatureException(String message) {
    super(message);
  }

  public Web3SignatureException(String message, Throwable cause) {
    super(message, cause);
  }
}
