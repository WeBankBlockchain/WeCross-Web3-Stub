package com.webank.wecross.stub.web3.client;

import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

public class ClientWrapperNotSuccessStatus extends ClientWrapperImplMock {

  @Override
  public EthCall ethCall(Transaction transaction) {
    Response.Error error = new Response.Error();
    error.setCode(3);
    error.setMessage("execution reverted: method reverted!");
    EthCall ethCall = new EthCall();
    ethCall.setId(1);
    ethCall.setJsonrpc("2.0");
    ethCall.setError(error);
    return ethCall;
  }
}
