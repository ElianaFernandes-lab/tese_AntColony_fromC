# MathOperations.py
class MathOperations:
    @staticmethod
    def add(a, b):
        return a + b

    @staticmethod
    def multiply(a, b):
        return a * b

    @staticmethod
    def factorial(n):
        if n <= 1:
            return 1
        return n * MathOperations.factorial(n - 1)

    @staticmethod
    def calculate_stats(numbers):
        import statistics
        return {
            "mean": statistics.mean(numbers),
            "median": statistics.median(numbers),
            "sum": sum(numbers),
            "count": len(numbers)
        }
