#!/usr/bin/env python3
"""
Calculator.py - A Python class with methods that can be called from Java
"""

import json
import math
import datetime
import statistics
import re
from typing import List, Dict, Any

class Calculator:
    """A comprehensive Python calculator class that can be called from Java"""
    
    def __init__(self, name="Python Calculator"):
        self.name = name
        self.history = []
        self.created_at = datetime.datetime.now()
    
    # ===== MATH OPERATIONS =====
    
    def add(self, a: float, b: float) -> float:
        """Add two numbers"""
        result = a + b
        self._record_operation(f"add", {"a": a, "b": b}, result)
        return result
    
    def subtract(self, a: float, b: float) -> float:
        """Subtract b from a"""
        result = a - b
        self._record_operation(f"subtract", {"a": a, "b": b}, result)
        return result
    
    def multiply(self, a: float, b: float) -> float:
        """Multiply two numbers"""
        result = a * b
        self._record_operation(f"multiply", {"a": a, "b": b}, result)
        return result
    
    def divide(self, a: float, b: float) -> float:
        """Divide a by b"""
        if b == 0:
            raise ValueError("Division by zero")
        result = a / b
        self._record_operation(f"divide", {"a": a, "b": b}, result)
        return result
    
    def power(self, base: float, exponent: float) -> float:
        """Raise base to the power of exponent"""
        result = math.pow(base, exponent)
        self._record_operation(f"power", {"base": base, "exponent": exponent}, result)
        return result
    
    def sqrt(self, number: float) -> float:
        """Calculate square root"""
        if number < 0:
            raise ValueError("Cannot calculate square root of negative number")
        result = math.sqrt(number)
        self._record_operation(f"sqrt", {"number": number}, result)
        return result
    
    def factorial(self, n: int) -> int:
        """Calculate factorial"""
        if n < 0:
            raise ValueError("Factorial not defined for negative numbers")
        result = math.factorial(n)
        self._record_operation(f"factorial", {"n": n}, result)
        return result
    
    # ===== ADVANCED MATH =====
    
    def calculate_stats(self, numbers: List[float]) -> Dict[str, Any]:
        """Calculate statistical measures"""
        if not numbers:
            raise ValueError("List cannot be empty")
        
        return {
            "sum": sum(numbers),
            "mean": statistics.mean(numbers),
            "median": statistics.median(numbers),
            "mode": statistics.mode(numbers) if len(numbers) > 1 else numbers[0],
            "std_dev": statistics.stdev(numbers) if len(numbers) > 1 else 0,
            "min": min(numbers),
            "max": max(numbers),
            "count": len(numbers)
        }
    
    def quadratic_formula(self, a: float, b: float, c: float) -> Dict[str, Any]:
        """Solve quadratic equation ax² + bx + c = 0"""
        discriminant = b**2 - 4*a*c
        
        if discriminant < 0:
            return {
                "type": "complex",
                "discriminant": discriminant,
                "message": "No real roots"
            }
        elif discriminant == 0:
            root = -b / (2*a)
            return {
                "type": "single",
                "root": root,
                "discriminant": discriminant
            }
        else:
            root1 = (-b + math.sqrt(discriminant)) / (2*a)
            root2 = (-b - math.sqrt(discriminant)) / (2*a)
            return {
                "type": "double",
                "root1": root1,
                "root2": root2,
                "discriminant": discriminant
            }
    
    # ===== STRING OPERATIONS =====
    
    def reverse_string(self, text: str) -> str:
        """Reverse a string"""
        result = text[::-1]
        self._record_operation(f"reverse_string", {"text": text}, result)
        return result
    
    def count_words(self, text: str) -> Dict[str, Any]:
        """Count words and characters"""
        words = text.split()
        characters = len(text)
        sentences = len(re.split(r'[.!?]+', text)) - 1
        
        return {
            "word_count": len(words),
            "character_count": characters,
            "sentence_count": sentences,
            "average_word_length": characters / len(words) if words else 0,
            "words": words[:10]  # First 10 words
        }
    
    def extract_emails(self, text: str) -> List[str]:
        """Extract email addresses from text"""
        email_pattern = r'[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}'
        return re.findall(email_pattern, text)
    
    # ===== UTILITY METHODS =====
    
    def get_current_time(self) -> Dict[str, Any]:
        """Get current date and time"""
        now = datetime.datetime.now()
        return {
            "iso_format": now.isoformat(),
            "date": now.strftime("%Y-%m-%d"),
            "time": now.strftime("%H:%M:%S"),
            "day_of_week": now.strftime("%A"),
            "timestamp": now.timestamp(),
            "timezone": str(now.astimezone().tzinfo)
        }
    
    def generate_fibonacci(self, n: int) -> List[int]:
        """Generate Fibonacci sequence up to n terms"""
        if n <= 0:
            return []
        elif n == 1:
            return [0]
        elif n == 2:
            return [0, 1]
        
        sequence = [0, 1]
        for i in range(2, n):
            sequence.append(sequence[i-1] + sequence[i-2])
        
        self._record_operation(f"fibonacci", {"n": n}, f"Generated {n} terms")
        return sequence
    
    def convert_temperature(self, value: float, from_unit: str, to_unit: str) -> float:
        """Convert temperature between Celsius, Fahrenheit, and Kelvin"""
        from_unit = from_unit.upper()
        to_unit = to_unit.upper()
        
        # Convert to Kelvin first
        if from_unit == "C":
            kelvin = value + 273.15
        elif from_unit == "F":
            kelvin = (value - 32) * 5/9 + 273.15
        elif from_unit == "K":
            kelvin = value
        else:
            raise ValueError(f"Unknown unit: {from_unit}")
        
        # Convert from Kelvin to target unit
        if to_unit == "C":
            result = kelvin - 273.15
        elif to_unit == "F":
            result = (kelvin - 273.15) * 9/5 + 32
        elif to_unit == "K":
            result = kelvin
        else:
            raise ValueError(f"Unknown unit: {to_unit}")
        
        self._record_operation(
            f"convert_temperature",
            {"value": value, "from": from_unit, "to": to_unit},
            result
        )
        return round(result, 2)
    
    # ===== HISTORY MANAGEMENT =====
    
    def _record_operation(self, operation: str, params: Dict, result: Any):
        """Record an operation in history"""
        self.history.append({
            "timestamp": datetime.datetime.now().isoformat(),
            "operation": operation,
            "parameters": params,
            "result": str(result),
            "execution_time": datetime.datetime.now().timestamp()
        })
    
    def get_history(self, limit: int = 10) -> List[Dict]:
        """Get recent operations from history"""
        return self.history[-limit:] if limit > 0 else self.history
    
    def clear_history(self):
        """Clear operation history"""
        self.history.clear()
        return "History cleared successfully"
    
    def get_statistics(self) -> Dict[str, Any]:
        """Get calculator usage statistics"""
        if not self.history:
            return {"total_operations": 0}
        
        operations_by_type = {}
        for entry in self.history:
            op = entry["operation"]
            operations_by_type[op] = operations_by_type.get(op, 0) + 1
        
        return {
            "total_operations": len(self.history),
            "operations_by_type": operations_by_type,
            "first_operation": self.history[0]["timestamp"] if self.history else None,
            "last_operation": self.history[-1]["timestamp"] if self.history else None,
            "calculator_age_days": (datetime.datetime.now() - self.created_at).days
        }
    
    # ===== METADATA =====
    
    def get_info(self) -> Dict[str, Any]:
        """Get calculator information"""
        return {
            "name": self.name,
            "created_at": self.created_at.isoformat(),
            "history_count": len(self.history),
            "available_methods": [m for m in dir(self) if not m.startswith('_') and callable(getattr(self, m))],
            "python_version": f"{sys.version_info.major}.{sys.version_info.minor}.{sys.version_info.micro}"
        }


