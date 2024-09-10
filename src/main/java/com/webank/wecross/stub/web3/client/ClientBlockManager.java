package com.webank.wecross.stub.web3.client;

import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.web3.contract.BlockUtility;
import java.io.IOException;
import java.math.BigInteger;
import org.web3j.protocol.core.methods.response.EthBlock;

public class ClientBlockManager implements BlockManager {
  private final ClientWrapper clientWrapper;

  public ClientBlockManager(ClientWrapper clientWrapper) {
    this.clientWrapper = clientWrapper;
  }

  public long getBlockNumber() throws IOException {
    BigInteger blockNumber = clientWrapper.ethBlockNumber();
    return blockNumber.longValue();
  }

  public Block getBlock(long blockNumber) throws IOException {
    EthBlock.Block block = clientWrapper.ethGetBlockByNumber(BigInteger.valueOf(blockNumber));
    return BlockUtility.convertToBlock(block, false);
  }

  @Override
  public void start() {}

  @Override
  public void stop() {}

  @Override
  public void asyncGetBlockNumber(GetBlockNumberCallback callback) {
    try {
      callback.onResponse(null, getBlockNumber());
    } catch (Exception e) {
      callback.onResponse(e, -1);
    }
  }

  @Override
  public void asyncGetBlock(long blockNumber, GetBlockCallback callback) {
    try {
      Block block = getBlock(blockNumber);
      callback.onResponse(null, block);
    } catch (IOException e) {
      callback.onResponse(e, null);
    }
  }
}
