package appcumple;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Aplicacion de demostracion: galeria festiva a pantalla maximizada. */
public final class AppCumple extends JFrame {
    private static final Color BACKGROUND = new Color(18, 20, 38);
    private static final Color CARD = new Color(31, 34, 58);
    private static final Color ACCENT = new Color(255, 102, 120);
    private static final Color MUTED = new Color(194, 198, 218);

    private final List<Slide> slides;
    private final Random random = new Random();
    private final ImagePanel imagePanel = new ImagePanel();
    private final JLabel titleLabel = new JLabel();
    private final JTextArea descriptionArea = new JTextArea();
    private final JLabel counterLabel = new JLabel();
    private int currentIndex;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            new AppCumple().setVisible(true);
        });
    }

    public AppCumple() {
        super("Momentos de celebracion");
        slides = createRandomSlides(loadCollage());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setContentPane(createContent());
        configureKeyboard();
        showSlide(0);
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(28, 42, 28, 42));

        JLabel header = new JLabel("UNA PEQUENA COLECCION DE MOMENTOS");
        header.setForeground(ACCENT);
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        root.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridLayout(1, 2, 34, 0));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(28, 28, 28, 28));
        card.add(imagePanel);
        card.add(createTextPanel());
        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private JPanel createTextPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 38));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setOpaque(false);
        descriptionArea.setForeground(MUTED);
        descriptionArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));

        counterLabel.setForeground(MUTED);
        counterLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));

        JButton previous = createButton("Anterior", false);
        previous.addActionListener(e -> move(-1));
        JButton next = createButton("Siguiente", true);
        next.addActionListener(e -> move(1));
        JButton surprise = createButton("Sorprendeme", false);
        surprise.addActionListener(e -> showSlide(random.nextInt(slides.size())));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controls.setOpaque(false);
        controls.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(previous);
        controls.add(next);
        controls.add(surprise);

        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(22));
        panel.add(descriptionArea);
        panel.add(Box.createVerticalStrut(14));
        panel.add(counterLabel);
        panel.add(Box.createVerticalStrut(28));
        panel.add(controls);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(12, 18, 12, 18));
        button.setBackground(primary ? ACCENT : new Color(55, 59, 87));
        button.setForeground(Color.WHITE);
        return button;
    }

    private void move(int offset) {
        showSlide(Math.floorMod(currentIndex + offset, slides.size()));
    }

    private void showSlide(int index) {
        currentIndex = index;
        Slide slide = slides.get(index);
        titleLabel.setText(slide.title());
        descriptionArea.setText(slide.description());
        counterLabel.setText(String.format("%02d  /  %02d", index + 1, slides.size()));
        imagePanel.setImage(slide.image());
    }

    private void configureKeyboard() {
        JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("RIGHT"), "next");
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("LEFT"), "previous");
        root.getActionMap().put("next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { move(1); }
        });
        root.getActionMap().put("previous", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { move(-1); }
        });
    }

    private List<Slide> createRandomSlides(BufferedImage collage) {
        String[] titles = {"Brindis inesperado", "Deseos encendidos", "Regalos con misterio",
                "Una noche para recordar", "Risas de colores", "El mejor momento"};
        String[] descriptions = {
                "A veces basta una mesa, buena compania y un poco de musica para convertir una tarde cualquiera en un recuerdo enorme.",
                "Cada vela guarda un deseo distinto. Hoy toca pedirlos todos y dejar que la celebracion haga el resto.",
                "Los mejores regalos no siempre llevan lazo: tambien llegan en forma de abrazos, historias y carcajadas compartidas.",
                "Luces suaves, cielo despejado y conversaciones que se alargan sin mirar el reloj. Ese es el plan perfecto.",
                "Que nunca falten motivos para celebrar, colores para decorar y personas con las que compartir el momento.",
                "Esta es una version de prueba: los textos y el orden cambian cada vez que se abre la aplicacion."
        };

        List<BufferedImage> images = splitCollage(collage);
        List<String> titleList = new ArrayList<>(List.of(titles));
        List<String> textList = new ArrayList<>(List.of(descriptions));
        Collections.shuffle(titleList, random);
        Collections.shuffle(textList, random);

        List<Slide> result = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            result.add(new Slide(titleList.get(i), textList.get(i), images.get(i)));
        }
        Collections.shuffle(result, random);
        return result;
    }

    private static List<BufferedImage> splitCollage(BufferedImage source) {
        int halfWidth = source.getWidth() / 2;
        int halfHeight = source.getHeight() / 2;
        int gutter = Math.max(4, source.getWidth() / 200);
        return List.of(
                source.getSubimage(0, 0, halfWidth - gutter, halfHeight - gutter),
                source.getSubimage(halfWidth + gutter, 0, source.getWidth() - halfWidth - gutter, halfHeight - gutter),
                source.getSubimage(0, halfHeight + gutter, halfWidth - gutter, source.getHeight() - halfHeight - gutter),
                source.getSubimage(halfWidth + gutter, halfHeight + gutter,
                        source.getWidth() - halfWidth - gutter, source.getHeight() - halfHeight - gutter)
        );
    }

    private static BufferedImage loadCollage() {
        try (InputStream stream = AppCumple.class.getResourceAsStream("/images/fiesta-collage.png")) {
            if (stream == null) throw new IOException("Recurso no encontrado");
            return ImageIO.read(stream);
        } catch (IOException ex) {
            BufferedImage fallback = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = fallback.createGraphics();
            g.setPaint(new GradientPaint(0, 0, ACCENT, 1000, 1000, new Color(62, 205, 190)));
            g.fillRect(0, 0, 1000, 1000);
            g.dispose();
            return fallback;
        }
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ignored) {
            // Swing utilizara su apariencia multiplataforma.
        }
    }

    private record Slide(String title, String description, BufferedImage image) { }

    private static final class ImagePanel extends JPanel {
        private BufferedImage image;

        private ImagePanel() {
            setOpaque(true);
            setBackground(new Color(24, 26, 46));
        }

        void setImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (image == null) return;
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            double scale = Math.max((double) getWidth() / image.getWidth(),
                    (double) getHeight() / image.getHeight());
            int width = (int) Math.ceil(image.getWidth() * scale);
            int height = (int) Math.ceil(image.getHeight() * scale);
            int x = (getWidth() - width) / 2;
            int y = (getHeight() - height) / 2;
            g.drawImage(image, x, y, width, height, null);
            g.dispose();
        }
    }
}
