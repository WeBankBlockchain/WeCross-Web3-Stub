package com.webank.wecross.stub.web3.integration;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.web3.Web3Connection;
import com.webank.wecross.stub.web3.Web3ConnectionFactory;
import com.webank.wecross.stub.web3.Web3StubFactory;
import com.webank.wecross.stub.web3.account.Web3Account;
import com.webank.wecross.stub.web3.account.Web3AccountFactory;
import com.webank.wecross.stub.web3.client.ClientBlockManager;
import com.webank.wecross.stub.web3.client.ClientWrapperImpl;
import com.webank.wecross.stub.web3.common.Web3Constant;
import com.webank.wecross.stub.web3.common.Web3StatusCode;
import com.webank.wecross.stub.web3.custom.RegisterResourceHandler;
import com.webank.wecross.stub.web3.integration.contract.HelloWorld;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.StaticGasProvider;

public class Web3StubCallContractIntegTest {
  private static final Logger logger = LoggerFactory.getLogger(Web3StubCallContractIntegTest.class);

  public static final String CHAINS_PATH = "./chains/web3/";
  public static final String WEB3_WALLET = "Web3Wallet";

  public final StaticGasProvider staticGasProvider =
      new StaticGasProvider(BigInteger.valueOf(22_000_000_000L), BigInteger.valueOf(4_300_000));
  private final RegisterResourceHandler registerResourceHandler = new RegisterResourceHandler();

  private Driver driver = null;
  private Web3Connection connection = null;
  private Web3Account account = null;
  private BlockManager blockManager = null;
  private Map<String, ResourceInfo> resourceInfoMap = null;

  @Before
  public void initializer() throws Exception {
    connection = Web3ConnectionFactory.build(CHAINS_PATH, Web3Constant.STUB_TOML_NAME);
    connection.setConnectionEventHandler(new ConnectionEventHandlerImplMock());

    account = Web3AccountFactory.build(WEB3_WALLET, CHAINS_PATH + WEB3_WALLET);

    Web3StubFactory web3StubFactory = new Web3StubFactory();
    driver = web3StubFactory.newDriver();

    ClientWrapperImpl clientWrapper = (ClientWrapperImpl) connection.getClientWrapper();
    blockManager = new ClientBlockManager(clientWrapper);

    Web3j web3j = clientWrapper.getWeb3j();
    Credentials credentials = account.getCredentials();

    // deploy HelloWorld
    HelloWorld helloWorld = HelloWorld.deploy(web3j, credentials, staticGasProvider).send();
    registerResource(HelloWorld.NAME, helloWorld.getContractAddress(), HelloWorld.ABI);

    resourceInfoMap =
        connection.getResourceInfoList().stream()
            .collect(Collectors.toMap(ResourceInfo::getName, Function.identity()));
  }

  @Test
  public void getBlockNumberIntegIntegTest() throws InterruptedException {
    AsyncToSync asyncToSync = new AsyncToSync();

    driver.asyncGetBlockNumber(
        connection,
        (e, blockNumber) -> {
          assertTrue(blockNumber > 0);
          asyncToSync.getSemaphore().release();
        });

    asyncToSync.semaphore.acquire(1);
  }

  @Test
  public void getBlockHeaderIntegTest() throws InterruptedException {
    AsyncToSync asyncToSync = new AsyncToSync();

    driver.asyncGetBlockNumber(
        connection,
        (e1, blockNumber) -> {
          assertNull(e1);
          assertTrue(blockNumber > 0);

          driver.asyncGetBlock(
              blockNumber,
              false,
              connection,
              (e2, block) -> {
                assertNull(e2);
                assertNotNull(block);
                BlockHeader blockHeader = block.getBlockHeader();
                assertTrue(block.getRawBytes().length > 1);
                assertNotNull(blockHeader);
                assertNotNull(blockHeader.getHash());
                assertNotNull(blockHeader.getReceiptRoot());
                assertNotNull(blockHeader.getTransactionRoot());
                assertNotNull(blockHeader.getPrevHash());
                assertNotNull(blockHeader.getStateRoot());
                assertEquals(blockHeader.getNumber(), blockNumber);
                asyncToSync.getSemaphore().release();
              });
        });
    asyncToSync.semaphore.acquire(1);
  }

  @Test
  public void getBlockHeaderFailedIntegTest() throws InterruptedException {
    AsyncToSync asyncToSync = new AsyncToSync();

    driver.asyncGetBlockNumber(
        connection,
        (e1, blockNumber) -> {
          assertNull(e1);
          assertTrue(blockNumber > 0);

          driver.asyncGetBlock(
              blockNumber + 1000,
              true,
              connection,
              (e2, bytesBlockHeader) -> {
                assertNotNull(e2);
                assertNull(bytesBlockHeader);
                asyncToSync.getSemaphore().release();
              });
        });

    asyncToSync.semaphore.acquire(1);
  }

  @Test
  public void getGenesisBlockIntegTest() throws InterruptedException {
    AsyncToSync asyncToSync = new AsyncToSync();

    driver.asyncGetBlock(
        0,
        true,
        connection,
        (e1, block) -> {
          assertNull(e1);
          assertNotNull(block);
          BlockHeader blockHeader = block.getBlockHeader();
          assertTrue(block.getRawBytes().length > 1);
          assertNotNull(blockHeader);
          assertNotNull(blockHeader.getHash());
          assertEquals(0, blockHeader.getNumber());
          asyncToSync.getSemaphore().release();
        });

    asyncToSync.semaphore.acquire(1);
  }

