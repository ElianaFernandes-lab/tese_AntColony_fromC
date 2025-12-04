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

public class PythonExecutorExample {
    
    private String pythonPath;
    private String workingDirectory;
    private int timeoutSeconds;
    
    public PythonExecutorExample() {
        this.pythonPath = findPythonPath();
        this.workingDirectory = System.getProperty("user.dir");
        this.timeoutSeconds = 30;
    }
    
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
    
    public String executeScript(String scriptPath, String... args) throws PythonExecutionException {
        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add(scriptPath);
        command.addAll(Arrays.asList(args));
        return executeCommand(command);
    }
    
    public String executeWithJson(String scriptPath, String jsonInput) throws PythonExecutionException {
        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add(scriptPath);
        command.add(jsonInput);
        return executeCommand(command);
    }
    
    public String callCalculatorMethod(String methodName, Object... args) throws PythonExecutionException {
        StringBuilder json = new StringBuilder();
        json.append("{\"method\":\"").append(methodName).append("\"");
        
        if (args.length > 0) {
            json.append(",\"args\":[");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) json.append(",");
                appendJsonValue(json, args[i]);
            }
            json.append("]");
        } else {
            json.append(",\"args\":[]");
        }
        
        json.append("}");
        
        String scriptPath = "Calculator.py";
        return executeWithJson(scriptPath, json.toString());
    }
    
    private void appendJsonValue(StringBuilder json, Object value) {
        if (value == null) {
            json.append("null");
        } else if (value instanceof String) {
            json.append("\"").append(escapeJson((String) value)).append("\"");
        } else if (value instanceof Number || value instanceof Boolean) {
            json.append(value.toString());
        } else if (value instanceof Object[]) {
            json.append("[");
            Object[] arr = (Object[]) value;
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) json.append(",");
                appendJsonValue(json, arr[i]);
            }
            json.append("]");
        } else {
            json.append("\"").append(escapeJson(value.toString())).append("\"");
        }
    }
    
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    public String executeCode(String pythonCode) throws PythonExecutionException {
        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add("-c");
        command.add(pythonCode);
        return executeCommand(command);
    }
    
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
    
    public void setWorkingDirectory(String path) {
        this.workingDirectory = path;
    }
    
    public void setTimeout(int seconds) {
        this.timeoutSeconds = seconds;
    }
    
    public String getPythonPath() {
        return pythonPath;
    }
    
    public static class PythonExecutionException extends Exception {
        public PythonExecutionException(String message) {
            super(message);
        }
        
        public PythonExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static void main(String[] args) {
        try {
        	PythonExecutorExample executor = new PythonExecutorExample();
            
            System.out.println("Java Version: " + System.getProperty("java.version"));
            System.out.println("Using Python at: " + executor.getPythonPath());
            System.out.println("Working directory: " + executor.workingDirectory);
            
            // Create Calculator.py first
            createCalculatorScript();
            
            // Example 1: Simple Python code
            System.out.println("\n=== Example 1: Simple Python Code ===");
            String result1 = executor.executeCode("print('Hello from Python!')");
            System.out.println("Result: " + result1);
            
            // Example 2: Math operations
            System.out.println("\n=== Example 2: Math Operations ===");
            String result2 = executor.executeCode(
                "import json; print(json.dumps({'sum': 10 + 20, 'product': 5 * 6}))"
            );
            System.out.println("Result: " + result2);
            
            // Example 3: Call Calculator methods
            System.out.println("\n=== Example 3: Calculator Methods ===");
            
            String addResult = executor.callCalculatorMethod("add", 15, 25);
            System.out.println("15 + 25 = " + addResult);
            
            String factResult = executor.callCalculatorMethod("factorial", 5);
            System.out.println("5! = " + factResult);
            
            String reverseResult = executor.callCalculatorMethod("reverse_string", "Hello Java!");
            System.out.println("Reversed: " + reverseResult);
            
            // Example 4: Process list
            System.out.println("\n=== Example 4: Process List ===");
            // Wrap the array in another array to pass it as a single argument
            Object[] listArg = new Object[]{new Object[]{1, 2, 3, 4, 5}};
            String listResult = executor.callCalculatorMethod("process_list", listArg);
            System.out.println("List stats: " + listResult);
            
            System.out.println("\n✅ All tests completed successfully!");
            
        } catch (PythonExecutionException e) {
            System.err.println("\n❌ Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createCalculatorScript() {
        try {
            // Using string concatenation (Java 11 compatible)
            String calculatorCode = 
                "import sys\n" +
                "import json\n" +
                "import math\n\n" +
                "class Calculator:\n" +
                "    def add(self, a, b):\n" +
                "        return a + b\n" +
                "    \n" +
                "    def multiply(self, a, b):\n" +
                "        return a * b\n" +
                "    \n" +
                "    def factorial(self, n):\n" +
                "        return math.factorial(int(n))\n" +
                "    \n" +
                "    def reverse_string(self, s):\n" +
                "        return s[::-1]\n" +
                "    \n" +
                "    def process_list(self, lst):\n" +
                "        return {\n" +
                "            'sum': sum(lst),\n" +
                "            'count': len(lst),\n" +
                "            'avg': sum(lst) / len(lst) if lst else 0\n" +
                "        }\n" +
                "    \n" +
                "    def get_info(self):\n" +
                "        return {'name': 'Calculator', 'version': '1.0'}\n\n" +
                "if __name__ == '__main__':\n" +
                "    if len(sys.argv) > 1:\n" +
                "        try:\n" +
                "            request = json.loads(sys.argv[1])\n" +
                "            calc = Calculator()\n" +
                "            \n" +
                "            method_name = request.get('method')\n" +
                "            args = request.get('args', [])\n" +
                "            \n" +
                "            if hasattr(calc, method_name):\n" +
                "                method = getattr(calc, method_name)\n" +
                "                result = method(*args) if args else method()\n" +
                "                print(json.dumps({'result': result}))\n" +
                "            else:\n" +
                "                print(json.dumps({'error': f'Method {method_name} not found'}))\n" +
                "        except Exception as e:\n" +
                "            print(json.dumps({'error': str(e)}))\n" +
                "    else:\n" +
                "        print(json.dumps({'error': 'No input provided'}))\n";
            
            Files.write(Path.of("Calculator.py"), calculatorCode.getBytes());
            System.out.println("Created Calculator.py");
            
        } catch (IOException e) {
            System.err.println("Failed to create Calculator.py: " + e.getMessage());
        }
    }
}