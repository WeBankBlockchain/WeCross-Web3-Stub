package com.webank.wecross.stub.web3.contract;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Transaction;
import com.webank.wecross.stub.web3.common.ObjectMapperFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;

public class BlockUtility {

  private static final Logger logger = LoggerFactory.getLogger(BlockUtility.class);

  public static BlockHeader convertToBlockHeader(EthBlock.Block block) {
    BlockHeader blockHeader = new BlockHeader();
    blockHeader.setHash(block.getHash());
    blockHeader.setPrevHash(block.getParentHash().isEmpty() ? null : block.getParentHash());
    blockHeader.setNumber(block.getNumber().longValue());
    blockHeader.setReceiptRoot(block.getReceiptsRoot());
    blockHeader.setStateRoot(block.getStateRoot());
    blockHeader.setTransactionRoot(block.getTransactionsRoot());
    blockHeader.setTimestamp(block.getTimestamp().longValue());
    return blockHeader;
  }

  public static Block convertToBlock(EthBlock.Block block, boolean onlyHeader)
      throws JsonProcessingException {
    Block stubBlock = new Block();

    /** BlockHeader */
    BlockHeader blockHeader = convertToBlockHeader(block);
    stubBlock.setBlockHeader(blockHeader);

    List<Transaction> transactionList = new ArrayList<>();
    if (!onlyHeader
        && !block.getTransactions().isEmpty()
        && block.getTransactions().get(0) instanceof EthBlock.TransactionObject) {
      for (int i = 0; i < block.getTransactions().size(); i++) {
        EthBlock.TransactionObject transactionObject =
            (EthBlock.TransactionObject) block.getTransactions().get(i);
        byte[] txBytes = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transactionObject);
        String transactionHash = transactionObject.getHash();
        Transaction transaction = new Transaction();
        transaction.setTxBytes(txBytes);
        transaction.setAccountIdentity(transactionObject.getFrom());
        transaction.setTransactionByProxy(false);
        transaction.getTransactionResponse().setHash(transactionHash);
        transaction.getTransactionResponse().setBlockNumber(block.getNumber().longValue());
        transactionList.add(transaction);
      }
    }
    stubBlock.setTransactionsWithDetail(transactionList);
    return stubBlock;
  }

  public static Block convertToBlock(byte[] blockBytes, boolean onlyHeader) throws IOException {
    EthBlock.Block block =
        ObjectMapperFactory.getObjectMapper().readValue(blockBytes, EthBlock.Block.class);
    if (logger.isDebugEnabled()) {
      logger.debug("blockNumber: {}, blockHash: {}", block.getNumber(), block.getHash());
    }
    Block stubBlock = convertToBlock(block, onlyHeader);
    stubBlock.setRawBytes(blockBytes);
    return stubBlock;
  }
}
