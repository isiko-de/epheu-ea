package de.isiko.hedera;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.PrivateKey;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.HashMap;
import java.util.Objects;

public class Transfer {


    public static void multiAppTransfer(Client client, PrivateKey exchangeKey, AccountId operatorId, Hbar value, PrivateKey userKey) throws HederaStatusException, InvalidProtocolBufferException {
        Hbar transferAmount = Hbar.fromTinybar(10_000);

        // the exchange creates an account for the user to transfer funds to
        TransactionId createExchangeAccountTxnId = new AccountCreateTransaction()
                // the exchange only accepts transfers that it validates through a side channel (e.g. REST API)
                .setReceiverSignatureRequired(true)
                .setKey(exchangeKey.publicKey)
                // The owner key has to sign this transaction
                // when setReceiverSignatureRequired is true
                .build(client)
                .sign(exchangeKey)
                .execute(client);

        AccountId exchangeAccountId = createExchangeAccountTxnId.getReceipt(client).getAccountId();

        Transaction transferTxn = new CryptoTransferTransaction()
                .addSender(operatorId, value)
                .addRecipient(exchangeAccountId, transferAmount)
                // the exchange-provided memo required to validate the transaction
                .setTransactionMemo("https://some-exchange.com/user1/account1")
                // To manually sign, you must explicitly build the Transaction
                .build(client)
                .sign(userKey);

        // the exchange must sign the transaction in order for it to be accepted by the network
        // assume this is some REST call to the exchange API server
        byte[] signedTxnBytes = exchangeSignsTransaction(transferTxn.toBytes(), exchangeKey);

        // we execute the signed transaction and wait for it to be accepted
        Transaction signedTransferTxn = Transaction.fromBytes(signedTxnBytes);

        signedTransferTxn.execute(client);
        // (important!) wait for consensus by querying for the receipt
        signedTransferTxn.getReceipt(client);

        System.out.println("transferred " + transferAmount + "...");

        Hbar senderBalanceAfter = client.getAccountBalance(operatorId);
        Hbar receiptBalanceAfter = client.getAccountBalance(exchangeAccountId);

        System.out.println("" + operatorId + " balance = " + senderBalanceAfter);
        System.out.println("" + exchangeAccountId + " balance = " + receiptBalanceAfter);
    }

    private static byte[] exchangeSignsTransaction(byte[] transactionData, PrivateKey exchangeKey) throws InvalidProtocolBufferException {
        return Transaction.fromBytes(transactionData)
                .sign(exchangeKey)
                .toBytes();
    }



    public static void transferCrypto(Client client, AccountId recipientId, AccountId operatorId) throws HederaStatusException {



        Hbar amount = Hbar.fromTinybar(10_000);

        Hbar senderBalanceBefore = new AccountBalanceQuery()
                .setAccountId(operatorId)
                .execute(client);

        Hbar receiptBalanceBefore = new AccountBalanceQuery()
                .setAccountId(recipientId)
                .execute(client);

        System.out.println("" + operatorId + " balance = " + senderBalanceBefore);
        System.out.println("" + recipientId + " balance = " + receiptBalanceBefore);

        TransactionId transactionId = new CryptoTransferTransaction()
                // .addSender and .addRecipient can be called as many times as you want as long as the total sum from
                // both sides is equivalent
                .addSender(operatorId, amount)
                .addRecipient(recipientId, amount)
                .setTransactionMemo("transfer test")
                .execute(client);

        System.out.println("transaction ID: " + transactionId);

        TransactionRecord record = transactionId.getRecord(client);

        System.out.println("transferred " + amount + "...");

        Hbar senderBalanceAfter = new AccountBalanceQuery()
                .setAccountId(operatorId)
                .execute(client);

        Hbar receiptBalanceAfter = new AccountBalanceQuery()
                .setAccountId(recipientId)
                .execute(client);

        System.out.println("" + operatorId + " balance = " + senderBalanceAfter);
        System.out.println("" + recipientId + " balance = " + receiptBalanceAfter);
        System.out.println("Transfer memo: " + record.transactionMemo);
    }

}
