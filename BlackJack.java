import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements https://www.youtube.com/watch?v=xjqTIzYkGdI except splitting.
 * Minor deviations:
 * 1. No split
 * 2. If table has an Ace, it is always counted as 11
 * 3. When it is table's turn, table must hit with a rank 16 or less
 */
public class BlackJack extends JFrame {

    final static int MAX_CARDS = 11;

    final JButton btnDeal = new JButton("Deal");
    final JButton btnInsurance = new JButton("Buy Insurance");
    final JButton btnSurrender = new JButton("Surrender");
    final JButton btnDoubleDown = new JButton("Double Down");
    final JButton btnHit = new JButton("Hit");
    final JButton btnStay = new JButton("Stay");
    final JButton btnRestart = new JButton("Restart");
    final JButton btnExit = new JButton("Exit");

    final JLabel lblServingDeck = new JLabel();
    final JLabel lblServingDeckSize = new JLabel("Serving Deck: 52 Cards");
    final JLabel lblBackupDeck = new JLabel();
    final JLabel lblBackupDeckSize = new JLabel("Backup Deck: 0 Cards");

    final JLabel lblTableEarning = new JLabel("Table Earning: 0");
    final JLabel lblBid = new JLabel("Bid: 0");
    final JLabel lblPlayerEarning = new JLabel("Player Earning: 0");

    final JLabel lblTableCard[] = new JLabel[MAX_CARDS];
    final JLabel lblPlayerCard[] = new JLabel[MAX_CARDS];
    final JLabel lblTableRank = new JLabel("Rank: ");
    final JLabel lblPlayerRank = new JLabel("Rank: ");

    final JLabel lblStatus = new JLabel("Game On Going ...");

