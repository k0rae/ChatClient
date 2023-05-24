package chat.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class ChatViewController {
    @FXML
    public ScrollPane chatScroll;
    @FXML
    public ScrollPane usersScroll;
    @FXML
    public TextField messageInput;
    @FXML
    public Button sendButton;
    @FXML
    public Button logoutButton;
    @FXML
    public AnchorPane anchorPane;
    @FXML
    public VBox vbox;
    @FXML
    public VBox messageContainer;
    @FXML
    public VBox usersContainer;

    @FXML
    public void initialize() {
        vbox.setMaxWidth(Double.MAX_VALUE);
        sendButton.setOnAction(event -> sendMessage());
        logoutButton.setOnAction(actionEvent -> ChatApplication.LogOut());
    }
    public void addMessage(String message) {
        messageContainer.getChildren().add(new Label(message));
    }
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            String command = "<command name=\"message\"><message>" + message + "</message><session>" + SocketClient.uuid + "</session></command>\n";
            SocketClient.SendMessage(command);
            messageInput.clear();
        }
    }
    public void updateUsers(String userListXML) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(userListXML));
            Document document = builder.parse(inputSource);

            Element listUsersElement = document.getDocumentElement();
            NodeList userNodes = listUsersElement.getElementsByTagName("user");

            usersContainer.setSpacing(10);

            usersContainer.getChildren().clear();
            for (int i = 0; i < userNodes.getLength(); i++) {
                Element userElement = (Element) userNodes.item(i);
                String userName = userElement.getElementsByTagName("name").item(0).getTextContent();
                String userType = userElement.getElementsByTagName("type").item(0).getTextContent();

                Label userLabel = new Label(userName + " (" + userType + ")");
                usersContainer.getChildren().add(userLabel);
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}
