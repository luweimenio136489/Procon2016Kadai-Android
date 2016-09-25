package mksource;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("layout/main.fxml"));
        primaryStage.setTitle("ソースリスト作るよ");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("icons.png")));
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(600);
//        primaryStage.setMaxHeight(400);
//        primaryStage.setMaxWidth(600);


        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


}
