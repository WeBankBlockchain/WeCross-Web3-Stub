package com.webank.wecross.stub.web3;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.web3.client.ClientWrapper;
import com.webank.wecross.stub.web3.client.ClientWrapperImplMock;
import com.webank.wecross.stub.web3.common.Web3Constant;
import com.webank.wecross.stub.web3.common.Web3RequestType;
import com.webank.wecross.stub.web3.common.Web3StatusCode;
import com.webank.wecross.stub.web3.config.Web3StubConfig;
import com.webank.wecross.stub.web3.config.Web3StubConfigParser;
import com.webank.wecross.stub.web3.contract.BlockUtility;
import com.webank.wecross.stub.web3.protocol.request.TransactionParams;
import com.webank.wecross.stub.web3.protocol.response.TransactionPair;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

public class Web3ConnectionTest {
  private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
  private Web3Connection web3Connection;

  @Before
  public void init() throws IOException {
    this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    ClientWrapper clientWrapper = new ClientWrapperImplMock();
    this.web3Connection = new Web3Connection(clientWrapper, "./stub-sample.toml");
  }

  @Test
  public void toBlockHeaderTest() throws IOException {
    String blockJson =
        "{\"number\":108214,\"hash\":\"0x8abe7ca191df7a3b0cc3f081d6d5a1bd2d542bb6a6b7a5c6a9e75244d90c5aad\",\"parentHash\":\"0x21a26483f5968cdaecfc8a85bd92fda6692ceee52b6716dd2c8fa65225820496\",\"nonce\":0,\"sha3Uncles\":\"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\"transactionsRoot\":\"0xe7acf5d55d7d2e63d7c0989660a72cf099539be3abe06a446b1db73b3ff5adf4\",\"stateRoot\":\"0x2473e460d2818392c43f5775203c160d23fa13774b82798687cf99e3db7ec054\",\"receiptsRoot\":\"0x3998b0b006ea68156060551908975d78935a9add49a23f65ce497af6a5be311e\",\"author\":null,\"miner\":\"0x8943545177806ed17b9f23f0a21ee5948ecaa776\",\"mixHash\":\"0x928fe08f6690213632620d84bae3826710c959bf9f079001a75c6ff537932c19\",\"difficulty\":0,\"totalDifficulty\":1,\"extraData\":\"0xd883010e07846765746888676f312e32322e35856c696e7578\",\"size\":816,\"gasLimit\":30000000,\"gasUsed\":24780,\"timestamp\":1723105141,\"transactions\":[{\"hash\":\"0x4980adef7b9a6d2cd709cdbd1f308ddff18c6486b3a06fd85387450a87bce8c4\",\"nonce\":77,\"blockHash\":\"0x8abe7ca191df7a3b0cc3f081d6d5a1bd2d542bb6a6b7a5c6a9e75244d90c5aad\",\"blockNumber\":108214,\"chainId\":1337,\"transactionIndex\":0,\"from\":\"0xaff0ca253b97e54440965855cec0a8a2e2399896\",\"to\":\"0x7cf92bd66acacf18f46a4e2dc0280cd67bfae923\",\"value\":0,\"gasPrice\":22000000000,\"gas\":4300000,\"input\":\"0x4ed3885e0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000e78697869786978692121217e7e7e000000000000000000000000000000000000\",\"creates\":null,\"publicKey\":null,\"raw\":null,\"r\":\"0x4e608162ddfc7e43284df9d8d6e40b151b116fcb0f7c14690c0a92ead1214c30\",\"s\":\"0x49789a1f93f74ef1060048a5871604c4419650ced2ab37786094f2cdd5677224\",\"v\":2710,\"type\":\"0x0\",\"maxFeePerGas\":null,\"maxPriorityFeePerGas\":null,\"accessList\":null,\"transactionIndexRaw\":\"0x0\",\"blockNumberRaw\":\"0x1a6b6\",\"maxPriorityFeePerGasRaw\":null,\"gasPriceRaw\":\"0x51f4d5c00\",\"nonceRaw\":\"0x4d\",\"gasRaw\":\"0x419ce0\",\"valueRaw\":\"0x0\",\"chainIdRaw\":\"0x539\",\"maxFeePerGasRaw\":null}],\"uncles\":[],\"sealFields\":null,\"baseFeePerGas\":7,\"gasUsedRaw\":\"0x60cc\",\"nonceRaw\":\"0x0000000000000000\",\"difficultyRaw\":\"0x0\",\"sizeRaw\":\"0x330\",\"numberRaw\":\"0x1a6b6\",\"gasLimitRaw\":\"0x1c9c380\",\"timestampRaw\":\"0x66b47f75\",\"totalDifficultyRaw\":\"0x1\",\"baseFeePerGasRaw\":\"0x7\"}";
    EthBlock.Block block = objectMapper.readValue(blockJson, EthBlock.Block.class);
    BlockHeader blockHeader = BlockUtility.convertToBlockHeader(block);

    assertEquals(blockHeader.getNumber(), block.getNumber().longValue());
    assertEquals(blockHeader.getPrevHash(), block.getParentHash());
    assertEquals(blockHeader.getHash(), block.getHash());
    assertEquals(blockHeader.getStateRoot(), block.getStateRoot());
    assertEquals(blockHeader.getTransactionRoot(), block.getTransactionsRoot());
    assertEquals(blockHeader.getReceiptRoot(), block.getReceiptsRoot());
    assertEquals(blockHeader.getTimestamp(), block.getTimestamp().longValue());
  }

