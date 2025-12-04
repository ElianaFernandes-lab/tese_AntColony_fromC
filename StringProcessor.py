# StringProcessor.py
class StringProcessor:
    @staticmethod
    def reverse_string(text):
        return text[::-1]

    @staticmethod
    def to_uppercase(text):
        return text.upper()

    @staticmethod
    def count_words(text):
        return len(text.split())

    @staticmethod
    def extract_emails(text):
        import re
        return re.findall(r'[\w\.-]+@[\w\.-]+\.[\w]+', text)
