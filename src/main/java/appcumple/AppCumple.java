package appcumple;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Mapa interactivo de capitales europeas y sus recuerdos fotograficos. */
public final class AppCumple extends JFrame {
    private static final Color BG = new Color(14, 20, 33);
    private static final Color PANEL = new Color(24, 33, 50);
    private static final Color LAND = new Color(66, 98, 105);
    private static final Color LAND_EDGE = new Color(117, 164, 159);
    private static final Color TEXT = new Color(238, 242, 248);
    private static final Color MUTED = new Color(169, 180, 198);
    private static final Color ACCENT = new Color(255, 183, 77);
    private static final Color SELECTED = new Color(255, 94, 120);
    private static final Path IMAGE_ROOT = Path.of("imagenes-capitales");
    private static final Set<String> MAIN_CAPITALS = Set.of(
            "Amsterdam", "Ankara", "Atenas", "Belgrado", "Berlin", "Berna", "Bratislava",
            "Bruselas", "Bucarest", "Budapest", "Chisinau", "Copenhague", "Dublin", "Estocolmo",
            "Helsinki", "Kiev", "Lisboa", "Liubliana", "Londres", "Madrid", "Minsk", "Moscu",
            "Oslo", "Paris", "Podgorica", "Praga", "Pristina", "Reikiavik", "Riga", "Roma",
            "Sarajevo", "Skopie", "Sofia", "Tallin", "Tirana", "Varsovia", "Viena", "Vilna", "Zagreb");