  @Test
  public void resourceInfoListTest() throws IOException {
    Web3StubConfigParser web3StubConfigParser = new Web3StubConfigParser("./", "stub-sample.toml");
    Web3StubConfig web3StubConfig = web3StubConfigParser.loadConfig();
    List<ResourceInfo> resourceInfoList = web3StubConfig.convertToResourceInfos();

    assertEquals(resourceInfoList.size(), web3StubConfig.getResources().size());

    for (ResourceInfo resourceInfo : resourceInfoList) {
      assertEquals(resourceInfo.getStubType(), web3StubConfig.getCommon().getType());
      assertEquals(
          resourceInfo.getProperties().get(Web3Constant.WEB3_PROPERTY_CHAIN_URL),
          web3StubConfig.getService().getUrl());
    }
  }

  @Test
  public void handleUnknownTypeTest() {
    Request request = new Request();
    request.setType(2000);
    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.UnrecognizedRequestType);
        });
  }

  @Test
  public void handleGetBlockNumberTest() {
    Request request = new Request();
    request.setType(Web3RequestType.GET_BLOCK_NUMBER);
    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.Success);

          BigInteger blockNumber = new BigInteger(response.getData());
          assertEquals(blockNumber.longValue(), 108214);
        });
  }

  @Test
  public void handleGetBlockTest() {
    Request request = new Request();
    request.setType(Web3RequestType.GET_BLOCK_BY_NUMBER);
    request.setData(BigInteger.valueOf(108214).toByteArray());
    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.Success);

          Block block = null;
          try {
            block = BlockUtility.convertToBlock(response.getData(), false);
          } catch (IOException e) {
            e.printStackTrace();
          }
          assertTrue(Objects.nonNull(block));

          BlockHeader blockHeader = block.getBlockHeader();
          assertEquals(blockHeader.getNumber(), 108214);
          assertEquals(
              blockHeader.getPrevHash(),
              "0x21a26483f5968cdaecfc8a85bd92fda6692ceee52b6716dd2c8fa65225820496");
          assertEquals(
              blockHeader.getHash(),
              "0x8abe7ca191df7a3b0cc3f081d6d5a1bd2d542bb6a6b7a5c6a9e75244d90c5aad");
          assertEquals(
              blockHeader.getStateRoot(),
              "0x2473e460d2818392c43f5775203c160d23fa13774b82798687cf99e3db7ec054");
          assertEquals(
              blockHeader.getTransactionRoot(),
              "0xe7acf5d55d7d2e63d7c0989660a72cf099539be3abe06a446b1db73b3ff5adf4");
          assertEquals(
              blockHeader.getReceiptRoot(),
              "0x3998b0b006ea68156060551908975d78935a9add49a23f65ce497af6a5be311e");
          assertEquals(blockHeader.getTimestamp(), 1723105141);
        });
  }

  @Test
  public void handleGetTransactionTest() {
    String transactionHash = "0x4980adef7b9a6d2cd709cdbd1f308ddff18c6486b3a06fd85387450a87bce8c4";
    Request request = new Request();
    request.setType(Web3RequestType.GET_TRANSACTION);
    request.setData(transactionHash.getBytes());
    web3Connection.asyncSend(
        request,
        response -> {
          assertEquals(response.getErrorCode(), Web3StatusCode.Success);
          TransactionPair transactionPair = null;
          try {
            transactionPair = objectMapper.readValue(response.getData(), TransactionPair.class);
          } catch (IOException e) {
            e.printStackTrace();
          }
          assertTrue(Objects.nonNull(transactionPair));
          Transaction transaction = transactionPair.getTransaction();
          TransactionReceipt transactionReceipt = transactionPair.getTransactionReceipt();
          assertTrue(Objects.nonNull(transaction));
          assertTrue(Objects.nonNull(transactionReceipt));
          assertEquals(
              transaction.getHash(),
              "0x4980adef7b9a6d2cd709cdbd1f308ddff18c6486b3a06fd85387450a87bce8c4");
          assertEquals(
              transactionReceipt.getTransactionHash(),
              "0x4980adef7b9a6d2cd709cdbd1f308ddff18c6486b3a06fd85387450a87bce8c4");
        });
  }

  @Test
  public void handleCallTest() throws JsonProcessingException {
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
          assertEquals(response.getErrorCode(), Web3StatusCode.Success);

          EthCall ethCall = null;
          try {
            ethCall = objectMapper.readValue(response.getData(), EthCall.class);
          } catch (IOException e) {
            e.printStackTrace();
          }
          assertTrue(Objects.nonNull(ethCall));
          assertFalse(ethCall.hasError());
          assertEquals(data, ethCall.getResult());
        });
  }

  @Test
  public void handleSendTransactionTest() throws IOException {
    String method = "set";
    String[] params = new String[] {"hello world!!!"};
    TransactionRequest transactionRequest = new TransactionRequest(method, params);
    Function function =
        new Function(
            method, Arrays.asList(new Utf8String("hello world!!!")), Collections.emptyList());
    String data = FunctionEncoder.encode(function);

    ClientWrapper clientWrapper = web3Connection.getClientWrapper();
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
          assertEquals(response.getErrorCode(), Web3StatusCode.Success);

          TransactionReceipt transactionReceipt = null;
          try {
            transactionReceipt =
                objectMapper.readValue(response.getData(), TransactionReceipt.class);
          } catch (IOException e) {
            e.printStackTrace();
          }
          assertTrue(Objects.nonNull(transactionReceipt));
          assertEquals(transactionReceipt.getBlockNumber().longValue(), 108214);
          assertEquals(
              transactionReceipt.getTransactionHash(),
              "0x4980adef7b9a6d2cd709cdbd1f308ddff18c6486b3a06fd85387450a87bce8c4");
          assertEquals(transactionReceipt.getFrom(), credentials.getAddress());
          assertEquals(transactionReceipt.getTo(), "0x7cf92bd66acacf18f46a4e2dc0280cd67bfae923");
        });
  }
}
