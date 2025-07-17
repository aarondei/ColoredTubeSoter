package com.example.coloredtubesorter.Logic.Simulation;

import com.example.coloredtubesorter.Elements.Tube;
import com.example.coloredtubesorter.Logic.Pourable;
import com.example.coloredtubesorter.ShellController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simulator implements Pourable {
    private static Simulator instance;

    private final List<Tube> origTubeList;
    private List<Tube> tubeList = new ArrayList<>();
    private final String moveHistory;

    private Simulator(List<Tube> origTubeList, String moveHistory) {
        this.origTubeList = origTubeList;
        this.moveHistory = moveHistory;
    }

    public static Simulator getInstance(List<Tube> tubeList, String moveHistory) {
        if (instance == null) {
            instance = new Simulator(tubeList, moveHistory);
        }
        return instance;
    }

    private void setupGUI(AnchorPane ap) {

        if (!tubeList.isEmpty()) tubeList.clear();

        Map<String, Double> layoutSetting = new HashMap<>();
        configTubeLayout(layoutSetting);

        for (int i = 0; i < origTubeList.size(); i++) {

            // deep copy
            tubeList.add(origTubeList.get(i).cloneTube());

            Tube copy = tubeList.get(i);
            setTubeDesign(copy);
            setTubeLayout(copy, layoutSetting);
            setTubeLabel(copy, ap);

            ap.getChildren().add(copy.getContainer());
        }
    }

    public void simulate(AnchorPane apMainPane, ShellController controller) {

        setupGUI(apMainPane);

        int[] i = {0};
        String[] history = moveHistory.split("\n");

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> {
                    if (i[0] < history.length) {
                        controller.track(i[0]);
                        String str = history[i[0]];
                        List<Integer> move = decryptMove(str);
                        applyMove(move.getFirst(), move.getLast());
                        i[0]++;
                    }
                })
        );
        timeline.setCycleCount(history.length);
        timeline.play();
    }
    public void restart() {

    }

    private List<Integer> decryptMove(String str) {
        String[] raw = str.substring(str.indexOf(":") + 2).split("[^0-9]+");
        List<Integer> moves = new ArrayList<>();

        try {
            moves.add(Integer.parseInt(raw[1]));
            moves.add(Integer.parseInt(raw[2]));
        } catch (NumberFormatException e) {
            System.err.println("Attempting to decrypt: " + raw[1]);
            System.err.println("Attempting to decrypt: " + raw[2]);
        }

        return moves;
    }
    private void applyMove(int fromID, int toID) {

        Tube from = null, to = null;
        for (Tube t : tubeList) {
            if (t.getName() == fromID) from = t;
            if (t.getName() == toID) to = t;
            if (from != null && to != null) break;
        }

        if (from == null || to == null) throw new RuntimeException("Simulated tubes are null.");

        pour(from, to);
    }

    private void configTubeLayout(Map<String, Double> layoutSettings) {
        layoutSettings.put("paneWidth", 800.0);
        layoutSettings.put("spacing", 80.0);
        layoutSettings.put("rowSpacing", 150.0);
        layoutSettings.put("startX", 20.0);
        layoutSettings.put("startY", 50.0);
        layoutSettings.put("x", layoutSettings.get("startX"));
        layoutSettings.put("y", layoutSettings.get("startY"));
    }
    private void setTubeDesign(Tube tube) {

        // GUI
        VBox container = tube.getContainer();

        container.setSpacing(2);
        container.setPrefSize(60, 120);
        container.setAlignment(Pos.BOTTOM_CENTER);

        highlightTube(tube, false);
    }
    private void setTubeLayout(Tube tube, Map<String, Double> layoutSetting) {
        // GUI
        // row warp
        if (layoutSetting.get("x") + 60 > layoutSetting.get("paneWidth")) {
            layoutSetting.put("x", layoutSetting.get("startX"));
            layoutSetting.put("y", layoutSetting.get("y") + layoutSetting.get("rowSpacing"));
        }
        tube.getContainer().setLayoutX(layoutSetting.get("x"));
        tube.getContainer().setLayoutY(layoutSetting.get("y"));

        layoutSetting.put(("x"), layoutSetting.get("x") + layoutSetting.get("spacing"));
    }
    private void setTubeLabel(Tube tube, AnchorPane apMainPane) {

        Text label = new Text();
        label.setText(String.valueOf(tube.getName()));
        label.setLayoutX(tube.getContainer().getLayoutX());
        label.setLayoutY(tube.getContainer().getLayoutY());
        label.setFill(Color.GRAY);
        apMainPane.getChildren().add(label);
    }

    public void highlightTube(Tube tube, boolean highlight) {

        if (highlight) {
            tube.getContainer().setStyle("""
                -fx-background-color: linear-gradient(to bottom, #e0e0e0cc, #ffffff33);
                -fx-border-color: #0ADD08;
                -fx-border-width: 4;
                -fx-border-radius: 15;
                -fx-background-radius: 15;
            """);
        } else {
            tube.getContainer().setStyle("""
                -fx-background-color: linear-gradient(to bottom, #e0e0e0cc, #ffffff33);
                -fx-border-color: #999;
                -fx-border-width: 4;
                -fx-border-radius: 15;
                -fx-background-radius: 15;
            """);
        }
    }
}
