package de.hochschule.trier.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    // ui components
    private TextView displayTextView;
    private TextView operationTextView;
    private GridLayout portraitButtonGrid;
    private GridLayout landscapeButtonGrid;

    // variables for calculations
    private StringBuilder currentExpression = new StringBuilder();
    private boolean isNewExpression = true;
    private boolean errorState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find all the views we need
        displayTextView = findViewById(R.id.displayTextView);
        operationTextView = findViewById(R.id.operationTextView);
        portraitButtonGrid = findViewById(R.id.portraitButtonGrid);
        landscapeButtonGrid = findViewById(R.id.landscapeButtonGrid);

        // start with 0
        displayTextView.setText("0");

        // check if we're in portrait or landscape
        updateLayoutForOrientation(getResources().getConfiguration().orientation);

        // set up our button click handlers
        setupButtonListeners();

        // test some expressions - leave this here in case we need to test again
        /*
        String[] testExpressions = {
            "2+2",
            "sin(pi/2)",
            "log10(100)",
            "3^2",
            "sqrt(16)",
            "5*(3+2)"
        };

        for (String expr : testExpressions) {
            try {
                double result = evaluateExpression(expr);
                Log.d("CALC_TEST", expr + " = " + result);
            } catch (Exception e) {
                Log.e("CALC_TEST", "Error calculating " + expr + ": " + e.getMessage());
            }
        }
        */
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // show the right buttons when user rotates phone
        updateLayoutForOrientation(newConfig.orientation);
    }

    /**
     * changes which buttons are visible based on phone orientation
     */
    private void updateLayoutForOrientation(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // landscape mode: show scientific calculator
            portraitButtonGrid.setVisibility(View.GONE);
            landscapeButtonGrid.setVisibility(View.VISIBLE);
        } else {
            // portrait mode: show regular calculator
            portraitButtonGrid.setVisibility(View.VISIBLE);
            landscapeButtonGrid.setVisibility(View.GONE);
        }
    }

    /**
     * sets up all the button click handlers
     */
    private void setupButtonListeners() {
        // portrait buttons
        setupPortraitButtonListeners();

        // landscape buttons with scientific stuff
        setupLandscapeButtonListeners();
    }

    /**
     * portrait mode buttons setup
     */
    private void setupPortraitButtonListeners() {
        // number buttons 0-9
        for (int i = 0; i <= 9; i++) {
            int buttonId = getResources().getIdentifier("button" + i, "id", getPackageName());
            setupButtonIfExists(buttonId, this);
        }

        // math operation buttons
        setupButtonIfExists(R.id.plusButton, this);
        setupButtonIfExists(R.id.minusButton, this);
        setupButtonIfExists(R.id.multiplyButton, this);
        setupButtonIfExists(R.id.divideButton, this);
        setupButtonIfExists(R.id.percentButton, this);
        setupButtonIfExists(R.id.equalsButton, this);
        setupButtonIfExists(R.id.decimalButton, this);
        setupButtonIfExists(R.id.signButton, this);

        // clear button needs special handling for long press
        Button clearButton = findViewById(R.id.clearButton);
        if (clearButton != null) {
            clearButton.setOnClickListener(this);
            clearButton.setOnLongClickListener(this);
        }
    }

    /**
     * landscape mode buttons setup (includes scientific functions)
     */
    private void setupLandscapeButtonListeners() {
        // number buttons but with Land suffix
        for (int i = 0; i <= 9; i++) {
            int buttonId = getResources().getIdentifier("button" + i + "Land", "id", getPackageName());
            setupButtonIfExists(buttonId, this);
        }

        // math operations for landscape
        setupButtonIfExists(R.id.plusButtonLand, this);
        setupButtonIfExists(R.id.minusButtonLand, this);
        setupButtonIfExists(R.id.multiplyButtonLand, this);
        setupButtonIfExists(R.id.divideButtonLand, this);
        setupButtonIfExists(R.id.percentButtonLand, this);
        setupButtonIfExists(R.id.equalsButtonLand, this);
        setupButtonIfExists(R.id.decimalButtonLand, this);
        setupButtonIfExists(R.id.signButtonLand, this);

        // scientific calculator buttons
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

        // clear button for landscape needs long press too
        Button clearButtonLand = findViewById(R.id.clearButtonLand);
        if (clearButtonLand != null) {
            clearButtonLand.setOnClickListener(this);
            clearButtonLand.setOnLongClickListener(this);
        }
    }

    /**
     * helper to set up button click handler if button exists
     */
    private void setupButtonIfExists(int buttonId, View.OnClickListener listener) {
        Button button = findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(listener);
        }
    }

    /**
     * main click handler for all buttons
     */
    @Override
    public void onClick(View view) {
        // clear error state if there was one
        if (errorState) {
            currentExpression = new StringBuilder();
            errorState = false;
        }

        int viewId = view.getId();

        // handle number buttons first
        if (handleNumberButtonIfMatches(viewId)) {
            return;
        }

        // handle clear button in both layouts
        if (viewId == R.id.clearButton || viewId == R.id.clearButtonLand) {
            handleClearButton();
            return;
        }

        // handle equals button in both layouts
        if (viewId == R.id.equalsButton || viewId == R.id.equalsButtonLand) {
            calculateResult();
            return;
        }

        // handle + - * / buttons
        if (handleBasicOperations(viewId)) {
            return;
        }

        // handle sin, cos, etc in landscape
        handleScientificOperations(viewId);
    }

    /**
     * checks if a number button was clicked and handles it
     * @return true if we handled a number button
     */
    private boolean handleNumberButtonIfMatches(int viewId) {
        // check all the number buttons
        for (int i = 0; i <= 9; i++) {
            int portraitId = getResources().getIdentifier("button" + i, "id", getPackageName());
            int landscapeId = getResources().getIdentifier("button" + i + "Land", "id", getPackageName());

            if (viewId == portraitId || viewId == landscapeId) {
                addToExpression(String.valueOf(i));
                return true;
            }
        }

        // check for decimal point too
        if (viewId == R.id.decimalButton || viewId == R.id.decimalButtonLand) {
            // don't let user add multiple decimal points in one number
            if (!lastNumberContainsDecimal()) {
                addToExpression(".");
            }
            return true;
        }

        return false;
    }

    /**
     * checks if the current number already has a decimal point
     */
    private boolean lastNumberContainsDecimal() {
        String expr = currentExpression.toString();
        int lastOperatorIndex = Math.max(
                Math.max(expr.lastIndexOf('+'), expr.lastIndexOf('-')),
                Math.max(expr.lastIndexOf('*'), expr.lastIndexOf('/'))
        );

        return expr.substring(lastOperatorIndex + 1).contains(".");
    }

    /**
     * handles + - * / % operations
     * @return true if handled one of these operations
     */
    private boolean handleBasicOperations(int viewId) {
        // make sure there's something to work with
        if (currentExpression.length() == 0) {
            if (viewId == R.id.minusButton || viewId == R.id.minusButtonLand) {
                // special case: allow minus sign for negative numbers
                addToExpression("-");
                return true;
            }
            return false;
        }

        // handle basic math operators
        if (viewId == R.id.plusButton || viewId == R.id.plusButtonLand) {
            addToExpression("+");
            return true;
        } else if (viewId == R.id.minusButton || viewId == R.id.minusButtonLand) {
            addToExpression("-");
            return true;
        } else if (viewId == R.id.multiplyButton || viewId == R.id.multiplyButtonLand) {
            addToExpression("*");
            return true;
        } else if (viewId == R.id.divideButton || viewId == R.id.divideButtonLand) {
            addToExpression("/");
            return true;
        } else if (viewId == R.id.percentButton || viewId == R.id.percentButtonLand) {
            addToExpression("/100");
            return true;
        } else if (viewId == R.id.signButton || viewId == R.id.signButtonLand) {
            toggleSign();
            return true;
        }

        return false;
    }

    /**
     * handles scientific calculator operations
     */
    private void handleScientificOperations(int viewId) {
        if (viewId == R.id.sqrtButton) {
            addFunction("sqrt(", ")");
        } else if (viewId == R.id.sinButton) {
            addFunction("sin(", ")");

            /*
            // this was for converting degrees to radians but exp4j already uses radians
            // if we want to use degrees later, we can uncomment this
            try {
                double value = Double.parseDouble(currentExpression.toString());
                // Convert to radians
                double radians = Math.toRadians(value);
                double result = Math.sin(radians);
                currentExpression = new StringBuilder(String.valueOf(result));
                updateDisplay();
                operationTextView.setText("sin(" + value + "°) = " + result);
            } catch (Exception e) {
                addFunction("sin(", ")");
            }
            */
        } else if (viewId == R.id.cosButton) {
            addFunction("cos(", ")");
        } else if (viewId == R.id.tanButton) {
            addFunction("tan(", ")");
        } else if (viewId == R.id.logButton) {
            addFunction("log10(", ")");
        } else if (viewId == R.id.expButton) {
            addFunction("exp(", ")");
        } else if (viewId == R.id.powerButton) {
            addToExpression("^2");
        } else if (viewId == R.id.inverseButton) {
            addToExpression("^(-1)");
        } else if (viewId == R.id.factorialButton) {
            // need to calculate it separately
            calculateFactorial();
        } else if (viewId == R.id.piButton) {
            // add pi constant
            addToExpression("pi");
        }
    }

    /**
     * adds a function wrapper around the current expression
     */
    private void addFunction(String prefix, String suffix) {
        // if we have something already, wrap it in the function
        if (currentExpression.length() > 0) {
            try {
                // try to calculate what we have first, then apply function
                double result = evaluateExpression(currentExpression.toString());
                currentExpression = new StringBuilder(prefix + result + suffix);
            } catch (Exception e) {
                // if that fails, just wrap everything in the function
                currentExpression.insert(0, prefix);
                currentExpression.append(suffix);
            }
        } else {
            // otherwise just put the function with empty parentheses
            currentExpression = new StringBuilder(prefix + suffix);
        }
        updateDisplay();
    }

    /**
     * calculates factorial for the current expression
     */
    private void calculateFactorial() {
        try {
            // first evaluate whatever's there
            double value = evaluateExpression(currentExpression.toString());
            int intValue = (int) value;

            // factorials need to be integers
            if (value != intValue || intValue < 0) {
                showError("Factorial only works with non-negative integers");
                return;
            }

            // calculate factorial the simple way
            long factorial = 1;
            for (int i = 2; i <= intValue; i++) {
                factorial *= i;
                if (factorial < 0) {
                    showError("Factorial result too large");
                    return;
                }
            }

            currentExpression = new StringBuilder(String.valueOf(factorial));
            updateDisplay();
            operationTextView.setText(intValue + "! = " + factorial);
        } catch (Exception e) {
            showError("Error calculating factorial: " + e.getMessage());
        }
    }

    /**
     * adds text to the expression we're building
     */
    private void addToExpression(String text) {
        if (isNewExpression) {
            currentExpression = new StringBuilder();
            isNewExpression = false;
        }

        currentExpression.append(text);
        updateDisplay();
    }

    /**
     * toggles between positive and negative
     */
    private void toggleSign() {
        if (currentExpression.length() == 0) {
            return;
        }

        String expr = currentExpression.toString();

        // need to find the last operator to toggle only the current number
        int lastOpIndex = Math.max(
                Math.max(expr.lastIndexOf('+'), expr.lastIndexOf('-')),
                Math.max(expr.lastIndexOf('*'), expr.lastIndexOf('/'))
        );

        // check if toggling first number or one after an operator
        if (lastOpIndex == -1) {
            // no operator, so toggle the whole thing
            if (expr.startsWith("-")) {
                currentExpression.deleteCharAt(0);
            } else {
                currentExpression.insert(0, "-");
            }
        } else {
            // we have an operator, toggle just the number after it
            if (lastOpIndex + 1 < expr.length() && expr.charAt(lastOpIndex + 1) == '-') {
                // already has minus, remove it
                currentExpression.deleteCharAt(lastOpIndex + 1);
            } else {
                // add minus sign after the operator
                currentExpression.insert(lastOpIndex + 1, "-");
            }
        }

        updateDisplay();
    }

    /**
     * handles backspace (clear button press)
     */
    private void handleClearButton() {
        if (currentExpression.length() > 0) {
            currentExpression.deleteCharAt(currentExpression.length() - 1);

            if (currentExpression.length() == 0) {
                displayTextView.setText("0");
                isNewExpression = true;
            } else {
                updateDisplay();
            }
        } else {
            displayTextView.setText("0");
            isNewExpression = true;
        }
    }

    /**
     * handles long click on clear button (reset everything)
     */
    @Override
    public boolean onLongClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.clearButton || viewId == R.id.clearButtonLand) {
            resetCalculator();
            return true;
        }
        return false;
    }

    /**
     * calculates and shows the result using exp4j
     */
    private void calculateResult() {
        if (currentExpression.length() == 0) {
            return;
        }

        String expressionText = currentExpression.toString();

        try {
            // save what we're calculating for history
            String originalExpression = expressionText;

            // do the math
            double result = evaluateExpression(expressionText);

            /*
            // I was trying to fix an issue where Calculator gave different results than Google Calculator
            // Turns out Google uses degrees for sin/cos/tan but exp4j uses radians - leaving this here
            // in case someone else runs into this problem
            if (expressionText.contains("sin") || expressionText.contains("cos") || expressionText.contains("tan")) {
                Log.d("CALC_DEBUG", "Original result: " + result);
                // Check if we should round to zero (floating point errors)
                if (Math.abs(result) < 1E-10) {
                    result = 0.0;
                    Log.d("CALC_DEBUG", "Rounded to zero");
                }
            }
            */

            // show the result
            currentExpression = new StringBuilder(String.valueOf(result));
            isNewExpression = true;
            updateDisplay();

            // show the calculation in history
            operationTextView.setText(originalExpression + " = " + result);
        } catch (Exception e) {
            showError("Calculation error: " + e.getMessage());
            errorState = true;
        }
    }

    /**
     * uses exp4j to do the math
     */
    private double evaluateExpression(String expressionText) {
        // set up the expression with exp4j
        Expression expression = new ExpressionBuilder(expressionText)
                .variables("pi", "e")
                .build()
                .setVariable("pi", Math.PI)
                .setVariable("e", Math.E);

        /*
        // I was testing other ways to handle expressions before switching to exp4j
        // This was my attempt using the Android built-in evaluator (it's limited)
        // Might be useful to compare results if we find bugs
        try {
            String jsExpression = expressionText.replace("pi", "Math.PI")
                                             .replace("e", "Math.E")
                                             .replace("sin", "Math.sin")
                                             .replace("cos", "Math.cos")
                                             .replace("tan", "Math.tan")
                                             .replace("log10", "Math.log10")
                                             .replace("sqrt", "Math.sqrt")
                                             .replace("exp", "Math.exp");

            Context rhino = Context.enter();
            rhino.setOptimizationLevel(-1);
            Scriptable scope = rhino.initStandardObjects();

            Object result = rhino.evaluateString(scope, jsExpression, "JavaScript", 1, null);
            double jsResult = Context.toNumber(result);
            Log.d("CALC_COMPARE", "JS result: " + jsResult + " vs exp4j: " + expression.evaluate());

            return jsResult;
        } catch (Exception e) {
            Log.e("CALC_DEBUG", "JS eval failed, using exp4j: " + e.getMessage());
        }
        */

        // let exp4j do the work
        return expression.evaluate();
    }

    /**
     * updates the display with current expression
     */
    private void updateDisplay() {
        if (currentExpression.length() > 0) {
            displayTextView.setText(currentExpression.toString());
        } else {
            displayTextView.setText("0");
        }
    }

    /**
     * resets the calculator (called for long press on clear)
     */
    private void resetCalculator() {
        currentExpression = new StringBuilder();
        isNewExpression = true;
        errorState = false;
        displayTextView.setText("0");
        operationTextView.setText("");
    }

    /**
     * shows an error message
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        operationTextView.setText("Error: " + message);
    }
}