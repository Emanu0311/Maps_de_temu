package com.ud;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.awt.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;

public class AppView {

    // INSTANCE is typed as GraphicFactory (the interface), which AwtGraphicFactory implements
    private static final GraphicFactory GF = AwtGraphicFactory.INSTANCE;

    // ── JavaFX controls ──────────────────────────────────────────────────────
    private final Scene scene;
    private final ComboBox<String> strategySelector;
    private final Button calcButton;
    private final Button resetButton;
    private final TextField originField;
    private final TextField destinationField;

    // ── Mapsforge state ──────────────────────────────────────────────────────
    private final MapView mapView;
    private Circle startCircle;
    private Circle endCircle;
    private Polyline routePolyline;

    // ────────────────────────────────────────────────────────────────────────
    public AppView(BiConsumer<Double, Double> onMapClick) {
        BorderPane root = new BorderPane();

        // ── Top control panel ────────────────────────────────────────────────
        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(15));

        HBox selectors = new HBox(15);
        strategySelector = new ComboBox<>();
        strategySelector.getItems().addAll("Carro", "Moto", "Bicicleta", "A pie");
        strategySelector.setValue("Carro");
        calcButton = new Button("Calcular Ruta");
        selectors.getChildren().addAll(new Label("Vehículo:"), strategySelector, calcButton);

        HBox coords = new HBox(15);
        originField = new TextField();
        originField.setPromptText("Lat, Lng");
        originField.setEditable(false);
        destinationField = new TextField();
        destinationField.setPromptText("Lat, Lng");
        destinationField.setEditable(false);
        resetButton = new Button("Limpiar Puntos");
        coords.getChildren().addAll(
                new Label("Origen:"), originField,
                new Label("Destino:"), destinationField,
                resetButton);

        topPanel.getChildren().addAll(
                new Label("Haz clic en el mapa para seleccionar Origen y luego Destino."),
                selectors,
                coords);
        root.setTop(topPanel);

        // ── Mapsforge map ────────────────────────────────────────────────────
        mapView = new MapView();
        mapView.getMapScaleBar().setVisible(true);

        File mapFile = findMapFile();
        if (mapFile != null) {
            MapDataStore mapDataStore = new MapFile(mapFile);

            TileCache tileCache = new InMemoryTileCache(64);

            TileRendererLayer tileLayer = new TileRendererLayer(
                    tileCache,
                    mapDataStore,
                    mapView.getModel().mapViewPosition,
                    GF);
            tileLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
            mapView.getLayerManager().getLayers().add(tileLayer);

            org.mapsforge.core.model.BoundingBox bb = mapDataStore.boundingBox();
            LatLong center = bb.getCenterPoint();
            mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(center, (byte) 8));
        } else {
            System.err.println("[AppView] No se encontró el archivo colombia.map en resources.");
        }

        // Mouse clicks → fire callback
        mapView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LatLong ll = mapView.getMapViewProjection().fromPixels(e.getX(), e.getY());
                if (ll != null && onMapClick != null) {
                    onMapClick.accept(ll.latitude, ll.longitude);
                }
            }
        });

        // MapView extends java.awt.Container — wrap in JPanel so SwingNode accepts it
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(mapView, BorderLayout.CENTER);

        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> swingNode.setContent(wrapper));

        root.setCenter(swingNode);
        scene = new Scene(root, 1100, 750);
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public Scene getScene()                        { return scene; }
    public ComboBox<String> getStrategySelector()  { return strategySelector; }
    public Button getCalcButton()                  { return calcButton; }
    public Button getResetButton()                 { return resetButton; }

    public void setOriginText(String text) {
        Platform.runLater(() -> originField.setText(text));
    }

    public void setDestinationText(String text) {
        Platform.runLater(() -> destinationField.setText(text));
    }

    /** Green circle = origin marker */
    public void setStartMarker(double lat, double lng) {
        SwingUtilities.invokeLater(() -> {
            removeLayer(startCircle);
            startCircle = createCircle(lat, lng, java.awt.Color.GREEN);
            mapView.getLayerManager().getLayers().add(startCircle);
            mapView.getLayerManager().redrawLayers();
        });
    }

    /** Red circle = destination marker */
    public void setEndMarker(double lat, double lng) {
        SwingUtilities.invokeLater(() -> {
            removeLayer(endCircle);
            endCircle = createCircle(lat, lng, java.awt.Color.RED);
            mapView.getLayerManager().getLayers().add(endCircle);
            mapView.getLayerManager().redrawLayers();
        });
    }

    /** Blue polyline = calculated route */
    public void setRoute(List<double[]> path) {
        SwingUtilities.invokeLater(() -> {
            removeLayer(routePolyline);
            if (path == null || path.isEmpty()) return;

            Paint paint = GF.createPaint();
            paint.setColor(GF.createColor(220, 30, 80, 220));
            paint.setStrokeWidth(6);
            paint.setStyle(Style.STROKE);

            routePolyline = new Polyline(paint, GF);
            for (double[] coord : path) {
                routePolyline.getLatLongs().add(new LatLong(coord[0], coord[1]));
            }
            mapView.getLayerManager().getLayers().add(routePolyline);
            mapView.getLayerManager().redrawLayers();
        });
    }

    /** Removes all markers and the route overlay */
    public void clearOverlays() {
        SwingUtilities.invokeLater(() -> {
            removeLayer(startCircle);
            removeLayer(endCircle);
            removeLayer(routePolyline);
            startCircle = null;
            endCircle = null;
            routePolyline = null;
            mapView.getLayerManager().redrawLayers();
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Circle createCircle(double lat, double lng, java.awt.Color awtColor) {
        // Fill
        Paint fill = GF.createPaint();
        fill.setStyle(Style.FILL);
        int argb = awtColor.getRGB();
        fill.setColor(GF.createColor(
                (argb >> 24) & 0xFF,
                (argb >> 16) & 0xFF,
                (argb >> 8)  & 0xFF,
                 argb        & 0xFF));
        // Stroke (white outline)
        Paint stroke = GF.createPaint();
        stroke.setStyle(Style.STROKE);
        stroke.setColor(GF.createColor(255, 255, 255, 255));
        stroke.setStrokeWidth(2);

        return new Circle(new LatLong(lat, lng), 12, fill, stroke);
    }

    private void removeLayer(Layer layer) {
        if (layer != null) {
            mapView.getLayerManager().getLayers().remove(layer);
        }
    }

    private File findMapFile() {
        String[] candidates = {
            "demo/src/main/resources/colombia.map",
            "src/main/resources/colombia.map",
            "demo/target/classes/colombia.map",
            "target/classes/colombia.map"
        };
        for (String path : candidates) {
            File f = new File(path);
            if (f.exists()) {
                System.out.println("[AppView] Usando mapa: " + f.getAbsolutePath());
                return f;
            }
        }
        return null;
    }
}
