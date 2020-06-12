module de.isiko {
    requires javafx.controls;
    requires javafx.fxml;
    requires sdk;
    requires java.dotenv;
    requires org.bouncycastle.provider;
    requires com.google.gson;
    requires com.google.protobuf;
    requires annotations;

    opens de.isiko to javafx.fxml;
    exports de.isiko;
}