# ===== HELPER FUNCTIONS FOR JAVA INTEGRATION =====

def create_calculator(name="Default Calculator"):
    """Factory function to create a calculator instance"""
    return Calculator(name)


def call_method(method_name: str, args: List[Any] = None, kwargs: Dict[str, Any] = None) -> Dict[str, Any]:
    """
    Call a method on a calculator instance
    This is the main entry point for Java calls
    """
    try:
        calc = Calculator()
        
        if not hasattr(calc, method_name):
            return {
                "success": False,
                "error": f"Method '{method_name}' not found",
                "available_methods": [m for m in dir(calc) if not m.startswith('_')]
            }
        
        method = getattr(calc, method_name)
        
        # Prepare arguments
        call_args = args or []
        call_kwargs = kwargs or {}
        
        # Call the method
        if call_kwargs:
            result = method(*call_args, **call_kwargs)
        else:
            result = method(*call_args)
        
        # Prepare response
        response = {
            "success": True,
            "result": result,
            "method": method_name,
            "timestamp": datetime.datetime.now().isoformat(),
            "history_count": len(calc.history)
        }
        
        # Try to convert result to JSON-serializable format
        try:
            json.dumps(response)
        except:
            response["result"] = str(result)
        
        return response
        
    except Exception as e:
        return {
            "success": False,
            "error": str(e),
            "method": method_name,
            "timestamp": datetime.datetime.now().isoformat()
        }


def batch_operations(operations: List[Dict[str, Any]]) -> Dict[str, Any]:
    """
    Execute multiple operations in sequence
    """
    calc = Calculator("Batch Processor")
    results = []
    
    for i, op in enumerate(operations):
        method_name = op.get("method")
        args = op.get("args", [])
        kwargs = op.get("kwargs", {})
        
        try:
            if hasattr(calc, method_name):
                method = getattr(calc, method_name)
                result = method(*args, **kwargs) if kwargs else method(*args)
                results.append({
                    "index": i,
                    "success": True,
                    "method": method_name,
                    "result": str(result)
                })
            else:
                results.append({
                    "index": i,
                    "success": False,
                    "method": method_name,
                    "error": "Method not found"
                })
        except Exception as e:
            results.append({
                "index": i,
                "success": False,
                "method": method_name,
                "error": str(e)
            })
    
    return {
        "success": True,
        "total_operations": len(operations),
        "successful": len([r for r in results if r["success"]]),
        "failed": len([r for r in results if not r["success"]]),
        "results": results,
        "history": calc.get_history()
    }


# ===== COMMAND LINE INTERFACE =====

if __name__ == "__main__":
    import sys
    
    # This allows the script to be called from Java
    if len(sys.argv) > 1:
        try:
            # Parse JSON input from Java
            input_data = json.loads(sys.argv[1])
            
            method = input_data.get("method")
            args = input_data.get("args", [])
            kwargs = input_data.get("kwargs", {})
            
            if method == "batch":
                # Batch operations
                operations = input_data.get("operations", [])
                result = batch_operations(operations)
            else:
                # Single method call
                result = call_method(method, args, kwargs)
            
            # Output JSON for Java to parse
            print(json.dumps(result))
            
        except json.JSONDecodeError:
            print(json.dumps({
                "success": False,
                "error": "Invalid JSON input"
            }))
        except Exception as e:
            print(json.dumps({
                "success": False,
                "error": f"Unexpected error: {str(e)}"
            }))
    else:
        # Interactive mode if called directly
        print("Python Calculator Class")
        print("Available for Java integration via JSON-RPC")
        print("Usage from Java: python3 Calculator.py '{\"method\":\"add\",\"args\":[5,3]}'")