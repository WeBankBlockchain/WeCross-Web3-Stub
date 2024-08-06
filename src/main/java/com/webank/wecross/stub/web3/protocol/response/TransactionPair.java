package com.webank.wecross.stub.web3.protocol.response;

import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class TransactionPair {
  private Transaction transaction;
  private TransactionReceipt transactionReceipt;

  public TransactionPair() {}

  public TransactionPair(Transaction transaction, TransactionReceipt transactionReceipt) {
    this.transaction = transaction;
    this.transactionReceipt = transactionReceipt;
  }

  public TransactionReceipt getTransactionReceipt() {
    return transactionReceipt;
  }

  public void setTransactionReceipt(TransactionReceipt transactionReceipt) {
    this.transactionReceipt = transactionReceipt;
  }

  public Transaction getTransaction() {
    return transaction;
  }

  public void setTransaction(Transaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public String toString() {
    return "TransactionPair{"
        + "transactionReceipt="
        + transactionReceipt
        + ", transaction="
        + transaction
        + '}';
  }
}
