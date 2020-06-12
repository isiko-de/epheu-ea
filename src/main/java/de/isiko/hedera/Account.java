package de.isiko.hedera;
import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.account.*;
import com.hedera.hashgraph.sdk.crypto.PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ThresholdKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;

import io.github.cdimascio.dotenv.Dotenv;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Account {

    public static void createAccount(Client client, long initialBalanceInTinyBar) throws HederaStatusException {
        // Generate a Ed25519 private, public key pair
        Ed25519PrivateKey newKey = Ed25519PrivateKey.generate();
        Ed25519PublicKey newPublicKey = newKey.publicKey;

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        TransactionId txId = new AccountCreateTransaction()
                // The only _required_ property here is `key`
                .setKey(newKey.publicKey)
                .setInitialBalance(Hbar.fromTinybar(initialBalanceInTinyBar))
                .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = txId.getReceipt(client);

        AccountId newAccountId = receipt.getAccountId();

        System.out.println("account = " + newAccountId);
        System.out.println("initial balance = " + initialBalanceInTinyBar);


    }

    private static void createAccountThresholdKey(Client client) throws HederaStatusException {
        // Generate three new Ed25519 private, public key pairs
        final ArrayList<Ed25519PrivateKey> keys = new ArrayList<Ed25519PrivateKey>(3);
        for (int i = 0; i < 3; i++) {
            keys.add(Ed25519PrivateKey.generate());
        }

        final List<Ed25519PublicKey> pubKeys = keys.stream().map((key) -> key.publicKey)
                .collect(Collectors.toList());

        System.out.println("private keys: \n"
                + keys.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n")));

        Transaction tx = new AccountCreateTransaction()
                // require 2 of the 3 keys we generated to sign on anything modifying this account
                .setKey(new ThresholdKey(2).addAll(pubKeys))
                .setInitialBalance(new Hbar(10))
                .build(client);

        tx.execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = tx.getReceipt(client);

        AccountId newAccountId = receipt.getAccountId();

        System.out.println("account = " + newAccountId);

        TransactionId tsfrTxnId = new CryptoTransferTransaction()
                .addSender(newAccountId, new Hbar(10))
                .addRecipient(new AccountId(3), new Hbar(10))
                // To manually sign, you must explicitly build the Transaction
                .build(client)
                // we sign with 2 of the 3 keys
                .sign(keys.get(0))
                .sign(keys.get(1))
                .execute(client);

        // (important!) wait for the transfer to go to consensus
        tsfrTxnId.getReceipt(client);

        Hbar balanceAfter = new AccountBalanceQuery()
                .setAccountId(newAccountId)
                .execute(client);

        System.out.println("account balance after transfer: " + balanceAfter);
    }

    public static void deleteAccount(Client client, AccountId operatorId, AccountId accountToDeleteId, PrivateKey accountToDeletePrivateKey) throws HederaStatusException {

        new AccountDeleteTransaction()
                // note the transaction ID has to use the ID of the account being deleted
                .setTransactionId(new TransactionId(accountToDeleteId))
                .setDeleteAccountId(accountToDeleteId)
                .setTransferAccountId(operatorId)
                .build(client)
                .sign(accountToDeletePrivateKey)
                .execute(client)
                .getReceipt(client);

        final AccountInfo accountInfo = new AccountInfoQuery()
                .setAccountId(accountToDeleteId)
                .setQueryPayment(25)
                .execute(client);

        // note the above accountInfo will fail with ACCOUNT_DELETED due to a known issue on Hedera

        System.out.println("account info: " + accountInfo);
    }

    public static void getAccountBalance(Client client, AccountId accountId) throws HederaStatusException {

        Hbar balance = new AccountBalanceQuery()
                .setAccountId(accountId)
                .execute(client);

        System.out.println("balance = " + balance);
    }

}
