package com.webank.wecross.stub.web3;

import com.webank.wecross.stub.BlockManager;

public class BlockManagerImplMock implements BlockManager {

  @Override
  public void start() {}

  @Override
  public void stop() {}

  @Override
  public void asyncGetBlockNumber(GetBlockNumberCallback callback) {}

  @Override
  public void asyncGetBlock(long blockNumber, GetBlockCallback callback) {}
}
