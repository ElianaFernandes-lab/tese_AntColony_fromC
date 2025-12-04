package pythonuse.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

public class PythonExecutorExample_old {
    
    private String pythonPath;
    private String workingDirectory;
    private int timeoutSeconds;
    
    /**
     * Constructor - Automatically finds Python 3 on macOS
     */
    public PythonExecutorExample_old() {
        this.pythonPath = findPythonPath();
        this.workingDirectory = System.getProperty("user.dir");
        this.timeoutSeconds = 30;
    }
    
    /**
     * Find Python 3 path on macOS
     */
    private String findPythonPath() {
        String[] possiblePaths = {
            "/opt/homebrew/bin/python3",
            "/usr/local/bin/python3", 
            "/usr/bin/python3",
            "/Library/Frameworks/Python.framework/Versions/Current/bin/python3"
        };
        
        for (String path : possiblePaths) {
            File python = new File(path);
            if (python.exists() && python.canExecute()) {
                System.out.println("Found Python at: " + path);
                return path;
            }
        }
        
        System.out.println("Using python3 from PATH");
        return "python3";
    }
    
    /**
     * Execute a Python script with arguments
     */
    public String executeScript(String scriptPath, String... args) throws PythonExecutionException {
        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add(scriptPath);
        command.addAll(Arrays.asList(args));
        
        return executeCommand(command);
    }
    
    /**
     * Execute a Python script with JSON input
     */
    public String executeWithJson(String scriptPath, String jsonInput) throws PythonExecutionException {
        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add(scriptPath);
        command.add(jsonInput);
        
        return executeCommand(command);
    }
    
    /**
     * Call Calculator.py methods with proper JSON
     */
    public String callCalculatorMethod(String methodName, Object... args) throws PythonExecutionException {
        // Build JSON request
        JSONObject request = new JSONObject();
        request.put("method", methodName);
        request.put("args", args);
        
        String scriptPath = "src/pythonuse/examples/Calculator.py";
        return executeWithJson(scriptPath, request.toString());
    }
    
    /**
     * Execute Python code directly
     */
    public String executeCode(String pythonCode) throws PythonExecutionException {
        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add("-c");
        command.add(pythonCode);
        
        return executeCommand(command);
    }
    