    final Border compoundBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createLoweredBevelBorder());
    final ImageIcon cardBackIcon = new ImageIcon(getClass().getResource("images/back.png"));
    final Deck backupDeck = new Deck();
    final Deck servingDeck = new Deck(false, backupDeck);

    final List<Card> tableCards = new ArrayList<>();
    final List<Card> playerCards = new ArrayList<>();

    int currentBid;
    int tableEarning;
    int playerEarning;
    boolean purchasedInsurance;

    int tableRank;
    int playerRank;
    boolean tableHasAce;
    boolean playerHasAce;

    public BlackJack() {
        setTitle("BlackJack");
        setSize(1536, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel pnlMain = new JPanel(); // Main Window
        pnlMain.setLayout(new BorderLayout(5, 5));

        final JPanel pnlActions = new JPanel();
        pnlActions.setLayout(new FlowLayout());
        pnlActions.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        pnlActions.setBorder(compoundBorder);
        pnlActions.add(btnDeal);
        pnlActions.add(btnInsurance);
        pnlActions.add(btnSurrender);
        pnlActions.add(btnDoubleDown);
        pnlActions.add(btnHit);
        pnlActions.add(btnStay);
        pnlActions.add(btnRestart);
        pnlActions.add(btnExit);

        btnExit.addActionListener(l -> {
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        btnHit.addActionListener(l -> {
            handleHit();
        });
        btnStay.addActionListener(l -> {
            handleStay();
        });
        btnSurrender.addActionListener(l -> {
            handleSurrender();
        });
        btnDeal.addActionListener(l -> {
            handlePostDealState();
        });
        btnDoubleDown.addActionListener(l -> {
            handleDoubleDown();
        });
        btnRestart.addActionListener(l -> {
            handleRestart();
        });

        pnlMain.add(pnlActions, BorderLayout.PAGE_END);

        final JPanel pnlDecks = new JPanel();
        pnlDecks.setLayout(new GridLayout(0, 1));
        pnlDecks.setBorder(compoundBorder);
        pnlDecks.add(lblServingDeck);
        pnlDecks.add(lblServingDeckSize);
        pnlDecks.add(lblBackupDeck);
        pnlDecks.add(lblBackupDeckSize);

        pnlMain.add(pnlDecks, BorderLayout.LINE_START);

        final JPanel pnlEarning = new JPanel();
        pnlEarning.setLayout(new GridLayout(0, 1));
        pnlEarning.setBorder(compoundBorder);
        pnlEarning.add(lblTableEarning);
        pnlEarning.add(lblBid);
        pnlEarning.add(lblPlayerEarning);

        pnlMain.add(pnlEarning, BorderLayout.LINE_END);

        final JPanel pnlTableCards = new JPanel();
        pnlTableCards.setLayout(new GridLayout(1, 0));
        pnlTableCards.setBorder(BorderFactory.createRaisedBevelBorder());
        final JPanel pnlPlayerCards = new JPanel();
        pnlPlayerCards.setLayout(new GridLayout(1, 0));
        pnlPlayerCards.setBorder(compoundBorder);
        for (int i = 0; i < MAX_CARDS; i++) {
            lblTableCard[i] = new JLabel("Pick A Card");
            lblPlayerCard[i] = new JLabel("Pick A Card");

            lblTableCard[i].setBorder(BorderFactory.createRaisedBevelBorder());
            lblPlayerCard[i].setBorder(BorderFactory.createRaisedBevelBorder());

            lblTableCard[i].setHorizontalAlignment(SwingConstants.CENTER);
            lblTableCard[i].setVerticalAlignment(SwingConstants.CENTER);
            lblPlayerCard[i].setHorizontalAlignment(SwingConstants.CENTER);
            lblPlayerCard[i].setVerticalAlignment(SwingConstants.CENTER);

            pnlTableCards.add(lblTableCard[i]);
            pnlPlayerCards.add(lblPlayerCard[i]);
        }
        pnlTableCards.add(lblTableRank);
        pnlPlayerCards.add(lblPlayerRank);

        final JPanel pnlGame = new JPanel();
        pnlGame.setLayout(new GridLayout(0, 1));
        pnlGame.setBorder(compoundBorder);
        pnlGame.add(pnlTableCards);
        pnlGame.add(lblStatus);
        pnlGame.add(pnlPlayerCards);
        lblStatus.setVerticalAlignment(SwingConstants.CENTER);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        pnlMain.add(pnlGame, BorderLayout.CENTER);

        setContentPane(pnlMain);
        setVisible(true);

        servingDeck.shuffle();

        setDealState();
    }

    private static void fitImageToLabel(final JLabel label, final ImageIcon icon) {
        final ImageIcon resizedIcon = new ImageIcon(icon.getImage().getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_DEFAULT));
        label.setIcon(resizedIcon);
        label.setText(null);
    }

    private void setDealState() {
        setActionPanelState(true, false);

        lblStatus.setText("Game On Going ...");

        for (int i = 0; i < MAX_CARDS; i++) {
            lblTableCard[i].setIcon(null);
            lblPlayerCard[i].setIcon(null);
        }

        purchasedInsurance = false;
        tableHasAce = playerHasAce = false;
        playerRank = tableRank = 0;

        drawCardPanel();
        drawEarningPanel();
        lblTableRank.setText("Rank: ");
        lblPlayerRank.setText("Rank: ");
    }

    private void handlePostDealState() {
        currentBid = 100; // take as input

        setActionPanelState(false, false);

        for (int i = 0; i < 2; i++) {
            if (servingDeck.size() == 0) {
                servingDeck.moveCards(backupDeck);
            }

            // add to player
            final Card playerCard = servingDeck.take();
            fitImageToLabel(lblPlayerCard[i], playerCard.getIcon());
            playerCards.add(playerCard);
            if (playerCard.getRank().equals(Rank.Ace)) {
                playerHasAce = true;
            }
            playerRank += playerCard.getRank().getNumericValue();

            // add to table
            final Card tableCard = servingDeck.take();
            if (i == 0) {
                fitImageToLabel(lblTableCard[i], tableCard.getIcon());
            } else {
                fitImageToLabel(lblTableCard[i], cardBackIcon);
            }
            tableCards.add(tableCard);
            if (tableCard.getRank().equals(Rank.Ace)) {
                tableHasAce = true;
            }
            tableRank += tableCard.getRank().getNumericValue();
        }
        drawPlayerRank();

        drawCardPanel();
        drawEarningPanel();

        // do more... winner/backjack/looser
        lblStatus.setText("Game On Going ...");
    }

    private void handleDoubleDown() {
        currentBid *= 2;

        final Card playerCard = servingDeck.take();
        fitImageToLabel(lblPlayerCard[playerCards.size()], playerCard.getIcon());
        playerCards.add(playerCard);
        if (playerCard.getRank().equals(Rank.Ace)) {
            playerHasAce = true;
        }
        playerRank += playerCard.getRank().getNumericValue();

        drawCardPanel();
        drawPlayerRank();
        fitImageToLabel(lblTableCard[tableCards.size() - 1],
                tableCards.get(tableCards.size() - 1).getIcon());
        //tableTurn();
        drawTableRank();
        determineWinner();

        setActionPanelState(true, true);
        drawEarningPanel();
    }

    private void tableTurn() {
        fitImageToLabel(lblTableCard[tableCards.size() - 1],
                tableCards.get(tableCards.size() - 1).getIcon());

        int tableRankFinal = tableRank + (tableHasAce ? 10 : 0);
        for (int i = 2  ; i < MAX_CARDS && tableRankFinal <= 16; i++) {
            final Card tableCard = servingDeck.take();
            fitImageToLabel(lblTableCard[i], tableCard.getIcon());
            tableCards.add(tableCard);

            if (!tableHasAce && tableCard.getRank().equals(Rank.Ace)) {
                tableHasAce = true;
                tableRankFinal += 10;
            }
            tableRank += tableCard.getRank().getNumericValue();
            tableRankFinal += tableCard.getRank().getNumericValue();
        }
    }

    private void determineWinner() {
        final int playerScore;
        if (playerHasAce) {
            playerScore = (playerRank + 10) > 21 ? playerRank : playerRank + 10;
        } else {
            playerScore = playerRank;
        }

        if (playerScore > 21) {
            tableEarning += currentBid;
            lblTableEarning.setText("Table Earning: " + tableEarning);
            lblStatus.setText("Looser!");
        } else {
            int tableScore = tableRank + (tableHasAce ? 10 : 0);

            if (playerScore == tableScore) {
                // no winner
                lblStatus.setText("No Winner!!");
            } else if (playerScore > tableScore) {
                // player is the winner
                playerEarning += currentBid;
                lblPlayerEarning.setText("Player Earning: " + playerEarning);
                lblStatus.setText("Winner!!!");
            } else {
                // table is the winner
                tableEarning += currentBid;
                lblTableEarning.setText("Table Earning: " + tableEarning);
                lblStatus.setText("Looser!");
            }
        }
    }

    private void handleRestart() {
        currentBid = 0;

        backupDeck.addCards(playerCards);
        backupDeck.addCards(tableCards);
        playerCards.clear();
        tableCards.clear();

        setDealState();
    }

    private void setActionPanelState(final boolean deal, final boolean setRestart) {
        if (setRestart) {
            btnDeal.setEnabled(false);
            btnInsurance.setEnabled(false);
            btnSurrender.setEnabled(false);
            btnDoubleDown.setEnabled(false);
            btnHit.setEnabled(false);
            btnStay.setEnabled(false);
            btnRestart.setEnabled(true);
        } else {
            btnDeal.setEnabled(deal);
            btnInsurance.setEnabled(!deal);
            btnSurrender.setEnabled(!deal);
            btnDoubleDown.setEnabled(!deal);
            btnHit.setEnabled(!deal);
            btnStay.setEnabled(!deal);
            btnRestart.setEnabled(false);
        }
    }

    private void drawCardPanel() {
        lblServingDeckSize.setText("Serving Deck: " + servingDeck.size() + " Cards");
        lblBackupDeckSize.setText("Backup Deck: " + backupDeck.size() + " Cards");

        if (servingDeck.size() > 0) {
            fitImageToLabel(lblServingDeck, cardBackIcon);
        } else {
            lblServingDeck.setIcon(null);
        }
        if (backupDeck.size() > 0) {
            fitImageToLabel(lblBackupDeck, backupDeck.peak().getIcon());
        } else {
            lblBackupDeck.setIcon(null);
        }
    }

    private void drawEarningPanel() {
        lblBid.setText("Current Bid: " + currentBid);
        lblPlayerEarning.setText("Player Earning: " + playerEarning);
        lblTableEarning.setText("Table Earning: " + tableEarning);
    }

    private void drawPlayerRank() {
        final String rank;
        if (playerHasAce) {
            if ((playerRank + 10) > 21) {
                rank = "" + playerRank;
            } else {
                rank = "" + playerRank + " OR " + (playerRank + 10);
            }
        } else {
            rank = "" + playerRank;
        }

        lblPlayerRank.setText("Rank: " + rank);
    }

    private void drawTableRank() {
        final String rank;
        if (tableHasAce) {
            rank = "" + (tableRank + 10);
        } else {
            rank = "" + tableRank;
        }

        lblTableRank.setText("Rank: " + rank);
    }

    private void handleHit() {
        if (servingDeck.size() == 0) {
            servingDeck.moveCards(backupDeck);

        }

        final Card playerCard = servingDeck.take();
        fitImageToLabel(lblPlayerCard[playerCards.size()], playerCard.getIcon());
        playerCards.add(playerCard);
        if (playerCard.getRank().equals(Rank.Ace)) {
            playerHasAce = true;
        }

        playerRank += playerCard.getRank().getNumericValue();

        drawCardPanel();
        drawPlayerRank();
        fitImageToLabel(lblTableCard[tableCards.size() - 1],
                tableCards.get(tableCards.size() - 1).getIcon());
        
        final int playerScore = playerHasAce && (playerRank + 10) <= 21 ? playerRank + 10 : playerRank;
        if (playerScore > 21) {
            lblStatus.setText("Busted! Table Wins.");
            tableEarning += currentBid;
            setActionPanelState(true, true);
            drawEarningPanel();
        }
    }

    private void handleStay() {
        lblStatus.setText("Table's Turn...");
        tableTurn();

        determineWinner();

        setActionPanelState(true, true);
    }

    private void handleSurrender() {
        lblStatus.setText("Player Surrendered. Table Wins.");
        tableEarning += currentBid / 2;
        playerEarning -= currentBid / 2;

        drawEarningPanel();
        setActionPanelState(true, true);
    }


    public static void main(String[] args) {
        new BlackJack();
    }
}