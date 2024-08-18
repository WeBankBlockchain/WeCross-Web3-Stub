package com.webank.wecross.stub.web3;

import static junit.framework.TestCase.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.web3.client.ClientWrapper;
import com.webank.wecross.stub.web3.client.ClientWrapperNotSuccessStatus;
import com.webank.wecross.stub.web3.client.ClientWrapperWithExceptionMock;
import com.webank.wecross.stub.web3.client.ClientWrapperWithNullMock;
import com.webank.wecross.stub.web3.common.Web3RequestType;
import com.webank.wecross.stub.web3.common.Web3StatusCode;
import com.webank.wecross.stub.web3.protocol.request.TransactionParams;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.utils.Numeric;

public class Web3ConnectionFailedTest {
  private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

  @Before
  public void init() {
    this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
  }

  @Test
  public void handleExceptionGetBlockNumberTest() throws IOException {
    ClientWrapper clientWrapper = new ClientWrapperWithExceptionMock();
    Web3Connection web3Connection = new Web3Connection(clientWrapper, "./stub-sample.toml");
    Request request = new Request();
    request.setType(Web3RequestType.GET_BLOCK_NUMBER);
    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.HandleGetBlockNumberFailed);
        });
  }

  @Test
  public void handleNullGetBlockNumberTest() throws IOException {
    ClientWrapper clientWrapper = new ClientWrapperWithNullMock();
    Web3Connection web3Connection = new Web3Connection(clientWrapper, "./stub-sample.toml");
    Request request = new Request();
    request.setType(Web3RequestType.GET_BLOCK_NUMBER);
    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.BlockNumberNotExist);
        });
  }

  @Test
  public void handleExceptionGetBlockTest() throws IOException {
    ClientWrapper clientWrapper = new ClientWrapperWithExceptionMock();
    Web3Connection web3Connection = new Web3Connection(clientWrapper, "./stub-sample.toml");
    Request request = new Request();
    request.setType(Web3RequestType.GET_BLOCK_BY_NUMBER);
    request.setData(BigInteger.valueOf(108214).toByteArray());
    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.HandleGetBlockFailed);
        });
  }

  @Test
  public void handleNullGetBlockTest() throws IOException {
    ClientWrapper clientWrapper = new ClientWrapperWithNullMock();
    Web3Connection web3Connection = new Web3Connection(clientWrapper, "./stub-sample.toml");
    Request request = new Request();
    request.setType(Web3RequestType.GET_BLOCK_BY_NUMBER);
    request.setData(BigInteger.valueOf(108214).toByteArray());
    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.BlockNotExist);
        });
  }

  @Test
  public void handleExceptionGetTransactionTest() throws IOException {
    ClientWrapper clientWrapper = new ClientWrapperWithExceptionMock();
    Web3Connection web3Connection = new Web3Connection(clientWrapper, "./stub-sample.toml");
    String transactionHash = "0x4980adef7b9a6d2cd709cdbd1f308ddff18c6486b3a06fd85387450a87bce8c4";
    Request request = new Request();
    request.setType(Web3RequestType.GET_TRANSACTION);
    request.setData(transactionHash.getBytes());
    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.HandleGetTransactionFailed);
        });
  }

  @Test
  public void handleNullGetTransactionTest() throws IOException {
    ClientWrapper clientWrapper = new ClientWrapperWithNullMock();
    Web3Connection web3Connection = new Web3Connection(clientWrapper, "./stub-sample.toml");
    String transactionHash = "0x4980adef7b9a6d2cd709cdbd1f308ddff18c6486b3a06fd85387450a87bce8c4";
    Request request = new Request();
    request.setType(Web3RequestType.GET_TRANSACTION);
    request.setData(transactionHash.getBytes());
    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.TransactionNotExist);
        });
  }

  @Test
  public void handleExceptionCallTest() throws IOException {
    ClientWrapper clientWrapper = new ClientWrapperWithExceptionMock();
    Web3Connection web3Connection = new Web3Connection(clientWrapper, "./stub-sample.toml");

    String method = "get";
    String[] params = new String[] {};
    TransactionRequest transactionRequest = new TransactionRequest(method, params);

    Function function =
        new Function(
            method, Collections.emptyList(), Arrays.asList(new TypeReference<Utf8String>() {}));
    String from = "0xaff0ca253b97e54440965855cec0a8a2e2399896";
    String to = "0x7cf92bd66acacf18f46a4e2dc0280cd67bfae923";
    String data = FunctionEncoder.encode(function);
    String abi =
        "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";
    TransactionParams transactionParams =
        new TransactionParams(
            transactionRequest, TransactionParams.SUB_TYPE.CALL, from, to, data, abi);

    Request request = new Request();
    request.setType(Web3RequestType.CALL);
    request.setData(ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transactionParams));

    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.HandleCallRequestFailed);
        });
  }

  @Test
  public void handleNullCallTest() throws IOException {
    ClientWrapper clientWrapper = new ClientWrapperWithNullMock();
    Web3Connection web3Connection = new Web3Connection(clientWrapper, "./stub-sample.toml");

    String method = "get";
    String[] params = new String[] {};
    TransactionRequest transactionRequest = new TransactionRequest(method, params);

    Function function =
        new Function(
            method, Collections.emptyList(), Arrays.asList(new TypeReference<Utf8String>() {}));
    String from = "0xaff0ca253b97e54440965855cec0a8a2e2399896";
    String to = "0x7cf92bd66acacf18f46a4e2dc0280cd67bfae923";
    String data = FunctionEncoder.encode(function);
    String abi =
        "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";
    TransactionParams transactionParams =
        new TransactionParams(
            transactionRequest, TransactionParams.SUB_TYPE.CALL, from, to, data, abi);

    Request request = new Request();
    request.setType(Web3RequestType.CALL);
    request.setData(ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transactionParams));

    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.CallNotSuccessStatus);
        });
  }

  @Test
  public void handleNotSuccessCallTest() throws IOException {
    ClientWrapper clientWrapper = new ClientWrapperNotSuccessStatus();
    Web3Connection web3Connection = new Web3Connection(clientWrapper, "./stub-sample.toml");

    String method = "get";
    String[] params = new String[] {};
    TransactionRequest transactionRequest = new TransactionRequest(method, params);

    Function function =
        new Function(
            method, Collections.emptyList(), Arrays.asList(new TypeReference<Utf8String>() {}));
    String from = "0xaff0ca253b97e54440965855cec0a8a2e2399896";
    String to = "0x7cf92bd66acacf18f46a4e2dc0280cd67bfae923";
    String data = FunctionEncoder.encode(function);
    String abi =
        "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";
    TransactionParams transactionParams =
        new TransactionParams(
            transactionRequest, TransactionParams.SUB_TYPE.CALL, from, to, data, abi);

    Request request = new Request();
    request.setType(Web3RequestType.CALL);
    request.setData(ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transactionParams));

    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.CallNotSuccessStatus);
          assertEquals(response.getErrorMessage(), "execution reverted: method reverted!");
        });
  }

  @Test
  public void handleExceptionSendTransactionTest() throws IOException {
    ClientWrapper clientWrapper = new ClientWrapperWithExceptionMock();
    Web3Connection web3Connection = new Web3Connection(clientWrapper, "./stub-sample.toml");

    String method = "set";
    String[] params = new String[] {"hello world!!!"};
    TransactionRequest transactionRequest = new TransactionRequest(method, params);
    Function function =
        new Function(
            method, Arrays.asList(new Utf8String("hello world!!!")), Collections.emptyList());
    String data = FunctionEncoder.encode(function);

    String privateKey = "4b9f63ecf84210c5366c66d68fa1f5da1fa4f634fad6dfc86178e4d79ff9e59";
    Credentials credentials = Credentials.create(privateKey);
    RawTransaction rawTransaction =
        RawTransaction.createTransaction(
            clientWrapper.getNonce(credentials.getAddress()),
            clientWrapper.ethGasPrice(),
            clientWrapper.ethGasLimit(),
            "0x7cf92bd66acacf18f46a4e2dc0280cd67bfae923",
            data);

    byte[] signedMessage =
        TransactionEncoder.signMessage(
            rawTransaction, clientWrapper.ethChainId().longValue(), credentials);
    String abi =
        "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";

    TransactionParams transactionParams =
        new TransactionParams(
            transactionRequest,
            TransactionParams.SUB_TYPE.SEND_TX,
            Numeric.toHexString(signedMessage),
            abi);

    Request request = new Request();
    request.setType(Web3RequestType.SEND_TRANSACTION);
    request.setData(ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transactionParams));

    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.HandleSendTransactionFailed);
        });
  }

  @Test
  public void handleNullSendTransactionTest() throws IOException {
    ClientWrapper clientWrapper = new ClientWrapperWithNullMock();
    Web3Connection web3Connection = new Web3Connection(clientWrapper, "./stub-sample.toml");

    String method = "set";
    String[] params = new String[] {"hello world!!!"};
    TransactionRequest transactionRequest = new TransactionRequest(method, params);
    Function function =
        new Function(
            method, Arrays.asList(new Utf8String("hello world!!!")), Collections.emptyList());
    String data = FunctionEncoder.encode(function);

    String privateKey = "4b9f63ecf84210c5366c66d68fa1f5da1fa4f634fad6dfc86178e4d79ff9e59";
    Credentials credentials = Credentials.create(privateKey);
    RawTransaction rawTransaction =
        RawTransaction.createTransaction(
            clientWrapper.getNonce(credentials.getAddress()),
            clientWrapper.ethGasPrice(),
            clientWrapper.ethGasLimit(),
            "0x7cf92bd66acacf18f46a4e2dc0280cd67bfae923",
            data);

    byte[] signedMessage =
        TransactionEncoder.signMessage(
            rawTransaction, clientWrapper.ethChainId().longValue(), credentials);
    String abi =
        "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";

    TransactionParams transactionParams =
        new TransactionParams(
            transactionRequest,
            TransactionParams.SUB_TYPE.SEND_TX,
            Numeric.toHexString(signedMessage),
            abi);

    Request request = new Request();
    request.setType(Web3RequestType.SEND_TRANSACTION);
    request.setData(ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transactionParams));

    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.TransactionReceiptNotExist);
        });
  }
}
