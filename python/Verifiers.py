import helper
import json
import re

class URLVerifier:
    
    def __init__(self, url, use_blocklist):
        self.url = str(url)
        self.components = helper.get_url_components(url)
        self.use_blocklist =  use_blocklist

    def is_scheme_secured(self):
        return True if (self.components["scheme"] == "https") else False
    
    def is_domain_blocklisted(self):
        with open('config.json') as f:
            config = json.load(f)
        
        filepath = config["domain_blocklist_file_path"]

        return helper.word_exists_in_csv(self.components["domain"], filepath) 
        
    def is_url_ip_based(self):
    
        pattern = r'(?i)\b(?:http|https|ftp)://(?:\d{1,3}\.){3}\d{1,3}(?::\d+)?(?:/[^\s]*)?'

        result = re.match(pattern,self.url)
        return True if (result == None) else False
    
    def calculate_score(self):
        return
    
    
        
class TextVerifier:

    def __init__(self, text):
        self.text = str(text)

    

    