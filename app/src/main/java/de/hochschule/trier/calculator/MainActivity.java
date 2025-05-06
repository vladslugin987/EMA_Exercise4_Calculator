package de.hochschule.trier.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    // UI components
    private TextView displayTextView;
    private TextView operationTextView;

    // Calculation variables
    private String currentNumber = "0";
    private String currentOperation = "";
    private double firstOperand = 0.0;
    private double secondOperand = 0.0;
    private boolean isNewOperation = true;
    private boolean lastInputWasOperation = false;
    private boolean decimalPointAdded = false;

    // For number formatting
    private DecimalFormat formatter = new DecimalFormat("#.##########");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        displayTextView = findViewById(R.id.displayTextView);
        operationTextView = findViewById(R.id.operationTextView);

        // Set initial display value
        updateDisplay();

        // Setup button click listeners
        setupButtonListeners();
    }

    /**
     * Sets up click listeners for all calculator buttons
     */
    private void setupButtonListeners() {
        // Number buttons
        for (int i = 0; i <= 9; i++) {
            int buttonId = getResources().getIdentifier("button" + i, "id", getPackageName());
            Button button = findViewById(buttonId);
            if (button != null) {
                button.setOnClickListener(this);
            }
        }

        // Standard operation buttons
        setupButtonIfExists(R.id.plusButton, this);
        setupButtonIfExists(R.id.minusButton, this);
        setupButtonIfExists(R.id.multiplyButton, this);
        setupButtonIfExists(R.id.divideButton, this);
        setupButtonIfExists(R.id.percentButton, this);
        setupButtonIfExists(R.id.equalsButton, this);
        setupButtonIfExists(R.id.decimalButton, this);
        setupButtonIfExists(R.id.signButton, this);

        // Scientific calculator buttons (only available in landscape)
        setupButtonIfExists(R.id.inverseButton, this);
        setupButtonIfExists(R.id.powerButton, this);
        setupButtonIfExists(R.id.sqrtButton, this);
        setupButtonIfExists(R.id.factorialButton, this);
        setupButtonIfExists(R.id.sinButton, this);
        setupButtonIfExists(R.id.cosButton, this);
        setupButtonIfExists(R.id.tanButton, this);
        setupButtonIfExists(R.id.piButton, this);
        setupButtonIfExists(R.id.logButton, this);
        setupButtonIfExists(R.id.expButton, this);

        // Clear button with special handling
        Button clearButton = findViewById(R.id.clearButton);
        if (clearButton != null) {
            clearButton.setOnClickListener(this);
            clearButton.setOnLongClickListener(this);
        }
    }

    /**
     * Helper method to set up button click listeners if the button exists
     */
    private void setupButtonIfExists(int buttonId, View.OnClickListener listener) {
        Button button = findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(listener);
        }
    }

    /**
     * Main click handler for all buttons
     */
    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        // Handle number buttons (0-9)
        for (int i = 0; i <= 9; i++) {
            int buttonId = getResources().getIdentifier("button" + i, "id", getPackageName());
            if (viewId == buttonId) {
                handleNumberInput(String.valueOf(i));
                return;
            }
        }

        // Handle basic calculator functions
        if (viewId == R.id.decimalButton) {
            handleDecimalPoint();
        } else if (viewId == R.id.clearButton) {
            handleClearButton();
        } else if (viewId == R.id.signButton) {
            handleSignToggle();
        } else if (viewId == R.id.percentButton) {
            handlePercent();
        } else if (viewId == R.id.equalsButton) {
            calculateResult();
        }
        // Handle basic operations
        else if (viewId == R.id.plusButton) {
            handleOperation("+");
        } else if (viewId == R.id.minusButton) {
            handleOperation("-");
        } else if (viewId == R.id.multiplyButton) {
            handleOperation("*");
        } else if (viewId == R.id.divideButton) {
            handleOperation("/");
        }
        // Handle scientific calculator functions
        else if (viewId == R.id.inverseButton) {
            handleInverse();
        } else if (viewId == R.id.powerButton) {
            handlePower();
        } else if (viewId == R.id.sqrtButton) {
            handleSquareRoot();
        } else if (viewId == R.id.factorialButton) {
            handleFactorial();
        } else if (viewId == R.id.sinButton) {
            handleSin();
        } else if (viewId == R.id.cosButton) {
            handleCos();
        } else if (viewId == R.id.tanButton) {
            handleTan();
        } else if (viewId == R.id.piButton) {
            handlePi();
        } else if (viewId == R.id.logButton) {
            handleLog();
        } else if (viewId == R.id.expButton) {
            handleExp();
        }
    }

    /**
     * Handles long click on the Clear button to clear everything
     */
    @Override
    public boolean onLongClick(View view) {
        if (view.getId() == R.id.clearButton) {
            resetCalculator();
            return true;
        }
        return false;
    }

    /**
     * Handles number input (0-9)
     */
    private void handleNumberInput(String number) {
        if (isNewOperation || currentNumber.equals("0")) {
            currentNumber = number;
            isNewOperation = false;
        } else {
            currentNumber += number;
        }
        lastInputWasOperation = false;
        updateDisplay();
    }

    /**
     * Handles decimal point input
     */
    private void handleDecimalPoint() {
        if (isNewOperation) {
            currentNumber = "0.";
            isNewOperation = false;
            decimalPointAdded = true;
        } else if (!decimalPointAdded) {
            currentNumber += ".";
            decimalPointAdded = true;
        }
        lastInputWasOperation = false;
        updateDisplay();
    }

    /**
     * Handles single clear operation (delete last character)
     */
    private void handleClearButton() {
        if (currentNumber.length() > 1) {
            // Remove last character
            if (currentNumber.endsWith(".")) {
                decimalPointAdded = false;
            }
            currentNumber = currentNumber.substring(0, currentNumber.length() - 1);
        } else {
            // Only one character, reset to 0
            currentNumber = "0";
            decimalPointAdded = false;
        }
        updateDisplay();
    }

    /**
     * Handles sign toggle (+/-)
     */
    private void handleSignToggle() {
        if (currentNumber.startsWith("-")) {
            currentNumber = currentNumber.substring(1);
        } else if (!currentNumber.equals("0")) {
            currentNumber = "-" + currentNumber;
        }
        updateDisplay();
    }

    /**
     * Handles percent operation
     */
    private void handlePercent() {
        try {
            double value = Double.parseDouble(currentNumber);
            value = value / 100.0;
            currentNumber = formatResult(value);
            decimalPointAdded = currentNumber.contains(".");
            updateDisplay();
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_format));
        }
    }

    /**
     * Handles mathematical operations (+, -, *, /)
     */
    private void handleOperation(String operation) {
        if (lastInputWasOperation) {
            // Replace previous operation
            currentOperation = operation;
            updateOperationDisplay();
        } else {
            // If there's a pending operation, calculate the result first
            if (!currentOperation.isEmpty()) {
                calculateResult();
            }

            try {
                firstOperand = Double.parseDouble(currentNumber);
                currentOperation = operation;
                isNewOperation = true;
                decimalPointAdded = false;
                lastInputWasOperation = true;
                updateOperationDisplay();
            } catch (NumberFormatException e) {
                showError(getString(R.string.error_invalid_format));
            }
        }
    }

    /**
     * Calculates and displays the result
     */
    private void calculateResult() {
        if (currentOperation.isEmpty()) {
            // Nothing to calculate
            return;
        }

        try {
            secondOperand = Double.parseDouble(currentNumber);
            double result = 0.0;

            // Perform calculation based on operation
            switch (currentOperation) {
                case "+":
                    result = firstOperand + secondOperand;
                    break;
                case "-":
                    result = firstOperand - secondOperand;
                    break;
                case "*":
                    result = firstOperand * secondOperand;
                    break;
                case "/":
                    if (secondOperand == 0) {
                        showError(getString(R.string.error_division_by_zero));
                        resetCalculator();
                        return;
                    }
                    result = firstOperand / secondOperand;
                    break;
            }

            // Format and display result
            currentNumber = formatResult(result);
            decimalPointAdded = currentNumber.contains(".");

            // Clear operation for new calculation
            String lastOperation = currentOperation;
            double lastFirstOperand = firstOperand;
            double lastSecondOperand = secondOperand;

            resetOperation();

            // Update operation display to show completed calculation
            operationTextView.setText(formatResult(lastFirstOperand) + " " + lastOperation + " " +
                    formatResult(lastSecondOperand) + " = " + currentNumber);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_format));
        }
    }

    // Scientific calculator function implementations

    /**
     * Handles inverse (1/x) function
     */
    private void handleInverse() {
        try {
            double value = Double.parseDouble(currentNumber);
            if (value == 0) {
                showError(getString(R.string.error_division_by_zero));
                return;
            }
            value = 1.0 / value;
            currentNumber = formatResult(value);
            isNewOperation = true;
            decimalPointAdded = currentNumber.contains(".");
            updateDisplay();
            operationTextView.setText("1/" + formatResult(1.0/value) + " = " + currentNumber);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_format));
        }
    }

    /**
     * Handles power (x²) function
     */
    private void handlePower() {
        try {
            double value = Double.parseDouble(currentNumber);
            double originalValue = value;
            value = value * value;
            currentNumber = formatResult(value);
            isNewOperation = true;
            decimalPointAdded = currentNumber.contains(".");
            updateDisplay();
            operationTextView.setText(formatResult(originalValue) + "² = " + currentNumber);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_format));
        }
    }

    /**
     * Handles square root (√) function
     */
    private void handleSquareRoot() {
        try {
            double value = Double.parseDouble(currentNumber);
            if (value < 0) {
                showError("Wurzel aus negativer Zahl nicht möglich!");
                return;
            }
            double originalValue = value;
            value = Math.sqrt(value);
            currentNumber = formatResult(value);
            isNewOperation = true;
            decimalPointAdded = currentNumber.contains(".");
            updateDisplay();
            operationTextView.setText("√(" + formatResult(originalValue) + ") = " + currentNumber);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_format));
        }
    }

    /**
     * Handles factorial (x!) function
     */
    private void handleFactorial() {
        try {
            double doubleValue = Double.parseDouble(currentNumber);
            int value = (int) doubleValue;

            // Check if the input is a valid non-negative integer
            if (doubleValue < 0 || doubleValue != value) {
                showError("Fakultät nur für nicht-negative ganzzahlige Werte!");
                return;
            }

            // Calculate factorial
            long factorial = 1;
            for (int i = 2; i <= value; i++) {
                factorial *= i;

                // Check for overflow
                if (factorial < 0) {
                    showError("Überlauf bei Fakultätsberechnung!");
                    return;
                }
            }

            currentNumber = formatResult(factorial);
            isNewOperation = true;
            decimalPointAdded = currentNumber.contains(".");
            updateDisplay();
            operationTextView.setText(value + "! = " + currentNumber);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_format));
        }
    }

    /**
     * Handles sine function
     */
    private void handleSin() {
        try {
            double value = Double.parseDouble(currentNumber);
            // Convert to radians if needed for calculation
            double result = Math.sin(Math.toRadians(value));
            currentNumber = formatResult(result);
            isNewOperation = true;
            decimalPointAdded = currentNumber.contains(".");
            updateDisplay();
            operationTextView.setText("sin(" + value + "°) = " + currentNumber);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_format));
        }
    }

    /**
     * Handles cosine function
     */
    private void handleCos() {
        try {
            double value = Double.parseDouble(currentNumber);
            // Convert to radians for calculation
            double result = Math.cos(Math.toRadians(value));
            currentNumber = formatResult(result);
            isNewOperation = true;
            decimalPointAdded = currentNumber.contains(".");
            updateDisplay();
            operationTextView.setText("cos(" + value + "°) = " + currentNumber);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_format));
        }
    }

    /**
     * Handles tangent function
     */
    private void handleTan() {
        try {
            double value = Double.parseDouble(currentNumber);
            // Check for invalid input (multiple of 90° - odd)
            if (value % 180 == 90) {
                showError("Tangens bei " + value + "° nicht definiert!");
                return;
            }
            // Convert to radians for calculation
            double result = Math.tan(Math.toRadians(value));
            currentNumber = formatResult(result);
            isNewOperation = true;
            decimalPointAdded = currentNumber.contains(".");
            updateDisplay();
            operationTextView.setText("tan(" + value + "°) = " + currentNumber);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_format));
        }
    }

    /**
     * Handles PI constant
     */
    private void handlePi() {
        currentNumber = formatResult(Math.PI);
        isNewOperation = true;
        decimalPointAdded = true;
        updateDisplay();
        operationTextView.setText("π = " + currentNumber);
    }

    /**
     * Handles logarithm (base 10) function
     */
    private void handleLog() {
        try {
            double value = Double.parseDouble(currentNumber);
            if (value <= 0) {
                showError("Logarithmus nur für positive Zahlen!");
                return;
            }
            double originalValue = value;
            value = Math.log10(value);
            currentNumber = formatResult(value);
            isNewOperation = true;
            decimalPointAdded = currentNumber.contains(".");
            updateDisplay();
            operationTextView.setText("log(" + formatResult(originalValue) + ") = " + currentNumber);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_format));
        }
    }

    /**
     * Handles exponential function (e^x)
     */
    private void handleExp() {
        try {
            double value = Double.parseDouble(currentNumber);
            double originalValue = value;
            value = Math.exp(value);
            currentNumber = formatResult(value);
            isNewOperation = true;
            decimalPointAdded = currentNumber.contains(".");
            updateDisplay();
            operationTextView.setText("e^(" + formatResult(originalValue) + ") = " + currentNumber);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_format));
        }
    }

    /**
     * Updates the number display
     */
    private void updateDisplay() {
        displayTextView.setText(currentNumber);
    }

    /**
     * Updates the operation display
     */
    private void updateOperationDisplay() {
        operationTextView.setText(formatResult(firstOperand) + " " + currentOperation);
    }

    /**
     * Formats a numeric result for display
     */
    private String formatResult(double value) {
        // Remove trailing zeros for whole numbers
        String formatted = formatter.format(value);
        return formatted;
    }

    /**
     * Resets operation-related variables
     */
    private void resetOperation() {
        currentOperation = "";
        isNewOperation = true;
        lastInputWasOperation = false;
    }

    /**
     * Completely resets the calculator (for long press on Clear)
     */
    private void resetCalculator() {
        currentNumber = "0";
        decimalPointAdded = false;
        resetOperation();
        firstOperand = 0.0;
        secondOperand = 0.0;
        operationTextView.setText("");
        updateDisplay();
    }

    /**
     * Shows an error message to the user
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}