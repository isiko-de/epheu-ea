package de.isiko;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import de.isiko.hedera.Account;
import de.isiko.hedera.Transfer;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Controller {

    // ########## FXML GUI LINKS ##########

    // Operator
    @FXML
    private TextField operatorIdTf;
    @FXML
    private TextField operatorKeyTf;

    // Node
    @FXML
    private TextField nodeIdTf;
    @FXML
    private TextField nodeAddressTf;


    // Create account
    @FXML
    private TextField initialBalanceTf;
    @FXML
    private TextArea createAccountTa;
    @FXML
    private TextField accountExpirationTimeTf;

    // Update account
    @FXML
    private TextField setProxyAccountAccountIdTf;
    @FXML
    private TextField setNewKeyForAccountTf;
    @FXML
    private TextField setSendRecordThresholdTf;
    @FXML
    private TextField setReceiveRecordThresholdTf;
    @FXML
    private TextField setAutoRenewPeriodTf;
    @FXML
    private TextField setExpirationTimeTf;

    @FXML
    private TextField accountToBeChangedId;

    @FXML
    private TextField accountToBeChangedPrivateKey;



    // Transfer crypto currency
    @FXML
    private TextField recipientIdTf;
    @FXML
    private TextField amountToSendTf;

    // Delete account
    @FXML
    private TextField newOwnerOfHbarsTf;
    @FXML
    private TextField accountToDeleteTf;
    @FXML
    private TextField accountToDeletePrivateKeyTf;

    // Get account infos
    @FXML
    private TextField accountInfoQueryIdTf;
    @FXML
    private TextArea accountInfoQueryResultsTa;

    // Get account records
    @FXML
    private TextField accountRecordsQueryIdTf;
    @FXML
    private TextArea accountRecordsQueryResultsTa;
    @FXML
    private TextArea globalConsoleTA;


    //////////// File Managing

    @FXML
    private TextArea createFileContentsTa;
    @FXML
    private TextField createFileExpirationTimeTf;
    @FXML
    private TextField fileIdOfFileToAppendTf;
    @FXML
    private TextArea bytesToAppendToContentsOfFileTa;
    @FXML
    private TextField setIdOfToBeUpdatedFileTf;
    @FXML
    private TextField updateExpirationTimeTf;
    @FXML
    private TextArea updatedContentsOfFileTa;
    @FXML
    private TextField fileToDeleteTf;
    @FXML
    private TextField fileToGetContentsFromTf;
    @FXML
    private TextArea fileContentQueryResultsTa;
    @FXML
    private TextField fileToGetInfosFromTf;
    @FXML
    private TextArea fileInfoQueryResultsTa;


    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss | ");
    private LocalDateTime now = LocalDateTime.now();


    // ########## LOGIC PROPERTIES ##########


    // the exchange should possess this key, we're only generating it for demonstration purposes
    private static final Ed25519PrivateKey exchangeKey = Ed25519PrivateKey.generate();

    // this is the only key we should actually possess
    private static final Ed25519PrivateKey userKey = Ed25519PrivateKey.generate();

    private static Client client;

    static {
        // To improve responsiveness, you should specify multiple nodes using the
        // `Client(<Map<AccountId, String>>)` constructor instead
        client = new Client(new HashMap<AccountId, String>() {
            {
                put(AccountId.fromString("0.0.3"), "0.testnet.hedera.com:50211");
                put(AccountId.fromString("0.0.4"), "1.testnet.hedera.com:50211");
                put(AccountId.fromString("0.0.5"), "2.testnet.hedera.com:50211");
                put(AccountId.fromString("0.0.6"), "3.testnet.hedera.com:50211");

            }
        });
    }

    // ----- EVENT HANDLERS -----

    @FXML
    private void connectToAccount(ActionEvent actionEvent) throws HederaStatusException {
        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(
                AccountId.fromString(Objects.requireNonNull(operatorIdTf.getText())),
                Ed25519PrivateKey.fromString(Objects.requireNonNull(operatorKeyTf.getText()))
        );
        System.out.println();
    }

    @FXML
    private void createAccount(ActionEvent actionEvent) throws HederaStatusException {

        long initialBalance = Long.parseLong(initialBalanceTf.getText());
        Account.createAccount(client, initialBalance);
    }

    @FXML
    private void setProxyAccount(ActionEvent actionEvent) {

    }

    @FXML
    private void setKeyForAccount(ActionEvent actionEvent) {

    }

    @FXML
    private void setSendRecordThreshold(ActionEvent actionEvent) {

    }

    @FXML
    private void setReceiveRecordThreshold(ActionEvent actionEvent) {

    }

    @FXML
    private void setAutoRenewPeriod(ActionEvent actionEvent) {

    }

    @FXML
    private void setExpirationTime(ActionEvent actionEvent) {

    }

    @FXML
    private void sendTinybars(ActionEvent actionEvent) throws HederaStatusException {
        AccountId recipientId = AccountId.fromString(recipientIdTf.getText());
        Transfer.transferCrypto(client,recipientId,AccountId.fromString(operatorIdTf.getText()));
    }

    @FXML
    private void deleteAccount(ActionEvent actionEvent) {

        AccountId operatorId = AccountId.fromString(operatorIdTf.getText());
        AccountId accountToDelete = AccountId.fromString(accountToDeleteTf.getText());
        PrivateKey accountToDeletePrivateKey = Ed25519PrivateKey.fromString(accountToDeletePrivateKeyTf.getText());

        try {
            Account.deleteAccount(client, operatorId, accountToDelete, accountToDeletePrivateKey);
        } catch (HederaStatusException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void accountInfoQuery(ActionEvent actionEvent) {

    }

    @FXML
    private void accountRecordsQuery(ActionEvent actionEvent) {

    }

    @FXML
    private void checkSenderBalance(ActionEvent actionEvent) {

    }

    @FXML
    private void checkRecipientBalance(ActionEvent actionEvent) {

    }

    @FXML
    private void createFile(ActionEvent actionEvent) {

    }

    @FXML
    private void appendToFile(ActionEvent actionEvent) {

    }

    @FXML
    private void updateFile(ActionEvent actionEvent) {

    }

    @FXML
    private void deleteFile(ActionEvent actionEvent) {

    }

    @FXML
    private void getFileContents(ActionEvent actionEvent) {

    }

    @FXML
    private void getFileInfo(ActionEvent actionEvent) {

    }
}
