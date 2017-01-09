import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TxHandler {

    private UTXOPool pool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        Set<UTXO> utxoUsed = new HashSet<UTXO>();
        double inputSum = 0d;
        int inputIndex = 0;
        for (Transaction.Input input : tx.getInputs()) {
            // a partir do hash e do indice, procura no pool. Se encontrar, eh porque pode gastar.
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);

            // (1) all outputs claimed by {@code tx} are in the current UTXO pool
            Transaction.Output outputBeingConsumed = this.pool.getTxOutput(utxo);
            if (outputBeingConsumed == null) {
                System.out.println("isValid (1) falhou");
                return false;
            }

            // (2) the signatures on each input of {@code tx} are valid
            byte[] message = tx.getRawDataToSign(inputIndex++);
            if (!Crypto.verifySignature(outputBeingConsumed.address, message, input.signature)) {
                System.out.println("isValid (2) falhou");
                return false;
            }

            // (3) no UTXO is claimed multiple times by {@code tx}
            if (utxoUsed.contains(utxo)) {
                System.out.println("isValid (3) falhou");
                return false;
            }
            utxoUsed.add(utxo);
            inputSum += outputBeingConsumed.value;
        }

        double outputSum = 0;
        for (Transaction.Output output : tx.getOutputs()) {
            // (4) all of {@code tx}s output values are non-negative
            if (output.value < 0d) {
                System.out.println("isValid (4) falhou");
                return false;
            }
            outputSum += output.value;
        }

        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values; and false otherwise.
        if (inputSum < outputSum) {
            System.out.println("isValid (5) falhou");
            return false;
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        List<Transaction> validTxs = new ArrayList<Transaction>();
        for (Transaction tx : possibleTxs) {
            if (this.isValidTx(tx)) {
                validTxs.add(tx);
                // remove entradas sendo consumidas do pool
                for (Transaction.Input input : tx.getInputs()) {
                    this.pool.removeUTXO(new UTXO(input.prevTxHash, input.outputIndex));
                }

                // adiciona novas transacoes que nao foram consumidas no pool
                int index = 0;
                for (Transaction.Output output : tx.getOutputs()) {
                    this.pool.addUTXO(new UTXO(tx.getHash(), index++), output);
                }
            }
        }

        return validTxs.toArray(new Transaction[1000]);
    }
}
