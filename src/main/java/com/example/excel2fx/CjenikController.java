package com.example.excel2fx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class CjenikController {

    Optional<String> pathToOpenString = Optional.empty();
    Optional<String> pathToSaveString = Optional.empty();
    Optional<String> priceCellNameString = Optional.empty();

    StringBuilder errorMessage = new StringBuilder();
    StringBuilder badSheetsNoCell = new StringBuilder();
    StringBuilder badSheets = new StringBuilder();
    StringBuilder sheetsWithMultiplePriceCells = new StringBuilder();


    Map<SheetC, List<CodeAndPrice>> sheetsWithCPMap = new LinkedHashMap<>();


    @FXML
    private TextField priceCellNameField;
    @FXML
    private Label fileNameLabel;
    @FXML
    private TableView<SheetC> sheetTableView;
    @FXML
    private TableColumn<SheetC, String> sheetNameColumn;
    @FXML
    private TableColumn<SheetC, String> sheetNumColumn;
    @FXML
    private Button saveNewExcelFileButton;
    @FXML
    private Label totalLabel;

    private boolean checkIfAnySheetHasZeroPairs() {

        //vrati true ako posoji jedan sheet gdje ima 0 parova
        for (SheetC key: sheetsWithCPMap.keySet()){
            if (key.numOfPairs.equals(0)) {
                return true;
            }
        }

        return false;
    }

    private Integer checkForPricingCellNames() {

        //vrati 1 ako svi sheetovi imaju celiju s cijenama
        //vrati 0 ako neki od sheetova nema celiju s cijenama
        //vrati -1 ako niti jedan sheet nema taj naziv celije s cijenama
        //vrati -2 ako sheet ima vise istih celija s cijenom

        badSheetsNoCell = new StringBuilder();
        sheetsWithMultiplePriceCells = new StringBuilder();

        Integer numberOfAllSheets = 0;
        Integer numberOfSheetMatched = 0;

        //ako je prisutna putanja do datoteke i ime celije s cijenama 1
        if (pathToOpenString.isPresent() && priceCellNameString.isPresent()) {

            Optional<XSSFWorkbook> workbook = Optional.empty();
            Optional<Iterator<Sheet>> sheetIterator;

            try {
                workbook = Optional.of(new XSSFWorkbook(new FileInputStream(pathToOpenString.get())));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(workbook.isPresent()){

                sheetIterator = Optional.of( workbook.get().sheetIterator());

                //prolazak po svim sheetovima
                while (sheetIterator.get().hasNext()) {

                    numberOfAllSheets++;

                    Integer tmpNumberOfSheetMatched = numberOfSheetMatched;
                    int tmpNumberOfPriceCells = 0;
                    Sheet sheet = sheetIterator.get().next();

                    for (Row row1 : sheet) {
                        for (Cell cell : row1) {
                            if (cell.toString().equals(priceCellNameString.get())) {
                                //ako je pronadjeno ime celije s cijenom uvecaj numberOfSheetMatched
                                tmpNumberOfPriceCells++;
                                numberOfSheetMatched++;
                                //break;
                            }
                        }
                    }

                    if(tmpNumberOfPriceCells > 1){
                        sheetsWithMultiplePriceCells.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                    }

                    if (tmpNumberOfSheetMatched.equals(numberOfSheetMatched)) {
                        //prikazi sheetove koji nemaju ime celije s cijenama
                        badSheetsNoCell.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                    }

                }

                try {
                    workbook.get().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //povratne vrijednosti
        if (numberOfAllSheets.equals(numberOfSheetMatched)) {
            return 1;
        }
        else if (numberOfSheetMatched == 0) {
            return -1;
        }
        else if(!sheetsWithMultiplePriceCells.isEmpty()){
            System.out.println(sheetsWithMultiplePriceCells);
            return -2;
        }
        else {
            return 0;
        }

    }


    private Map<SheetC, List<CodeAndPrice>> getAllPairsFromExcel() {

        Map<SheetC, List<CodeAndPrice>> sheetsWithCPMap = new HashMap<>();

        //sheetList = new ArrayList<>();
        badSheets = new StringBuilder();

        //ako je prisutna putanja do datoteke i ime celije s cijenama 1
        if (pathToOpenString.isPresent() && priceCellNameString.isPresent()) {

            Optional<XSSFWorkbook> workbook = Optional.empty();
            Optional<Iterator<Sheet>> sheetIterator;

            Optional<Integer> columnWithPrices = Optional.empty();
            Optional<BigDecimal> code = Optional.empty();
            Optional<BigDecimal> price = Optional.empty();

            boolean columnWithPricesFound;

            try {
                workbook = Optional.of(new XSSFWorkbook(new FileInputStream(pathToOpenString.get())));
            } catch (IOException e) {
                e.printStackTrace();
            }


            if(workbook.isPresent()){
                sheetIterator = Optional.of(workbook.get().sheetIterator());


                while (sheetIterator.get().hasNext()) {

                    List<CodeAndPrice> tmpListCP = new ArrayList<>();

                    Integer numberOfPairs = 0;
                    Sheet sheet = sheetIterator.get().next();
                    columnWithPricesFound = false;

                    System.out.println();
                    System.out.println(sheet.getSheetName() + "\n\n");

                    for (Row row1 : sheet) {
                        for (Cell cell : row1) {
                            if (cell.toString().equals(priceCellNameString.get())) {
                                columnWithPrices = Optional.of(cell.getColumnIndex());
                                columnWithPricesFound = true;
                                break;
                            }
                        }
                    }


                    if (columnWithPricesFound) {
                        for (Row row : sheet) {
                            if (row.getCell(0) != null && row.getCell(columnWithPrices.get()) != null) {
                                try {

                                    String codeS = row.getCell(0).toString();
                                    codeS = codeS.replaceAll("[^a-zA-Z0-9.\\\\s]+","");
                                    code = Optional.of(BigDecimal.valueOf(Double.parseDouble(codeS)));

                                    BigDecimal priceBD = BigDecimal.valueOf(Double.parseDouble(row.getCell(columnWithPrices.get()).toString()));
                                    priceBD = priceBD.setScale(2, RoundingMode.HALF_EVEN);
                                    price = Optional.of(priceBD);

                                    System.out.println(codeS + " | " + row.getCell(columnWithPrices.get()));

                                } catch (NumberFormatException e) {
                                    continue;
                                }
                            }


                            if (code.isPresent() && price.isPresent()) {
                                //codeAndPriceList.add(new CodeAndPrice(code.get(), price.get()));


                                tmpListCP.add(new CodeAndPrice(code.get(), price.get()));


                                numberOfPairs++;
                            }
                            code = Optional.empty();
                            price = Optional.empty();

                        }
                    }


                    if (numberOfPairs.equals(0)) {
                        badSheets.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                    }

                    //sheetList.add(new SheetC(sheet.getSheetName(), numberOfPairs));

                    /*test*/
                    sheetsWithCPMap.put(new SheetC(sheet.getSheetName(), numberOfPairs), tmpListCP);
                    /*test*/

                }


                try {
                    workbook.get().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return sheetsWithCPMap;
        } else {
            return new HashMap<>();
        }

    }


    @FXML
    private void writeNewCPExcel() {

        //otvara prozor za spremanje nove excel datoteke
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Microsoft Excel Worksheet (.xlsx)", "*.xlsx");
        fileChooser.setTitle("Odaberi excel datoteku");
        fileChooser.getExtensionFilters().add(extFilter);

        Optional<File> file = Optional.ofNullable(fileChooser.showSaveDialog(null));

        //spremanje putanje za novu datoteku
        file.ifPresent(value -> pathToSaveString = Optional.of(value.getAbsolutePath()));

        //novi excel file
        Workbook workbookForOutput = new XSSFWorkbook();

        //novi sheet
        Sheet sheet = workbookForOutput.createSheet("SifreICijene");

        int rowNum = 0;

        //zapisuje sve CP objekte u workbook

        for (SheetC key: sheetsWithCPMap.keySet()){
            for( CodeAndPrice codeAndPrice : sheetsWithCPMap.get(key) ){
                Row row = sheet.createRow(rowNum);
                rowNum++;
                row.createCell(0).setCellValue(codeAndPrice.getCode().toPlainString());
                row.createCell(1).setCellValue(codeAndPrice.getPrice().toPlainString());
            }
        }


        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        //spremanje nove excel datoteke
        Optional<FileOutputStream> fileOutputStream = Optional.empty();
        try {
            if (pathToSaveString.isPresent()) {
                fileOutputStream = Optional.of(new FileOutputStream(pathToSaveString.get()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (fileOutputStream.isPresent()) {
                workbookForOutput.write(fileOutputStream.get());
                fileOutputStream.get().close();
            }
            workbookForOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void getPath() {

        //otvara prozor za odabir excel datoteke
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Microsoft Excel Worksheet (.xlsx)", "*.xlsx");
        fileChooser.setTitle("Odaberi excel datoteku");
        fileChooser.getExtensionFilters().add(extFilter);


        Optional<File> file = Optional.ofNullable(fileChooser.showOpenDialog(null));

        //dohvacanje putanje i imena datoteke
        file.ifPresent(value -> pathToOpenString = Optional.of(value.getAbsolutePath()));
        file.ifPresent(value -> fileNameLabel.setText(value.getName()));
    }

    private void showError() {
        sheetTableView.getItems().clear();
        totalLabel.setText("");

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setHeaderText("Greška!");
        alert.setContentText(errorMessage.toString());
        alert.showAndWait();

        errorMessage = new StringBuilder();
    }

    @FXML
    private void showNewScreen() throws IOException {

        Optional<SheetC> sheet = Optional.ofNullable(sheetTableView.getSelectionModel().getSelectedItem()) ;


        if(sheet.isPresent()){
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(Cjenik.class.getResource("detalji.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 500);

            DetaljiController detaljiController = fxmlLoader.getController();


            detaljiController.displaySheetDetails(sheet.get(), sheetsWithCPMap.get(sheet.get()));
            stage.setTitle("Detalji sheeta " + sheet.get().getName());

            stage.setScene(scene);
            stage.show();

            stage.setX(stage.getX()+100);
            stage.setY(stage.getY()+100);
        }



    }


    @FXML
    private void createNewCPList() {

        //onemoguci spremanje nove excel datoteke
        saveNewExcelFileButton.setDisable(true);

        //provjera postoji li putanja do Excel datoteke
        if (pathToOpenString.isEmpty()) {
            errorMessage.append("Datoteka nije odabrana!\n");
            saveNewExcelFileButton.setDisable(false);
        }

        priceCellNameString = Optional.of(priceCellNameField.getText());
        Integer match = checkForPricingCellNames();


        //provjera postoji li unesen naziv celije s cijenama
        if (priceCellNameString.get().equals("")) {
            //ako naziv nije unesen, ispisi error
            errorMessage.append("Celija s cijenama nije upisana!\n");

        } else {

            //ako je unesen, provjeri ima li gresaka
            switch (match) {
                case 0 -> {
                    errorMessage.append("Neki od sheetova ima drugacije ime celije ili postoji prazan sheet!\n\n");
                    errorMessage.append(badSheetsNoCell);
                }
                case -1 -> errorMessage.append("Ne postoji niti jedan sheet s tim nazivom celije!");
                case -2 -> {
                    errorMessage.append("Neki od sheetova ima vise celija s cijenom pod istim nazivom!\n\n");
                    errorMessage.append(sheetsWithMultiplePriceCells);
                }
            }

        }


        //provjeri postoji li unesena putanja do datoteke,
        //naziv celije s cijenama i ako je ukupan broj sheetova jednak
        //broju sheetova gdje je nadjena celija s cijenom
        if (pathToOpenString.isPresent() && !priceCellNameString.get().equals("") && match == 1 ) {

            //ispraznjava prikaz tablice
            sheetTableView.getItems().clear();

            //prikuplja listu CP
            //codeAndPriceList = getAllCPFromExcel();
            sheetsWithCPMap = getAllPairsFromExcel();

            //provjera postoji li sheet bez parova CP
            //prikaz greske i sheeta kojem je celija s cijenom drugacija od zadane

            boolean anyZero = checkIfAnySheetHasZeroPairs();

            System.out.println(anyZero);

            if (!anyZero) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Uspjeh");
                alert.setHeaderText("Uspjeh!");
                alert.setContentText("Novi cjenik uspješno je napravljen!");
                alert.showAndWait();

                sheetNameColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getName()));

                sheetNumColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getNumOfPairs().toString()));


                List<SheetC> sheetsToDisplay = new ArrayList<>(sheetsWithCPMap.keySet());


                sheetTableView.setItems(FXCollections.observableList(sheetsToDisplay));

                int total = 0;

                for (SheetC key: sheetsWithCPMap.keySet()){
                    for( CodeAndPrice ignored : sheetsWithCPMap.get(key) ){
                        total++;
                    }
                }

                totalLabel.setText( "Total: " + total);
                saveNewExcelFileButton.setDisable(false);

                for (SheetC key: sheetsWithCPMap.keySet()){
                    for( CodeAndPrice cp : sheetsWithCPMap.get(key) ){
                        System.out.println(key.getName() + " - " + cp);
                    }
                }

            }
            //postoji sheet bez parova
            else {
                errorMessage.append("Neki od sheetova nema parove. Može biti greška jer nisu zapisane vrijednosti nego formula!\n\n");
                errorMessage.append(badSheets);
                showError();
            }
        } else {
            showError();
        }
    }


    @FXML
    private void initialize(){
        fileNameLabel.setText("datoteka nije odabrana");
        totalLabel.setText("");
        saveNewExcelFileButton.setDisable(true);
    }

}