import re

# helper function to convert to lowercase and remove special characters
def normalise(message):
    text = message.lower()
    text = re.sub(r"\s+", " ", text).strip()
    return text