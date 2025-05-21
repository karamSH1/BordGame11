package gameboard11;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/*
 * لعبة لوحة تفاعلية بين لاعبين، حيث يتحرك اللاعبون على لوحة 9x9 باستخدام الأسهم.
 * الهدف: جمع النقود، زيادة الأرواح، والإجابة على الأسئلة لتجنب خسارة الأرواح.
 * المميزات:
 * - يبدأ اللاعبون من الخلية (0,0) ويتحركون لكشف التأثيرات (نقود، قلوب، أسئلة).
 * - الأسئلة متنوعة (ثقافة عامة، جغرافيا، رياضيات، علوم، تاريخ، دينية إسلامية، متنوع).
 * - يتم تبديل الأدوار بعد كل حركة أو إجابة على سؤال.
 * - اللاعب يخسر عندما تصبح أرواحه صفر، ويُعلن الفائز.
 * - تحتوي اللوحة على 15 جدارًا عشوائيًا (لا تظهر في أول صفين أو عمودين).
 * - الإجابات على الأسئلة غير حساسة للحروف، والتواريخ/الحساب بالأرقام.
 */
public class GameBoard11 extends JFrame {

    
    // المتغيرات الأساسية للواجهة واللعبة
    private JPanel mainPanel;
    private JPanel startPanel, boardPanel, infoPanel;
    private CardLayout cardLayout;

    private JLabel effectLabel; // ملصق يعرض التأثير (مثل 💰 أو ❓)
    private JLabel p1Info, p2Info; // ملصقات معلومات اللاعبين (المال، الأرواح، الدور)

    private final int SIZE = 9; // حجم اللوحة 9x9
    private JButton[][] cells = new JButton[SIZE][SIZE]; // مصفوفة الأزرار (خلايا اللوحة)
    private int[][] boardEffects = new int[SIZE][SIZE]; // مصفوفة التأثيرات (0=جدار، 1=نقود، 2=قلب، 3=قلب أسود، 5=سؤال)
    private boolean[][] revealed = new boolean[SIZE][SIZE]; // مصفوفة لتتبع الخلايا المكشوفة

    private int[] player1 = {0, 0}; // موقع اللاعب 1
    private int[] player2 = {0, 0}; // موقع اللاعب 2

    private int playerTurn = 1; // الدور الحالي (1=اللاعب 1، 2=اللاعب 2)
    private int player1Money = 0, player2Money = 0; // النقود لكل لاعب
    private int player1Lives = 5, player2Lives = 5; // الأرواح لكل لاعب

    private String player1Name = "اللاعب 1"; // اسم اللاعب 1
    private String player2Name = "اللاعب 2"; // اسم اللاعب 2

    private Random rand = new Random(); // مولد أرقام عشوائية
    private boolean rolesAssigned = false; // حالة تخصيص الأدوار (بعد شاشة البداية)
    private boolean isProcessingMove = false; // منع الحركات المتعددة أثناء معالجة الحركة
    private boolean gameOver = false; // علامة لإنهاء اللعبة عند خسارة لاعب

