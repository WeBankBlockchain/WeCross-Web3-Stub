package com.webank.wecross.stub.web3;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.web3.account.Web3Account;
import com.webank.wecross.stub.web3.account.Web3AccountFactory;
import com.webank.wecross.stub.web3.client.ClientWrapper;
import com.webank.wecross.stub.web3.client.ClientWrapperImplMock;
import com.webank.wecross.stub.web3.client.ClientWrapperNotSuccessStatus;
import com.webank.wecross.stub.web3.client.ClientWrapperWithExceptionMock;
import com.webank.wecross.stub.web3.client.ClientWrapperWithNullMock;
import com.webank.wecross.stub.web3.common.ObjectMapperFactory;
import com.webank.wecross.stub.web3.common.Web3RequestType;
import com.webank.wecross.stub.web3.common.Web3StatusCode;
import com.webank.wecross.stub.web3.config.Web3StubConfig;
import com.webank.wecross.stub.web3.config.Web3StubConfigParser;
import com.webank.wecross.stub.web3.protocol.request.TransactionParams;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.CryptoType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

public class Web3DriverTest {
  private Driver driver = null;
  private Web3Connection connection = null;
  private Web3Connection exceptionConnection = null;
  private Web3Connection callNotOkStatusConnection = null;
  private Web3Connection nonExistConnection = null;
  private Web3Account account = null;
  private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
  private TransactionContext transactionContext = null;
  private ABICodec abiCodec = null;

  @Before
  public void initializer() throws Exception {
    String web3StubConfigPath = "./";
    abiCodec = new ABICodec(new CryptoSuite(CryptoType.ECDSA_TYPE), true);
    account = Web3AccountFactory.build("wallet", "./account");
    Web3StubConfig web3StubConfig =
        new Web3StubConfigParser(web3StubConfigPath, "stub-sample.toml").loadConfig();
    connection = new Web3Connection(new ClientWrapperImplMock(), web3StubConfigPath);
    connection.refreshStubConfig(web3StubConfig);

    exceptionConnection =
        new Web3Connection(new ClientWrapperWithExceptionMock(), web3StubConfigPath);
    exceptionConnection.refreshStubConfig(web3StubConfig);

    callNotOkStatusConnection =
        new Web3Connection(new ClientWrapperNotSuccessStatus(), web3StubConfigPath);
    callNotOkStatusConnection.refreshStubConfig(web3StubConfig);

    nonExistConnection = new Web3Connection(new ClientWrapperWithNullMock(), web3StubConfigPath);
    nonExistConnection.refreshStubConfig(web3StubConfig);

    Web3StubFactory web3StubFactory = new Web3StubFactory();
    driver = web3StubFactory.newDriver();
    transactionContext =
        new TransactionContext(
            account,
            Path.decode("a.b.HelloWorld"),
            connection.getResourceInfoList().get(0),
            new BlockManagerImplMock());
  }

  @Test
  public void getBlockNumberTest() {
    Request request = new Request();
    request.setType(Web3RequestType.GET_BLOCK_NUMBER);
    driver.asyncGetBlockNumber(
        connection,
        (e, blockNumber) -> {
          assertEquals(blockNumber, 108214L);
        });
  }

  @Test
  public void getBlockNumberExceptionTest() {
    Request request = new Request();
    request.setType(Web3RequestType.GET_BLOCK_NUMBER);
    driver.asyncGetBlockNumber(
        exceptionConnection, (e, blockNumber) -> assertEquals(e.getMessage(), "IOException"));
  }

  @Test
  public void getBlockNumberNullTest() {
    Request request = new Request();
    request.setType(Web3RequestType.GET_BLOCK_NUMBER);
    driver.asyncGetBlockNumber(
        nonExistConnection,
        (e, blockNumber) ->
            assertEquals(
                e.getMessage(),
                Web3StatusCode.getStatusMessage(Web3StatusCode.BlockNumberNotExist)));
  }

