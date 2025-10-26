import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

public class Calculator extends JFrame implements ActionListener {
    private final JTextField display;
    private String currentOperator = "";
    private double storedValue = 0;
    private boolean startNewNumber = true; // if true, next digit replaces the display
    private final DecimalFormat df = new DecimalFormat("0.##########"); // up to 10 decimal places

    public Calculator() {
        setTitle("Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(320, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(6, 6));
        setResizable(false);

        display = new JTextField("0");
        display.setFont(new Font("SansSerif", Font.BOLD, 28));
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setEditable(false);
        display.setBackground(Color.WHITE);
        display.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(display, BorderLayout.NORTH);

        // Buttons panel
        JPanel buttons = new JPanel(new GridLayout(5, 4, 6, 6));
        String[] labels = {
            "C", "←", "/", "*",
            "7", "8", "9", "-",
            "4", "5", "6", "+",
            "1", "2", "3", "=",
            "0", "0", ".", "=" // We will handle layout for wide 0; duplicate placeholder removed in code below
        };

        // We'll construct a better layout manually so 0 spans two columns
        buttons.removeAll();
        String[] row1 = {"C", "←", "/", "*"};
        String[] row2 = {"7", "8", "9", "-"};
        String[] row3 = {"4", "5", "6", "+"};
        String[] row4 = {"1", "2", "3", "="};
        String[] row5 = {"0", ".", "", ""}; // we'll add an empty placeholder to keep grid consistent

        addRow(buttons, row1);
        addRow(buttons, row2);
        addRow(buttons, row3);
        addRow(buttons, row4);
        addRow(buttons, row5);

        // Instead of spanning cells in GridLayout, we'll wrap with another panel for a larger 0 button.
        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.add(buttons, BorderLayout.CENTER);

        // Build a custom bottom row with a wide 0 and dot and equals
        JPanel bottomRow = new JPanel(new GridLayout(1, 3, 6, 6));
        JButton zero = createButton("0");
        zero.setFont(new Font("SansSerif", Font.PLAIN, 20));
        JButton dot = createButton(".");
        JButton equals = createButton("=");

        bottomRow.add(zero);
        bottomRow.add(dot);
        bottomRow.add(equals);

        // Remove the last row placeholders and instead add the bottomRow
        add(center, BorderLayout.CENTER);
        add(bottomRow, BorderLayout.SOUTH);

        // Final tweaks: pack children, keep size consistent
        setVisible(true);
    }

    // Helper to add a row of buttons to a panel
    private void addRow(JPanel panel, String[] labels) {
        for (String lbl : labels) {
            if (lbl == null || lbl.isEmpty()) {
                panel.add(new JLabel()); // placeholder
            } else {
                panel.add(createButton(lbl));
            }
        }
    }

    // Helper to create a styled button
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 20));
        btn.addActionListener(this);
        return btn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if ("0123456789".contains(cmd)) {
            appendDigit(cmd);
        } else if (cmd.equals(".")) {
            appendDecimalPoint();
        } else if (cmd.equals("C")) {
            clearAll();
        } else if (cmd.equals("←")) {
            backspace();
        } else if ("+-*/".contains(cmd)) {
            applyOperator(cmd);
        } else if (cmd.equals("=")) {
            calculateResult();
        }
    }

    private void appendDigit(String d) {
        if (startNewNumber) {
            display.setText(d);
            startNewNumber = false;
        } else {
            // Avoid leading zeros like "000"
            if (display.getText().equals("0")) {
                display.setText(d);
            } else {
                display.setText(display.getText() + d);
            }
        }
    }

    private void appendDecimalPoint() {
        if (startNewNumber) {
            display.setText("0.");
            startNewNumber = false;
        } else {
            if (!display.getText().contains(".")) {
                display.setText(display.getText() + ".");
            }
        }
    }

    private void clearAll() {
        display.setText("0");
        currentOperator = "";
        storedValue = 0;
        startNewNumber = true;
    }

    private void backspace() {
        if (startNewNumber) {
            // nothing to backspace; keep 0
            display.setText("0");
            startNewNumber = true;
            return;
        }
        String text = display.getText();
        if (text.length() > 1) {
            display.setText(text.substring(0, text.length() - 1));
        } else {
            display.setText("0");
            startNewNumber = true;
        }
    }

    private void applyOperator(String op) {
        try {
            double current = Double.parseDouble(display.getText());
            if (!currentOperator.isEmpty()) {
                // chain calculation: compute storedValue (op) current
                storedValue = compute(storedValue, current, currentOperator);
                display.setText(df.format(storedValue));
            } else {
                storedValue = current;
            }
            currentOperator = op;
            startNewNumber = true;
        } catch (NumberFormatException ex) {
            display.setText("Error");
            startNewNumber = true;
            currentOperator = "";
        } catch (ArithmeticException ex) {
            display.setText("Error");
            startNewNumber = true;
            currentOperator = "";
        }
    }

    private void calculateResult() {
        if (currentOperator.isEmpty()) {
            // nothing to compute
            return;
        }
        try {
            double current = Double.parseDouble(display.getText());
            double result = compute(storedValue, current, currentOperator);
            display.setText(df.format(result));
            currentOperator = "";
            storedValue = 0;
            startNewNumber = true;
        } catch (NumberFormatException ex) {
            display.setText("Error");
            startNewNumber = true;
            currentOperator = "";
        } catch (ArithmeticException ex) {
            display.setText("Error: " + ex.getMessage());
            startNewNumber = true;
            currentOperator = "";
        }
    }

    private double compute(double a, double b, String op) {
        switch (op) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) throw new ArithmeticException("Divide by zero");
                return a / b;
            default:
                throw new IllegalArgumentException("Unknown operator");
        }
    }

    public static void main(String[] args) {
        // Ensure GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new Calculator());
    }
}
