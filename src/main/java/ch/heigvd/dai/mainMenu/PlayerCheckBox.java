package ch.heigvd.dai.mainMenu;

import javax.swing.*;
import java.awt.*;

public class PlayerCheckBox extends JPanel {
    private String playerName;
    private boolean ready;
    private JLabel nameLabel;
    private JPanel colorBox;

    public PlayerCheckBox(String playerName) {
        this.playerName = playerName;
        this.ready = false;  // Par défaut, le joueur n'est pas prêt

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 80));

        // Nom du joueur
        nameLabel = new JLabel(playerName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(nameLabel, BorderLayout.NORTH);

        // Carré coloré
        colorBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(ready ? Color.GREEN : Color.RED);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        colorBox.setPreferredSize(new Dimension(200, 50));
        add(colorBox, BorderLayout.CENTER);
    }

    // Basculer le statut de "ready"
    public void toggleReady() {
        ready = !ready;
        updatePanel();
    }

    public void setReady(boolean ready) {
        this.ready = ready;
        updatePanel();
    }

    // Récupérer le statut "ready"
    public boolean isReady() {
        return ready;
    }

    // Récupérer le nom du joueur
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        nameLabel.setText(playerName);
        repaint(); // Réactualiser l'affichage si nécessaire
    }

    private void updatePanel() {
        // Appeler repaint pour forcer la mise à jour de la couleur du carré
        colorBox.repaint();
        // Si d'autres changements sont nécessaires, les ajouter ici
    }
}
