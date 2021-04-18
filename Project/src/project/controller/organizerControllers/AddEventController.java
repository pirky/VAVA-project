package project.controller.organizerControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import project.controller.Main;
import project.model.CustomImage;
import project.model.Rooms.LibraryRoom;
import project.model.Rooms.RoomReservation;
import project.model.books.BookReservation;
import project.model.events.BookDiscussion;
import project.model.events.BookExchange;
import project.model.events.Event;
import project.model.users.Organizer;
import project.model.users.Reader;
import project.model.users.User;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

public class AddEventController {
    ObservableList<LibraryRoom> allRooms = FXCollections.observableArrayList();
    ObservableList<ImageView> roomImages = FXCollections.observableArrayList();
    @FXML
    private ListView<ImageView> listView;
    @FXML
    private ToggleGroup group;
    @FXML
    private ComboBox<LibraryRoom> comboBox;
    @FXML
    private TextArea hostArea;
    @FXML
    private TextArea nameArea;
    @FXML
    private TextArea noteArea;
    @FXML
    private DatePicker datePickerFrom;
    @FXML
    private DatePicker datePickerTo;
    @FXML
    private Label hostLabel;
    @FXML
    private Label infoLabel;
    @FXML
    private Button btn;
    @FXML
    private RadioButton exchangeRB;
    @FXML
    private RadioButton discussionRB;

    @FXML
    public void initialize(){
        infoLabel.setVisible(false);
        hostLabel.setVisible(false);
        hostArea.setVisible(false);
        comboBox.setDisable(true);
        nameArea.setDisable(true);
        datePickerFrom.setDisable(true);
        datePickerTo.setDisable(true);
        noteArea.setDisable(true);
        btn.setDisable(true);
        allRooms.addAll(Main.roomsDatabase.getRooms());
        comboBox.setItems(allRooms);
    }

    public void radioClicked(){
        comboBox.setDisable(false);
        nameArea.setDisable(false);
        noteArea.setDisable(false);
        hostLabel.setVisible(true);
        hostArea.setVisible(true);
        if(exchangeRB.isSelected()){
            hostLabel.setVisible(false);
            hostArea.setVisible(false);
        }
        else if(discussionRB.isSelected()){
            hostLabel.setVisible(true);
            hostArea.setVisible(true);
        }
    }

    public void updateDate(int roomId, LocalDate dateSelected){

        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
            LocalDate dateBlock = null;
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate dateCurr, boolean empty) {
                        super.updateItem(dateCurr, empty);
                        LocalDate now = Main.booksDatabase.getDate();
                        if(dateCurr.compareTo(now) < 0){
                            setDisable(dateCurr.compareTo(now) < 0);
                            return;
                        }

                        for(User user: Main.userDatabase.getUserDatabase()){
                            if (!(user instanceof Organizer)){
                                continue;
                            }
                            Organizer organizer = (Organizer) user;
                            for(Event event: organizer.getEvents()){
                                if(event.getReservation().getRoomId() == roomId){
                                    LocalDate dateFrom = event.getReservation().getDateFrom();
                                    LocalDate dateTo = event.getReservation().getDateTo();
                                    if ((dateCurr.isAfter(dateFrom) && dateCurr.isBefore(dateTo)) || dateCurr.equals(dateFrom) || dateCurr.equals(dateTo)) {
                                        setStyle("-fx-background-color: #ff4444;");
                                        setDisable(true);
                                        dateBlock = dateCurr;
                                    }
                                    if (dateSelected != null && (dateCurr.isBefore(dateSelected))){
                                        setDisable(true);
                                    }
                                    if (dateSelected != null && dateBlock != null && dateSelected.isBefore(dateBlock) && dateCurr.isAfter(dateBlock)){
                                        setDisable(true);
                                    }
                                }
                                else{
                                    if (dateSelected!= null && (dateCurr.isBefore(dateSelected))){
                                        setDisable(true);
                                    }
                                }
                            }
                        }
                    }
                };
            }
        };
        if (dateSelected == null){
            datePickerFrom.setDayCellFactory(dayCellFactory);
            datePickerFrom.setDisable(false);
            datePickerTo.setDisable(true);
        }
        else {
            datePickerTo.setDayCellFactory(dayCellFactory);
            datePickerTo.setDisable(false);
        }
    }

    public void showDatesFrom(){
        LibraryRoom room = comboBox.getValue();
        if (room == null){
            return;
        }
        listView.getItems().clear();
        for(CustomImage image: room.getImages()){
            ImageView imageView = new ImageView(image.getImage());
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(523);
            roomImages.add(imageView);
        }
        listView.setItems(roomImages);
        listView.refresh();
        datePickerFrom.setValue(null);
        datePickerTo.setValue(null);
        updateDate(room.getId(), null);
    }

    public void showDatesTo(){
        LibraryRoom room = comboBox.getValue();
        if (room == null){
            return;
        }
        datePickerTo.setValue(null);
        LocalDate dateFrom = datePickerFrom.getValue();
        updateDate(room.getId(), dateFrom);
    }

    public void showBtn(){
        btn.setDisable(false);
    }

    public void createEvent(){
        LibraryRoom room = comboBox.getValue();
        String name = nameArea.getText();
        String host;
        if(discussionRB.isSelected()){
            host = hostArea.getText();
        }
        else {
            host = "nothing";
        }
        String note = noteArea.getText();
        if(testRequired(name, host, note)){
            return;
        }
        LocalDate dateFrom = datePickerFrom.getValue();
        LocalDate dateTo = datePickerTo.getValue();

        Main.userDatabase.removeUser(Main.currUser);
        Organizer organizer = (Organizer) Main.currUser;
        if(exchangeRB.isSelected()){
            BookExchange bookExchange = new BookExchange(name, note, new RoomReservation(dateFrom, dateTo, room.getId()),(Organizer) Main.currUser);
            organizer.addEvent(bookExchange);
        }
        else if(discussionRB.isSelected()){
            BookDiscussion bookDiscussion = new BookDiscussion(name, note, new RoomReservation(dateFrom, dateTo, room.getId()),(Organizer) Main.currUser, host);
            organizer.addEvent(bookDiscussion);
        }
        Main.userDatabase.addUser(organizer);
        Main.currUser = organizer;

        deleteFields();
    }

    private boolean testRequired(String name, String host, String note){
        if(name.isEmpty() || host.isEmpty() || note.isEmpty()){
            infoLabel.setText("Vyplň všetky polia");
            infoLabel.setVisible(true);
            return true;
        }

        infoLabel.setVisible(false);
        return false;
    }

    private void deleteFields(){
        exchangeRB.setSelected(false);
        discussionRB.setSelected(false);
        comboBox.getSelectionModel().clearSelection();
        nameArea.clear();
        hostArea.clear();
        datePickerFrom.setValue(null);
        datePickerTo.setValue(null);
        noteArea.clear();
        listView.getItems().clear();
        listView.refresh();

        hostLabel.setVisible(false);
        hostArea.setVisible(false);
        comboBox.setDisable(true);
        nameArea.setDisable(true);
        datePickerFrom.setDisable(true);
        datePickerTo.setDisable(true);
        noteArea.setDisable(true);
        btn.setDisable(true);
    }

    public void showMenu() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("/project/view/organizerViews/OrganizerView.fxml")));
        Scene scene = new Scene(root);
        Main.mainStage.setScene(scene);
        Main.mainStage.show();
    }
}