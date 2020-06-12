package de.isiko.hedera;

import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

public class Files {

    // #################### CREATE FILE ####################

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    public static void createFile() throws HederaStatusException {
        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // The file is required to be a byte array,
        // you can easily use the bytes of a file instead.
        byte[] fileContents = "Hedera hashgraph is great!".getBytes();

        TransactionId txId = new FileCreateTransaction()
                // Use the same key as the operator to "own" this file
                .addKey(OPERATOR_KEY.publicKey)
                .setContents(fileContents)
                // The default max fee of 1 HBAR is not enough to make a file ( starts around 1.1 HBAR )
                .setMaxTransactionFee(200_000_000) // 2 HBAR
                .execute(client);

        TransactionReceipt receipt = txId.getReceipt(client);
        FileId newFileId = receipt.getFileId();

        System.out.println("file: " + newFileId);
    }

    // #################### GET FILE CONTENTS ####################

    public static void getFileContents() throws HederaStatusException {
        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Content to be stored in the file
        byte[] fileContents = ("Hedera is great!").getBytes();

        // Create the new file and set its properties
        TransactionId newFileTxId = new FileCreateTransaction()
                .addKey(OPERATOR_KEY.publicKey) // The public key of the owner of the file
                .setContents(fileContents) // Contents of the file
                .setMaxTransactionFee(new Hbar(2))
                .execute(client);

        FileId newFileId = newFileTxId.getReceipt(client).getFileId();

        //Print the file ID to console
        System.out.println("The new file ID is " + newFileId.toString());

        // Get file contents
        byte[] contents = new FileContentsQuery()
                .setFileId(newFileId)
                .execute(client);

        // Prints query results to console
        System.out.println("File content query results: " + new String(contents));
    }

    public static void deleteFile() throws HederaStatusException {
        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // The file is required to be a byte array,
        // you can easily use the bytes of a file instead.
        byte[] fileContents = "Hedera hashgraph is great!".getBytes();

        TransactionId txId = new FileCreateTransaction()
                .addKey(OPERATOR_KEY.publicKey)
                .setContents(fileContents)
                .setMaxTransactionFee(new Hbar(2))
                .execute(client);

        TransactionReceipt receipt = txId.getReceipt(client);
        FileId newFileId = receipt.getFileId();

        System.out.println("file: " + newFileId);

        // now delete the file
        TransactionId fileDeleteTxnId = new FileDeleteTransaction()
                .setFileId(newFileId)
                .execute(client);

        // if this doesn't throw then the transaction was a success
        fileDeleteTxnId.getReceipt(client);

        System.out.println("File deleted successfully.");

        FileInfo fileInfo = new FileInfoQuery()
                .setFileId(newFileId)
                .execute(client);

        // note the above fileInfo will fail with FILE_DELETED due to a known issue on Hedera

    }

}
