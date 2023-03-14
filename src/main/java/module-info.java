module com.example.excel2fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.commons.lang3;


    opens com.example.excel2fx to javafx.fxml;
    exports com.example.excel2fx;
}