  @Test
  public void getBlockWithTransTest() {
    driver.asyncGetBlock(
        108214L,
        false,
        connection,
        (e, block) -> {
          assertTrue(Objects.isNull(e));
          assertTrue(Objects.nonNull(block));
          assertTrue(block.getRawBytes().length > 1);
          assertFalse(block.getTransactionsWithDetail().isEmpty());
          BlockHeader blockHeader = block.getBlockHeader();
          assertEquals(
              blockHeader.getHash(),
              "0x8abe7ca191df7a3b0cc3f081d6d5a1bd2d542bb6a6b7a5c6a9e75244d90c5aad");
          assertEquals(
              blockHeader.getPrevHash(),
              "0x21a26483f5968cdaecfc8a85bd92fda6692ceee52b6716dd2c8fa65225820496");
          assertEquals(
              blockHeader.getReceiptRoot(),
              "0x3998b0b006ea68156060551908975d78935a9add49a23f65ce497af6a5be311e");
          assertEquals(
              blockHeader.getStateRoot(),
              "0x2473e460d2818392c43f5775203c160d23fa13774b82798687cf99e3db7ec054");
          assertEquals(
              blockHeader.getTransactionRoot(),
              "0xe7acf5d55d7d2e63d7c0989660a72cf099539be3abe06a446b1db73b3ff5adf4");
        });
  }

  @Test
  public void getBlockHeaderTest() {
    driver.asyncGetBlock(
        108214L,
        true,
        connection,
        (e, block) -> {
          assertTrue(Objects.isNull(e));
          assertTrue(Objects.nonNull(block));
          assertTrue(block.getRawBytes().length > 1);
          assertTrue(block.getTransactionsHashes().isEmpty());
          assertTrue(block.getTransactionsWithDetail().isEmpty());
          BlockHeader blockHeader = block.getBlockHeader();
          assertEquals(
              blockHeader.getHash(),
              "0x8abe7ca191df7a3b0cc3f081d6d5a1bd2d542bb6a6b7a5c6a9e75244d90c5aad");
          assertEquals(
              blockHeader.getPrevHash(),
              "0x21a26483f5968cdaecfc8a85bd92fda6692ceee52b6716dd2c8fa65225820496");
          assertEquals(
              blockHeader.getReceiptRoot(),
              "0x3998b0b006ea68156060551908975d78935a9add49a23f65ce497af6a5be311e");
          assertEquals(
              blockHeader.getStateRoot(),
              "0x2473e460d2818392c43f5775203c160d23fa13774b82798687cf99e3db7ec054");
          assertEquals(
              blockHeader.getTransactionRoot(),
              "0xe7acf5d55d7d2e63d7c0989660a72cf099539be3abe06a446b1db73b3ff5adf4");
        });
  }

  @Test
  public void callTest() {
    TransactionRequest transactionRequest = new TransactionRequest("get", new String[] {});
    driver.asyncCall(
        transactionContext,
        transactionRequest,
        false,
        connection,
        (e, transactionResponse) -> {
          assertNull(e);
          assertNotNull(transactionResponse);
          assertNotNull(transactionResponse.getHash());
        });
  }

  @Test
  public void callExceptionTest() {
    TransactionRequest transactionRequest = new TransactionRequest("get", new String[] {});
    driver.asyncCall(
        transactionContext,
        transactionRequest,
        false,
        exceptionConnection,
        (e, transactionResponse) -> {
          assertEquals(e.getMessage(), "IOException");
          assertNull(transactionResponse);
        });
  }

  @Test
  public void callNullTest() {
    TransactionRequest transactionRequest = new TransactionRequest("get", new String[] {});
    driver.asyncCall(
        transactionContext,
        transactionRequest,
        false,
        nonExistConnection,
        (e, transactionResponse) -> {
          assertEquals(e.getMessage(), "ethCall is null");
          assertNull(transactionResponse);
        });
  }

  @Test
  public void callRevertTest() {
    TransactionRequest transactionRequest = new TransactionRequest("get", new String[] {});
    driver.asyncCall(
        transactionContext,
        transactionRequest,
        false,
        callNotOkStatusConnection,
        (e, transactionResponse) -> {
          assertEquals(e.getMessage(), "execution reverted: method reverted!");
          assertNull(transactionResponse);
        });
  }

