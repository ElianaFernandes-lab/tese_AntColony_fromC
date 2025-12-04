import sys
import json
import math

class Calculator:
    def add(self, a, b):
        return a + b
    
    def multiply(self, a, b):
        return a * b
    
    def factorial(self, n):
        return math.factorial(int(n))
    
    def reverse_string(self, s):
        return s[::-1]
    
    def process_list(self, lst):
        return {
            'sum': sum(lst),
            'count': len(lst),
            'avg': sum(lst) / len(lst) if lst else 0
        }
    
    def get_info(self):
        return {'name': 'Calculator', 'version': '1.0'}

if __name__ == '__main__':
    if len(sys.argv) > 1:
        try:
            request = json.loads(sys.argv[1])
            calc = Calculator()
            
            method_name = request.get('method')
            args = request.get('args', [])
            
            if hasattr(calc, method_name):
                method = getattr(calc, method_name)
                result = method(*args) if args else method()
                print(json.dumps({'result': result}))
            else:
                print(json.dumps({'error': f'Method {method_name} not found'}))
        except Exception as e:
            print(json.dumps({'error': str(e)}))
    else:
        print(json.dumps({'error': 'No input provided'}))
