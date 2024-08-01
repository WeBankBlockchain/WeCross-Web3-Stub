package com.webank.wecross.stub.web3.protocol.request;

import com.webank.wecross.stub.TransactionRequest;

public class TransactionParams {
  private TransactionRequest transactionRequest;
  private SUB_TYPE subType;
  private String from;
  private String to;
  private String data;
  private String abi;

  public TransactionParams() {}

  // SendTransaction
  public TransactionParams(
      TransactionRequest transactionRequest, SUB_TYPE subType, String data, String abi) {
    this.transactionRequest = transactionRequest;
    this.subType = subType;
    this.data = data;
    this.abi = abi;
  }

  // Call
  public TransactionParams(
      TransactionRequest transactionRequest,
      SUB_TYPE subType,
      String from,
      String to,
      String data,
      String abi) {
    this.transactionRequest = transactionRequest;
    this.subType = subType;
    this.from = from;
    this.to = to;
    this.data = data;
    this.abi = abi;
  }

  public enum SUB_TYPE {
    SEND_TX_BY_PROXY,
    CALL_BY_PROXY,
    SEND_TX,
    CALL
  }

  public TransactionRequest getTransactionRequest() {
    return transactionRequest;
  }

  public void setTransactionRequest(TransactionRequest transactionRequest) {
    this.transactionRequest = transactionRequest;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getAbi() {
    return abi;
  }

  public void setAbi(String abi) {
    this.abi = abi;
  }

  public SUB_TYPE getSubType() {
    return subType;
  }

  public void setSubType(SUB_TYPE subType) {
    this.subType = subType;
  }

  @Override
  public String toString() {
    return "TransactionParams{"
        + "transactionRequest="
        + transactionRequest
        + ", subType="
        + subType
        + ", from='"
        + from
        + '\''
        + ", to='"
        + to
        + '\''
        + ", data='"
        + data
        + '\''
        + ", abi='"
        + abi
        + '\''
        + '}';
  }
}
