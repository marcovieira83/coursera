// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private BlockNode maxHeightBlockNode;
    private Map<ByteArrayWrapper, BlockNode> blockchain = new HashMap<>();
    private TransactionPool transactionPool = new TransactionPool();

    private class BlockNode {
        private Block element;
        private BlockNode parent;
        private List<BlockNode> children = new ArrayList<>();

        private int height;
        private UTXOPool utxoPool;

        public BlockNode(Block block, UTXOPool utxoPool, BlockNode parent) {
            this.element = block;
            this.utxoPool = utxoPool;
            this.parent = parent;
            this.height = this.parent == null ? 0 : (this.parent.height + 1);
        }

        public BlockNode addChild(Block block) {
            BlockNode child = new BlockNode(block, new UTXOPool(this.utxoPool), this);
            this.children.add(child);
            return child;
        }
    }
    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        BlockNode blockNode = new BlockNode(genesisBlock, new UTXOPool(), null);
        this.maxHeightBlockNode = blockNode;
        ByteArrayWrapper genesisBlockHash = new ByteArrayWrapper(genesisBlock.getHash());
        this.blockchain.put(genesisBlockHash, this.maxHeightBlockNode);

        updateUTXOPool(genesisBlock, blockNode);
    }

    private void updateUTXOPool(Block genesisBlock, BlockNode blockNode) {
        Transaction tx = genesisBlock.getCoinbase();
        int index = 0;
        for (Transaction.Output output : tx.getOutputs()) {
            final UTXO utxo = new UTXO(tx.getHash(), index++);
            blockNode.utxoPool.addUTXO(utxo, output);
        }
    }


    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return this.maxHeightBlockNode.element;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return this.maxHeightBlockNode.utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return this.transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     *
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     *
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        byte[] prevBlockHash = block.getPrevBlockHash();
        if (prevBlockHash == null) return false;

        ByteArrayWrapper prevBlockHashWrap = new ByteArrayWrapper(prevBlockHash);
        BlockNode parentBlockNode = this.blockchain.get(prevBlockHashWrap);
        if (parentBlockNode == null) return false;

        UTXOPool utxoPool = parentBlockNode.utxoPool;
        TxHandler txHandler = new TxHandler(utxoPool);
        Transaction[] validTxs = txHandler.handleTxs(
                block.getTransactions().toArray(new Transaction[block.getTransactions().size()]));
        if (validTxs.length != block.getTransactions().size()) return false;

        if ((parentBlockNode.height + 1) <= this.maxHeightBlockNode.height - CUT_OFF_AGE) return false;

        BlockNode newBlockNode = parentBlockNode.addChild(block);
        blockchain.put(new ByteArrayWrapper(block.getHash()), newBlockNode);
        this.updateUTXOPool(block, newBlockNode);


//        if (true)
//            throw new IllegalStateException("newHeight: " + newBlockNode.height + "; maxHeight: " + this.maxHeightBlockNode.height);

        if (newBlockNode.height > this.maxHeightBlockNode.height) { this.maxHeightBlockNode = newBlockNode;
        }
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        this.transactionPool.addTransaction(tx);
    }
}
