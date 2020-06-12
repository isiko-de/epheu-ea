package de.isiko.hedera;

import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.account.*;
import com.hedera.hashgraph.sdk.crypto.Mnemonic;
import com.hedera.hashgraph.sdk.crypto.PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hashgraph.sdk.file.FileContentsQuery;
import com.hedera.hashgraph.sdk.file.FileId;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Objects;

public class General {

    public static void generateKey() {
        // Generate a Ed25519 private, public key pair
        Ed25519PrivateKey newKey = Ed25519PrivateKey.generate();
        Ed25519PublicKey newPublicKey = newKey.publicKey;

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);
    }

    public static void generateKeyWithMnemonic() {
        Mnemonic mnemonic = Mnemonic.generate();

        Ed25519PrivateKey newKey = mnemonic.toPrivateKey();
        Ed25519PublicKey newPublicKey = newKey.publicKey;

        System.out.println("mnemonic = " + mnemonic);
        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);
    }

    public static void getAddressBook(Client client) throws HederaStatusException, IOException {

        final FileContentsQuery fileQuery = new FileContentsQuery()
                .setFileId(FileId.ADDRESS_BOOK);

        final long cost = fileQuery.getCost(client);
        System.out.println("file contents cost: " + cost);

        fileQuery.setMaxQueryPayment(new Hbar(1));

        final byte[] contents = fileQuery.execute(client);

        Files.copy(new ByteArrayInputStream(contents),
                FileSystems.getDefault().getPath("address-book.proto.bin"));
    }



    public static void UpdateAccountPublicKey(Client client,PrivateKey originalPrivateKey, PrivateKey newPrivateKey, AccountId accountToBeChangedId) throws HederaStatusException {

        System.out.println(" :: update public key of account " + accountToBeChangedId);
        System.out.println("set key = " + newPrivateKey.publicKey);

        TransactionId transactionId = new AccountUpdateTransaction()
                .setAccountId(accountToBeChangedId)
                .setKey(newPrivateKey.publicKey)
                .build(client)
                // Sign with the previous key and the new key
                .sign(originalPrivateKey)
                .sign(newPrivateKey)
                .execute(client);

        System.out.println("transaction ID: " + transactionId);

        // (important!) wait for the transaction to complete by querying the receipt
        transactionId.getReceipt(client);

        // Now we fetch the account information to check if the key was changed
        System.out.println(" :: getAccount and check our current key");

        AccountInfo info = new AccountInfoQuery()
                .setAccountId(accountToBeChangedId)
                .execute(client);

        System.out.println("key = " + info.key);
    }

}