    // المُنشئ: إعداد النافذة الرئيسية
    public GameBoard11() {
        setTitle("لعبة اللوحة المطوّرة");
        setSize(1000, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        initStartScreen(); // تهيئة شاشة البداية
        initGameBoard(); // تهيئة لوحة اللعب

        mainPanel.add(startPanel, "start");
        mainPanel.add(boardPanel, "game");

        add(mainPanel);
        cardLayout.show(mainPanel, "start");

        setFocusable(true);
        requestFocusInWindow();

        // معالج لوحة المفاتيح لتحريك اللاعبين بالأسهم
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!rolesAssigned || isProcessingMove || gameOver) return; // إيقاف الحركة عند نهاية اللعبة
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> movePlayer(-1, 0);
                    case KeyEvent.VK_DOWN -> movePlayer(1, 0);
                    case KeyEvent.VK_LEFT -> movePlayer(0, -1);
                    case KeyEvent.VK_RIGHT -> movePlayer(0, 1);
                }
            }
        });
    }

    // تهيئة شاشة البداية (إدخال أسماء اللاعبين واختيار اللاعب الأول مع تعليمات)
    private void initStartScreen() {
        startPanel = new JPanel(new BorderLayout());

        // ملصق "السلام عليكم" في الأعلى
        JLabel greetingLabel = new JLabel("السلام عليكم", SwingConstants.CENTER);
        greetingLabel.setFont(new Font("Arial", Font.BOLD, 48));
        greetingLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // عنوان اللعبة
        JLabel title = new JLabel("<html><center>مرحباً بك في لعبة الذكاء<br>أدخل أسماء اللاعبين ثم اضغط ابدأ</center></html>", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setBorder(BorderFactory.createEmptyBorder(20, 10, 40, 10));

        // لوحة إدخال أسماء اللاعبين واختيار اللاعب الأول
        JPanel selectionPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel p1NameLabel = new JLabel("اسم اللاعب 1: ");
        p1NameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        JTextField p1NameField = new JTextField("اللاعب 1", 15);
        p1NameField.setFont(new Font("Arial", Font.PLAIN, 24));

        JLabel p2NameLabel = new JLabel("اسم اللاعب 2: ");
        p2NameLabel.setFont(new Font("Arial", Font.BOLD, 28));
        JTextField p2NameField = new JTextField("اللاعب 2", 15);
        p2NameField.setFont(new Font("Arial", Font.PLAIN, 24));

        JLabel selectLabel = new JLabel("اختر اللاعب الأول: ");
        selectLabel.setFont(new Font("Arial", Font.BOLD, 28));
        String[] playerOptions = {"اللاعب 1", "اللاعب 2"};
        JComboBox<String> playerSelector = new JComboBox<>(playerOptions);
        playerSelector.setFont(new Font("Arial", Font.PLAIN, 24));

        selectionPanel.add(p1NameLabel);
        selectionPanel.add(p1NameField);
        selectionPanel.add(p2NameLabel);
        selectionPanel.add(p2NameField);
        selectionPanel.add(selectLabel);
        selectionPanel.add(playerSelector);

        // زر بدء اللعبة
        JButton startButton = new JButton("ابدأ اللعبة");
        startButton.setFont(new Font("Arial", Font.BOLD, 28));
        startButton.setBackground(Color.ORANGE);
        startButton.setFocusable(false);

        startButton.addActionListener(e -> {
            if (!rolesAssigned) {
                player1Name = p1NameField.getText().trim().isEmpty() ? "اللاعب 1" : p1NameField.getText().trim();
                player2Name = p2NameField.getText().trim().isEmpty() ? "اللاعب 2" : p2NameField.getText().trim();
                playerOptions[0] = player1Name;
                playerOptions[1] = player2Name;
                playerSelector.repaint();
                playerTurn = playerSelector.getSelectedIndex() + 1;
                rolesAssigned = true;
                cardLayout.show(mainPanel, "game");
                updateBoard();
                requestFocusInWindow();
            }
        });

        // زر تعليمات اللعبة
        JButton instructionsButton = new JButton("تعليمات اللعبة");
        instructionsButton.setFont(new Font("Arial", Font.BOLD, 28));
        instructionsButton.setBackground(Color.LIGHT_GRAY);
        instructionsButton.setFocusable(false);

        instructionsButton.addActionListener(e -> {
            String instructions = "<html><body style='text-align: center; font-family: Arial; font-size: 16px;'>"
                    + "<h2>تعليمات اللعبة</h2>"
                    + "<p>1. اللعبة مخصصة للاعبين اثنين يتحركان على لوحة 9x9 باستخدام مفاتيح الأسهم (↑, ↓, ←, →).</p>"
                    + "<p>2. الهدف: جمع النقود (💰) للوصول إلى 2000 لتحويلها إلى حياة إضافية (❤️)، وتجنب خسارة الأرواح (🖤).</p>"
                    + "<p>3. عند الوقوف على خلية تحتوي على ❓، يجب الإجابة على سؤال. الإجابة الصحيحة تمنح 500 نقود، والخاطئة تطرح حياة.</p>"
                    + "<p>4. تحتوي اللوحة على جدران (لونها رمادي) لا يمكن المرور عبرها.</p>"
                    + "<p>5. يتم تبديل الأدوار بعد كل حركة أو إجابة. اللاعب الذي تنفد أرواحه (تصبح 0) يخسر، والآخر يفوز.</p>"
                    + "</body></html>";
            JOptionPane.showMessageDialog(this, instructions, "تعليمات اللعبة", JOptionPane.INFORMATION_MESSAGE);
        });

        // لوحة الأزرار في الأسفل
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonPanel.add(instructionsButton);
        buttonPanel.add(startButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(greetingLabel, BorderLayout.NORTH);
        topPanel.add(title, BorderLayout.CENTER);

        startPanel.add(topPanel, BorderLayout.NORTH);
        startPanel.add(selectionPanel, BorderLayout.CENTER);
        startPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    // تهيئة لوحة اللعب (إنشاء الخلايا، توزيع الجدران والتأثيرات)
    private void initGameBoard() {
        boardPanel = new JPanel(new BorderLayout());
        boardPanel.setBackground(new Color(245, 245, 220)); // خلفية بيج للوحة

        // إنشاء شبكة الخلايا (9x9 أزرار)
        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        Dimension cellSize = new Dimension(60, 60);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                JButton cell = new JButton();
                cell.setPreferredSize(cellSize);
                cell.setFont(new Font("Noto Color Emoji", Font.BOLD, 20));
                if (cell.getFont().canDisplayUpTo("💰") != -1) {
                    cell.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
                }
                cell.setFocusable(false);
                cells[i][j] = cell;
                gridPanel.add(cell);
            }
        }

        // توزيع الجدران والتأثيرات
        distributeWalls();
        assignInitialEffects();

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        boardPanel.add(scrollPane, BorderLayout.CENTER);

        // لوحة معلومات اللاعبين
        infoPanel = new JPanel(new GridLayout(2, 1));
        p1Info = new JLabel(player1Name + ": المال 0 JD - الأرواح: 5", SwingConstants.CENTER);
        p2Info = new JLabel(player2Name + ": المال 0 JD - الأرواح: 5", SwingConstants.CENTER);
        p1Info.setFont(new Font("Arial", Font.BOLD, 22));
        p2Info.setFont(new Font("Arial", Font.BOLD, 22));
        p1Info.setOpaque(true);
        p2Info.setOpaque(true);
        infoPanel.add(p1Info);
        infoPanel.add(p2Info);

        boardPanel.add(infoPanel, BorderLayout.NORTH);

        // ملصق التأثير (يظهر في الأسفل عند كشف خلية)
        effectLabel = new JLabel("", SwingConstants.CENTER);
        effectLabel.setFont(new Font("Noto Color Emoji", Font.BOLD, 30));
        if (effectLabel.getFont().canDisplayUpTo("💰") != -1) {
            effectLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 30));
        }
        effectLabel.setOpaque(true);
        effectLabel.setBackground(new Color(255, 255, 200));
        effectLabel.setVisible(false);
        boardPanel.add(effectLabel, BorderLayout.PAGE_END);
    }

    // توزيع 15 جدارًا عشوائيًا (لا تظهر في أول صفين أو عمودين)
    private void distributeWalls() {
        // تهيئة اللوحة بدون تأثيرات
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                boardEffects[i][j] = 1; // التأثير الافتراضي: نقود
                revealed[i][j] = false;
            }
        }

        // وضع 15 جدارًا في خلايا مسموحة (i >= 2 أو j >= 2)
        int wallsToPlace = 15;
        int placedWalls = 0;
        while (placedWalls < wallsToPlace) {
            int i = rand.nextInt(SIZE);
            int j = rand.nextInt(SIZE);
            if ((i >= 2 || j >= 2) && boardEffects[i][j] != 0 && !(i == 0 && j == 0)) {
                boardEffects[i][j] = 0; // جدار
                placedWalls++;
            }
        }
    }

    // توزيع التأثيرات (نقود، قلوب، أسئلة) على الخلايا غير الجدران
    private void assignInitialEffects() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (boardEffects[i][j] != 0) {
                    int[] effects = {1, 2, 3, 5}; // نقود، قلب، قلب أسود، سؤال
                    boardEffects[i][j] = effects[rand.nextInt(effects.length)];
                }
            }
        }
        // التأكد من أن الخلية (0,0) ليست جدارًا
        if (boardEffects[0][0] == 0) {
            int[] effects = {1, 2, 3, 5};
            boardEffects[0][0] = effects[rand.nextInt(effects.length)];
        }
    }

    // تحديث واجهة اللوحة (عرض الخلايا، اللاعبين، المعلومات)
    private void updateBoard() {
        // تحديث خلفية ونصوص الخلايا
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                JButton cell = cells[i][j];
                if (boardEffects[i][j] == 0) {
                    cell.setBackground(Color.DARK_GRAY);
                    cell.setText("");
                    cell.setForeground(Color.BLACK);
                    revealed[i][j] = true;
                } else {
                    cell.setBackground(new Color(240, 248, 255));
                    cell.setText("");
                    cell.setForeground(Color.BLACK);
                }
            }
        }

        // تحديث خلية اللاعب 1
        cells[player1[0]][player1[1]].setBackground(Color.GREEN);
        EffectInfo effectInfo1 = revealed[player1[0]][player1[1]] ? getEffectSymbol(boardEffects[player1[0]][player1[1]]) : new EffectInfo("", Color.BLACK);
        cells[player1[0]][player1[1]].setText(effectInfo1.symbol.isEmpty() ? "P1" : effectInfo1.symbol);
        cells[player1[0]][player1[1]].setForeground(effectInfo1.color);

        // تحديث خلية اللاعب 2
        cells[player2[0]][player2[1]].setBackground(Color.CYAN);
        EffectInfo effectInfo2 = revealed[player2[0]][player2[1]] ? getEffectSymbol(boardEffects[player2[0]][player2[1]]) : new EffectInfo("", Color.BLACK);
        cells[player2[0]][player2[1]].setText(effectInfo2.symbol.isEmpty() ? "P2" : effectInfo2.symbol);
        cells[player2[0]][player2[1]].setForeground(effectInfo2.color);

        // تحديث معلومات اللاعبين (المال، الأرواح، الدور)
        p1Info.setText(player1Name + ": المال " + player1Money + " JD - الأرواح: " + player1Lives + (playerTurn == 1 && !gameOver ? " ← دورك" : ""));
        p2Info.setText(player2Name + ": المال " + player2Money + " JD - الأرواح: " + player2Lives + (playerTurn == 2 && !gameOver ? " ← دورك" : ""));
        p1Info.setBackground(playerTurn == 1 && !gameOver ? Color.GREEN : Color.WHITE);
        p2Info.setBackground(playerTurn == 2 && !gameOver ? Color.CYAN : Color.WHITE);
    }

    // كائن لتخزين رمز ولون التأثير
    private static class EffectInfo {
        String symbol;
        Color color;

        EffectInfo(String symbol, Color color) {
            this.symbol = symbol;
            this.color = color;
        }
    }

    // إرجاع رمز ولون التأثير بناءً على رقم التأثير
    private EffectInfo getEffectSymbol(int effect) {
        return switch (effect) {
            case 1 -> new EffectInfo("💰", Color.YELLOW); // نقود
            case 2 -> new EffectInfo("❤️", Color.RED); // قلب
            case 3 -> new EffectInfo("🖤", Color.BLACK); // قلب أسود
            case 5 -> new EffectInfo("❓", Color.BLUE); // سؤال
            default -> new EffectInfo("", Color.BLACK);
        };
    }

    // تحريك اللاعب إلى خلية جديدة
    private void movePlayer(int dx, int dy) {
        if (isProcessingMove || gameOver) return; // إيقاف الحركة عند نهاية اللعبة
        isProcessingMove = true;

        int[] currentPlayer = playerTurn == 1 ? player1 : player2;
        int prevX = currentPlayer[0];
        int prevY = currentPlayer[1];
        int newX = prevX + dx;
        int newY = prevY + dy;

        // التحقق من حدود اللوحة وعدم الاصطدام بجدار
        if (newX < 0 || newX >= SIZE || newY < 0 || newY >= SIZE) {
            isProcessingMove = false;
            return;
        }
        if (boardEffects[newX][newY] == 0) {
            isProcessingMove = false;
            return;
        }

        currentPlayer[0] = newX;
        currentPlayer[1] = newY;

        // عرض رمز اللاعب أولاً
        cells[newX][newY].setText(playerTurn == 1 ? "P1" : "P2");
        cells[newX][newY].setForeground(Color.BLACK);
        updateBoard();

        // تأخير عرض رمز التأثير (0.5 ثانية)
        revealed[newX][newY] = true;
        EffectInfo effectInfo = getEffectSymbol(boardEffects[newX][newY]);
        Timer effectTimer = new Timer(500, e -> {
            cells[newX][newY].setText(effectInfo.symbol);
            cells[newX][newY].setForeground(effectInfo.color);
        });
        effectTimer.setRepeats(false);
        effectTimer.start();

        // معالجة تأثير الخلية
        handleCellEffect(newX, newY);

        // الاحتفاظ بالخلية السابقة بدون جدار
        revealed[prevX][prevY] = false;
        if (boardEffects[prevX][prevY] == 0) {
            int[] effects = {1, 2, 3, 5};
            boardEffects[prevX][prevY] = effects[rand.nextInt(effects.length)];
        }

        // إخفاء رمز التأثير بعد 1.2 ثانية
        Timer clearEffectTimer = new Timer(1200, e -> {
            revealed[newX][newY] = false;
            updateBoard();
        });
        clearEffectTimer.setRepeats(false);
        clearEffectTimer.start();
    }

    // معالجة تأثير الخلية (نقود، قلب، سؤال)
    private void handleCellEffect(int x, int y) {
        if (gameOver) return; // إيقاف التأثيرات عند نهاية اللعبة
        int effect = boardEffects[x][y];
        // تعيين تأثير جديد (غير جدار)
        int[] effects = {1, 2, 3, 5};
        boardEffects[x][y] = effects[rand.nextInt(effects.length)];

        EffectInfo effectInfo = getEffectSymbol(effect);
        int currentPlayerIndex = playerTurn; // حفظ اللاعب الحالي الذي داس على الخلية
        Runnable effectAction = switch (effect) {
            case 1 -> () -> {
                addMoney(1000, currentPlayerIndex); // تطبيق النقود على اللاعب الحالي
                isProcessingMove = false;
            };
            case 2 -> () -> {
                addLife(1, currentPlayerIndex); // تطبيق الحياة على اللاعب الحالي
                isProcessingMove = false;
            };
            case 3 -> () -> {
                addLife(-1, currentPlayerIndex); // تطبيق خسارة الحياة على اللاعب الحالي
                isProcessingMove = false;
            };
            case 5 -> this::askQuestion;
            default -> () -> {
                isProcessingMove = false;
            };
        };

        showEffect(effectInfo, effectAction);
        // تبديل الدور بعد تطبيق التأثير
        if (effect != 5) { // لا يتم تبديل الدور في حالة السؤال حتى يتم الإجابة
            playerTurn = (playerTurn == 1) ? 2 : 1;
        }
    }

    // عرض التأثير في ملصق الأسفل مع تأخير
    private void showEffect(EffectInfo effectInfo, Runnable effectAction) {
        if (gameOver) return; // إيقاف العرض عند نهاية اللعبة
        effectLabel.setText(effectInfo.symbol + " " + switch (effectInfo.symbol) {
            case "💰" -> "مال +1000";
            case "❤️" -> "حياة +1";
            case "🖤" -> "خسارة -1";
            case "❓" -> "سؤال";
            default -> "";
        });
        effectLabel.setForeground(effectInfo.color);
        effectLabel.setVisible(true);

        // إخفاء التأثير وتنفيذ الإجراء بعد 1.2 ثانية
        Timer timer = new Timer(1200, e -> {
            effectLabel.setVisible(false);
            effectAction.run();
            updateBoard();
        });
        timer.setRepeats(false);
        timer.start();
    }

    // إضافة نقود للاعب محدد (2000 نقود = حياة إضافية)
    private void addMoney(int amount, int playerIndex) {
        if (gameOver) return; // إيقاف الإضافة عند نهاية اللعبة
        if (playerIndex == 1) {
            player1Money += amount;
            if (player1Money >= 2000) {
                player1Money = 0;
                player1Lives += 1;
                JOptionPane.showMessageDialog(this, "تم استبدال 2000 نقود بـ 1 روح لـ " + player1Name + "!", "مبروك!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showEffect(new EffectInfo("💰", Color.YELLOW), () -> {});
            }
        } else {
            player2Money += amount;
            if (player2Money >= 2000) {
                player2Money = 0;
                player2Lives += 1;
                JOptionPane.showMessageDialog(this, "تم استبدال 2000 نقود بـ 1 روح لـ " + player2Name + "!", "مبروك!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showEffect(new EffectInfo("💰", Color.YELLOW), () -> {});
            }
        }
        updateBoard();
    }

    // إضافة أو خصم الأرواح للاعب محدد (الخسارة عند صفر أرواح)
    private void addLife(int amount, int playerIndex) {
        if (gameOver) return; // إيقاف التغيير عند نهاية اللعبة
        if (playerIndex == 1) {
            player1Lives += amount;
            // منع الأرواح من أن تصبح أقل من 0
            if (player1Lives < 0) player1Lives = 0;
            // تحديث اللوحة لعرض عدد الأرواح المحدث
            updateBoard();
            if (player1Lives == 0) {
                gameOver = true; // تعيين علامة النهاية
                int response = JOptionPane.showOptionDialog(
                    this,
                    player1Name + " خسر! انتهت اللعبة.\n" + player2Name + " هو الفائز!",
                    "نهاية اللعبة",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"العب مجدداً", "خروج"},
                    "العب مجدداً"
                );
                if (response == JOptionPane.YES_OPTION) {
                    resetGame();
                } else {
                    System.exit(0);
                }
            }
        } else {
            player2Lives += amount;
            // منع الأرواح من أن تصبح أقل من 0
            if (player2Lives < 0) player2Lives = 0;
            // تحديث اللوحة لعرض عدد الأرواح المحدث
            updateBoard();
            if (player2Lives == 0) {
                gameOver = true; // تعيين علامة النهاية
                int response = JOptionPane.showOptionDialog(
                    this,
                    player2Name + " خسر! انتهت اللعبة.\n" + player1Name + " هو الفائز!",
                    "نهاية اللعبة",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"العب مجدداً", "خروج"},
                    "العب مجدداً"
                );
                if (response == JOptionPane.YES_OPTION) {
                    resetGame();
                } else {
                    System.exit(0);
                }
            }
        }
    }

    // إعادة تعيين اللعبة (إرجاع اللاعبين والمتغيرات إلى الحالة الافتراضية)
    private void resetGame() {
        player1[0] = 0;
        player1[1] = 0;
        player2[0] = 0;
        player2[1] = 0;

        playerTurn = 1;
        player1Money = 0;
        player2Money = 0;
        player1Lives = 5;
        player2Lives = 5;
        rolesAssigned = false;
        gameOver = false; // إعادة تعيين علامة النهاية

        // إعادة توزيع الجدران والتأثيرات
        distributeWalls();
        assignInitialEffects();

        cardLayout.show(mainPanel, "start");
        updateBoard();
    }

    // طرح سؤال عشوائي والتحقق من الإجابة
    private void askQuestion() {
        if (gameOver) return; // إيقاف الأسئلة عند نهاية اللعبة
        /*
         * الأسئلة مقسمة إلى فئات: ثقافة عامة، جغرافيا، رياضيات، علوم، تاريخ، دينية إسلامية، متنوع.
         * تحتوي على 88 سؤالًا، مع زيادة الأسئلة الدينية (21 سؤالًا).
         * تم حذف الأسئلة التالية بناءً على الطلب:
         * - الأنديز، دانيانغ-كونشان، ليوناردو دا فينشي، ماري كوري، أوقيانوسيا، أوتاوا.
         * الإجابة الصحيحة تمنح 500 نقود، والخاطئة تطرح حياة.
         * الإجابات غير حساسة للحروف، والتواريخ/الحساب بالأرقام.
         */
        String[] questions = {
            // ثقافة عامة
            "ما هو لون السماء في يوم مشمس؟",
            "كم عدد أيام الأسبوع؟",
            "ما اسم الحيوان الذي يُعرف بالملك في الغابة؟",
            "كم عدد ألوان قوس قزح؟",
            "ما هو اسم أكبر محيط في العالم؟",
            "ما هو اسم أكبر كوكب في المجموعة الشمسية؟",
            "ما هو اسم أسرع حيوان بري في العالم؟",
            "ما هو اسم أكبر دولة من حيث المساحة؟",
            "ما هو اسم أكبر طائر في العالم؟",
            "ما هو اسم أكبر ديناصور عاش على الأرض؟",
            // جغرافيا
            "ما هي عاصمة الأردن؟",
            "ما هي عاصمة السعودية؟",
            "ما هي أكبر قارة؟",
            "ما هي أطول أنهار العالم؟",
            "ما هي عاصمة فرنسا؟",
            "ما هو أعلى جبل في العالم؟",
            "ما هي عاصمة اليابان؟",
            "ما هي عاصمة مصر؟",
            "ما هو اسم أكبر صحراء في العالم؟",
            "ما هي عاصمة البرازيل؟",
            "ما هي عاصمة الهند؟",
            // رياضيات
            "ما هو ناتج 2 + 3؟",
            "ما هو ناتج 5 × 2؟",
            "ما هو ناتج 12 ÷ 3؟",
            "كم عدد أضلاع المثلث؟",
            "ما هو ناتج 7 + 8؟",
            "ما هو ناتج 9 × 3؟",
            "ما هو ناتج 16 ÷ 4؟",
            "كم عدد أضلاع المربع؟",
            "ما هو ناتج 6 × 7؟",
            "ما هو ناتج 20 ÷ 5؟",
            "ما هو ناتج 15 + 9؟",
            "كم عدد أضلاع الخماسي؟",
            "ما هو ناتج 8 × 4؟",
            "ما هو ناتج 100 ÷ 10؟",
            // علوم
            "ما هو الغاز الذي يشكل معظم الغلاف الجوي للأرض؟",
            "ما هو الكوكب الأقرب إلى الشمس؟",
            "ما هو العنصر الكيميائي الذي رمزه H؟",
            "ما هو اسم الحيوان الذي يعيش في الماء ويتنفس بالخياشيم؟",
            "ما هو اسم العضو الذي يضخ الدم في جسم الإنسان؟",
            "ما هو الكوكب المعروف بالحلقات؟",
            "ما هو العنصر الكيميائي الذي رمزه O؟",
            "ما هو اسم أكبر عضو في جسم الإنسان؟",
            "ما هو اسم الحيوان الذي يُعرف بأنه أكبر مخلوق على الأرض؟",
            "ما هو اسم العالم الذي اكتشف قوانين الحركة؟",
            "ما هو اسم الكوكب الذي يُعرف باسم الكوكب الأحمر؟",
            "ما هو اسم الجهاز الذي يكتشف الزلازل؟",
            "ما هو اسم المادة التي تُستخدم في صناعة الأسلاك الكهربائية؟",
            // تاريخ
            "في أي عام بدأت الحرب العالمية الثانية؟",
            "من هو الخليفة الأول في الإسلام؟",
            "ما هي الحضارة التي بنت الأهرامات في مصر؟",
            "في أي عام اكتشف كريستوفر كولومبوس أمريكا؟",
            "من هو الملك الذي بنى تاج محل؟",
            "في أي عام بدأت الحرب العالمية الأولى؟",
            "من هو القائد الذي فتح القسطنطينية؟",
            "في أي عام تم إنشاء الأمم المتحدة؟",
            "من هو العالم الذي اخترع المصباح الكهربائي؟",
            "في أي عام هبط الإنسان على القمر لأول مرة؟",
            // دينية إسلامية
            "ما هو اسم أول سورة في القرآن الكريم؟",
            "كم عدد أركان الإسلام؟",
            "ما هو اسم الشهر الذي يصوم فيه المسلمون؟",
            "من هو النبي الذي بنى الكعبة مع ابنه؟",
            "ما هي الصلاة التي تُصلى في وقت الفجر؟",
            "كم عدد أركان الإيمان؟",
            "ما هو اسم المسجد الذي يقع في مكة المكرمة؟",
            "من هو الصحابي الذي لقب بأمين الأمة؟",
            "ما هو اسم النبي الذي أُرسل إلى قوم عاد؟",
            "ما هو اسم أول معركة في الإسلام؟",
            "ما هو اسم المسجد الذي يقع في القدس؟",
            "من هو النبي الذي أُلقي في النار فلم تحرقه؟",
            "ما هو اسم الصحابي الذي لقب بفاروق الأمة؟",
            "ما هو اسم السورة التي تُسمى بقلب القرآن؟",
            "ما هو اسم الشهر الذي يُحرم فيه الصيد؟",
            "ما هو اسم النبي الذي عاش في بطن الحوت؟",
            "ما هو اسم المعركة التي وقعت في السنة الثالثة للهجرة؟",
            "ما هو اسم الصحابي الذي كان أول من أسلم من الرجال؟",
            "ما هو اسم الجبل الذي نزل عليه القرآن؟",
            "ما هو اسم السورة التي تبدأ بـ'إنا أعطيناك الكوثر'؟",
            "ما هو اسم الصحابية التي لقبت بأم المؤمنين؟",
            // متنوع
            "ما هو الطائر الذي لا يطير؟",
            "ما هو اسم الآلة الموسيقية التي تحتوي على مفاتيح بيضاء وسوداء؟",
            "ما هو الشهر الذي يأتي بعد يناير؟",
            "ما هو اسم العملة الرسمية في اليابان؟",
            "ما هو اسم أكبر حيوان بري في العالم؟",
            "ما هو اسم أول رائد فضاء هبط على القمر؟",
            "ما هو اسم الرياضة التي تُلعب بكرة ومضرب على طاولة؟",
            "ما هو اسم الكاتب الذي كتب مسرحية روميو وجولييت؟",
            "ما هو اسم أكبر مدينة في العالم من حيث عدد السكان؟",
            "ما هو اسم الشركة التي اخترعت أول هاتف ذكي آيفون؟",
            "ما هو اسم الرياضة التي تُلعب بكرة قدم في ملعب عشبي؟"
        };
        String[] answers = {
            // ثقافة عامة
            "أزرق",
            "7",
            "أسد",
            "7",
            "الهادئ",
            "المشتري",
            "الفهد",
            "روسيا",
            "النعامة",
            "تي ريكس",
            // جغرافيا
            "عمان",
            "الرياض",
            "آسيا",
            "النيل",
            "باريس",
            "إيفرست",
            "طوكيو",
            "القاهرة",
            "الصحراء الكبرى",
            "برازيليا",
            "نيودلهي",
            // رياضيات
            "5",
            "10",
            "4",
            "3",
            "15",
            "27",
            "4",
            "4",
            "42",
            "4",
            "24",
            "5",
            "32",
            "10",
            // علوم
            "النيتروجين",
            "عطارد",
            "الهيدروجين",
            "سمكة",
            "القلب",
            "زحل",
            "الأكسجين",
            "الجلد",
            "الحوت الأزرق",
            "إسحاق نيوتن",
            "المريخ",
            "السيسموغراف",
            "النحاس",
            // تاريخ
            "1939",
            "أبو بكر",
            "الفرعونية",
            "1492",
            "شاه جهان",
            "1914",
            "محمد الفاتح",
            "1945",
            "توماس إديسون",
            "1969",
            // دينية إسلامية
            "الفاتحة",
            "5",
            "رمضان",
            "إبراهيم",
            "الفجر",
            "6",
            "المسجد الحرام",
            "أبو عبيدة",
            "هود",
            "بدر",
            "المسجد الأقصى",
            "إبراهيم",
            "عمر",
            "يس",
            "ذو الحجة",
            "يونس",
            "أحد",
            "أبو بكر",
            "نور",
            "الكوثر",
            "عائشة",
            // متنوع
            "بطريق",
            "بيانو",
            "فبراير",
            "ين",
            "الزرافة",
            "نيل أرمسترونغ",
            "تنس الطاولة",
            "ويليام شكسبير",
            "طوكيو",
            "آبل",
            "كرة القدم"
        };
        int index = rand.nextInt(questions.length);
        String answer = JOptionPane.showInputDialog(this, questions[index]);
        if (answer != null && answer.trim().equalsIgnoreCase(answers[index])) {
            JOptionPane.showMessageDialog(this, "أحسنت!", "إجابة صحيحة", JOptionPane.INFORMATION_MESSAGE);
            addMoney(500, playerTurn); // تطبيق النقود على اللاعب الحالي
        } else {
            JOptionPane.showMessageDialog(this, "إجابتك خاطئة، الإجابة الصحيحة هي: " + answers[index], "إجابة خاطئة", JOptionPane.ERROR_MESSAGE);
            addLife(-1, playerTurn); // تطبيق خسارة الحياة على اللاعب الحالي
        }
        if (!gameOver) { // تبديل الدور فقط إذا لم تنتهِ اللعبة
            playerTurn = (playerTurn == 1) ? 2 : 1;
        }
        isProcessingMove = false;
        updateBoard();
    }

    // نقطة البداية لتشغيل اللعبة
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameBoard11 gui = new GameBoard11();
            gui.setVisible(true);
        });
    }
}