  @Test
  public void sendTXTest() {
    TransactionRequest transactionRequest = new TransactionRequest("set", new String[] {"abc-def"});
    driver.asyncSendTransaction(
        transactionContext,
        transactionRequest,
        false,
        connection,
        (e, transactionResponse) -> {
          Assert.assertNotNull(transactionResponse.getHash());
          Assert.assertNull(e);
        });
  }

  @Test
  public void sendTxExceptionTest() {
    TransactionRequest transactionRequest = new TransactionRequest("set", new String[] {"abc123"});
    driver.asyncSendTransaction(
        transactionContext,
        transactionRequest,
        false,
        exceptionConnection,
        (e, transactionResponse) -> {
          assertEquals(e.getMessage(), "IOException");
          assertNull(transactionResponse);
        });
  }

  @Test
  public void getTransaction() {
    driver.asyncGetTransaction(
        "0x4980adef7b9a6d2cd709cdbd1f308ddff18c6486b3a06fd85387450a87bce8c4",
        108214L,
        null,
        false,
        connection,
        (e, transaction) -> {
          Assert.assertNotNull(transaction);
          Assert.assertNull(e);
        });
  }

  @Test
  public void decodeCallRequest() throws Exception {
    String from = account.getCredentials().getAddress();
    String contractAbi =
        "[{\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String contractAddress = "";
    String[] args = new String[] {"abc-def"};
    String method = "set";
    String encodedMethodWithArgs =
        abiCodec.encodeMethodFromString(contractAbi, method, Arrays.asList(args));
    TransactionParams transaction =
        new TransactionParams(
            new TransactionRequest(method, args),
            TransactionParams.SUB_TYPE.CALL,
            encodedMethodWithArgs,
            contractAbi);
    transaction.setFrom(from);
    transaction.setTo(contractAddress);
    transaction.setAbi(contractAbi);
    Request req =
        Request.newRequest(Web3RequestType.CALL, objectMapper.writeValueAsBytes(transaction));
    ImmutablePair<Boolean, TransactionRequest> booleanTransactionRequestImmutablePair =
        driver.decodeTransactionRequest(req);
    Assert.assertTrue(booleanTransactionRequestImmutablePair.getLeft());
    Assert.assertTrue(Objects.nonNull(booleanTransactionRequestImmutablePair.getRight()));
  }

  @Test
  public void decodeSendTXRequest() throws Exception {
    String address = account.getCredentials().getAddress();
    String contractAddress = "";
    String contractAbi =
        "[{\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    String[] args = new String[] {"abc-def"};
    String method = "set";
    String encodedMethodWithArgs =
        abiCodec.encodeMethodFromString(contractAbi, method, Arrays.asList(args));
    ClientWrapper clientWrapper = connection.getClientWrapper();
    RawTransaction rawTransaction =
        RawTransaction.createTransaction(
            clientWrapper.getNonce(address),
            clientWrapper.ethGasPrice(),
            clientWrapper.ethGasLimit(),
            contractAddress,
            encodedMethodWithArgs);
    byte[] signedMessage =
        TransactionEncoder.signMessage(
            rawTransaction, connection.getChainId().longValue(), account.getCredentials());
    TransactionParams transaction =
        new TransactionParams(
            new TransactionRequest(method, args),
            TransactionParams.SUB_TYPE.SEND_TX,
            Numeric.toHexString(signedMessage),
            contractAbi);
    transaction.setFrom(address);
    transaction.setTo(contractAddress);
    transaction.setAbi(contractAbi);
    Request req =
        Request.newRequest(
            Web3RequestType.SEND_TRANSACTION, objectMapper.writeValueAsBytes(transaction));
    ImmutablePair<Boolean, TransactionRequest> booleanTransactionRequestImmutablePair =
        driver.decodeTransactionRequest(req);
    Assert.assertTrue(booleanTransactionRequestImmutablePair.getLeft());
    Assert.assertTrue(Objects.nonNull(booleanTransactionRequestImmutablePair.getRight()));
  }
}
