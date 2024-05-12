module com.justbelieveinmyself.mocf {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens com.justbelieveinmyself.mocf to javafx.fxml;
    exports com.justbelieveinmyself.mocf;
}