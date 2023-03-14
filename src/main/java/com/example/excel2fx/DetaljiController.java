package com.example.excel2fx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class DetaljiController {

    @FXML
    private Label firstCPLabel;
    @FXML
    private Label lastCPLabel;
    @FXML
    private TableView<CodeAndPrice> tableViewCP;
    @FXML
    private TableColumn<CodeAndPrice, String> codeColumn;
    @FXML
    private TableColumn<CodeAndPrice, String> priceColumn;


    @FXML
    public void displaySheetDetails(SheetC sheetC, List<CodeAndPrice> codeAndPriceList){


        codeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty( cellData.getValue().getCode().toPlainString()));

        priceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty( cellData.getValue().getPrice().toPlainString() ));

        tableViewCP.setItems(FXCollections.observableList(codeAndPriceList));


        firstCPLabel.setText(codeAndPriceList.get(0).toString());

        lastCPLabel.setText(codeAndPriceList.get(codeAndPriceList.size()-1).toString());


    }

}