    /**
     * Call a specific method in a Python class/module
     */
    public String callModuleMethod(String moduleName, String className, String methodName, Object... args) throws PythonExecutionException {
        StringBuilder pythonCode = new StringBuilder();
        
        // Import and create instance
        pythonCode.append("import ").append(moduleName).append("\n");
        pythonCode.append("import json\n");
        
        // Create instance if it's a class
        pythonCode.append("instance = ").append(moduleName).append(".")
                   .append(className).append("()\n");
        
        // Build argument string
        List<String> argStrings = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof String) {
                argStrings.add("\"" + arg.toString().replace("\"", "\\\"") + "\"");
            } else {
                argStrings.add(arg.toString());
            }
        }
        
        // Call method
        pythonCode.append("result = instance.").append(methodName)
                  .append("(").append(String.join(", ", argStrings)).append(")\n");
        
        // Return as JSON
        pythonCode.append("print(json.dumps({\"result\": result}))\n");
        
        return executeCode(pythonCode.toString());
    }
    
    /**
     * Execute command and handle output/errors
     */
    private String executeCommand(List<String> command) throws PythonExecutionException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(workingDirectory));
        pb.redirectErrorStream(true);
        
        StringBuilder output = new StringBuilder();
        
        try {
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroy();
                throw new PythonExecutionException("Python script timed out after " + timeoutSeconds + " seconds");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new PythonExecutionException("Python script failed with exit code: " + exitCode + 
                                                  "\nOutput: " + output.toString());
            }
            
        } catch (IOException | InterruptedException e) {
            throw new PythonExecutionException("Failed to execute Python script: " + e.getMessage(), e);
        }
        
        return output.toString().trim();
    }
    
    /**
     * Set working directory for Python scripts
     */
    public void setWorkingDirectory(String path) {
        this.workingDirectory = path;
    }
    
    /**
     * Set timeout in seconds
     */
    public void setTimeout(int seconds) {
        this.timeoutSeconds = seconds;
    }
    
    /**
     * Get Python path being used
     */
    public String getPythonPath() {
        return pythonPath;
    }
    
    /**
     * Custom exception for Python execution errors
     */
    public static class PythonExecutionException extends Exception {
        public PythonExecutionException(String message) {
            super(message);
        }
        
        public PythonExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Main method with corrected examples
     */
    public static void main(String[] args) {
        try {
            PythonExecutorExample_old executor = new PythonExecutorExample_old();
            
            System.out.println("Using Python at: " + executor.getPythonPath());
            System.out.println("Working directory: " + executor.workingDirectory);
            
            // ===== EXAMPLE 1: Call Calculator.py with JSON =====
            System.out.println("\n=== Example 1: Call Calculator Methods ===");
            
            // Test get_info method
            System.out.println("1. Getting calculator info:");
            String info = executor.callCalculatorMethod("get_info");
            System.out.println("Info: " + info);
            
            // Test add method
            System.out.println("\n2. Testing addition:");
            String addResult = executor.callCalculatorMethod("add", 10, 20);
            System.out.println("10 + 20 = " + addResult);
            
            // Test factorial
            System.out.println("\n3. Testing factorial:");
            String factResult = executor.callCalculatorMethod("factorial", 5);
            System.out.println("5! = " + factResult);
            
            // Test reverse string
            System.out.println("\n4. Testing string reverse:");
            String reverseResult = executor.callCalculatorMethod("reverse_string", "Hello Java!");
            System.out.println("Reversed: " + reverseResult);
            
            // Test with list
            System.out.println("\n5. Testing list processing:");
            String listResult = executor.callCalculatorMethod("process_list", 
                new Object[]{1, 2, 3, 4, 5});
            System.out.println("List stats: " + listResult);
            
            // ===== EXAMPLE 2: Direct Python code execution =====
            System.out.println("\n=== Example 2: Direct Python Code ===");
            String directResult = executor.executeCode("""
                import json
                result = {"message": "Hello from Python!", "sum": 5 + 3}
                print(json.dumps(result))
                """);
            System.out.println("Direct execution: " + directResult);
            
            // ===== EXAMPLE 3: Create and use MathOperations module =====
            System.out.println("\n=== Example 3: MathOperations Module ===");
            
            // First create the MathOperations module
            createMathOperationsModule();
            
            // Now call methods from it
            System.out.println("Testing MathOperations module:");
            
            // Method 1: Using the module directly
            String mathCode = """
                import MathOperations
                import json
                
                # Create instance and call method
                calc = MathOperations.MathOperations()
                result = calc.add(15, 25)
                
                print(json.dumps({
                    "operation": "15 + 25",
                    "result": result
                }))
                """;
            
            String mathResult = executor.executeCode(mathCode);
            System.out.println("Math result: " + mathResult);
            
            // Method 2: Using our helper method
            String multiplyResult = executor.callModuleMethod("MathOperations", 
                "MathOperations", "multiply", 7, 8);
            System.out.println("7 * 8 = " + multiplyResult);
            
            // ===== EXAMPLE 4: Batch operations =====
            System.out.println("\n=== Example 4: Batch Operations ===");
            
            String batchJson = """
            {
                "method": "batch",
                "operations": [
                    {"method": "add", "args": [100, 200]},
                    {"method": "multiply", "args": [12, 13]},
                    {"method": "sqrt", "args": [144]}
                ]
            }
            """;
            
            String scriptPath = "src/pythonuse/examples/Calculator.py";
            String batchResult = executor.executeWithJson(scriptPath, batchJson);
            System.out.println("Batch operations result: " + batchResult);
            
            System.out.println("\n✅ All tests completed successfully!");
            
        } catch (PythonExecutionException e) {
            System.err.println("\n❌ Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to create MathOperations.py module
     */
    private static void createMathOperationsModule() {
        try {
            String mathCode = """
                # MathOperations.py
                import math
                
                class MathOperations:
                    '''A simple math operations class'''
                    
                    def __init__(self, name="Math Calculator"):
                        self.name = name
                        self.history = []
                    
                    def add(self, a, b):
                        result = a + b
                        self.history.append(f"add({a}, {b}) = {result}")
                        return result
                    
                    def multiply(self, a, b):
                        result = a * b
                        self.history.append(f"multiply({a}, {b}) = {result}")
                        return result
                    
                    def subtract(self, a, b):
                        result = a - b
                        self.history.append(f"subtract({a}, {b}) = {result}")
                        return result
                    
                    def divide(self, a, b):
                        if b == 0:
                            raise ValueError("Cannot divide by zero")
                        result = a / b
                        self.history.append(f"divide({a}, {b}) = {result}")
                        return result
                    
                    def power(self, base, exponent):
                        result = math.pow(base, exponent)
                        self.history.append(f"power({base}, {exponent}) = {result}")
                        return result
                    
                    def factorial(self, n):
                        result = math.factorial(int(n))
                        self.history.append(f"factorial({n}) = {result}")
                        return result
                    
                    def get_history(self):
                        return self.history
                    
                    def clear_history(self):
                        self.history.clear()
                        return "History cleared"
                    
                    def get_info(self):
                        return {
                            "name": self.name,
                            "history_count": len(self.history),
                            "methods": [m for m in dir(self) if not m.startswith('_') and callable(getattr(self, m))]
                        }
                
                # For direct execution
                if __name__ == "__main__":
                    calc = MathOperations()
                    print(f"15 + 25 = {calc.add(15, 25)}")
                    print(f"7 * 8 = {calc.multiply(7, 8)}")
                    print(f"History: {calc.get_history()}")
                """;
            
            Files.write(Path.of("MathOperations.py"), mathCode.getBytes());
            System.out.println("Created MathOperations.py module");
            
        } catch (IOException e) {
            System.err.println("Failed to create MathOperations.py: " + e.getMessage());
        }
    }
}