    private final List<Capital> capitals = createCapitals();
    private final List<Connection> connections = new ArrayList<>();
    private final DefaultListModel<Connection> connectionModel = new DefaultListModel<>();
    private final MapPanel mapPanel = new MapPanel();
    private final GalleryPanel galleryPanel = new GalleryPanel();
    private final JToggleButton connectMode = new JToggleButton("Modo unir capitales");
    private final JLabel instruction = new JLabel("Activa el modo unir para seleccionar dos capitales.");
    private Capital firstForConnection;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setLookAndFeel();
            new AppCumple().setVisible(true);
        });
    }

    public AppCumple() {
        super("Capitales de Europa");
        createImageDirectories();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setContentPane(createContent());
    }

    private JComponent createContent() {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("CAPITALES DE EUROPA");
        title.setForeground(TEXT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
        JLabel subtitle = new JLabel("Selecciona una capital para ver sus imagenes");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        heading.add(title, BorderLayout.WEST);
        heading.add(subtitle, BorderLayout.EAST);
        root.add(heading, BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapPanel, createSidebar());
        split.setResizeWeight(.72);
        split.setDividerSize(8);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);
        return root;
    }

    private JComponent createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 14));
        sidebar.setBackground(PANEL);
        sidebar.setBorder(new EmptyBorder(18, 18, 18, 18));
        sidebar.setMinimumSize(new Dimension(330, 500));
        sidebar.setPreferredSize(new Dimension(390, 700));
        galleryPanel.setPreferredSize(new Dimension(340, 330));
        sidebar.add(galleryPanel, BorderLayout.CENTER);

        JPanel connectionArea = new JPanel(new BorderLayout(0, 10));
        connectionArea.setOpaque(false);
        JLabel connectionTitle = new JLabel("UNIONES ENTRE CAPITALES");
        connectionTitle.setForeground(TEXT);
        connectionTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        connectionArea.add(connectionTitle, BorderLayout.NORTH);
        JList<Connection> connectionList = new JList<>(connectionModel);
        connectionList.setBackground(new Color(18, 26, 41));
        connectionList.setForeground(TEXT);
        connectionList.setSelectionBackground(new Color(65, 78, 104));
        connectionList.setVisibleRowCount(5);
        connectionArea.add(new JScrollPane(connectionList), BorderLayout.CENTER);

        connectMode.setFocusPainted(false);
        connectMode.setBackground(new Color(50, 65, 88));
        connectMode.setForeground(TEXT);
        connectMode.addActionListener(e -> {
            firstForConnection = null;
            updateInstruction();
            mapPanel.repaint();
        });
        JButton delete = new JButton("Eliminar union seleccionada");
        delete.setBackground(SELECTED);
        delete.setForeground(Color.WHITE);
        delete.setFocusPainted(false);
        delete.addActionListener(e -> {
            int index = connectionList.getSelectedIndex();
            if (index >= 0) {
                connections.remove(index);
                connectionModel.remove(index);
                mapPanel.repaint();
            }
        });
        instruction.setForeground(MUTED);
        instruction.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        JPanel controls = new JPanel(new GridLayout(0, 1, 0, 7));
        controls.setOpaque(false);
        controls.add(connectMode);
        controls.add(instruction);
        controls.add(delete);
        connectionArea.add(controls, BorderLayout.SOUTH);
        sidebar.add(connectionArea, BorderLayout.SOUTH);
        return sidebar;
    }

    private void capitalClicked(Capital capital) {
        galleryPanel.showCapital(capital);
        if (!connectMode.isSelected()) return;
        if (firstForConnection == null) {
            firstForConnection = capital;
        } else if (!firstForConnection.equals(capital)) {
            Connection connection = new Connection(firstForConnection, capital);
            if (connections.stream().noneMatch(current -> current.samePair(connection))) {
                connections.add(connection);
                connectionModel.addElement(connection);
            }
            firstForConnection = null;
        }
        updateInstruction();
        mapPanel.repaint();
    }

    private void updateInstruction() {
        if (!connectMode.isSelected()) instruction.setText("Activa el modo unir para seleccionar dos capitales.");
        else if (firstForConnection == null) instruction.setText("Selecciona la primera capital del par.");
        else instruction.setText("Primera: " + firstForConnection.name() + ". Selecciona la segunda.");
    }

    private void createImageDirectories() {
        for (Capital capital : capitals) {
            try {
                Files.createDirectories(IMAGE_ROOT.resolve(capital.folder()));
            } catch (IOException ex) {
                System.err.println("No se pudo crear la carpeta de " + capital.name() + ": " + ex.getMessage());
            }
        }
    }

    private final class MapPanel extends JPanel {
        private static final double MIN_LON = -14.0, MAX_LON = 52.0, MIN_LAT = 30.0, MAX_LAT = 72.0;
        private final BufferedImage backgroundMap = loadBackgroundMap();
        private Capital hovered;

        private MapPanel() {
            setBackground(new Color(18, 43, 63));
            setToolTipText("");
            MouseAdapter mouse = new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    Capital nearest = findCapital(e.getPoint());
                    if (nearest != hovered) {
                        hovered = nearest;
                        setToolTipText(nearest == null ? null : nearest.name());
                        repaint();
                    }
                }
                @Override public void mouseExited(MouseEvent e) { hovered = null; repaint(); }
                @Override public void mouseClicked(MouseEvent e) {
                    Capital capital = findCapital(e.getPoint());
                    if (capital != null) capitalClicked(capital);
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            Rectangle map = mapBounds();
            g.drawImage(backgroundMap, map.x, map.y, map.width, map.height, null);
            paintConnections(g);
            paintCapitals(g);
            g.dispose();
        }

        private Rectangle mapBounds() {
            double imageRatio = (double) backgroundMap.getWidth() / backgroundMap.getHeight();
            double panelRatio = getHeight() == 0 ? imageRatio : (double) getWidth() / getHeight();
            if (panelRatio > imageRatio) {
                int height = getHeight();
                int width = (int) Math.round(height * imageRatio);
                return new Rectangle((getWidth() - width) / 2, 0, width, height);
            }
            int width = getWidth();
            int height = (int) Math.round(width / imageRatio);
            return new Rectangle(0, (getHeight() - height) / 2, width, height);
        }

        private void paintConnections(Graphics2D g) {
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(255, 192, 92, 210));
            for (Connection connection : connections) {
                if (!isVisible(connection.a()) || !isVisible(connection.b())) continue;
                Point a = project(connection.a().longitude(), connection.a().latitude());
                Point b = project(connection.b().longitude(), connection.b().latitude());
                g.drawLine(a.x, a.y, b.x, b.y);
            }
        }

        private void paintCapitals(Graphics2D g) {
            for (Capital capital : capitals) {
                if (!isVisible(capital)) continue;
                Point point = project(capital.longitude(), capital.latitude());
                boolean pending = capital.equals(firstForConnection), hot = capital.equals(hovered);
                int radius = pending || hot ? 8 : 6;
                g.setColor(pending ? SELECTED : hot ? Color.WHITE : ACCENT);
                g.fillOval(point.x - radius, point.y - radius, radius * 2, radius * 2);
                g.setColor(new Color(8, 17, 28)); g.setStroke(new BasicStroke(2f));
                g.drawOval(point.x - radius, point.y - radius, radius * 2, radius * 2);
                if (MAIN_CAPITALS.contains(capital.name())) {
                    int fontSize = hot ? 13 : 11;
                    g.setFont(new Font(Font.SANS_SERIF, hot ? Font.BOLD : Font.PLAIN, fontSize));
                    int boxWidth = g.getFontMetrics().stringWidth(capital.name()) + 10;
                    int x = Math.min(getWidth() - boxWidth - 4, point.x + 8);
                    int y = Math.max(18, point.y - 7);
                    g.setColor(new Color(250, 250, 248, hot ? 235 : 205));
                    g.fillRoundRect(x, y - fontSize - 3, boxWidth, fontSize + 7, 7, 7);
                    g.setColor(new Color(20, 23, 27));
                    g.drawString(capital.name(), x + 5, y);
                }
            }
        }

        private Capital findCapital(Point mouse) {
            return capitals.stream().filter(this::isVisible)
                    .filter(c -> project(c.longitude(), c.latitude()).distance(mouse) <= 13)
                    .min(Comparator.comparingDouble(c -> project(c.longitude(), c.latitude()).distance(mouse)))
                    .orElse(null);
        }

        private Point project(double longitude, double latitude) {
            Rectangle map = mapBounds();
            // El mapa de referencia desplaza Islandia ligeramente para incluirla en el encuadre.
            if (longitude < -18 && latitude > 60) {
                return new Point(map.x + (int) (map.width * .085), map.y + (int) (map.height * .13));
            }
            double x = map.x + (longitude - MIN_LON) / (MAX_LON - MIN_LON) * map.width;
            double y = map.y + (MAX_LAT - latitude) / (MAX_LAT - MIN_LAT) * map.height;
            return new Point((int) Math.round(x), (int) Math.round(y));
        }

        private boolean isVisible(Capital capital) {
            return (capital.name().equals("Reikiavik") || capital.longitude() >= MIN_LON)
                    && capital.longitude() <= MAX_LON
                    && capital.latitude() >= MIN_LAT && capital.latitude() <= MAX_LAT;
        }

        private BufferedImage loadBackgroundMap() {
            try (InputStream stream = AppCumple.class.getResourceAsStream("/images/mapa-europa-capitales.png")) {
                if (stream == null) throw new IOException("No se encontro el mapa");
                return ImageIO.read(stream);
            } catch (IOException ex) {
                BufferedImage fallback = new BufferedImage(1200, 900, BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D graphics = fallback.createGraphics();
                graphics.setPaint(new GradientPaint(0, 0, Color.DARK_GRAY, 1200, 900, Color.BLACK));
                graphics.fillRect(0, 0, 1200, 900);
                graphics.dispose();
                return fallback;
            }
        }
    }

    private static final class GalleryPanel extends JPanel {
        private final JLabel title = new JLabel("Selecciona una capital");
        private final JLabel path = new JLabel("Las fotografias apareceran aqui.");
        private final JPanel images = new JPanel(new GridLayout(0, 2, 8, 8));

        private GalleryPanel() {
            setOpaque(false); setLayout(new BorderLayout(0, 10));
            title.setForeground(TEXT); title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            path.setForeground(MUTED); path.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            JPanel header = new JPanel(new GridLayout(0, 1));
            header.setOpaque(false); header.add(title); header.add(path); add(header, BorderLayout.NORTH);
            images.setOpaque(false);
            JScrollPane scroll = new JScrollPane(images); scroll.setBorder(null); scroll.getViewport().setBackground(PANEL);
            add(scroll, BorderLayout.CENTER);
        }

        private void showCapital(Capital capital) {
            title.setText(capital.name());
            Path folder = IMAGE_ROOT.resolve(capital.folder());
            path.setText(folder.toString());
            images.removeAll();
            List<Path> files = imageFiles(folder);
            if (files.isEmpty()) {
                JLabel empty = new JLabel("<html>Aun no hay imagenes.<br>Anade archivos JPG, PNG, GIF o BMP<br>en la carpeta indicada.</html>");
                empty.setForeground(MUTED); empty.setHorizontalAlignment(SwingConstants.CENTER);
                empty.setBorder(new EmptyBorder(35, 8, 35, 8)); images.add(empty);
            } else for (Path file : files) addThumbnail(file);
            images.revalidate(); images.repaint();
        }

        private void addThumbnail(Path file) {
            try {
                BufferedImage source = ImageIO.read(file.toFile());
                if (source == null) return;
                JButton button = new JButton(new ImageIcon(scaleToFit(source, 165, 120)));
                button.setToolTipText(file.getFileName().toString());
                button.setBorder(BorderFactory.createLineBorder(new Color(70, 84, 105), 1));
                button.setBackground(new Color(18, 26, 41));
                button.addActionListener(e -> showLarge(file, source));
                images.add(button);
            } catch (IOException ignored) { }
        }

        private void showLarge(Path file, BufferedImage source) {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Image scaled = scaleToFit(source, (int) (screen.width * .72), (int) (screen.height * .72));
            JOptionPane.showMessageDialog(this, new JLabel(new ImageIcon(scaled)), file.getFileName().toString(), JOptionPane.PLAIN_MESSAGE);
        }

        private static Image scaleToFit(BufferedImage image, int maxWidth, int maxHeight) {
            double ratio = Math.min(1d, Math.min((double) maxWidth / image.getWidth(), (double) maxHeight / image.getHeight()));
            return image.getScaledInstance(Math.max(1, (int) (image.getWidth() * ratio)),
                    Math.max(1, (int) (image.getHeight() * ratio)), Image.SCALE_SMOOTH);
        }

        private static List<Path> imageFiles(Path folder) {
            if (!Files.isDirectory(folder)) return List.of();
            Set<String> extensions = Set.of("jpg", "jpeg", "png", "gif", "bmp");
            try (var stream = Files.list(folder)) {
                return stream.filter(Files::isRegularFile).filter(file -> {
                    String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
                    int dot = name.lastIndexOf('.');
                    return dot >= 0 && extensions.contains(name.substring(dot + 1));
                }).sorted().toList();
            } catch (IOException ex) { return List.of(); }
        }
    }

    private static List<Capital> createCapitals() {
        return List.of(
                c("Amsterdam","amsterdam",52.3676,4.9041),c("Andorra la Vella","andorra-la-vella",42.5063,1.5218),
                c("Ankara","ankara",39.9334,32.8597),c("Atenas","atenas",37.9838,23.7275),
                c("Belgrado","belgrado",44.7866,20.4489),c("Berlin","berlin",52.5200,13.4050),
                c("Berna","berna",46.9480,7.4474),c("Bratislava","bratislava",48.1486,17.1077),
                c("Bruselas","bruselas",50.8503,4.3517),c("Bucarest","bucarest",44.4268,26.1025),
                c("Budapest","budapest",47.4979,19.0402),c("Chisinau","chisinau",47.0105,28.8638),
                c("Copenhague","copenhague",55.6761,12.5683),c("Dublin","dublin",53.3498,-6.2603),
                c("Estocolmo","estocolmo",59.3293,18.0686),c("Helsinki","helsinki",60.1699,24.9384),
                c("Kiev","kiev",50.4501,30.5234),c("La Valeta","la-valeta",35.8989,14.5146),
                c("Lisboa","lisboa",38.7223,-9.1393),c("Liubliana","liubliana",46.0569,14.5058),
                c("Londres","londres",51.5074,-0.1278),c("Luxemburgo","luxemburgo",49.6116,6.1319),
                c("Madrid","madrid",40.4168,-3.7038),c("Minsk","minsk",53.9006,27.5590),
                c("Monaco","monaco",43.7384,7.4246),c("Moscu","moscu",55.7558,37.6173),
                c("Nicosia","nicosia",35.1856,33.3823),c("Oslo","oslo",59.9139,10.7522),
                c("Paris","paris",48.8566,2.3522),c("Podgorica","podgorica",42.4304,19.2594),
                c("Praga","praga",50.0755,14.4378),c("Pristina","pristina",42.6629,21.1655),
                c("Reikiavik","reikiavik",64.1466,-21.9426),c("Riga","riga",56.9496,24.1052),
                c("Roma","roma",41.9028,12.4964),c("San Marino","san-marino",43.9424,12.4578),
                c("Sarajevo","sarajevo",43.8563,18.4131),c("Skopie","skopie",41.9981,21.4254),
                c("Sofia","sofia",42.6977,23.3219),c("Tallin","tallin",59.4370,24.7536),
                c("Tirana","tirana",41.3275,19.8187),c("Vaduz","vaduz",47.1410,9.5209),
                c("Varsovia","varsovia",52.2297,21.0122),c("Ciudad del Vaticano","vaticano",41.9029,12.4534),
                c("Viena","viena",48.2082,16.3738),c("Vilna","vilna",54.6872,25.2797),
                c("Zagreb","zagreb",45.8150,15.9819));
    }

    private static Capital c(String name, String folder, double latitude, double longitude) {
        return new Capital(name, folder, latitude, longitude);
    }
    private static void setLookAndFeel() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (ReflectiveOperationException | UnsupportedLookAndFeelException ignored) { }
    }
    private record Capital(String name, String folder, double latitude, double longitude) { }
    private record Connection(Capital a, Capital b) {
        boolean samePair(Connection other) {
            return (a.equals(other.a) && b.equals(other.b)) || (a.equals(other.b) && b.equals(other.a));
        }
        @Override public String toString() { return a.name() + "  ↔  " + b.name(); }
    }
}