  @Test
  public void callIntegTest() throws Exception {
    AsyncToSync asyncToSync = new AsyncToSync();

    Path path = Path.decode("a.b.HelloWorld");
    TransactionRequest transactionRequest = createTransactionRequest("get", null);
    TransactionContext transactionContext = createTransactionContext(path);
    driver.asyncCall(
        transactionContext,
        transactionRequest,
        false,
        connection,
        (e1, transactionResponse) -> {
          assertNull(e1);
          assertNotNull(transactionResponse);
          assertEquals((int) transactionResponse.getErrorCode(), Web3StatusCode.Success);
          assertTrue(transactionResponse.getResult().length != 0);

          asyncToSync.getSemaphore().release();
        });

    asyncToSync.semaphore.acquire(1);
  }

  @Test
  public void callNotExistMethodIntegTest() throws Exception {
    AsyncToSync asyncToSync = new AsyncToSync();

    Path path = Path.decode("a.b.HelloWorld");
    TransactionRequest transactionRequest = createTransactionRequest("getNotExist", null);
    TransactionContext transactionContext = createTransactionContext(path);
    driver.asyncCall(
        transactionContext,
        transactionRequest,
        false,
        connection,
        (e1, transactionResponse) -> {
          assertNotNull(e1);
          assertNull(transactionResponse);

          asyncToSync.getSemaphore().release();
        });

    asyncToSync.semaphore.acquire(1);
  }

  @Test
  public void sendTransactionIntegTest() throws Exception {
    AsyncToSync asyncToSync = new AsyncToSync();
    Path path = Path.decode("a.b.HelloWorld");
    String[] params = new String[] {"Hello,ni hao !!!"};
    TransactionRequest transactionRequest = createTransactionRequest("set", params);
    TransactionContext transactionContext = createTransactionContext(path);
    final String[] hash = {""};
    final long[] blockNumber = {0};
    driver.asyncSendTransaction(
        transactionContext,
        transactionRequest,
        false,
        connection,
        (e1, transactionResponse) -> {
          assertNull(e1);
          assertNotNull(transactionResponse);
          assertEquals((int) transactionResponse.getErrorCode(), Web3StatusCode.Success);
          assertTrue(transactionResponse.getBlockNumber() > 0);
          hash[0] = transactionResponse.getHash();
          blockNumber[0] = transactionResponse.getBlockNumber();
          asyncToSync.getSemaphore().release();
        });
    asyncToSync.getSemaphore().acquire();

    AsyncToSync asyncToSync1 = new AsyncToSync();
    driver.asyncGetTransaction(
        hash[0],
        blockNumber[0],
        blockManager,
        false,
        connection,
        (e2, transaction) -> {
          assertNull(e2);
          assertNotNull(transaction);
          assertTrue(transaction.getReceiptBytes().length > 1);
          assertTrue(transaction.getTxBytes().length > 1);
          asyncToSync1.getSemaphore().release();
        });
    asyncToSync1.getSemaphore().acquire();

    AsyncToSync asyncToSync2 = new AsyncToSync();
    TransactionRequest transactionRequest2 = createTransactionRequest("get", null);
    driver.asyncCall(
        transactionContext,
        transactionRequest2,
        false,
        connection,
        (e3, transactionResponse) -> {
          assertNull(e3);
          assertNotNull(transactionResponse);
          assertEquals((int) transactionResponse.getErrorCode(), Web3StatusCode.Success);
          assertEquals(transactionResponse.getResult()[0], params[0]);
          asyncToSync2.getSemaphore().release();
        });
    asyncToSync2.getSemaphore().acquire();
  }

  @Test
  public void sendTransactionNotExistIntegTest() throws Exception {
    AsyncToSync asyncToSync = new AsyncToSync();
    Path path = Path.decode("a.b.HelloWorld");
    String[] params = new String[] {"Hello,ni hao !!!"};
    TransactionRequest transactionRequest = createTransactionRequest("setNotExist", params);
    TransactionContext transactionContext = createTransactionContext(path);
    driver.asyncSendTransaction(
        transactionContext,
        transactionRequest,
        false,
        connection,
        (e1, transactionResponse) -> {
          assertNotNull(e1);
          assertTrue(StringUtils.contains(e1.getMessage(), "Invalid method setNotExist"));
          asyncToSync.getSemaphore().release();
        });
    asyncToSync.getSemaphore().acquire();
  }

  public TransactionRequest createTransactionRequest(String method, String[] args) {
    return new TransactionRequest(method, args);
  }

  public TransactionContext createTransactionContext(Path path) {
    return new TransactionContext(
        account, path, resourceInfoMap.get(path.getResource()), blockManager);
  }

  public void registerResource(String resourceName, String contractAddress, String contractABI) {
    registerResourceHandler.handle(
        null,
        new Object[] {resourceName, contractAddress, contractABI},
        null,
        null,
        connection,
        (e1, response) -> {
          assertNull(e1);
        });
  }
}
